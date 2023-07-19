package dev.vality.magista.event.mapper.impl;

import dev.vality.damsel.domain.Cash;
import dev.vality.damsel.payment_processing.InvoiceChange;
import dev.vality.damsel.payment_processing.InvoicePaymentChange;
import dev.vality.damsel.payment_processing.InvoicePaymentChargebackChange;
import dev.vality.damsel.payment_processing.InvoicePaymentChargebackLevyChanged;
import dev.vality.geck.common.util.TypeUtil;
import dev.vality.machinegun.eventsink.MachineEvent;
import dev.vality.magista.domain.enums.InvoiceEventType;
import dev.vality.magista.domain.tables.pojos.ChargebackData;
import dev.vality.magista.event.ChangeType;
import dev.vality.magista.event.mapper.ChargebackMapper;
import org.springframework.stereotype.Component;

@Component
public class ChargebackLevyChangedMapper implements ChargebackMapper {

    @Override
    public ChargebackData map(InvoiceChange change, MachineEvent machineEvent) {
        ChargebackData chargebackData = new ChargebackData();
        chargebackData.setEventId(machineEvent.getEventId());
        chargebackData.setEventCreatedAt(TypeUtil.stringToLocalDateTime(machineEvent.getCreatedAt()));
        chargebackData.setEventType(InvoiceEventType.INVOICE_PAYMENT_CHARGEBACK_LEVY_CHANGED);
        chargebackData.setInvoiceId(machineEvent.getSourceId());

        InvoicePaymentChange invoicePaymentChange = change.getInvoicePaymentChange();

        String paymentId = invoicePaymentChange.getId();
        chargebackData.setPaymentId(paymentId);

        InvoicePaymentChargebackChange invoicePaymentChargebackChange = invoicePaymentChange
                .getPayload()
                .getInvoicePaymentChargebackChange();
        chargebackData.setChargebackId(invoicePaymentChargebackChange.getId());

        InvoicePaymentChargebackLevyChanged invoicePaymentChargebackLevyChanged = invoicePaymentChargebackChange
                .getPayload()
                .getInvoicePaymentChargebackLevyChanged();
        Cash levy = invoicePaymentChargebackLevyChanged.getLevy();
        chargebackData.setChargebackLevyAmount(levy.getAmount());
        chargebackData.setChargebackLevyCurrencyCode(levy.getCurrency().getSymbolicCode());

        return chargebackData;
    }

    @Override
    public ChangeType getChangeType() {
        return ChangeType.INVOICE_PAYMENT_CHARGEBACK_LEVY_CHANGED;
    }
}
