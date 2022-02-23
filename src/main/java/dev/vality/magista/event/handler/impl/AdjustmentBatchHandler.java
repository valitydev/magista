package dev.vality.magista.event.handler.impl;

import dev.vality.damsel.payment_processing.InvoiceChange;
import dev.vality.machinegun.eventsink.MachineEvent;
import dev.vality.magista.domain.tables.pojos.AdjustmentData;
import dev.vality.magista.event.Processor;
import dev.vality.magista.event.handler.BatchHandler;
import dev.vality.magista.event.mapper.AdjustmentMapper;
import dev.vality.magista.service.PaymentAdjustmentService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.stream.Collectors;

@Component
@RequiredArgsConstructor
public class AdjustmentBatchHandler implements BatchHandler<InvoiceChange, MachineEvent> {

    private final PaymentAdjustmentService paymentAdjustmentService;
    private final List<AdjustmentMapper> mappers;

    @Override
    public Processor handle(List<Map.Entry<InvoiceChange, MachineEvent>> changes) {
        List<AdjustmentData> adjustmentEvents = changes.stream()
                .map(changeWithParent -> {
                    InvoiceChange change = changeWithParent.getKey();
                    for (AdjustmentMapper adjustmentMapper : getMappers()) {
                        if (adjustmentMapper.accept(change)) {
                            return adjustmentMapper.map(changeWithParent.getKey(), changeWithParent.getValue());
                        }
                    }
                    return null;
                })
                .filter(Objects::nonNull)
                .collect(Collectors.toList());

        return () -> paymentAdjustmentService.saveAdjustments(adjustmentEvents);
    }

    @Override
    public List<AdjustmentMapper> getMappers() {
        return mappers;
    }

}
