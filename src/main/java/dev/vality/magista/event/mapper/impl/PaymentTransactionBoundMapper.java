package dev.vality.magista.event.mapper.impl;

import dev.vality.damsel.domain.AdditionalTransactionInfo;
import dev.vality.damsel.domain.TransactionInfo;
import dev.vality.damsel.payment_processing.InvoiceChange;
import dev.vality.damsel.payment_processing.InvoicePaymentChange;
import dev.vality.damsel.payment_processing.InvoicePaymentSessionChange;
import dev.vality.damsel.payment_processing.SessionTransactionBound;
import dev.vality.geck.common.util.TypeUtil;
import dev.vality.machinegun.eventsink.MachineEvent;
import dev.vality.magista.domain.enums.InvoiceEventType;
import dev.vality.magista.domain.tables.pojos.PaymentData;
import dev.vality.magista.event.ChangeType;
import dev.vality.magista.event.mapper.PaymentMapper;
import org.springframework.stereotype.Component;

@Component
public class PaymentTransactionBoundMapper implements PaymentMapper {
    @Override
    public PaymentData map(InvoiceChange change, MachineEvent machineEvent) {
        final PaymentData paymentData = new PaymentData();
        paymentData.setEventId(machineEvent.getEventId());
        paymentData.setEventType(InvoiceEventType.INVOICE_PAYMENT_TRANSACTION_BOUND);
        paymentData.setEventCreatedAt(TypeUtil.stringToLocalDateTime(machineEvent.getCreatedAt()));
        paymentData.setInvoiceId(machineEvent.getSourceId());

        final InvoicePaymentChange invoicePaymentChange = change.getInvoicePaymentChange();
        paymentData.setPaymentId(invoicePaymentChange.getId());

        final InvoicePaymentSessionChange invoicePaymentSessionChange = invoicePaymentChange
                .getPayload()
                .getInvoicePaymentSessionChange();

        final SessionTransactionBound sessionChangePayload = invoicePaymentSessionChange
                .getPayload()
                .getSessionTransactionBound();

        TransactionInfo transactionInfo = sessionChangePayload.getTrx();
        if (transactionInfo.isSetAdditionalInfo()) {
            AdditionalTransactionInfo additionalTransactionInfo = transactionInfo.getAdditionalInfo();
            paymentData.setPaymentRrn(additionalTransactionInfo.getRrn());
            paymentData.setPaymentApprovalCode(additionalTransactionInfo.getApprovalCode());
        }

        return paymentData;
    }

    @Override
    public ChangeType getChangeType() {
        return ChangeType.INVOICE_PAYMENT_TRANSACTION_BOUND;
    }
}
