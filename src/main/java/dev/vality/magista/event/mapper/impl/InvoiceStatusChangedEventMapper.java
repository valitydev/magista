package dev.vality.magista.event.mapper.impl;

import dev.vality.damsel.payment_processing.InvoiceChange;
import dev.vality.damsel.payment_processing.InvoiceStatusChanged;
import dev.vality.geck.common.util.TBaseUtil;
import dev.vality.geck.common.util.TypeUtil;
import dev.vality.machinegun.eventsink.MachineEvent;
import dev.vality.magista.domain.enums.InvoiceEventType;
import dev.vality.magista.domain.enums.InvoiceStatus;
import dev.vality.magista.domain.tables.pojos.InvoiceData;
import dev.vality.magista.event.ChangeType;
import dev.vality.magista.event.mapper.InvoiceMapper;
import dev.vality.magista.util.DamselUtil;
import org.springframework.stereotype.Component;

@Component
public class InvoiceStatusChangedEventMapper implements InvoiceMapper {

    @Override
    public InvoiceData map(InvoiceChange change, MachineEvent machineEvent) {
        InvoiceData invoiceData = new InvoiceData();
        invoiceData.setEventType(InvoiceEventType.INVOICE_STATUS_CHANGED);
        invoiceData.setEventId(machineEvent.getEventId());
        invoiceData.setEventCreatedAt(TypeUtil.stringToLocalDateTime(machineEvent.getCreatedAt()));
        invoiceData.setInvoiceId(machineEvent.getSourceId());

        InvoiceStatusChanged invoiceStatusChanged = change.getInvoiceStatusChanged();
        invoiceData.setInvoiceStatus(
                TBaseUtil.unionFieldToEnum(invoiceStatusChanged.getStatus(), InvoiceStatus.class)
        );
        invoiceData.setInvoiceStatusDetails(
                DamselUtil.getInvoiceStatusDetails(invoiceStatusChanged.getStatus())
        );

        return invoiceData;
    }

    @Override
    public ChangeType getChangeType() {
        return ChangeType.INVOICE_STATUS_CHANGED;
    }
}
