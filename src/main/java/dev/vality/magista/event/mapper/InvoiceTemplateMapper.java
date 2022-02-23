package dev.vality.magista.event.mapper;

import dev.vality.damsel.payment_processing.InvoiceTemplateChange;
import dev.vality.machinegun.eventsink.MachineEvent;
import dev.vality.magista.domain.tables.pojos.InvoiceTemplate;

public interface InvoiceTemplateMapper extends Mapper<InvoiceTemplateChange, MachineEvent, InvoiceTemplate> {
}
