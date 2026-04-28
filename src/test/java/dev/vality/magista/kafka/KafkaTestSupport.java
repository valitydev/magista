package dev.vality.magista.kafka;

import org.apache.kafka.clients.producer.KafkaProducer;
import org.apache.kafka.clients.producer.ProducerConfig;
import org.apache.kafka.clients.producer.ProducerRecord;
import org.apache.kafka.common.serialization.ByteArraySerializer;
import org.apache.kafka.common.serialization.StringSerializer;
import org.apache.thrift.TBase;
import org.apache.thrift.TSerializer;
import org.apache.thrift.protocol.TBinaryProtocol;
import org.springframework.kafka.config.KafkaListenerEndpointRegistry;
import org.springframework.kafka.listener.MessageListenerContainer;
import org.springframework.kafka.test.EmbeddedKafkaBroker;
import org.springframework.kafka.test.utils.ContainerTestUtils;

import java.util.Properties;

final class KafkaTestSupport {

    private KafkaTestSupport() {
    }

    static void waitForAssignments(
            KafkaListenerEndpointRegistry kafkaListenerEndpointRegistry,
            EmbeddedKafkaBroker embeddedKafkaBroker) {
        for (MessageListenerContainer listenerContainer : kafkaListenerEndpointRegistry.getListenerContainers()) {
            ContainerTestUtils.waitForAssignment(listenerContainer, embeddedKafkaBroker.getPartitionsPerTopic());
        }
    }

    static void send(EmbeddedKafkaBroker embeddedKafkaBroker, String topic, TBase<?, ?> event) throws Exception {
        try (var producer = new KafkaProducer<String, byte[]>(producerProperties(embeddedKafkaBroker))) {
            var serializer = new TSerializer(new TBinaryProtocol.Factory());
            producer.send(new ProducerRecord<>(topic, serializer.serialize(event))).get();
            producer.flush();
        }
    }

    private static Properties producerProperties(EmbeddedKafkaBroker embeddedKafkaBroker) {
        var properties = new Properties();
        properties.put(ProducerConfig.BOOTSTRAP_SERVERS_CONFIG, embeddedKafkaBroker.getBrokersAsString());
        properties.put(ProducerConfig.KEY_SERIALIZER_CLASS_CONFIG, StringSerializer.class);
        properties.put(ProducerConfig.VALUE_SERIALIZER_CLASS_CONFIG, ByteArraySerializer.class);
        properties.put(ProducerConfig.ACKS_CONFIG, "all");
        return properties;
    }
}
