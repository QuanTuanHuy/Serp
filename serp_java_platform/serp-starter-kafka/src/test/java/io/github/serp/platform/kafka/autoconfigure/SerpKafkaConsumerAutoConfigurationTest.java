/*
Author: QuanTuanHuy
Description: Part of Serp Project
*/

package io.github.serp.platform.kafka.autoconfigure;

import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.Test;
import org.mockito.Mockito;
import org.springframework.boot.autoconfigure.AutoConfigurations;
import org.springframework.boot.autoconfigure.kafka.KafkaProperties;
import org.springframework.boot.test.context.runner.ApplicationContextRunner;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.kafka.config.ConcurrentKafkaListenerContainerFactory;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.kafka.listener.CommonErrorHandler;
import org.springframework.kafka.listener.DeadLetterPublishingRecoverer;

import static org.assertj.core.api.Assertions.assertThat;

class SerpKafkaConsumerAutoConfigurationTest {

    private final ApplicationContextRunner contextRunner = new ApplicationContextRunner()
            .withConfiguration(AutoConfigurations.of(SerpKafkaConsumerAutoConfiguration.class))
            .withUserConfiguration(TestKafkaBeans.class)
            .withPropertyValues("spring.kafka.bootstrap-servers=localhost:9092");

    @Test
    void shouldCreateDefaultConsumerBeansWithDltEnabled() {
        contextRunner.run(context -> {
            assertThat(context).hasSingleBean(CommonErrorHandler.class);
            assertThat(context).hasSingleBean(DeadLetterPublishingRecoverer.class);
            assertThat(context).hasBean("kafkaListenerContainerFactory");
            assertThat(context.getBean("kafkaListenerContainerFactory"))
                    .isInstanceOf(ConcurrentKafkaListenerContainerFactory.class);
        });
    }

    @Test
    void shouldSkipDeadLetterRecovererWhenDltDisabled() {
        contextRunner.withPropertyValues("serp.kafka.consumer.dlt.enabled=false")
                .run(context -> {
                    assertThat(context).hasSingleBean(CommonErrorHandler.class);
                    assertThat(context).doesNotHaveBean(DeadLetterPublishingRecoverer.class);
                });
    }

    @Test
    void shouldApplyConfiguredAckModeAndConcurrency() {
        contextRunner.withPropertyValues(
                        "serp.kafka.consumer.listener.ack-mode=MANUAL",
                        "serp.kafka.consumer.listener.concurrency=4")
                .run(context -> {
                    @SuppressWarnings("unchecked")
                    ConcurrentKafkaListenerContainerFactory<Object, Object> factory =
                            (ConcurrentKafkaListenerContainerFactory<Object, Object>)
                                    context.getBean("kafkaListenerContainerFactory");
                    var container = factory.createContainer("test.events");

                    assertThat(factory.getContainerProperties().getAckMode().name()).isEqualTo("MANUAL");
                    assertThat(container.getConcurrency()).isEqualTo(4);
                });
    }

    @Configuration(proxyBeanMethods = false)
    static class TestKafkaBeans {
        @Bean
        KafkaProperties kafkaProperties() {
            return new KafkaProperties();
        }

        @Bean
        ObjectMapper objectMapper() {
            return new ObjectMapper();
        }

        @Bean
        KafkaTemplate<Object, Object> kafkaTemplate() {
            @SuppressWarnings("unchecked")
            KafkaTemplate<Object, Object> template = Mockito.mock(KafkaTemplate.class);
            return template;
        }
    }
}
