package dev.vality.magista.event.mapper.impl;

import dev.vality.damsel.base.Content;
import dev.vality.damsel.domain.Invoice;
import dev.vality.damsel.domain.InvoiceDetails;
import dev.vality.damsel.domain.InvoiceStatus;
import dev.vality.damsel.payment_processing.InvoiceChange;
import dev.vality.geck.common.util.TBaseUtil;
import dev.vality.geck.common.util.TypeUtil;
import dev.vality.machinegun.eventsink.MachineEvent;
import dev.vality.magista.domain.enums.InvoiceEventType;
import dev.vality.magista.domain.tables.pojos.InvoiceData;
import dev.vality.magista.event.ChangeType;
import dev.vality.magista.event.mapper.InvoiceMapper;
import dev.vality.magista.util.DamselUtil;
import org.springframework.stereotype.Component;

import java.util.UUID;

@Component
public class InvoiceCreatedEventMapper implements InvoiceMapper {

    @Override
    public InvoiceData map(InvoiceChange change, MachineEvent machineEvent) {
        InvoiceData invoiceData = new InvoiceData();
        invoiceData.setEventType(InvoiceEventType.INVOICE_CREATED);
        invoiceData.setEventId(machineEvent.getEventId());
        invoiceData.setInvoiceId(machineEvent.getSourceId());

        invoiceData.setEventCreatedAt(TypeUtil.stringToLocalDateTime(machineEvent.getCreatedAt()));

        Invoice invoice = change.getInvoiceCreated().getInvoice();

        invoiceData.setInvoiceId(invoice.getId());
        invoiceData.setPartyId(UUID.fromString(invoice.getOwnerId()));
        invoiceData.setPartyShopId(invoice.getShopId());
        invoiceData.setInvoiceTemplateId(invoice.getTemplateId());
        invoiceData.setInvoiceCreatedAt(TypeUtil.stringToLocalDateTime(invoice.getCreatedAt()));
        invoiceData.setInvoiceDue(TypeUtil.stringToLocalDateTime(invoice.getDue()));
        invoiceData.setInvoiceAmount(invoice.getCost().getAmount());
        invoiceData.setInvoiceCurrencyCode(invoice.getCost().getCurrency().getSymbolicCode());

        InvoiceDetails details = invoice.getDetails();
        invoiceData.setInvoiceProduct(details.getProduct());
        invoiceData.setInvoiceDescription(details.getDescription());
        if (details.isSetCart()) {
            invoiceData.setInvoiceCartJson(DamselUtil.toJsonString(details.getCart()));
        }

        if (invoice.isSetPartyRevision()) {
            invoiceData.setInvoicePartyRevision(invoice.getPartyRevision());
        }

        if (invoice.isSetContext()) {
            Content content = invoice.getContext();
            invoiceData.setInvoiceContextType(content.getType());
            invoiceData.setInvoiceContext(content.getData());
        }

        InvoiceStatus invoiceStatus = invoice.getStatus();
        invoiceData.setInvoiceStatus(
                TBaseUtil.unionFieldToEnum(
                        invoiceStatus,
                        dev.vality.magista.domain.enums.InvoiceStatus.class
                )
        );
        invoiceData.setInvoiceStatusDetails(
                DamselUtil.getInvoiceStatusDetails(invoiceStatus)
        );
        invoiceData.setExternalId(invoice.getExternalId());

        return invoiceData;
    }

    @Override
    public ChangeType getChangeType() {
        return ChangeType.INVOICE_CREATED;
    }

}
