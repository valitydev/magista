package dev.vality.magista.event.handler.impl;

import dev.vality.damsel.payment_processing.InvoiceChange;
import dev.vality.machinegun.eventsink.MachineEvent;
import dev.vality.magista.event.Processor;
import dev.vality.magista.event.handler.BatchHandler;
import dev.vality.magista.event.mapper.Mapper;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@Slf4j
@RequiredArgsConstructor
public class UnsupportedBatchHandler implements BatchHandler<InvoiceChange, MachineEvent> {

    @Override
    public Processor handle(List<Map.Entry<InvoiceChange, MachineEvent>> changes) {
        return () -> {
            if (log.isDebugEnabled()) {
                log.debug(
                        "Unsupported changes, size={}, eventSize={}, changes='{}'",
                        changes.size(),
                        changes.stream().map(Map.Entry::getValue).distinct().count(),
                        changes.stream().map(Map.Entry::getKey).collect(Collectors.toList())
                );
            }
        };
    }

    @Override
    public List<? extends Mapper> getMappers() {
        return Collections.EMPTY_LIST;
    }

}
