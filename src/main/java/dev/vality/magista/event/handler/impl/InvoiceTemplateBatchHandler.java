package dev.vality.magista.event.handler.impl;

import dev.vality.damsel.payment_processing.InvoiceTemplateChange;
import dev.vality.machinegun.eventsink.MachineEvent;
import dev.vality.magista.domain.tables.pojos.InvoiceTemplate;
import dev.vality.magista.event.Processor;
import dev.vality.magista.event.handler.BatchHandler;
import dev.vality.magista.event.mapper.InvoiceTemplateMapper;
import dev.vality.magista.service.InvoiceTemplateService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.stream.Collectors;

@Component
@RequiredArgsConstructor
public class InvoiceTemplateBatchHandler implements BatchHandler<InvoiceTemplateChange, MachineEvent> {

    private final InvoiceTemplateService invoiceTemplateService;
    private final List<InvoiceTemplateMapper> mappers;

    @Override
    public Processor handle(List<Map.Entry<InvoiceTemplateChange, MachineEvent>> changes) {
        List<InvoiceTemplate> invoiceTemplates = changes.stream()
                .map(changeWithParent -> {
                    InvoiceTemplateChange change = changeWithParent.getKey();
                    for (InvoiceTemplateMapper invoiceTemplateMapper : getMappers()) {
                        if (invoiceTemplateMapper.accept(change)) {
                            return invoiceTemplateMapper.map(changeWithParent.getKey(), changeWithParent.getValue());
                        }
                    }
                    return null;
                })
                .filter(Objects::nonNull)
                .collect(Collectors.toList());

        return () -> invoiceTemplateService.save(invoiceTemplates);
    }

    @Override
    public List<InvoiceTemplateMapper> getMappers() {
        return mappers;
    }
}
