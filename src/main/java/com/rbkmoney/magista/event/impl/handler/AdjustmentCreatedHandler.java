package com.rbkmoney.magista.event.impl.handler;

import com.rbkmoney.damsel.event_stock.StockEvent;
import com.rbkmoney.damsel.payment_processing.InvoiceChange;
import com.rbkmoney.magista.event.ChangeType;
import com.rbkmoney.magista.event.Mapper;
import com.rbkmoney.magista.event.Processor;
import com.rbkmoney.magista.event.impl.context.InvoiceEventContext;
import com.rbkmoney.magista.event.impl.mapper.EventMapper;
import com.rbkmoney.magista.event.impl.mapper.PaymentAdjustmentMapper;
import com.rbkmoney.magista.event.impl.processor.PaymentAdjustmentEventProcessor;
import com.rbkmoney.magista.service.InvoiceEventService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import java.util.Arrays;
import java.util.List;

/**
 * Created by tolkonepiu on 21/06/2017.
 */
@Component
public class AdjustmentCreatedHandler extends AbstractInvoiceEventHandler {

    @Autowired
    private InvoiceEventService invoiceEventService;

    @Override
    public Processor handle(InvoiceChange change, StockEvent event) {
        InvoiceEventContext context = generateContext(change, event);
        return new PaymentAdjustmentEventProcessor(invoiceEventService, context.getInvoiceEventStat());
    }

    @Override
    public ChangeType getChangeType() {
        return ChangeType.INVOICE_PAYMENT_ADJUSTMENT_CREATED;
    }

    @Override
    List<Mapper> getMappers() {
        return Arrays.asList(
                new EventMapper(),
                new PaymentAdjustmentMapper()
        );
    }
}