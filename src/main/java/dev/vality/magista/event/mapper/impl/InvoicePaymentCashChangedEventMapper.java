package dev.vality.magista.event.mapper.impl;

import dev.vality.damsel.domain.Cash;
import dev.vality.damsel.payment_processing.InvoiceChange;
import dev.vality.damsel.payment_processing.InvoicePaymentChange;
import dev.vality.geck.common.util.TypeUtil;
import dev.vality.machinegun.eventsink.MachineEvent;
import dev.vality.magista.domain.enums.InvoiceEventType;
import dev.vality.magista.domain.tables.pojos.PaymentData;
import dev.vality.magista.event.ChangeType;
import dev.vality.magista.event.mapper.PaymentMapper;
import org.springframework.stereotype.Component;

@Component
public class InvoicePaymentCashChangedEventMapper implements PaymentMapper {

    @Override
    public PaymentData map(InvoiceChange change, MachineEvent machineEvent) {
        PaymentData paymentData = new PaymentData();
        paymentData.setEventType(InvoiceEventType.INVOICE_PAYMENT_CASH_CHANGED);
        paymentData.setEventId(machineEvent.getEventId());
        paymentData.setEventCreatedAt(TypeUtil.stringToLocalDateTime(machineEvent.getCreatedAt()));
        paymentData.setInvoiceId(machineEvent.getSourceId());

        InvoicePaymentChange invoicePaymentChange = change.getInvoicePaymentChange();
        paymentData.setPaymentId(invoicePaymentChange.getId());
        Cash newCash = invoicePaymentChange
                .getPayload()
                .getInvoicePaymentCashChanged()
                .getNewCash();
        paymentData.setPaymentAmount(newCash.getAmount());
        return paymentData;
    }

    @Override
    public ChangeType getChangeType() {
        return ChangeType.INVOICE_PAYMENT_CASH_CHANGED;
    }
}
