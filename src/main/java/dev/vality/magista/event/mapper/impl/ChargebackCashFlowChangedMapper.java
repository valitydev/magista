package dev.vality.magista.event.mapper.impl;

import dev.vality.damsel.domain.FinalCashFlowPosting;
import dev.vality.damsel.payment_processing.InvoiceChange;
import dev.vality.damsel.payment_processing.InvoicePaymentChange;
import dev.vality.damsel.payment_processing.InvoicePaymentChargebackCashFlowChanged;
import dev.vality.damsel.payment_processing.InvoicePaymentChargebackChange;
import dev.vality.geck.common.util.TypeUtil;
import dev.vality.machinegun.eventsink.MachineEvent;
import dev.vality.magista.domain.enums.InvoiceEventType;
import dev.vality.magista.domain.tables.pojos.ChargebackData;
import dev.vality.magista.event.ChangeType;
import dev.vality.magista.event.mapper.ChargebackMapper;
import dev.vality.magista.util.DamselUtil;
import dev.vality.magista.util.FeeType;
import org.springframework.stereotype.Component;

import java.util.List;
import java.util.Map;

@Component
public class ChargebackCashFlowChangedMapper implements ChargebackMapper {

    @Override
    public ChargebackData map(InvoiceChange change, MachineEvent machineEvent) {
        ChargebackData chargebackData = new ChargebackData();
        chargebackData.setEventId(machineEvent.getEventId());
        chargebackData.setEventCreatedAt(TypeUtil.stringToLocalDateTime(machineEvent.getCreatedAt()));
        chargebackData.setEventType(InvoiceEventType.INVOICE_PAYMENT_CHARGEBACK_CASHFLOW_CHANGED);
        chargebackData.setInvoiceId(machineEvent.getSourceId());

        InvoicePaymentChange invoicePaymentChange = change.getInvoicePaymentChange();

        String paymentId = invoicePaymentChange.getId();
        chargebackData.setPaymentId(paymentId);

        InvoicePaymentChargebackChange invoicePaymentChargebackChange = invoicePaymentChange
                .getPayload()
                .getInvoicePaymentChargebackChange();
        chargebackData.setChargebackId(invoicePaymentChargebackChange.getId());

        InvoicePaymentChargebackCashFlowChanged invoicePaymentChargebackCashFlowChanged = invoicePaymentChargebackChange
                .getPayload()
                .getInvoicePaymentChargebackCashFlowChanged();
        List<FinalCashFlowPosting> cashFlow = invoicePaymentChargebackCashFlowChanged.getCashFlow();
        Map<FeeType, Long> fees = DamselUtil.getFees(cashFlow);
        chargebackData.setChargebackFee(fees.getOrDefault(FeeType.FEE, 0L));
        chargebackData.setChargebackProviderFee(fees.getOrDefault(FeeType.PROVIDER_FEE, 0L));
        chargebackData.setChargebackExternalFee(fees.getOrDefault(FeeType.EXTERNAL_FEE, 0L));

        return chargebackData;
    }

    @Override
    public ChangeType getChangeType() {
        return ChangeType.INVOICE_PAYMENT_CASH_FLOW_CHANGED;
    }

}
