/*
Author: QuanTuanHuy
Description: Part of Serp Project
*/

package io.github.serp.platform.kafka.integration;

import org.apache.kafka.clients.consumer.Consumer;
import org.apache.kafka.clients.consumer.ConsumerConfig;
import org.apache.kafka.clients.consumer.ConsumerRecord;
import org.apache.kafka.clients.consumer.ConsumerRecords;
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
import java.util.Map;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicInteger;

import static org.assertj.core.api.Assertions.assertThat;

@SpringBootTest(
        classes = KafkaConsumerDltDisabledIntegrationTest.TestApplication.class,
        properties = {
                "spring.kafka.bootstrap-servers=${spring.embedded.kafka.brokers}",
                "spring.kafka.consumer.group-id=starter-it-dlt-disabled-group",
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
                "serp.kafka.consumer.dlt.enabled=false",
                "serp.kafka.consumer.dlt.suffix=.dlt"
        })
@EmbeddedKafka(partitions = 1, topics = {"nodlt.events", "nodlt.events.dlt"})
@DirtiesContext(classMode = DirtiesContext.ClassMode.AFTER_CLASS)
class KafkaConsumerDltDisabledIntegrationTest {

    @Autowired
    private KafkaTemplate<String, String> kafkaTemplate;

    @Autowired
    private EmbeddedKafkaBroker embeddedKafkaBroker;

    @Autowired
    private DltDisabledListener listener;

    @BeforeEach
    void resetState() {
        listener.reset();
    }

    @Test
    void shouldRetryAndNotPublishToDltWhenDltDisabled() throws Exception {
        kafkaTemplate.send("nodlt.events", "key-no-dlt", "{\"id\":\"evt-nodlt\"}");

        assertThat(listener.retryLatch.await(20, TimeUnit.SECONDS)).isTrue();
        assertThat(listener.attempts.get()).isEqualTo(3);

        Consumer<String, String> consumer = createConsumer("verify-nodlt");
        try {
            ConsumerRecords<String, String> records = KafkaTestUtils.getRecords(consumer, Duration.ofSeconds(5));
            assertThat(records.records("nodlt.events.dlt")).isEmpty();
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
        embeddedKafkaBroker.consumeFromEmbeddedTopics(consumer, "nodlt.events.dlt");
        return consumer;
    }

    @EnableKafka
    @SpringBootApplication
    static class TestApplication {
        @Bean
        DltDisabledListener dltDisabledListener() {
            return new DltDisabledListener();
        }
    }

    static class DltDisabledListener {
        private final AtomicInteger attempts = new AtomicInteger();
        private CountDownLatch retryLatch = new CountDownLatch(3);

        void reset() {
            attempts.set(0);
            retryLatch = new CountDownLatch(3);
        }

        @KafkaListener(topics = "nodlt.events", groupId = "${spring.kafka.consumer.group-id}")
        public void handle(ConsumerRecord<String, String> record) {
            attempts.incrementAndGet();
            retryLatch.countDown();
            throw new RuntimeException("Fail for retry without DLT");
        }
    }
}
