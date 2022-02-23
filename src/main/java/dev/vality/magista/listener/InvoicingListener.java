package dev.vality.magista.listener;

import dev.vality.damsel.payment_processing.InvoiceChange;
import dev.vality.machinegun.eventsink.MachineEvent;
import dev.vality.machinegun.eventsink.SinkEvent;
import dev.vality.magista.converter.SourceEventParser;
import dev.vality.magista.service.HandlerManager;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.apache.kafka.clients.consumer.ConsumerRecord;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.kafka.support.Acknowledgment;
import org.springframework.stereotype.Service;

import java.util.*;
import java.util.stream.Collectors;

import static dev.vality.kafka.common.util.LogUtil.toSummaryStringWithSinkEventValues;

@Slf4j
@Service
@RequiredArgsConstructor
public class InvoicingListener {

    private final HandlerManager handlerManager;
    private final SourceEventParser eventParser;

    @Value("${kafka.consumer.throttling-timeout-ms}")
    private int throttlingTimeout;

    @KafkaListener(
            autoStartup = "${kafka.topics.invoicing.consume.enabled}",
            topics = "${kafka.topics.invoicing.id}",
            containerFactory = "invoicingListenerContainerFactory")
    public void listen(
            List<ConsumerRecord<String, SinkEvent>> batch,
            Acknowledgment ack) throws InterruptedException {
        log.info("InvoicingListener listen offsets, size={}, {}",
                batch.size(), toSummaryStringWithSinkEventValues(batch));
        List<MachineEvent> machineEvents = batch.stream()
                .map(ConsumerRecord::value)
                .map(SinkEvent::getEvent)
                .collect(Collectors.toList());
        handleMessages(machineEvents);
        ack.acknowledge();
        log.info("InvoicingListener Records have been committed, size={}, {}",
                batch.size(), toSummaryStringWithSinkEventValues(batch));
    }

    public void handleMessages(List<MachineEvent> machineEvents) throws InterruptedException {
        try {
            machineEvents.stream()
                    .map(machineEvent -> Map.entry(eventParser.parseEvent(machineEvent), machineEvent))
                    .filter(entry -> entry.getKey().isSetInvoiceChanges())
                    .map(entry -> {
                        var entries = new ArrayList<Map.Entry<InvoiceChange, MachineEvent>>();
                        for (InvoiceChange invoiceChange : entry.getKey().getInvoiceChanges()) {
                            entries.add(Map.entry(invoiceChange, entry.getValue()));
                        }
                        return entries;
                    })
                    .flatMap(List::stream)
                    .sorted(Comparator.comparingLong(o -> o.getValue().getEventId()))
                    .collect(Collectors.groupingBy(
                            entry -> handlerManager.getHandler(entry.getKey()),
                            LinkedHashMap::new,
                            Collectors.toList()))
                    .entrySet().stream()
                    .filter(entry -> entry.getKey() != null)
                    .forEach(entry -> entry.getKey().handle(entry.getValue()).execute());
        } catch (Exception e) {
            log.error("Error when InvoicingListener listen e: ", e);
            Thread.sleep(throttlingTimeout);
            throw e;
        }
    }
}
