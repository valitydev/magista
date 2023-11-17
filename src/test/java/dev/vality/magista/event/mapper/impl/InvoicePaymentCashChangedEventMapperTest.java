package dev.vality.magista.event.mapper.impl;

import dev.vality.damsel.domain.Cash;
import dev.vality.damsel.payment_processing.InvoiceChange;
import dev.vality.damsel.payment_processing.InvoicePaymentCashChanged;
import dev.vality.damsel.payment_processing.InvoicePaymentChange;
import dev.vality.damsel.payment_processing.InvoicePaymentChangePayload;
import dev.vality.geck.common.util.TypeUtil;
import dev.vality.machinegun.eventsink.MachineEvent;
import dev.vality.magista.domain.tables.pojos.PaymentData;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;

import java.time.LocalDateTime;

class InvoicePaymentCashChangedEventMapperTest {

    public static final long AMOUNT = 1000L;

    @Test
    void map() {
        var mapper = new InvoicePaymentCashChangedEventMapper();

        InvoiceChange invoiceChange = new InvoiceChange();
        InvoicePaymentChange invoicePaymentChange = new InvoicePaymentChange();
        InvoicePaymentChangePayload payload = new InvoicePaymentChangePayload();
        payload.setInvoicePaymentCashChanged(new InvoicePaymentCashChanged()
                .setNewCash(new Cash()
                        .setAmount(AMOUNT))
        );
        invoicePaymentChange.setPayload(payload);
        invoiceChange.setInvoicePaymentChange(invoicePaymentChange);
        MachineEvent machineEvent = new MachineEvent();
        machineEvent.setEventId(100L);
        machineEvent.setCreatedAt(TypeUtil.temporalToString(LocalDateTime.now()));
        PaymentData paymentData = mapper.map(invoiceChange, machineEvent);

        Assertions.assertEquals(AMOUNT, paymentData.getPaymentAmount());
    }
}