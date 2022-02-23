package dev.vality.magista.listener;

import dev.vality.damsel.payment_processing.InvoiceTemplateChange;
import dev.vality.machinegun.eventsink.MachineEvent;
import dev.vality.machinegun.eventsink.SinkEvent;
import dev.vality.magista.converter.SourceEventsParser;
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
public class InvoiceTemplateListener {

    private final HandlerManager handlerManager;
    private final SourceEventsParser sourceEventsParser;

    @Value("${kafka.consumer.throttling-timeout-ms}")
    private int throttlingTimeout;

    @KafkaListener(
            autoStartup = "${kafka.topics.invoice-template.consume.enabled}",
            topics = "${kafka.topics.invoice-template.id}",
            containerFactory = "invoiceTemplateListenerContainerFactory")
    public void listen(
            List<ConsumerRecord<String, SinkEvent>> batch,
            Acknowledgment ack) throws InterruptedException {
        log.info("InvoiceTemplateListener listen offsets, size={}, {}",
                batch.size(), toSummaryStringWithSinkEventValues(batch));
        List<MachineEvent> machineEvents = batch.stream()
                .map(ConsumerRecord::value)
                .map(SinkEvent::getEvent)
                .collect(Collectors.toList());
        handleMessages(machineEvents);
        ack.acknowledge();
        log.info("InvoiceTemplateListener Records have been committed, size={}, {}",
                batch.size(), toSummaryStringWithSinkEventValues(batch));
    }

    public void handleMessages(List<MachineEvent> machineEvents) throws InterruptedException {
        try {
            machineEvents.stream()
                    .flatMap(machineEvent -> sourceEventsParser.parseEvents(machineEvent).stream()
                            .map(eventPayload -> Map.entry(eventPayload, machineEvent)))
                    .filter(entry -> entry.getKey().isSetInvoiceTemplateChanges())
                    .map(entry -> {
                        var entries = new ArrayList<Map.Entry<InvoiceTemplateChange, MachineEvent>>();
                        for (InvoiceTemplateChange change : entry.getKey().getInvoiceTemplateChanges()) {
                            entries.add(Map.entry(change, entry.getValue()));
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
            log.error("Error when InvoiceTemplateListener listen e: ", e);
            Thread.sleep(throttlingTimeout);
            throw e;
        }
    }
}
