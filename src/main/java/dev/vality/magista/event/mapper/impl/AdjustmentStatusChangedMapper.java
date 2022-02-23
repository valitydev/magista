package dev.vality.magista.event.mapper.impl;

import dev.vality.damsel.domain.InvoicePaymentAdjustmentStatus;
import dev.vality.damsel.payment_processing.InvoiceChange;
import dev.vality.damsel.payment_processing.InvoicePaymentAdjustmentChange;
import dev.vality.damsel.payment_processing.InvoicePaymentAdjustmentStatusChanged;
import dev.vality.damsel.payment_processing.InvoicePaymentChange;
import dev.vality.geck.common.util.TBaseUtil;
import dev.vality.geck.common.util.TypeUtil;
import dev.vality.machinegun.eventsink.MachineEvent;
import dev.vality.magista.domain.enums.AdjustmentStatus;
import dev.vality.magista.domain.enums.InvoiceEventType;
import dev.vality.magista.domain.tables.pojos.AdjustmentData;
import dev.vality.magista.event.ChangeType;
import dev.vality.magista.event.mapper.AdjustmentMapper;
import dev.vality.magista.util.DamselUtil;
import org.springframework.stereotype.Component;

@Component
public class AdjustmentStatusChangedMapper implements AdjustmentMapper {

    @Override
    public AdjustmentData map(InvoiceChange change, MachineEvent machineEvent) {
        AdjustmentData adjustment = new AdjustmentData();

        adjustment.setEventId(machineEvent.getEventId());
        adjustment.setEventCreatedAt(TypeUtil.stringToLocalDateTime(machineEvent.getCreatedAt()));
        adjustment.setEventType(InvoiceEventType.INVOICE_PAYMENT_ADJUSTMENT_STATUS_CHANGED);
        adjustment.setInvoiceId(machineEvent.getSourceId());

        InvoicePaymentChange invoicePaymentChange = change.getInvoicePaymentChange();

        String paymentId = invoicePaymentChange.getId();
        adjustment.setPaymentId(paymentId);

        InvoicePaymentAdjustmentChange adjustmentChange = invoicePaymentChange
                .getPayload()
                .getInvoicePaymentAdjustmentChange();
        adjustment.setAdjustmentId(adjustmentChange.getId());

        InvoicePaymentAdjustmentStatusChanged adjustmentStatusChanged = adjustmentChange
                .getPayload()
                .getInvoicePaymentAdjustmentStatusChanged();

        InvoicePaymentAdjustmentStatus invoicePaymentAdjustmentStatus = adjustmentStatusChanged.getStatus();
        adjustment.setAdjustmentStatus(
                TBaseUtil.unionFieldToEnum(invoicePaymentAdjustmentStatus, AdjustmentStatus.class)
        );
        adjustment.setAdjustmentStatusCreatedAt(
                DamselUtil.getAdjustmentStatusCreatedAt(invoicePaymentAdjustmentStatus)
        );

        return adjustment;
    }

    @Override
    public ChangeType getChangeType() {
        return ChangeType.INVOICE_PAYMENT_ADJUSTMENT_STATUS_CHANGED;
    }

}
