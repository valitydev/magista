package dev.vality.magista.event.mapper.impl;

import dev.vality.damsel.payment_processing.InvoiceTemplateChange;
import dev.vality.geck.common.util.TypeUtil;
import dev.vality.machinegun.eventsink.MachineEvent;
import dev.vality.magista.domain.enums.InvoiceTemplateEventType;
import dev.vality.magista.domain.tables.pojos.InvoiceTemplate;
import dev.vality.magista.event.ChangeType;
import dev.vality.magista.event.mapper.InvoiceTemplateMapper;
import org.springframework.stereotype.Component;

@Component
public class InvoiceTemplateDeletedEventMapper implements InvoiceTemplateMapper {

    @Override
    public ChangeType getChangeType() {
        return ChangeType.INVOICE_TEMPLATE_DELETED;
    }

    @Override
    public InvoiceTemplate map(InvoiceTemplateChange change, MachineEvent machineEvent) {
        InvoiceTemplate invoiceTemplate = new InvoiceTemplate();
        invoiceTemplate.setEventId(machineEvent.getEventId());
        invoiceTemplate.setEventCreatedAt(TypeUtil.stringToLocalDateTime(machineEvent.getCreatedAt()));
        invoiceTemplate.setEventType(InvoiceTemplateEventType.INVOICE_TEMPLATE_DELETED);
        invoiceTemplate.setInvoiceTemplateId(machineEvent.getSourceId());
        return invoiceTemplate;
    }
}
