package dev.vality.magista.event.mapper.impl;

import dev.vality.damsel.payment_processing.InvoiceChange;
import dev.vality.damsel.payment_processing.InvoicePaymentChange;
import dev.vality.damsel.user_interaction.PaymentTerminalReceipt;
import dev.vality.geck.common.util.TypeUtil;
import dev.vality.machinegun.eventsink.MachineEvent;
import dev.vality.magista.domain.enums.InvoiceEventType;
import dev.vality.magista.domain.tables.pojos.PaymentData;
import dev.vality.magista.event.ChangeType;
import dev.vality.magista.event.mapper.PaymentMapper;
import org.springframework.stereotype.Component;

@Component
public class PaymentTerminalRecieptEventMapper implements PaymentMapper {

    @Override
    public PaymentData map(InvoiceChange change, MachineEvent machineEvent) {

        PaymentData paymentData = new PaymentData();
        paymentData.setEventId(machineEvent.getEventId());
        paymentData.setEventType(InvoiceEventType.PAYMENT_TERMINAL_RECIEPT);
        paymentData.setEventCreatedAt(TypeUtil.stringToLocalDateTime(machineEvent.getCreatedAt()));
        paymentData.setInvoiceId(machineEvent.getSourceId());

        InvoicePaymentChange invoicePaymentChange = change.getInvoicePaymentChange();

        String paymentId = invoicePaymentChange.getId();
        paymentData.setPaymentId(paymentId);

        PaymentTerminalReceipt paymentTerminalReceipt = invoicePaymentChange
                .getPayload()
                .getInvoicePaymentSessionChange()
                .getPayload()
                .getSessionInteractionRequested()
                .getInteraction()
                .getPaymentTerminalReciept();

        paymentData.setPaymentShortId(paymentTerminalReceipt.getShortPaymentId());

        return paymentData;
    }

    @Override
    public ChangeType getChangeType() {
        return ChangeType.INVOICE_PAYMENT_TERMINAL_RECIEPT;
    }
}
