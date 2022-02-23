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
import org.springframework.util.StringUtils;

@Component
public class InvoiceTemplateCreatedEventMapper implements InvoiceTemplateMapper {

    @Override
    public ChangeType getChangeType() {
        return ChangeType.INVOICE_TEMPLATE_CREATED;
    }

    @Override
    public InvoiceTemplate map(InvoiceTemplateChange change, MachineEvent machineEvent) {
        InvoiceTemplate invoiceTemplate = new InvoiceTemplate();
        invoiceTemplate.setEventId(machineEvent.getEventId());
        var eventCreatedAt = TypeUtil.stringToLocalDateTime(machineEvent.getCreatedAt());
        invoiceTemplate.setEventCreatedAt(eventCreatedAt);
        invoiceTemplate.setEventType(InvoiceTemplateEventType.INVOICE_TEMPLATE_CREATED);
        invoiceTemplate.setInvoiceTemplateId(machineEvent.getSourceId());
        var invoiceTemplateThrift = change.getInvoiceTemplateCreated().getInvoiceTemplate();
        invoiceTemplate.setPartyId(invoiceTemplateThrift.getOwnerId());
        invoiceTemplate.setShopId(invoiceTemplateThrift.getShopId());
        invoiceTemplate.setInvoiceValidUntil(
                LifetimeIntervalThriftUtil.getInvoiceValidUntil(
                        eventCreatedAt,
                        invoiceTemplateThrift.getInvoiceLifetime()));
        invoiceTemplate.setProduct(invoiceTemplateThrift.getProduct());
        invoiceTemplate.setDescription(invoiceTemplateThrift.getDescription());
        var details = invoiceTemplateThrift.getDetails();
        switch (details.getSetField()) {
            case CART -> invoiceTemplate.setInvoiceDetailsCartJson(DamselUtil.toJsonString(details.getCart()));
            case PRODUCT -> invoiceTemplate.setInvoiceDetailsProductJson(DamselUtil.toJsonString(details.getProduct()));
            default -> throw new IllegalArgumentException("Unknown field parameter, details=" + details);
        }
        if (invoiceTemplateThrift.isSetContext()) {
            var content = invoiceTemplateThrift.getContext();
            invoiceTemplate.setInvoiceContextType(content.getType());
            invoiceTemplate.setInvoiceContextData(content.getData());
        }
        invoiceTemplate.setName(invoiceTemplateThrift.getName());
        invoiceTemplate.setInvoiceTemplateCreatedAt(
                StringUtils.hasText(invoiceTemplateThrift.getCreatedAt())
                        ? TypeUtil.stringToLocalDateTime(invoiceTemplateThrift.getCreatedAt())
                        : null);
        return invoiceTemplate;
    }
}
