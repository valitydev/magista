package dev.vality.magista.event.mapper.impl;

import dev.vality.damsel.domain.InvoicePaymentChargeback;
import dev.vality.damsel.payment_processing.InvoiceChange;
import dev.vality.damsel.payment_processing.InvoicePaymentChange;
import dev.vality.damsel.payment_processing.InvoicePaymentChargebackCreated;
import dev.vality.geck.common.util.TBaseUtil;
import dev.vality.geck.common.util.TypeUtil;
import dev.vality.machinegun.eventsink.MachineEvent;
import dev.vality.magista.domain.enums.ChargebackCategory;
import dev.vality.magista.domain.enums.ChargebackStage;
import dev.vality.magista.domain.enums.ChargebackStatus;
import dev.vality.magista.domain.enums.InvoiceEventType;
import dev.vality.magista.domain.tables.pojos.ChargebackData;
import dev.vality.magista.event.ChangeType;
import dev.vality.magista.event.mapper.ChargebackMapper;
import org.springframework.stereotype.Component;

@Component
public class ChargebackCreatedMapper implements ChargebackMapper {

    @Override
    public ChargebackData map(InvoiceChange change, MachineEvent machineEvent) {
        ChargebackData chargeback = new ChargebackData();
        chargeback.setEventId(machineEvent.getEventId());
        chargeback.setEventCreatedAt(TypeUtil.stringToLocalDateTime(machineEvent.getCreatedAt()));
        chargeback.setEventType(InvoiceEventType.INVOICE_PAYMENT_CHARGEBACK_CREATED);
        chargeback.setInvoiceId(machineEvent.getSourceId());

        InvoicePaymentChange invoicePaymentChange = change.getInvoicePaymentChange();

        String paymentId = invoicePaymentChange.getId();
        chargeback.setPaymentId(paymentId);

        InvoicePaymentChargebackCreated invoicePaymentChargebackCreated = invoicePaymentChange
                .getPayload()
                .getInvoicePaymentChargebackChange()
                .getPayload()
                .getInvoicePaymentChargebackCreated();

        InvoicePaymentChargeback invoicePaymentChargeback = invoicePaymentChargebackCreated.getChargeback();

        chargeback.setChargebackId(invoicePaymentChargeback.getId());
        chargeback.setChargebackStatus(
                TBaseUtil.unionFieldToEnum(invoicePaymentChargeback.getStatus(), ChargebackStatus.class));
        chargeback.setChargebackCreatedAt(TypeUtil.stringToLocalDateTime(invoicePaymentChargeback.getCreatedAt()));
        chargeback.setChargebackAmount(invoicePaymentChargeback.getBody().getAmount());
        chargeback.setChargebackCurrencyCode(invoicePaymentChargeback.getBody().getCurrency().getSymbolicCode());
        chargeback.setChargebackLevyAmount(invoicePaymentChargeback.getLevy().getAmount());
        chargeback.setChargebackStage(
                TBaseUtil.unionFieldToEnum(invoicePaymentChargeback.getStage(), ChargebackStage.class)
        );
        chargeback.setChargebackLevyCurrencyCode(invoicePaymentChargeback.getLevy().getCurrency().getSymbolicCode());
        chargeback.setChargebackReason(invoicePaymentChargeback.getReason().getCode());
        chargeback.setChargebackReasonCategory(
                TBaseUtil.unionFieldToEnum(invoicePaymentChargeback.getReason().getCategory(), ChargebackCategory.class)
        );
        chargeback.setChargebackDomainRevision(invoicePaymentChargeback.getDomainRevision());
        chargeback.setChargebackPartyRevision(invoicePaymentChargeback.getPartyRevision());
        if (invoicePaymentChargeback.getContext() != null) {
            chargeback.setChargebackContext(invoicePaymentChargeback.getContext().getData());
        }
        chargeback.setExternalId(invoicePaymentChargeback.getExternalId());

        return chargeback;
    }

    @Override
    public ChangeType getChangeType() {
        return ChangeType.INVOICE_PAYMENT_CHARGEBACK_CREATED;
    }
}
