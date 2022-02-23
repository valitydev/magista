package dev.vality.magista.event.mapper.impl;

import dev.vality.damsel.domain.Cash;
import dev.vality.damsel.domain.InvoicePaymentRefund;
import dev.vality.damsel.payment_processing.InvoiceChange;
import dev.vality.damsel.payment_processing.InvoicePaymentChange;
import dev.vality.damsel.payment_processing.InvoicePaymentRefundCreated;
import dev.vality.geck.common.util.TBaseUtil;
import dev.vality.geck.common.util.TypeUtil;
import dev.vality.machinegun.eventsink.MachineEvent;
import dev.vality.magista.domain.enums.InvoiceEventType;
import dev.vality.magista.domain.enums.RefundStatus;
import dev.vality.magista.domain.tables.pojos.RefundData;
import dev.vality.magista.event.ChangeType;
import dev.vality.magista.event.mapper.RefundMapper;
import dev.vality.magista.util.DamselUtil;
import dev.vality.magista.util.FeeType;
import org.springframework.stereotype.Component;

import java.util.Map;

@Component
public class RefundCreatedMapper implements RefundMapper {

    @Override
    public RefundData map(InvoiceChange change, MachineEvent machineEvent) {
        RefundData refund = new RefundData();

        refund.setEventId(machineEvent.getEventId());
        refund.setEventCreatedAt(TypeUtil.stringToLocalDateTime(machineEvent.getCreatedAt()));
        refund.setEventType(InvoiceEventType.INVOICE_PAYMENT_REFUND_CREATED);
        refund.setInvoiceId(machineEvent.getSourceId());

        InvoicePaymentChange invoicePaymentChange = change.getInvoicePaymentChange();

        String paymentId = invoicePaymentChange.getId();
        refund.setPaymentId(paymentId);

        InvoicePaymentRefundCreated invoicePaymentRefundCreated = invoicePaymentChange
                .getPayload()
                .getInvoicePaymentRefundChange()
                .getPayload()
                .getInvoicePaymentRefundCreated();

        InvoicePaymentRefund invoicePaymentRefund = invoicePaymentRefundCreated
                .getRefund();

        refund.setRefundId(invoicePaymentRefund.getId());
        refund.setRefundStatus(
                TBaseUtil.unionFieldToEnum(invoicePaymentRefund.getStatus(), RefundStatus.class)
        );
        refund.setRefundReason(invoicePaymentRefund.getReason());
        refund.setRefundCreatedAt(
                TypeUtil.stringToLocalDateTime(invoicePaymentRefund.getCreatedAt())
        );
        if (invoicePaymentRefund.isSetCash()) {
            Cash refundCash = invoicePaymentRefund.getCash();
            refund.setRefundAmount(refundCash.getAmount());
            refund.setRefundCurrencyCode(refundCash.getCurrency().getSymbolicCode());
        }
        refund.setRefundDomainRevision(invoicePaymentRefund.getDomainRevision());

        Map<FeeType, Long> fees = DamselUtil.getFees(invoicePaymentRefundCreated.getCashFlow());
        refund.setRefundFee(fees.getOrDefault(FeeType.FEE, 0L));
        refund.setRefundProviderFee(fees.getOrDefault(FeeType.PROVIDER_FEE, 0L));
        refund.setRefundExternalFee(fees.getOrDefault(FeeType.EXTERNAL_FEE, 0L));
        refund.setExternalId(invoicePaymentRefund.getExternalId());

        return refund;
    }

    @Override
    public ChangeType getChangeType() {
        return ChangeType.INVOICE_PAYMENT_REFUND_CREATED;
    }

}
