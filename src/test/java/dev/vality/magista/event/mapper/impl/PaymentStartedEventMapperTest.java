package dev.vality.magista.event.mapper.impl;

import dev.vality.damsel.domain.*;
import dev.vality.damsel.payment_processing.InvoiceChange;
import dev.vality.damsel.payment_processing.InvoicePaymentChange;
import dev.vality.damsel.payment_processing.InvoicePaymentChangePayload;
import dev.vality.damsel.payment_processing.InvoicePaymentStarted;
import dev.vality.machinegun.eventsink.MachineEvent;
import dev.vality.magista.domain.tables.pojos.PaymentData;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

public class PaymentStartedEventMapperTest {

    @Test
    public void testMap() {
        var mapper = new PaymentStartedEventMapper();
        String paymentServiceId = "paymentServiceId";
        InvoiceChange change = InvoiceChange.invoice_payment_change(
                new InvoicePaymentChange()
                        .setId("124")
                        .setPayload(InvoicePaymentChangePayload.invoice_payment_started(
                                new InvoicePaymentStarted().setPayment(new InvoicePayment()
                                        .setCost(new Cash(112L, new CurrencyRef()))
                                        .setCreatedAt("2016-03-22T06:12:27Z")
                                        .setFlow(InvoicePaymentFlow.instant(new InvoicePaymentFlowInstant()))
                                                .setStatus(InvoicePaymentStatus.pending(new InvoicePaymentPending()))
                                        .setPayer(Payer.payment_resource(new PaymentResourcePayer()
                                                .setContactInfo(new ContactInfo())
                                                .setResource(
                                                        new DisposablePaymentResource().setPaymentTool(
                                                                PaymentTool.payment_terminal(new PaymentTerminal()
                                                                        .setPaymentService(new PaymentServiceRef()
                                                                                .setId(paymentServiceId)))))
                                        )))
                        )));
        PaymentData paymentData = mapper.map(change, new MachineEvent().setCreatedAt("2016-03-22T06:12:27Z"));
        assertEquals(paymentServiceId, paymentData.getPaymentTerminalPaymentServiceRefId());
    }
}