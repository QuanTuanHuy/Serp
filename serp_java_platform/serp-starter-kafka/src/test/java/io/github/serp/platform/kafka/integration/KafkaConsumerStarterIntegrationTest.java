/*
Author: QuanTuanHuy
Description: Part of Serp Project
*/

package io.github.serp.platform.kafka.integration;

import io.github.serp.platform.kafka.consumer.SerpKafkaRecordParser;
import org.apache.kafka.clients.consumer.Consumer;
import org.apache.kafka.clients.consumer.ConsumerConfig;
import org.apache.kafka.clients.consumer.ConsumerRecord;
import org.apache.kafka.clients.consumer.ConsumerRecords;
import org.apache.kafka.common.header.Header;
import org.apache.kafka.common.serialization.StringDeserializer;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.context.annotation.Bean;
import org.springframework.kafka.annotation.EnableKafka;
import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.kafka.core.DefaultKafkaConsumerFactory;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.kafka.test.EmbeddedKafkaBroker;
import org.springframework.kafka.test.context.EmbeddedKafka;
import org.springframework.kafka.test.utils.KafkaTestUtils;
import org.springframework.test.annotation.DirtiesContext;

import java.time.Duration;
import java.nio.charset.StandardCharsets;
import java.util.Map;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicInteger;

import static org.assertj.core.api.Assertions.assertThat;

@SpringBootTest(
        classes = KafkaConsumerStarterIntegrationTest.TestApplication.class,
        properties = {
                "spring.kafka.bootstrap-servers=${spring.embedded.kafka.brokers}",
                "spring.kafka.consumer.group-id=starter-it-group",
                "spring.kafka.consumer.auto-offset-reset=earliest",
                "spring.kafka.consumer.enable-auto-commit=false",
                "spring.kafka.consumer.key-deserializer=org.apache.kafka.common.serialization.StringDeserializer",
                "spring.kafka.consumer.value-deserializer=org.apache.kafka.common.serialization.StringDeserializer",
                "spring.kafka.producer.key-serializer=org.apache.kafka.common.serialization.StringSerializer",
                "spring.kafka.producer.value-serializer=org.apache.kafka.common.serialization.StringSerializer",
                "serp.kafka.consumer.retry.max-attempts=3",
                "serp.kafka.consumer.retry.initial-interval-ms=50",
                "serp.kafka.consumer.retry.multiplier=1.0",
                "serp.kafka.consumer.retry.max-interval-ms=50",
                "serp.kafka.consumer.dlt.enabled=true",
                "serp.kafka.consumer.dlt.suffix=.dlt"
        })
@EmbeddedKafka(
        partitions = 1,
        topics = {
                "retry.events", "retry.events.dlt",
                "nonretry.events", "nonretry.events.dlt",
                "typed.events", "typed.events.dlt"
        })
@DirtiesContext(classMode = DirtiesContext.ClassMode.AFTER_CLASS)
class KafkaConsumerStarterIntegrationTest {

    @Autowired
    private KafkaTemplate<String, String> kafkaTemplate;

    @Autowired
    private EmbeddedKafkaBroker embeddedKafkaBroker;

    @Autowired
    private TestListener testListener;

    @BeforeEach
    void resetState() {
        testListener.reset();
    }

    @Test
    void shouldRetryAndPublishToDltForRetryableException() throws Exception {
        kafkaTemplate.send("retry.events", "key-retry", "{\"id\":\"evt-1\"}");

        assertThat(testListener.retryableLatch.await(20, TimeUnit.SECONDS)).isTrue();
        assertThat(testListener.retryableAttempts.get()).isEqualTo(3);

        ConsumerRecord<String, String> dltRecord = consumeSingleRecord("retry.events.dlt");
        assertThat(dltRecord.key()).isEqualTo("key-retry");
        assertThat(dltRecord.value()).isEqualTo("{\"id\":\"evt-1\"}");
        assertThat(readHeader(dltRecord, "x-serp-exception-class")).isEqualTo(RuntimeException.class.getName());
        assertThat(readHeader(dltRecord, "x-serp-original-topic")).isEqualTo("retry.events");
    }

