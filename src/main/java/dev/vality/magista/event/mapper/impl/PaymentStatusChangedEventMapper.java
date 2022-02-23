package dev.vality.magista.event.mapper.impl;

import dev.vality.damsel.domain.Cash;
import dev.vality.damsel.domain.Failure;
import dev.vality.damsel.domain.InvoicePaymentCaptured;
import dev.vality.damsel.domain.OperationFailure;
import dev.vality.damsel.payment_processing.InvoiceChange;
import dev.vality.damsel.payment_processing.InvoicePaymentChange;
import dev.vality.damsel.payment_processing.InvoicePaymentStatusChanged;
import dev.vality.geck.common.util.TBaseUtil;
import dev.vality.geck.common.util.TypeUtil;
import dev.vality.geck.serializer.kit.tbase.TErrorUtil;
import dev.vality.machinegun.eventsink.MachineEvent;
import dev.vality.magista.domain.enums.FailureClass;
import dev.vality.magista.domain.enums.InvoiceEventType;
import dev.vality.magista.domain.enums.InvoicePaymentStatus;
import dev.vality.magista.domain.tables.pojos.PaymentData;
import dev.vality.magista.event.ChangeType;
import dev.vality.magista.event.mapper.PaymentMapper;
import org.springframework.stereotype.Component;

@Component
public class PaymentStatusChangedEventMapper implements PaymentMapper {

    @Override
    public PaymentData map(InvoiceChange change, MachineEvent machineEvent) {

        PaymentData paymentData = new PaymentData();
        paymentData.setEventType(InvoiceEventType.INVOICE_PAYMENT_STATUS_CHANGED);
        paymentData.setEventId(machineEvent.getEventId());

        paymentData.setEventCreatedAt(TypeUtil.stringToLocalDateTime(machineEvent.getCreatedAt()));
        paymentData.setInvoiceId(machineEvent.getSourceId());

        InvoicePaymentChange invoicePaymentChange = change.getInvoicePaymentChange();
        paymentData.setPaymentId(invoicePaymentChange.getId());

        InvoicePaymentStatusChanged invoicePaymentStatusChanged = invoicePaymentChange
                .getPayload()
                .getInvoicePaymentStatusChanged();

        paymentData.setPaymentStatus(
                TBaseUtil.unionFieldToEnum(invoicePaymentStatusChanged.getStatus(), InvoicePaymentStatus.class)
        );

        if (invoicePaymentStatusChanged.getStatus().isSetCaptured()) {
            InvoicePaymentCaptured invoicePaymentCaptured = invoicePaymentStatusChanged.getStatus().getCaptured();
            if (invoicePaymentCaptured.isSetCost()) {
                Cash cost = invoicePaymentCaptured.getCost();
                paymentData.setPaymentAmount(cost.getAmount());
                paymentData.setPaymentCurrencyCode(cost.getCurrency().getSymbolicCode());
            }
        }

        if (invoicePaymentStatusChanged.getStatus().isSetFailed()) {
            OperationFailure operationFailure = invoicePaymentStatusChanged.getStatus().getFailed().getFailure();
            paymentData.setPaymentOperationFailureClass(
                    TBaseUtil.unionFieldToEnum(operationFailure, FailureClass.class)
            );
            if (operationFailure.isSetFailure()) {
                Failure failure = operationFailure.getFailure();
                paymentData.setPaymentExternalFailure(TErrorUtil.toStringVal(failure));
                paymentData.setPaymentExternalFailureReason(failure.getReason());
            }
        }

        return paymentData;
    }

    @Override
    public ChangeType getChangeType() {
        return ChangeType.INVOICE_PAYMENT_STATUS_CHANGED;
    }
}
