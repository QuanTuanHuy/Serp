/*
Author: QuanTuanHuy
Description: Part of Serp Project
*/

package io.github.serp.platform.kafka.consumer;

import org.apache.kafka.clients.consumer.ConsumerRecord;
import org.junit.jupiter.api.Test;
import org.springframework.kafka.support.KafkaHeaders;

import java.nio.ByteBuffer;
import java.nio.charset.StandardCharsets;

import static org.assertj.core.api.Assertions.assertThat;

class SerpKafkaConsumerMetadataTest {

    @Test
    void shouldExtractMetadataAndDeliveryAttempt() {
        ConsumerRecord<String, String> record =
                new ConsumerRecord<>("orders.events", 2, 15L, "order-1", "{\"id\":\"order-1\"}");
        record.headers().add("X-Correlation-Id", "corr-001".getBytes(StandardCharsets.UTF_8));
        record.headers().add(KafkaHeaders.DELIVERY_ATTEMPT, ByteBuffer.allocate(4).putInt(3).array());

        SerpKafkaConsumerMetadata metadata = SerpKafkaConsumerMetadata.fromRecord(record, "X-Correlation-Id");

        assertThat(metadata.getTopic()).isEqualTo("orders.events");
        assertThat(metadata.getPartition()).isEqualTo(2);
        assertThat(metadata.getOffset()).isEqualTo(15L);
        assertThat(metadata.getKey()).isEqualTo("order-1");
        assertThat(metadata.getCorrelationId()).isEqualTo("corr-001");
        assertThat(metadata.getDeliveryAttempt()).isEqualTo(3);
    }
}
