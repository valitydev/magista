package dev.vality.magista.kafka;

import dev.vality.damsel.payment_processing.EventPayload;
import dev.vality.machinegun.eventsink.MachineEvent;
import dev.vality.machinegun.eventsink.SinkEvent;
import dev.vality.magista.config.KafkaPostgresqlSpringBootITest;
import dev.vality.magista.converter.SourceEventsParser;
import dev.vality.magista.service.HandlerManager;
import dev.vality.testcontainers.annotations.kafka.config.KafkaProducer;
import org.apache.thrift.TBase;
import org.junit.jupiter.api.Test;
import org.mockito.ArgumentCaptor;
import org.mockito.Captor;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.test.context.bean.override.mockito.MockitoBean;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@KafkaPostgresqlSpringBootITest
public class InvoiceTemplateListenerTest {

    @Value("${kafka.topics.invoice-template.id}")
    private String invoiceTemplateTopicName;

    @MockitoBean
    private HandlerManager handlerManager;

    @MockitoBean
    private SourceEventsParser sourceEventsParser;

    @Autowired
    private KafkaProducer<TBase<?, ?>> testThriftKafkaProducer;

    @Captor
    private ArgumentCaptor<MachineEvent> arg;

    @Test
    public void shouldInvoiceTemplateSinkEventListen() {
        var message = new MachineEvent();
        message.setCreatedAt(LocalDateTime.now().format(DateTimeFormatter.ISO_DATE_TIME));
        message.setEventId(1L);
        message.setSourceId("source_id");
        message.setSourceNs("source_ns");
        var data = new dev.vality.machinegun.msgpack.Value();
        data.setBin(new byte[0]);
        message.setData(data);
        var sinkEvent = new SinkEvent();
        sinkEvent.setEvent(message);
        when(sourceEventsParser.parseEvents(any()))
                .thenReturn(List.of(EventPayload.invoice_template_changes(List.of())));
        testThriftKafkaProducer.send(invoiceTemplateTopicName, sinkEvent);
        verify(sourceEventsParser, timeout(5000).times(1)).parseEvents(arg.capture());
        assertThat(arg.getValue())
                .isEqualTo(message);
    }
}
