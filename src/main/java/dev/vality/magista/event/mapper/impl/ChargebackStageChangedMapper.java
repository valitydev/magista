package dev.vality.magista.event.mapper.impl;

import dev.vality.damsel.domain.InvoicePaymentChargebackStage;
import dev.vality.damsel.payment_processing.InvoiceChange;
import dev.vality.damsel.payment_processing.InvoicePaymentChange;
import dev.vality.damsel.payment_processing.InvoicePaymentChargebackChange;
import dev.vality.damsel.payment_processing.InvoicePaymentChargebackStageChanged;
import dev.vality.geck.common.util.TBaseUtil;
import dev.vality.geck.common.util.TypeUtil;
import dev.vality.machinegun.eventsink.MachineEvent;
import dev.vality.magista.domain.enums.ChargebackStage;
import dev.vality.magista.domain.enums.InvoiceEventType;
import dev.vality.magista.domain.tables.pojos.ChargebackData;
import dev.vality.magista.event.ChangeType;
import dev.vality.magista.event.mapper.ChargebackMapper;
import org.springframework.stereotype.Component;

@Component
public class ChargebackStageChangedMapper implements ChargebackMapper {

    @Override
    public ChargebackData map(InvoiceChange change, MachineEvent machineEvent) {
        ChargebackData chargebackData = new ChargebackData();
        chargebackData.setEventId(machineEvent.getEventId());
        chargebackData.setEventCreatedAt(TypeUtil.stringToLocalDateTime(machineEvent.getCreatedAt()));
        chargebackData.setEventType(InvoiceEventType.INVOICE_PAYMENT_CHARGEBACK_STAGE_CHANGE);
        chargebackData.setInvoiceId(machineEvent.getSourceId());

        InvoicePaymentChange invoicePaymentChange = change.getInvoicePaymentChange();

        String paymentId = invoicePaymentChange.getId();
        chargebackData.setPaymentId(paymentId);

        InvoicePaymentChargebackChange invoicePaymentChargebackChange = invoicePaymentChange
                .getPayload()
                .getInvoicePaymentChargebackChange();
        chargebackData.setChargebackId(invoicePaymentChargebackChange.getId());

        InvoicePaymentChargebackStageChanged invoicePaymentChargebackStageChanged = invoicePaymentChargebackChange
                .getPayload()
                .getInvoicePaymentChargebackStageChanged();
        InvoicePaymentChargebackStage stage = invoicePaymentChargebackStageChanged.getStage();
        chargebackData.setChargebackStage(TBaseUtil.unionFieldToEnum(stage, ChargebackStage.class));

        return chargebackData;
    }

    @Override
    public ChangeType getChangeType() {
        return ChangeType.INVOICE_PAYMENT_CHARGEBACK_STAGE_CHANGED;
    }
}
