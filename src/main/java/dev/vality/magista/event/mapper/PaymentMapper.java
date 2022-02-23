package dev.vality.magista.event.mapper;

import dev.vality.damsel.payment_processing.InvoiceChange;
import dev.vality.machinegun.eventsink.MachineEvent;
import dev.vality.magista.domain.tables.pojos.PaymentData;

public interface PaymentMapper extends Mapper<InvoiceChange, MachineEvent, PaymentData> {
}
