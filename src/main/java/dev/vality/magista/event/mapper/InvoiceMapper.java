package dev.vality.magista.event.mapper;

import dev.vality.damsel.payment_processing.InvoiceChange;
import dev.vality.machinegun.eventsink.MachineEvent;
import dev.vality.magista.domain.tables.pojos.InvoiceData;

public interface InvoiceMapper extends Mapper<InvoiceChange, MachineEvent, InvoiceData> {
}
