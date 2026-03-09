/*
Author: QuanTuanHuy
Description: Part of Serp Project
*/

package io.github.serp.platform.kafka.support;

import org.apache.kafka.clients.consumer.Consumer;
import org.apache.kafka.clients.consumer.ConsumerRecord;
import org.apache.kafka.common.header.Header;
import org.slf4j.MDC;
import org.springframework.kafka.listener.RecordInterceptor;

import java.nio.charset.StandardCharsets;

public class SerpKafkaRecordInterceptor implements RecordInterceptor<Object, Object> {
    private static final String MDC_CORRELATION_KEY = "correlationId";

    private final String correlationHeaderName;

    public SerpKafkaRecordInterceptor(String correlationHeaderName) {
        this.correlationHeaderName = correlationHeaderName;
    }

    @Override
    public ConsumerRecord<Object, Object> intercept(ConsumerRecord<Object, Object> record, Consumer<Object, Object> consumer) {
        if (record == null || record.headers() == null) {
            return record;
        }

        if (correlationHeaderName == null || correlationHeaderName.isBlank()) {
            return record;
        }

        Header header = record.headers().lastHeader(correlationHeaderName);
        if (header == null || header.value() == null) {
            return record;
        }

        String correlationId = new String(header.value(), StandardCharsets.UTF_8);
        if (!correlationId.isBlank()) {
            MDC.put(MDC_CORRELATION_KEY, correlationId);
        }

        return record;
    }

    @Override
    public void afterRecord(ConsumerRecord<Object, Object> record, Consumer<Object, Object> consumer) {
        MDC.remove(MDC_CORRELATION_KEY);
    }
}
