package dev.vality.magista.event.handler.impl;

import dev.vality.damsel.payment_processing.InvoiceChange;
import dev.vality.machinegun.eventsink.MachineEvent;
import dev.vality.magista.domain.tables.pojos.RefundData;
import dev.vality.magista.event.Processor;
import dev.vality.magista.event.handler.BatchHandler;
import dev.vality.magista.event.mapper.RefundMapper;
import dev.vality.magista.service.PaymentRefundService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.stream.Collectors;

@Component
@RequiredArgsConstructor
public class RefundBatchHandler implements BatchHandler<InvoiceChange, MachineEvent> {
    private final PaymentRefundService paymentRefundService;
    private final List<RefundMapper> mappers;

    @Override
    public Processor handle(List<Map.Entry<InvoiceChange, MachineEvent>> changes) {
        List<RefundData> refundEvents = changes.stream()
                .map(changeWithParent -> {
                    InvoiceChange change = changeWithParent.getKey();
                    for (RefundMapper refundMapper : getMappers()) {
                        if (refundMapper.accept(change)) {
                            return refundMapper.map(changeWithParent.getKey(), changeWithParent.getValue());
                        }
                    }
                    return null;
                })
                .filter(Objects::nonNull)
                .collect(Collectors.toList());

        return () -> paymentRefundService.saveRefunds(refundEvents);
    }

    @Override
    public List<RefundMapper> getMappers() {
        return mappers;
    }
}
