package dev.vality.magista.listener;

import dev.vality.damsel.domain.InvoiceTemplate;
import dev.vality.damsel.payment_processing.EventPayload;
import dev.vality.damsel.payment_processing.InvoiceTemplateChange;
import dev.vality.damsel.payment_processing.InvoiceTemplatingSrv;
import dev.vality.machinegun.eventsink.MachineEvent;
import dev.vality.machinegun.msgpack.Value;
import dev.vality.magista.config.PostgresqlSpringBootITest;
import dev.vality.magista.converter.SourceEventsParser;
import dev.vality.magista.domain.enums.InvoiceTemplateEventType;
import dev.vality.magista.exception.NotFoundException;
import dev.vality.magista.service.InvoiceTemplateService;
import lombok.SneakyThrows;
import org.apache.thrift.TBase;
import org.apache.thrift.TSerializer;
import org.apache.thrift.protocol.TBinaryProtocol;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.jdbc.JdbcTestUtils;

import java.time.Instant;
import java.util.Arrays;
import java.util.List;
import java.util.Map;

import static dev.vality.magista.domain.tables.InvoiceTemplate.INVOICE_TEMPLATE;
import static dev.vality.magista.util.InvoiceTemplateGenerator.*;
import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.*;

@PostgresqlSpringBootITest
public class InvoiceTemplateServiceTest {

    private static final String TABLE_NAME = INVOICE_TEMPLATE.getSchema().getName() + "." + INVOICE_TEMPLATE.getName();

    @Autowired
    private InvoiceTemplateListener invoiceTemplateListener;

    @Autowired
    private InvoiceTemplateService invoiceTemplateService;

    @Autowired
    private JdbcTemplate jdbcTemplate;

    @MockitoBean
    private InvoiceTemplatingSrv.Iface invoiceTemplatingClient;

    @Test
    public void shouldHandleAndSave() throws Exception {
        String invoiceTemplateId = "invoiceTemplateId";
        MachineEvent message = getEvent(
                invoiceTemplateId,
                1,
                List.of(
                        getCreated(getInvoiceTemplate(getCart())),
                        getUpdated(getParams(getCart())),
                        getDeleted()));
        invoiceTemplateListener.handleMessages(List.of(message));
        assertThat(JdbcTestUtils.countRowsInTable(jdbcTemplate, TABLE_NAME))
                .isEqualTo(1);
        assertThat(invoiceTemplateService.get(invoiceTemplateId).getEventType())
                .isEqualTo(InvoiceTemplateEventType.INVOICE_TEMPLATE_DELETED);
    }

    @Test
    public void shouldNotFound() {
        assertThrows(
                NotFoundException.class,
                () -> invoiceTemplateService.get("invoiceTemplateId"));
    }

    @Test
    public void shouldUpdateData() throws Exception {
        String savedId = "invoiceTemplateId1";
        invoiceTemplateListener.handleMessages(
                getEvents(savedId, 1, getCreated(getInvoiceTemplate(getCart()))));
        invoiceTemplateListener.handleMessages(
                getEvents(savedId, 2, getUpdated(getParams(getCart()))));
        invoiceTemplateListener.handleMessages(
                getEvents(savedId, 3, getUpdated(getParams(getCart()))));
        invoiceTemplateListener.handleMessages(
                getEvents(savedId, 4, getUpdated(getParams(getCart()))));
        InvoiceTemplateChange lastUpdated = getUpdated(getParams(getCart()));
        invoiceTemplateListener.handleMessages(
                getEvents(savedId, 5, lastUpdated));
        assertThat(invoiceTemplateService.get(savedId).getInvoiceContextType())
                .isEqualTo(lastUpdated.getInvoiceTemplateUpdated().getDiff().getContext().getType());
    }

    @Test
    public void shouldSkipDuplicatesWhenSequenceIdIsLessThanSequenceIdInStorage() throws Exception {
        String invoiceTemplateId = "invoiceTemplateId";
        MachineEvent message = getEvent(
                invoiceTemplateId,
                1,
                List.of(
                        getCreated(getInvoiceTemplate(getCart())),
                        getCreated(getInvoiceTemplate(getCart()))));
        invoiceTemplateListener.handleMessages(List.of(message));
        InvoiceTemplateChange lastUpdated = getUpdated(getParams(getCart()));
        invoiceTemplateListener.handleMessages(
                getEvents(invoiceTemplateId, 2, lastUpdated));
        // skip this
        invoiceTemplateListener.handleMessages(
                getEvents(invoiceTemplateId, 1, getUpdated(getParams(getCart()))));
        assertThat(invoiceTemplateService.get(invoiceTemplateId).getInvoiceContextType())
                .isEqualTo(lastUpdated.getInvoiceTemplateUpdated().getDiff().getContext().getType());
    }

