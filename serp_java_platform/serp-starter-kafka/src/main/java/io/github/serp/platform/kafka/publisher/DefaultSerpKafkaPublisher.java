/*
Author: QuanTuanHuy
Description: Part of Serp Project
*/

package io.github.serp.platform.kafka.publisher;

import org.apache.kafka.clients.producer.ProducerRecord;
import org.slf4j.MDC;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.util.Assert;

import java.nio.charset.StandardCharsets;
import java.util.Map;
import java.util.concurrent.ExecutionException;

import io.github.serp.platform.kafka.properties.SerpKafkaProperties;

public class DefaultSerpKafkaPublisher implements SerpKafkaPublisher {
    private final KafkaTemplate<Object, Object> kafkaTemplate;
    private final SerpKafkaProperties properties;

    public DefaultSerpKafkaPublisher(
            KafkaTemplate<Object, Object> kafkaTemplate,
            SerpKafkaProperties properties) {
        this.kafkaTemplate = kafkaTemplate;
        this.properties = properties;
    }

    @Override
    public void publish(String topic, String key, Object payload) {
        publish(topic, key, payload, Map.of());
    }

    @Override
    public void publish(String topic, String key, Object payload, Map<String, String> headers) {
        String resolvedTopic = resolveTopic(topic);
        ProducerRecord<Object, Object> record = new ProducerRecord<>(resolvedTopic, key, payload);
        attachHeaders(record, headers);

        if (!properties.getProducer().isFailOnPublishError()) {
            kafkaTemplate.send(record);
            return;
        }

        try {
            kafkaTemplate.send(record).get();
        } catch (InterruptedException ex) {
            Thread.currentThread().interrupt();
            throw new IllegalStateException("Kafka publish interrupted for topic: " + resolvedTopic, ex);
        } catch (ExecutionException ex) {
            throw new IllegalStateException("Kafka publish failed for topic: " + resolvedTopic, ex.getCause());
        }
    }

    private String resolveTopic(String topic) {
        if (topic != null && !topic.isBlank()) {
            return topic.trim();
        }

        String defaultTopic = properties.getProducer().getDefaultTopic();
        Assert.hasText(defaultTopic, "Kafka topic is required (or configure serp.kafka.producer.default-topic)");
        return defaultTopic.trim();
    }

    private void attachHeaders(ProducerRecord<Object, Object> record, Map<String, String> headers) {
        if (headers != null) {
            headers.forEach((headerName, headerValue) -> {
                if (headerName == null || headerName.isBlank() || headerValue == null) {
                    return;
                }
                record.headers().add(headerName.trim(), headerValue.getBytes(StandardCharsets.UTF_8));
            });
        }

        if (!properties.getProducer().isHeaderPropagationEnabled()) {
            return;
        }

        String correlationHeader = properties.getProducer().getCorrelationIdHeader();
        if (correlationHeader == null || correlationHeader.isBlank()) {
            return;
        }

        String correlationId = MDC.get("correlationId");
        if (correlationId == null || correlationId.isBlank()) {
            return;
        }

        record.headers().add(correlationHeader.trim(), correlationId.getBytes(StandardCharsets.UTF_8));
    }
}
