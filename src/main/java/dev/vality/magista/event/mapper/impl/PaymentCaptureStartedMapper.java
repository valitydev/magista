package dev.vality.magista.event.mapper.impl;

import dev.vality.damsel.domain.Cash;
import dev.vality.damsel.payment_processing.InvoiceChange;
import dev.vality.damsel.payment_processing.InvoicePaymentCaptureStarted;
import dev.vality.damsel.payment_processing.InvoicePaymentChange;
import dev.vality.geck.common.util.TypeUtil;
import dev.vality.machinegun.eventsink.MachineEvent;
import dev.vality.magista.domain.enums.InvoiceEventType;
import dev.vality.magista.domain.tables.pojos.PaymentData;
import dev.vality.magista.event.ChangeType;
import dev.vality.magista.event.mapper.PaymentMapper;
import org.springframework.stereotype.Component;

@Component
public class PaymentCaptureStartedMapper implements PaymentMapper {
    @Override
    public PaymentData map(InvoiceChange change, MachineEvent machineEvent) {
        final PaymentData paymentData = new PaymentData();
        paymentData.setEventId(machineEvent.getEventId());
        paymentData.setEventType(InvoiceEventType.INVOICE_PAYMENT_CAPTURE_STARTED);
        paymentData.setEventCreatedAt(TypeUtil.stringToLocalDateTime(machineEvent.getCreatedAt()));
        paymentData.setInvoiceId(machineEvent.getSourceId());

        final InvoicePaymentChange invoicePaymentChange = change.getInvoicePaymentChange();
        paymentData.setPaymentId(invoicePaymentChange.getId());

        final InvoicePaymentCaptureStarted invoicePaymentCaptureStarted = invoicePaymentChange
                .getPayload()
                .getInvoicePaymentCaptureStarted();

        if (invoicePaymentCaptureStarted.getData().isSetCash()) {
            Cash cash = invoicePaymentCaptureStarted.getData().getCash();
            paymentData.setPaymentAmount(cash.getAmount());
            paymentData.setPaymentCurrencyCode(cash.getCurrency().getSymbolicCode());
        }

        return paymentData;
    }

    @Override
    public ChangeType getChangeType() {
        return ChangeType.INVOICE_PAYMENT_CAPTURE_STARTED;
    }
}