    @Test
    public void shouldReWriteDuplicatesByProtocolOpportunityAndWillBeFine() throws Exception {
        String invoiceTemplateId = "invoiceTemplateId";
        InvoiceTemplateChange updated = getUpdated(getParams(getCart()));
        invoiceTemplateListener.handleMessages(
                List.of(
                        getEvent(
                                invoiceTemplateId,
                                1,
                                List.of(
                                        getCreated(getInvoiceTemplate(getCart())),
                                        getUpdated(getParams(getCart())))),
                        getEvent(
                                invoiceTemplateId,
                                1,
                                List.of(
                                        getCreated(getInvoiceTemplate(getCart())),
                                        updated))));
        assertThat(invoiceTemplateService.get(invoiceTemplateId).getInvoiceContextType())
                .isEqualTo(updated.getInvoiceTemplateUpdated().getDiff().getContext().getType());
    }

    @Test
    public void shouldParseMultiByteEventsSameTypes() throws Exception {
        String invoiceTemplateId = "invoiceTemplateId";
        MachineEvent message = new MachineEvent()
                .setData(toArray(
                        getDefaultInvoiceTemplateValue(),
                        getDefaultInvoiceTemplateValue(),
                        getDefaultInvoiceTemplateValue(),
                        getDefaultInvoiceTemplateValue()))
                .setCreatedAt(Instant.now().toString())
                .setEventId(1)
                .setSourceNs("source_ns")
                .setSourceId(invoiceTemplateId);
        invoiceTemplateListener.handleMessages(List.of(message));
        assertThat(JdbcTestUtils.countRowsInTable(jdbcTemplate, TABLE_NAME))
                .isEqualTo(1);
    }

    @Test
    public void shouldParseMultiByteEventsDiffTypes() throws Exception {
        String invoiceTemplateId = "invoiceTemplateId";
        MachineEvent message = new MachineEvent()
                .setData(toArray(
                        getDefaultInvoiceTemplateValue(),
                        toByteArray(EventPayload.invoice_changes(List.of())),
                        getDefaultInvoiceTemplateValue(),
                        getDefaultInvoiceTemplateValue()))
                .setCreatedAt(Instant.now().toString())
                .setEventId(1)
                .setSourceNs("source_ns")
                .setSourceId(invoiceTemplateId);
        invoiceTemplateListener.handleMessages(List.of(message));
        assertThat(JdbcTestUtils.countRowsInTable(jdbcTemplate, TABLE_NAME))
                .isEqualTo(1);
    }

    @Test
    public void shouldNotParseArrayEvents() throws Exception {
        String repairId = "repairId";
        InvoiceTemplate invoiceTemplate = getInvoiceTemplate(getCart());
        when(invoiceTemplatingClient.get(eq(repairId))).thenReturn(invoiceTemplate);
        String invoiceTemplateId = "invoiceTemplateId";
        MachineEvent message = new MachineEvent()
                .setData(toArray(
                        getDefaultInvoiceTemplateValue(),
                        // мы не можем выкидывать исключение в SourceEventsParser из за неправильной типизации,
                        // поэтому будем такие значения скипать
                        Value.str("error"),
                        Value.i(3),
                        // подобный вид msgpack будем обрабатывать
                        Value.obj(Map.of(
                                Value.str(SourceEventsParser.TPL),
                                Value.obj(Map.of(Value.str(SourceEventsParser.ID), Value.str(repairId)))))))
                .setCreatedAt(Instant.now().toString())
                .setEventId(1)
                .setSourceNs("source_ns")
                .setSourceId(invoiceTemplateId);
        invoiceTemplateListener.handleMessages(List.of(message));
        verify(invoiceTemplatingClient, timeout(5000).times(1)).get(eq(repairId));
    }

    private Value getDefaultInvoiceTemplateValue() {
        return toByteArray(EventPayload.invoice_template_changes(List.of(getCreated(getInvoiceTemplate(getCart())))));
    }

    @SneakyThrows
    public static Value toArray(Value... values) {
        return Value.arr(Arrays.asList(values));
    }

    @SneakyThrows
    public static Value toByteArray(TBase<?, ?> thrift) {
        return Value.bin(
                new TSerializer(new TBinaryProtocol.Factory())
                        .serialize(thrift));
    }
}
