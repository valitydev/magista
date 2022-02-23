package dev.vality.magista.event.handler.impl;

import dev.vality.damsel.payment_processing.InvoiceChange;
import dev.vality.machinegun.eventsink.MachineEvent;
import dev.vality.magista.domain.tables.pojos.InvoiceData;
import dev.vality.magista.event.Processor;
import dev.vality.magista.event.handler.BatchHandler;
import dev.vality.magista.event.mapper.InvoiceMapper;
import dev.vality.magista.service.InvoiceService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.stream.Collectors;

@Component
@RequiredArgsConstructor
public class InvoiceBatchHandler implements BatchHandler<InvoiceChange, MachineEvent> {

    private final InvoiceService invoiceService;
    private final List<InvoiceMapper> mappers;

    @Override
    public Processor handle(List<Map.Entry<InvoiceChange, MachineEvent>> changes) {
        List<InvoiceData> invoiceEvents = changes.stream()
                .map(changeWithParent -> {
                    InvoiceChange change = changeWithParent.getKey();
                    for (InvoiceMapper invoiceMapper : getMappers()) {
                        if (invoiceMapper.accept(change)) {
                            return invoiceMapper.map(changeWithParent.getKey(), changeWithParent.getValue());
                        }
                    }
                    return null;
                })
                .filter(Objects::nonNull)
                .collect(Collectors.toList());

        return () -> invoiceService.saveInvoices(invoiceEvents);
    }

    @Override
    public List<InvoiceMapper> getMappers() {
        return mappers;
    }
}
