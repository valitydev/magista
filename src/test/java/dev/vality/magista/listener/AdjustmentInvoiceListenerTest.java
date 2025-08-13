package dev.vality.magista.listener;

import dev.vality.damsel.domain.InvoicePaymentAdjustmentPending;
import dev.vality.damsel.domain.*;
import dev.vality.damsel.payment_processing.*;
import dev.vality.machinegun.eventsink.MachineEvent;
import dev.vality.magista.config.PostgresqlSpringBootITest;
import dev.vality.magista.domain.tables.pojos.AdjustmentData;
import dev.vality.magista.domain.tables.pojos.PaymentData;
import dev.vality.magista.event.handler.impl.AdjustmentBatchHandler;
import dev.vality.magista.service.PaymentAdjustmentService;
import dev.vality.magista.service.PaymentService;
import org.assertj.core.util.Lists;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.bean.override.mockito.MockitoBean;

import java.time.Instant;
import java.util.List;
import java.util.Map;

import static dev.vality.testcontainers.annotations.util.RandomBeans.random;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.BDDMockito.given;
import static org.mockito.Mockito.doAnswer;

@PostgresqlSpringBootITest
@SpringBootTest(properties = {
        "cache.invoiceData.size=10",
        "cache.paymentData.size=10"})
public class AdjustmentInvoiceListenerTest {

    @Autowired
    private AdjustmentBatchHandler adjustmentBatchHandler;

    @Autowired
    private PaymentAdjustmentService paymentAdjustmentService;

    @MockitoBean
    private PaymentService paymentService;

    @BeforeEach
    public void setup() {
        given(paymentService.getPaymentData(any(), any())).willReturn(random(PaymentData.class));
        doAnswer(
                answer -> {
                    List<PaymentData> paymentEvents = answer.getArgument(0);
                    assertEquals(1, paymentEvents.size());
                    PaymentData adjPaymentData = paymentEvents.get(0);
                    assertEquals(0L, (long) adjPaymentData.getPaymentFee());

                    return null;
                }
        ).when(paymentService).savePayments(any());
    }

    @Test
    public void shouldHandleAdjustmentData() {
        var events = List.of(
                Map.entry(
                        InvoiceChange.invoice_payment_change(
                                new InvoicePaymentChange(
                                        "payment_id",
                                        InvoicePaymentChangePayload.invoice_payment_adjustment_change(
                                                new InvoicePaymentAdjustmentChange(
                                                        "adjustment_id",
                                                        InvoicePaymentAdjustmentChangePayload
                                                                .invoice_payment_adjustment_created(
                                                                        getInvoicePaymentAdjustmentCreated()))))),
                        new MachineEvent()
                                .setSourceId("invoice_id")
                                .setCreatedAt(Instant.now().toString())),
                Map.entry(
                        InvoiceChange.invoice_payment_change(
                                new InvoicePaymentChange(
                                        "payment_id",
                                        InvoicePaymentChangePayload.invoice_payment_adjustment_change(
                                                new InvoicePaymentAdjustmentChange(
                                                        "adjustment_id",
                                                        InvoicePaymentAdjustmentChangePayload
                                                                .invoice_payment_adjustment_status_changed(
                                                                        getInvoicePaymentAdjustmentStatusChanged()))))),
                        new MachineEvent()
                                .setSourceId("invoice_id")
                                .setCreatedAt(Instant.now().toString())));

        adjustmentBatchHandler.handle(events).execute();
        AdjustmentData adjustmentData = paymentAdjustmentService.getAdjustment(
                "invoice_id",
                "payment_id",
                "adjustment_id");
        assertEquals(0L, (long) adjustmentData.getAdjustmentFee());
    }

    private InvoicePaymentAdjustmentCreated getInvoicePaymentAdjustmentCreated() {
        return new InvoicePaymentAdjustmentCreated(
                new InvoicePaymentAdjustment()
                        .setId("adjustment_id")
                        .setCreatedAt(Instant.now()
                                .toString())
                        .setDomainRevision(1)
                        .setStatus(
                                InvoicePaymentAdjustmentStatus
                                        .pending(
                                                new InvoicePaymentAdjustmentPending()))
                        .setReason("reason")
                        .setNewCashFlow(
                                Lists.emptyList())
                        .setOldCashFlowInverse(
                                Lists.emptyList())
                        .setState(
                                buildInvoicePaymentAdjustmentState())
        );
    }

    private InvoicePaymentAdjustmentStatusChanged getInvoicePaymentAdjustmentStatusChanged() {
        return new InvoicePaymentAdjustmentStatusChanged(
                InvoicePaymentAdjustmentStatus.captured(
                        new InvoicePaymentAdjustmentCaptured(
                                Instant.now()
                                        .toString())
                )
        );
    }

    private InvoicePaymentAdjustmentState buildInvoicePaymentAdjustmentState() {
        InvoicePaymentAdjustmentStatusChangeState invoicePaymentAdjustmentStatusChangeState =
                new InvoicePaymentAdjustmentStatusChangeState();
        InvoicePaymentAdjustmentStatusChange invoicePaymentAdjustmentStatusChange =
                new InvoicePaymentAdjustmentStatusChange();
        InvoicePaymentStatus invoicePaymentStatus = new InvoicePaymentStatus();
        invoicePaymentStatus.setFailed(
                new InvoicePaymentFailed(
                        OperationFailure.failure(
                                new Failure()
                                        .setCode("code")
                                        .setReason("reason")
                                        .setSub(
                                                new SubFailure()
                                                        .setCode("sub_failure_code")
                                        )
                        )
                )
        );
        invoicePaymentAdjustmentStatusChange.setTargetStatus(invoicePaymentStatus);
        invoicePaymentAdjustmentStatusChangeState.setScenario(invoicePaymentAdjustmentStatusChange);
        InvoicePaymentAdjustmentState invoicePaymentAdjustmentState = new InvoicePaymentAdjustmentState();
        invoicePaymentAdjustmentState.setStatusChange(invoicePaymentAdjustmentStatusChangeState);
        return invoicePaymentAdjustmentState;
    }
}
