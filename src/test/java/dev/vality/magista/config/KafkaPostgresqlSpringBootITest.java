package dev.vality.magista.config;

import dev.vality.testcontainers.annotations.KafkaSpringBootTest;
import dev.vality.testcontainers.annotations.kafka.KafkaTestcontainerSingleton;
import dev.vality.testcontainers.annotations.postgresql.PostgresqlTestcontainerSingleton;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

@Target({ElementType.TYPE})
@Retention(RetentionPolicy.RUNTIME)
@PostgresqlTestcontainerSingleton
@KafkaTestcontainerSingleton(
        properties = {
                "kafka.topics.invoicing.consume.enabled=true",
                "kafka.topics.invoice-template.consume.enabled=true",
                "kafka.topics.pm-events-payout.consume.enabled=true",
                "kafka.state.cache.size=0"},
        topicsKeys = {
                "kafka.topics.invoicing.id",
                "kafka.topics.invoice-template.id",
                "kafka.topics.pm-events-payout.id"})
@KafkaSpringBootTest
public @interface KafkaPostgresqlSpringBootITest {
}
