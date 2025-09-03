package dev.vality.magista.event.mapper.impl;

import dev.vality.damsel.domain.*;
import dev.vality.damsel.payment_processing.InvoiceChange;
import dev.vality.damsel.payment_processing.InvoicePaymentChange;
import dev.vality.damsel.payment_processing.InvoicePaymentChangePayload;
import dev.vality.damsel.payment_processing.InvoicePaymentStarted;
import dev.vality.machinegun.eventsink.MachineEvent;
import dev.vality.magista.domain.tables.pojos.PaymentData;
import org.jetbrains.annotations.NotNull;
import org.junit.jupiter.api.Test;

import java.util.UUID;

import static org.junit.jupiter.api.Assertions.assertEquals;

public class PaymentStartedEventMapperTest {

    @Test
    public void testMapPaymentTerminal() {
        var mapper = new PaymentStartedEventMapper();
        String paymentServiceId = "paymentServiceId";
        InvoiceChange change = buildChange(buildPaymentToolTerminal(paymentServiceId));
        PaymentData paymentData = mapper.map(change, new MachineEvent().setCreatedAt("2016-03-22T06:12:27Z"));
        assertEquals(paymentServiceId, paymentData.getPaymentTerminalPaymentServiceRefId());
    }

    @Test
    public void testMapDigitalWallet() {
        var mapper = new PaymentStartedEventMapper();
        String paymentServiceId = "paymentServiceId";
        InvoiceChange change = buildChange(buildPaymentToolWallet(paymentServiceId));
        PaymentData paymentData = mapper.map(change, new MachineEvent().setCreatedAt("2016-03-22T06:12:27Z"));
        assertEquals(paymentServiceId, paymentData.getPaymentDigitalWalletServiceRefId());
    }

    @NotNull
    private InvoiceChange buildChange(PaymentTool paymentTool) {
        return InvoiceChange.invoice_payment_change(
                new InvoicePaymentChange()
                        .setId("124")
                        .setPayload(InvoicePaymentChangePayload.invoice_payment_started(
                                new InvoicePaymentStarted().setPayment(new InvoicePayment()
                                        .setPartyRef(new PartyConfigRef(UUID.randomUUID().toString()))
                                        .setCost(new Cash(112L, new CurrencyRef()))
                                        .setCreatedAt("2016-03-22T06:12:27Z")
                                        .setFlow(InvoicePaymentFlow.instant(new InvoicePaymentFlowInstant()))
                                        .setStatus(InvoicePaymentStatus.pending(new InvoicePaymentPending()))
                                        .setPayer(Payer.payment_resource(new PaymentResourcePayer()
                                                .setContactInfo(new ContactInfo())
                                                .setResource(new DisposablePaymentResource()
                                                        .setPaymentTool(paymentTool))
                                        )))
                        )));
    }

    @NotNull
    private PaymentTool buildPaymentToolTerminal(String paymentServiceId) {
        return PaymentTool.payment_terminal(new PaymentTerminal()
                .setPaymentService(new PaymentServiceRef()
                        .setId(paymentServiceId)));
    }

    @NotNull
    private PaymentTool buildPaymentToolWallet(String paymentServiceId) {
        return PaymentTool.digital_wallet(new DigitalWallet()
                .setPaymentService(new PaymentServiceRef()
                        .setId(paymentServiceId)));
    }
}