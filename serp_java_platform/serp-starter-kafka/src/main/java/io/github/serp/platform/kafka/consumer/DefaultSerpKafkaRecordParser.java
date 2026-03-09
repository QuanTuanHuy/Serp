/*
Author: QuanTuanHuy
Description: Part of Serp Project
*/

package io.github.serp.platform.kafka.consumer;

import com.fasterxml.jackson.databind.ObjectMapper;
import org.apache.kafka.clients.consumer.ConsumerRecord;
import org.springframework.core.ParameterizedTypeReference;
import org.springframework.util.Assert;

public class DefaultSerpKafkaRecordParser implements SerpKafkaRecordParser {
    private final ObjectMapper objectMapper;

    public DefaultSerpKafkaRecordParser(ObjectMapper objectMapper) {
        this.objectMapper = objectMapper;
    }

    @Override
    public <T> T parse(ConsumerRecord<String, String> record, Class<T> type) {
        Assert.notNull(type, "Kafka parse target type is required");

        String payload = extractPayload(record);
        try {
            return objectMapper.readValue(payload, type);
        } catch (Exception ex) {
            throw new IllegalArgumentException("Failed to parse Kafka payload to type: " + type.getName(), ex);
        }
    }

    @Override
    public <T> T parse(ConsumerRecord<String, String> record, ParameterizedTypeReference<T> typeReference) {
        Assert.notNull(typeReference, "Kafka parse target type reference is required");

        String payload = extractPayload(record);
        try {
            return objectMapper.readValue(payload, objectMapper.getTypeFactory().constructType(typeReference.getType()));
        } catch (Exception ex) {
            throw new IllegalArgumentException("Failed to parse Kafka payload using parameterized type", ex);
        }
    }

    private String extractPayload(ConsumerRecord<String, String> record) {
        Assert.notNull(record, "Kafka consumer record is required");
        String payload = record.value();
        Assert.hasText(payload, "Kafka payload is required");
        return payload;
    }
}
