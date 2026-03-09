/*
Author: QuanTuanHuy
Description: Part of Serp Project
*/

package io.github.serp.platform.kafka.properties;

import org.junit.jupiter.api.Test;
import org.springframework.boot.context.properties.bind.Bindable;
import org.springframework.boot.context.properties.bind.Binder;
import org.springframework.boot.context.properties.source.MapConfigurationPropertySource;

import java.util.NoSuchElementException;
import java.util.Map;

import static org.assertj.core.api.Assertions.assertThat;

class SerpKafkaPropertiesBindingTest {

    @Test
    void shouldBindDefaultValues() {
        Binder binder = new Binder(new MapConfigurationPropertySource(Map.of()));

        SerpKafkaProperties properties = binder.bind("serp.kafka", Bindable.of(SerpKafkaProperties.class))
                .orElseGet(SerpKafkaProperties::new);

        assertThat(properties.isEnabled()).isTrue();
        assertThat(properties.getConsumer().getListener().getAckMode()).isEqualTo("RECORD");
        assertThat(properties.getConsumer().getRetry().getMaxAttempts()).isEqualTo(3);
        assertThat(properties.getConsumer().getDlt().isEnabled()).isTrue();
        assertThat(properties.getConsumer().getDlt().getSuffix()).isEqualTo(".dlt");
        assertThat(properties.getConsumer().getHeaders().getExceptionStackSummaryHeader())
                .isEqualTo("x-serp-exception-stack");
    }

    @Test
    void shouldBindCustomConsumerPolicy() {
        Map<String, Object> values = Map.of(
                "serp.kafka.consumer.listener.ack-mode", "MANUAL",
                "serp.kafka.consumer.listener.concurrency", "5",
                "serp.kafka.consumer.retry.max-attempts", "9",
                "serp.kafka.consumer.retry.initial-interval-ms", "2000",
                "serp.kafka.consumer.retry.multiplier", "3.0",
                "serp.kafka.consumer.retry.max-interval-ms", "30000",
                "serp.kafka.consumer.dlt.enabled", "false",
                "serp.kafka.consumer.dlt.suffix", ".dead",
                "serp.kafka.consumer.dlt.topic-mappings.orders.events", "orders.events.dead",
                "serp.kafka.consumer.headers.exception-stack-summary-header", "x-ex-stack");

        Binder binder = new Binder(new MapConfigurationPropertySource(values));
        SerpKafkaProperties properties = binder.bind("serp.kafka", Bindable.of(SerpKafkaProperties.class))
                .orElseThrow(() -> new NoSuchElementException("Missing serp.kafka properties"));

        assertThat(properties.getConsumer().getListener().getAckMode()).isEqualTo("MANUAL");
        assertThat(properties.getConsumer().getListener().getConcurrency()).isEqualTo(5);
        assertThat(properties.getConsumer().getRetry().getMaxAttempts()).isEqualTo(9);
        assertThat(properties.getConsumer().getRetry().getInitialIntervalMs()).isEqualTo(2000);
        assertThat(properties.getConsumer().getRetry().getMultiplier()).isEqualTo(3.0d);
        assertThat(properties.getConsumer().getRetry().getMaxIntervalMs()).isEqualTo(30000);
        assertThat(properties.getConsumer().getDlt().isEnabled()).isFalse();
        assertThat(properties.getConsumer().getDlt().getSuffix()).isEqualTo(".dead");
        assertThat(properties.getConsumer().getDlt().getTopicMappings().get("orders.events"))
                .isEqualTo("orders.events.dead");
        assertThat(properties.getConsumer().getHeaders().getExceptionStackSummaryHeader())
                .isEqualTo("x-ex-stack");
    }
}
