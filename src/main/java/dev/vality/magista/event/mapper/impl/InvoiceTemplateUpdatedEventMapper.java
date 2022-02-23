package dev.vality.magista.event.mapper.impl;

import dev.vality.damsel.payment_processing.InvoiceTemplateChange;
import dev.vality.geck.common.util.TypeUtil;
import dev.vality.machinegun.eventsink.MachineEvent;
import dev.vality.magista.domain.enums.InvoiceTemplateEventType;
import dev.vality.magista.domain.tables.pojos.InvoiceTemplate;
import dev.vality.magista.event.ChangeType;
import dev.vality.magista.event.mapper.InvoiceTemplateMapper;
import dev.vality.magista.util.DamselUtil;
import dev.vality.magista.util.LifetimeIntervalThriftUtil;
import org.springframework.stereotype.Component;

@Component
public class InvoiceTemplateUpdatedEventMapper implements InvoiceTemplateMapper {

    @Override
    public ChangeType getChangeType() {
        return ChangeType.INVOICE_TEMPLATE_UPDATED;
    }

    @Override
    public InvoiceTemplate map(InvoiceTemplateChange change, MachineEvent machineEvent) {
        InvoiceTemplate invoiceTemplate = new InvoiceTemplate();
        invoiceTemplate.setEventId(machineEvent.getEventId());
        var eventCreatedAt = TypeUtil.stringToLocalDateTime(machineEvent.getCreatedAt());
        invoiceTemplate.setEventCreatedAt(eventCreatedAt);
        invoiceTemplate.setEventType(InvoiceTemplateEventType.INVOICE_TEMPLATE_UPDATED);
        invoiceTemplate.setInvoiceTemplateId(machineEvent.getSourceId());
        var updateParams = change.getInvoiceTemplateUpdated().getDiff();
        if (updateParams.isSetInvoiceLifetime()) {
            invoiceTemplate.setInvoiceValidUntil(
                    LifetimeIntervalThriftUtil.getInvoiceValidUntil(
                            eventCreatedAt,
                            updateParams.getInvoiceLifetime()));
        }
        invoiceTemplate.setProduct(updateParams.getProduct());
        invoiceTemplate.setDescription(updateParams.getDescription());
        if (updateParams.isSetDetails()) {
            var details = updateParams.getDetails();
            switch (details.getSetField()) {
                case CART -> invoiceTemplate.setInvoiceDetailsCartJson(DamselUtil.toJsonString(details.getCart()));
                case PRODUCT -> invoiceTemplate.setInvoiceDetailsProductJson(
                        DamselUtil.toJsonString(details.getProduct()));
                default -> throw new IllegalArgumentException("Unknown field parameter, details=" + details);
            }
        }
        if (updateParams.isSetContext()) {
            var content = updateParams.getContext();
            invoiceTemplate.setInvoiceContextType(content.getType());
            invoiceTemplate.setInvoiceContextData(content.getData());
        }
        invoiceTemplate.setName(updateParams.getName());
        return invoiceTemplate;
    }
}
