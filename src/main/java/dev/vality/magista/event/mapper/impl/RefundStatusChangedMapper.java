package dev.vality.magista.event.mapper.impl;

import dev.vality.damsel.domain.Failure;
import dev.vality.damsel.domain.InvoicePaymentRefundStatus;
import dev.vality.damsel.domain.OperationFailure;
import dev.vality.damsel.payment_processing.InvoiceChange;
import dev.vality.damsel.payment_processing.InvoicePaymentChange;
import dev.vality.damsel.payment_processing.InvoicePaymentRefundChange;
import dev.vality.geck.common.util.TBaseUtil;
import dev.vality.geck.common.util.TypeUtil;
import dev.vality.geck.serializer.kit.tbase.TErrorUtil;
import dev.vality.machinegun.eventsink.MachineEvent;
import dev.vality.magista.domain.enums.FailureClass;
import dev.vality.magista.domain.enums.InvoiceEventType;
import dev.vality.magista.domain.enums.RefundStatus;
import dev.vality.magista.domain.tables.pojos.RefundData;
import dev.vality.magista.event.ChangeType;
import dev.vality.magista.event.mapper.RefundMapper;
import org.springframework.stereotype.Component;

@Component
public class RefundStatusChangedMapper implements RefundMapper {

    @Override
    public RefundData map(InvoiceChange change, MachineEvent machineEvent) {
        RefundData refundData = new RefundData();

        refundData.setEventId(machineEvent.getEventId());
        refundData.setEventType(InvoiceEventType.INVOICE_PAYMENT_REFUND_STATUS_CHANGED);
        refundData.setEventCreatedAt(TypeUtil.stringToLocalDateTime(machineEvent.getCreatedAt()));

        String invoiceId = machineEvent.getSourceId();
        refundData.setInvoiceId(invoiceId);

        InvoicePaymentChange invoicePaymentChange = change.getInvoicePaymentChange();

        String paymentId = invoicePaymentChange.getId();
        refundData.setPaymentId(paymentId);

        InvoicePaymentRefundChange refundChange = invoicePaymentChange
                .getPayload()
                .getInvoicePaymentRefundChange();

        String refundId = refundChange.getId();
        refundData.setRefundId(refundId);

        InvoicePaymentRefundStatus status = refundChange.getPayload()
                .getInvoicePaymentRefundStatusChanged()
                .getStatus();

        refundData.setRefundStatus(TBaseUtil.unionFieldToEnum(
                status,
                RefundStatus.class
        ));
        if (status.isSetFailed()) {
            OperationFailure operationFailure = status.getFailed().getFailure();
            refundData.setRefundOperationFailureClass(
                    TBaseUtil.unionFieldToEnum(operationFailure, FailureClass.class)
            );
            if (operationFailure.isSetFailure()) {
                Failure failure = operationFailure.getFailure();
                refundData.setRefundExternalFailure(TErrorUtil.toStringVal(failure));
                refundData.setRefundExternalFailureReason(failure.getReason());
            }
        }

        return refundData;
    }

    @Override
    public ChangeType getChangeType() {
        return ChangeType.INVOICE_PAYMENT_REFUND_STATUS_CHANGED;
    }

}
