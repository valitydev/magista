package dev.vality.magista.event.mapper.impl;

import dev.vality.damsel.domain.FinalCashFlowPosting;
import dev.vality.damsel.payment_processing.InvoiceChange;
import dev.vality.damsel.payment_processing.InvoicePaymentChange;
import dev.vality.geck.common.util.TypeUtil;
import dev.vality.machinegun.eventsink.MachineEvent;
import dev.vality.magista.domain.enums.InvoiceEventType;
import dev.vality.magista.domain.tables.pojos.PaymentData;
import dev.vality.magista.event.ChangeType;
import dev.vality.magista.event.mapper.PaymentMapper;
import dev.vality.magista.util.DamselUtil;
import dev.vality.magista.util.FeeType;
import org.springframework.stereotype.Component;

import java.util.List;
import java.util.Map;

@Component
public class InvoicePaymentCashFlowChangedEventMapper implements PaymentMapper {

    @Override
    public PaymentData map(InvoiceChange change, MachineEvent machineEvent) {
        PaymentData paymentData = new PaymentData();
        paymentData.setEventType(InvoiceEventType.INVOICE_PAYMENT_CASH_FLOW_CHANGED);
        paymentData.setEventId(machineEvent.getEventId());
        paymentData.setEventCreatedAt(TypeUtil.stringToLocalDateTime(machineEvent.getCreatedAt()));
        paymentData.setInvoiceId(machineEvent.getSourceId());

        InvoicePaymentChange invoicePaymentChange = change.getInvoicePaymentChange();
        paymentData.setPaymentId(invoicePaymentChange.getId());

        List<FinalCashFlowPosting> finalCashFlowPostings = invoicePaymentChange
                .getPayload()
                .getInvoicePaymentCashFlowChanged()
                .getCashFlow();

        Map<FeeType, Long> fees = DamselUtil.getFees(finalCashFlowPostings);
        paymentData.setPaymentAmount(fees.getOrDefault(FeeType.AMOUNT, 0L));
        paymentData.setPaymentFee(fees.getOrDefault(FeeType.FEE, 0L));
        paymentData.setPaymentExternalFee(fees.getOrDefault(FeeType.EXTERNAL_FEE, 0L));
        paymentData.setPaymentProviderFee(fees.getOrDefault(FeeType.PROVIDER_FEE, 0L));

        return paymentData;
    }

    @Override
    public ChangeType getChangeType() {
        return ChangeType.INVOICE_PAYMENT_CASH_FLOW_CHANGED;
    }
}
