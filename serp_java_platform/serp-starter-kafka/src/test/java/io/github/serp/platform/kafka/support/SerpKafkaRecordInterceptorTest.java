/*
Author: QuanTuanHuy
Description: Part of Serp Project
*/

package io.github.serp.platform.kafka.support;

import org.apache.kafka.clients.consumer.ConsumerRecord;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.Test;
import org.slf4j.MDC;

import java.nio.charset.StandardCharsets;

import static org.assertj.core.api.Assertions.assertThat;

class SerpKafkaRecordInterceptorTest {

    @AfterEach
    void cleanMdc() {
        MDC.clear();
    }

    @Test
    void shouldSetAndClearCorrelationIdInMdc() {
        SerpKafkaRecordInterceptor interceptor = new SerpKafkaRecordInterceptor("X-Correlation-Id");
        ConsumerRecord<Object, Object> record = new ConsumerRecord<>("orders.events", 0, 1L, "k", "v");
        record.headers().add("X-Correlation-Id", "corr-123".getBytes(StandardCharsets.UTF_8));

        interceptor.intercept(record, null);
        assertThat(MDC.get("correlationId")).isEqualTo("corr-123");

        interceptor.afterRecord(record, null);
        assertThat(MDC.get("correlationId")).isNull();
    }

    @Test
    void shouldIgnoreWhenCorrelationHeaderMissing() {
        SerpKafkaRecordInterceptor interceptor = new SerpKafkaRecordInterceptor("X-Correlation-Id");
        ConsumerRecord<Object, Object> record = new ConsumerRecord<>("orders.events", 0, 1L, "k", "v");

        interceptor.intercept(record, null);

        assertThat(MDC.get("correlationId")).isNull();
    }
}
