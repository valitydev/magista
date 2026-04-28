package dev.vality.magista.config;

import dev.vality.testcontainers.annotations.DefaultSpringBootTest;
import dev.vality.testcontainers.annotations.postgresql.PostgresqlTestcontainerSingleton;
import org.springframework.kafka.test.context.EmbeddedKafka;
import org.springframework.test.annotation.DirtiesContext;
import org.springframework.test.context.TestPropertySource;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

@Target({ElementType.TYPE})
@Retention(RetentionPolicy.RUNTIME)
@PostgresqlTestcontainerSingleton
@DefaultSpringBootTest
@EmbeddedKafka(partitions = 1, topics = {
        "magista-invoicing-test",
        "magista-invoice-template-test"
})
@TestPropertySource(properties = {
        "spring.kafka.bootstrap-servers=${spring.embedded.kafka.brokers}",
        "spring.kafka.consumer.group-id=magista-kafka-test",
        "kafka.topics.invoicing.id=magista-invoicing-test",
        "kafka.topics.invoicing.consume.enabled=true",
        "kafka.topics.invoice-template.id=magista-invoice-template-test",
        "kafka.topics.invoice-template.consume.enabled=true",
        "kafka.state.cache.size=0"
})
@DirtiesContext(classMode = DirtiesContext.ClassMode.AFTER_CLASS)
public @interface KafkaPostgresqlSpringBootITest {
}
