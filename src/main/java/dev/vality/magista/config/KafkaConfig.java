package dev.vality.magista.config;

import dev.vality.machinegun.eventsink.SinkEvent;
import dev.vality.magista.serde.PayoutEventDeserializer;
import dev.vality.magista.serde.SinkEventDeserializer;
import dev.vality.payout.manager.Event;
import org.apache.kafka.clients.consumer.ConsumerConfig;
import org.apache.kafka.common.serialization.Deserializer;
import org.apache.kafka.common.serialization.StringDeserializer;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.autoconfigure.kafka.KafkaProperties;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.kafka.config.ConcurrentKafkaListenerContainerFactory;
import org.springframework.kafka.core.DefaultKafkaConsumerFactory;
import org.springframework.kafka.listener.ContainerProperties;
import org.springframework.kafka.listener.DefaultErrorHandler;

import java.util.Map;

import static org.apache.kafka.clients.consumer.OffsetResetStrategy.EARLIEST;

@Configuration
public class KafkaConfig {

    @Value("${kafka.topics.invoicing.consume.max-poll-records}")
    private String invoicingMaxPollRecords;

    @Value("${kafka.topics.invoicing.consume.concurrency}")
    private int invoicingConcurrency;

    @Value("${kafka.topics.invoice-template.consume.max-poll-records}")
    private String invoiceTemplateMaxPollRecords;

    @Value("${kafka.topics.invoice-template.consume.concurrency}")
    private int invoiceTemplateConcurrency;

    @Value("${kafka.topics.pm-events-payout.consume.max-poll-records}")
    private String payoutMaxPollRecords;

    @Value("${kafka.topics.pm-events-payout.consume.concurrency}")
    private int payoutConcurrency;

    @Bean
    public ConcurrentKafkaListenerContainerFactory<String, SinkEvent> invoicingListenerContainerFactory(
            KafkaProperties kafkaProperties) {
        var containerFactory = new ConcurrentKafkaListenerContainerFactory<String, SinkEvent>();
        configureContainerFactory(
                containerFactory,
                new SinkEventDeserializer(),
                kafkaProperties.getClientId(),
                invoicingMaxPollRecords,
                kafkaProperties);
        containerFactory.setConcurrency(invoicingConcurrency);
        return containerFactory;
    }

    @Bean
    public ConcurrentKafkaListenerContainerFactory<String, SinkEvent> invoiceTemplateListenerContainerFactory(
            KafkaProperties kafkaProperties) {
        var containerFactory = new ConcurrentKafkaListenerContainerFactory<String, SinkEvent>();
        configureContainerFactory(
                containerFactory,
                new SinkEventDeserializer(),
                kafkaProperties.getClientId() + "-invoice-template",
                invoiceTemplateMaxPollRecords,
                kafkaProperties);
        containerFactory.setConcurrency(invoiceTemplateConcurrency);
        return containerFactory;
    }

    @Bean
    public ConcurrentKafkaListenerContainerFactory<String, Event> payoutListenerContainerFactory(
            KafkaProperties kafkaProperties) {
        var containerFactory = new ConcurrentKafkaListenerContainerFactory<String, Event>();
        configureContainerFactory(
                containerFactory,
                new PayoutEventDeserializer(),
                kafkaProperties.getClientId() + "-pm-events-payout",
                payoutMaxPollRecords,
                kafkaProperties);
        containerFactory.setConcurrency(payoutConcurrency);
        return containerFactory;
    }

    private <T> void configureContainerFactory(
            ConcurrentKafkaListenerContainerFactory<String, T> containerFactory,
            Deserializer<T> deserializer,
            String clientId,
            String maxPollRecords,
            KafkaProperties kafkaProperties) {
        var consumerFactory = createKafkaConsumerFactory(
                deserializer,
                clientId,
                maxPollRecords,
                kafkaProperties);
        containerFactory.setConsumerFactory(consumerFactory);
        containerFactory.setCommonErrorHandler(new DefaultErrorHandler());
        containerFactory.setBatchListener(true);
        containerFactory.getContainerProperties().setAckMode(ContainerProperties.AckMode.MANUAL);
    }

    private <T> DefaultKafkaConsumerFactory<String, T> createKafkaConsumerFactory(
            Deserializer<T> deserializer,
            String clientId,
            String maxPollRecords,
            KafkaProperties kafkaProperties) {
        Map<String, Object> properties = defaultProperties(kafkaProperties);
        properties.put(ConsumerConfig.CLIENT_ID_CONFIG, clientId);
        properties.put(ConsumerConfig.MAX_POLL_RECORDS_CONFIG, maxPollRecords);
        return new DefaultKafkaConsumerFactory<>(properties, new StringDeserializer(), deserializer);
    }

    private Map<String, Object> defaultProperties(KafkaProperties kafkaProperties) {
        var properties = kafkaProperties.buildConsumerProperties();
        properties.put(ConsumerConfig.AUTO_OFFSET_RESET_CONFIG, EARLIEST.name().toLowerCase());
        properties.put(ConsumerConfig.ENABLE_AUTO_COMMIT_CONFIG, false);
        return properties;
    }
}
