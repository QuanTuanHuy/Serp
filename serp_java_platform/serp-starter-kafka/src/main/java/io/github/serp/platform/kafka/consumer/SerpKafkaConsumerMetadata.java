/*
Author: QuanTuanHuy
Description: Part of Serp Project
*/

package io.github.serp.platform.kafka.consumer;

import org.apache.kafka.clients.consumer.ConsumerRecord;
import org.apache.kafka.common.header.Header;
import org.apache.kafka.common.header.Headers;
import org.springframework.kafka.support.KafkaHeaders;

import java.nio.ByteBuffer;
import java.nio.charset.StandardCharsets;
import java.util.HashMap;
import java.util.Map;

public class SerpKafkaConsumerMetadata {
    private final String topic;
    private final int partition;
    private final long offset;
    private final String key;
    private final String correlationId;
    private final Integer deliveryAttempt;
    private final Map<String, String> headers;

    public SerpKafkaConsumerMetadata(
            String topic,
            int partition,
            long offset,
            String key,
            String correlationId,
            Integer deliveryAttempt,
            Map<String, String> headers) {
        this.topic = topic;
        this.partition = partition;
        this.offset = offset;
        this.key = key;
        this.correlationId = correlationId;
        this.deliveryAttempt = deliveryAttempt;
        this.headers = headers;
    }

    public String getTopic() {
        return topic;
    }

    public int getPartition() {
        return partition;
    }

    public long getOffset() {
        return offset;
    }

    public String getKey() {
        return key;
    }

    public String getCorrelationId() {
        return correlationId;
    }

    public Integer getDeliveryAttempt() {
        return deliveryAttempt;
    }

    public Map<String, String> getHeaders() {
        return headers;
    }

    public static SerpKafkaConsumerMetadata fromRecord(
            ConsumerRecord<?, ?> record,
            String correlationHeaderName) {
        return fromRecord(record, correlationHeaderName, extractDeliveryAttempt(record));
    }

    public static SerpKafkaConsumerMetadata fromRecord(
            ConsumerRecord<?, ?> record,
            String correlationHeaderName,
            Integer deliveryAttempt) {
        String correlationId = null;
        Map<String, String> headerMap = new HashMap<>();

        if (record != null && record.headers() != null) {
            headerMap = toHeaderMap(record.headers());
            correlationId = headerMap.get(correlationHeaderName);
        }

        if (record == null) {
            return new SerpKafkaConsumerMetadata(null, -1, -1L, null, correlationId, deliveryAttempt, headerMap);
        }

        String key = record.key() == null ? null : String.valueOf(record.key());
        return new SerpKafkaConsumerMetadata(
                record.topic(),
                record.partition(),
                record.offset(),
                key,
                correlationId,
                deliveryAttempt,
                headerMap);
    }

    private static Integer extractDeliveryAttempt(ConsumerRecord<?, ?> record) {
        if (record == null || record.headers() == null) {
            return null;
        }

        Header deliveryHeader = record.headers().lastHeader(KafkaHeaders.DELIVERY_ATTEMPT);
        if (deliveryHeader == null || deliveryHeader.value() == null) {
            return null;
        }

        byte[] value = deliveryHeader.value();
        if (value.length == Integer.BYTES) {
            return ByteBuffer.wrap(value).getInt();
        }

        try {
            return Integer.parseInt(new String(value, StandardCharsets.UTF_8));
        } catch (NumberFormatException ex) {
            return null;
        }
    }

    private static Map<String, String> toHeaderMap(Headers headers) {
        Map<String, String> map = new HashMap<>();
        for (Header header : headers) {
            if (header.key() == null || header.value() == null) {
                continue;
            }
            map.put(header.key(), new String(header.value(), StandardCharsets.UTF_8));
        }
        return map;
    }
}