    @Test
    void shouldSendToDltImmediatelyForNonRetryableException() throws Exception {
        kafkaTemplate.send("nonretry.events", "key-nonretry", "{\"id\":\"evt-2\"}");

        assertThat(testListener.nonRetryableLatch.await(20, TimeUnit.SECONDS)).isTrue();
        assertThat(testListener.nonRetryableAttempts.get()).isEqualTo(1);

        ConsumerRecord<String, String> dltRecord = consumeSingleRecord("nonretry.events.dlt");
        assertThat(dltRecord.key()).isEqualTo("key-nonretry");
        assertThat(readHeader(dltRecord, "x-serp-exception-class")).isEqualTo(IllegalArgumentException.class.getName());
        assertThat(readHeader(dltRecord, "x-serp-original-topic")).isEqualTo("nonretry.events");
    }

    @Test
    void shouldParseTypedPayloadWithStarterParser() throws Exception {
        kafkaTemplate.send("typed.events", "key-typed", "{\"id\":\"evt-typed\",\"version\":7}");

        assertThat(testListener.typedLatch.await(20, TimeUnit.SECONDS)).isTrue();
        assertThat(testListener.typedEvent).isNotNull();
        assertThat(testListener.typedEvent.id()).isEqualTo("evt-typed");
        assertThat(testListener.typedEvent.version()).isEqualTo(7);
    }

    private ConsumerRecord<String, String> consumeSingleRecord(String topic) {
        Consumer<String, String> consumer = createConsumer("verify-" + topic);
        try {
            return KafkaTestUtils.getSingleRecord(consumer, topic, Duration.ofSeconds(20));
        } finally {
            consumer.close();
        }
    }

    private Consumer<String, String> createConsumer(String groupId) {
        Map<String, Object> props = KafkaTestUtils.consumerProps(groupId, "false", embeddedKafkaBroker);
        props.put(ConsumerConfig.AUTO_OFFSET_RESET_CONFIG, "earliest");
        DefaultKafkaConsumerFactory<String, String> factory =
                new DefaultKafkaConsumerFactory<>(props, new StringDeserializer(), new StringDeserializer());
        Consumer<String, String> consumer = factory.createConsumer();
        embeddedKafkaBroker.consumeFromAllEmbeddedTopics(consumer);
        return consumer;
    }

    private String readHeader(ConsumerRecord<String, String> record, String headerName) {
        Header header = record.headers().lastHeader(headerName);
        if (header == null || header.value() == null) {
            return null;
        }
        return new String(header.value(), StandardCharsets.UTF_8);
    }

    @EnableKafka
    @SpringBootApplication
    static class TestApplication {
        @Bean
        TestListener testListener(SerpKafkaRecordParser recordParser) {
            return new TestListener(recordParser);
        }
    }

    static class TestListener {
        private final SerpKafkaRecordParser recordParser;

        private volatile SampleEvent typedEvent;
        private final AtomicInteger retryableAttempts = new AtomicInteger();
        private final AtomicInteger nonRetryableAttempts = new AtomicInteger();
        private CountDownLatch retryableLatch = new CountDownLatch(3);
        private CountDownLatch nonRetryableLatch = new CountDownLatch(1);
        private CountDownLatch typedLatch = new CountDownLatch(1);

        TestListener(SerpKafkaRecordParser recordParser) {
            this.recordParser = recordParser;
        }

        void reset() {
            typedEvent = null;
            retryableAttempts.set(0);
            nonRetryableAttempts.set(0);
            retryableLatch = new CountDownLatch(3);
            nonRetryableLatch = new CountDownLatch(1);
            typedLatch = new CountDownLatch(1);
        }

        @KafkaListener(topics = "retry.events", groupId = "${spring.kafka.consumer.group-id}")
        public void handleRetryable(ConsumerRecord<String, String> record) {
            retryableAttempts.incrementAndGet();
            retryableLatch.countDown();
            throw new RuntimeException("Transient downstream failure");
        }

        @KafkaListener(topics = "nonretry.events", groupId = "${spring.kafka.consumer.group-id}")
        public void handleNonRetryable(ConsumerRecord<String, String> record) {
            nonRetryableAttempts.incrementAndGet();
            nonRetryableLatch.countDown();
            throw new IllegalArgumentException("Invalid payload");
        }

        @KafkaListener(topics = "typed.events", groupId = "${spring.kafka.consumer.group-id}")
        public void handleTyped(ConsumerRecord<String, String> record) {
            typedEvent = recordParser.parse(record, SampleEvent.class);
            typedLatch.countDown();
        }
    }

    private record SampleEvent(String id, int version) {
    }
}
