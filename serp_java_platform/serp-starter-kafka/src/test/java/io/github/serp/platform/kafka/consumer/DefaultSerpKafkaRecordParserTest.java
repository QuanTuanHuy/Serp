/*
Author: QuanTuanHuy
Description: Part of Serp Project
*/

package io.github.serp.platform.kafka.consumer;

import com.fasterxml.jackson.databind.ObjectMapper;
import org.apache.kafka.clients.consumer.ConsumerRecord;
import org.junit.jupiter.api.Test;
import org.springframework.core.ParameterizedTypeReference;

import java.util.Map;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

class DefaultSerpKafkaRecordParserTest {

    private final DefaultSerpKafkaRecordParser parser = new DefaultSerpKafkaRecordParser(new ObjectMapper());

    @Test
    void shouldParsePayloadToClass() {
        ConsumerRecord<String, String> record =
                new ConsumerRecord<>("orders.events", 0, 1L, "order-1", "{\"id\":\"order-1\",\"amount\":12}");

        SampleEvent event = parser.parse(record, SampleEvent.class);

        assertThat(event.id()).isEqualTo("order-1");
        assertThat(event.amount()).isEqualTo(12);
    }

    @Test
    void shouldParsePayloadToGenericType() {
        ConsumerRecord<String, String> record =
                new ConsumerRecord<>("orders.events", 0, 2L, "order-2", "{\"id\":\"order-2\",\"amount\":24}");

        Map<String, Object> payload = parser.parse(record, new ParameterizedTypeReference<Map<String, Object>>() {
        });

        assertThat(payload).containsEntry("id", "order-2");
        assertThat(payload).containsKey("amount");
    }

    @Test
    void shouldThrowIllegalArgumentExceptionWhenPayloadInvalid() {
        ConsumerRecord<String, String> record =
                new ConsumerRecord<>("orders.events", 0, 3L, "order-3", "{invalid-json");

        assertThatThrownBy(() -> parser.parse(record, SampleEvent.class))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessageContaining("Failed to parse Kafka payload");
    }

    private record SampleEvent(String id, int amount) {
    }
}
