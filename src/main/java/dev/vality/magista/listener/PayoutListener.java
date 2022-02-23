package dev.vality.magista.listener;

import dev.vality.magista.service.PayoutHandlerService;
import dev.vality.payout.manager.Event;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.apache.kafka.clients.consumer.ConsumerRecord;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.kafka.support.Acknowledgment;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.stream.Collectors;

import static dev.vality.kafka.common.util.LogUtil.toSummaryString;

@Slf4j
@Service
@RequiredArgsConstructor
public class PayoutListener {

    @Value("${kafka.consumer.throttling-timeout-ms}")
    private int throttlingTimeout;

    private final PayoutHandlerService payoutService;

    @KafkaListener(
            autoStartup = "${kafka.topics.pm-events-payout.consume.enabled}",
            topics = "${kafka.topics.pm-events-payout.id}",
            containerFactory = "payoutListenerContainerFactory")
    public void listen(
            List<ConsumerRecord<String, Event>> batch,
            Acknowledgment ack) throws InterruptedException {
        log.info("PayoutListener listen offsets, size={}, {}", batch.size(), toSummary(batch));
        List<Event> events = batch.stream()
                .map(ConsumerRecord::value)
                .collect(Collectors.toList());
        handleMessages(events);
        ack.acknowledge();
        log.info("PayoutListener Records have been committed, size={}, {}", batch.size(), toSummary(batch));
    }

    public void handleMessages(List<Event> events) throws InterruptedException {
        try {
            payoutService.handleEvents(events);
        } catch (Exception e) {
            log.error("Error when PayoutListener listen e: ", e);
            Thread.sleep(throttlingTimeout);
            throw e;
        }
    }

    private <K> String toSummary(List<ConsumerRecord<K, Event>> records) {
        String valueKeysString = records.stream()
                .map(ConsumerRecord::value)
                .map(value -> String.format("'%s.%d'", value.getPayoutId(), value.getSequenceId()))
                .collect(Collectors.joining(", "));
        return String.format("%s, values={%s}", toSummaryString(records), valueKeysString);
    }
}
