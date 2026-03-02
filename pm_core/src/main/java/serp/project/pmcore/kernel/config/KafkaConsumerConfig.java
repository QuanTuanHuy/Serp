/**
 * Author: QuanTuanHuy
 * Description: Part of Serp Project
 */

package serp.project.pmcore.kernel.config;

import org.apache.kafka.common.TopicPartition;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.kafka.config.ConcurrentKafkaListenerContainerFactory;
import org.springframework.kafka.core.ConsumerFactory;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.kafka.listener.ContainerProperties;
import org.springframework.kafka.listener.DefaultErrorHandler;
import org.springframework.kafka.listener.DeadLetterPublishingRecoverer;
import org.springframework.kafka.support.ExponentialBackOffWithMaxRetries;

import com.fasterxml.jackson.databind.JsonNode;

import serp.project.pmcore.core.exception.KafkaNonRetryableException;
import serp.project.pmcore.core.service.IConsumerInboxService;
import serp.project.pmcore.kernel.property.KafkaConsumerProperties;
import serp.project.pmcore.kernel.utils.JsonUtils;

@Configuration
public class KafkaConsumerConfig {

    private static final Logger log = LoggerFactory.getLogger(KafkaConsumerConfig.class);

    @Bean
    public DeadLetterPublishingRecoverer deadLetterPublishingRecoverer(
            KafkaTemplate<String, String> kafkaTemplate,
            KafkaConsumerProperties consumerProperties,
            IConsumerInboxService consumerInboxService,
            JsonUtils jsonUtils,
            @Value("${spring.kafka.consumer.group-id:pmcore-group}") String consumerGroupId) {
        return new DeadLetterPublishingRecoverer(
                kafkaTemplate,
                (record, ex) -> {
                    markDeadIfPossible(record.value(), consumerGroupId, ex, consumerInboxService, jsonUtils);
                    return new TopicPartition(
                            record.topic() + consumerProperties.getResolvedDlqSuffix(),
                            record.partition());
                });
    }

    @Bean
    public DefaultErrorHandler pmCoreKafkaErrorHandler(
            DeadLetterPublishingRecoverer deadLetterPublishingRecoverer,
            KafkaConsumerProperties consumerProperties) {
        ExponentialBackOffWithMaxRetries backOff = new ExponentialBackOffWithMaxRetries(
                Math.max(0, consumerProperties.getMaxRetryAttempts()));
        backOff.setInitialInterval(Math.max(100, consumerProperties.getInitialBackoffMs()));
        backOff.setMultiplier(Math.max(1.0, consumerProperties.getBackoffMultiplier()));
        backOff.setMaxInterval(Math.max(backOff.getInitialInterval(), consumerProperties.getMaxBackoffMs()));

        DefaultErrorHandler errorHandler = new DefaultErrorHandler(deadLetterPublishingRecoverer, backOff);
        errorHandler.addNotRetryableExceptions(KafkaNonRetryableException.class);
        errorHandler.setCommitRecovered(true);
        return errorHandler;
    }

    @Bean
    public ConcurrentKafkaListenerContainerFactory<String, String> pmCoreKafkaListenerContainerFactory(
            ConsumerFactory<String, String> consumerFactory,
            DefaultErrorHandler pmCoreKafkaErrorHandler,
            KafkaConsumerProperties consumerProperties) {
        ConcurrentKafkaListenerContainerFactory<String, String> factory =
                new ConcurrentKafkaListenerContainerFactory<>();
        factory.setConsumerFactory(consumerFactory);
        factory.setConcurrency(Math.max(1, consumerProperties.getConcurrency()));
        factory.setCommonErrorHandler(pmCoreKafkaErrorHandler);
        factory.getContainerProperties().setAckMode(ContainerProperties.AckMode.MANUAL);
        factory.getContainerProperties().setMissingTopicsFatal(false);
        return factory;
    }

    private void markDeadIfPossible(
            Object rawPayload,
            String consumerGroupId,
            Exception exception,
            IConsumerInboxService consumerInboxService,
            JsonUtils jsonUtils) {
        if (!(rawPayload instanceof String payload) || payload.isBlank()) {
            return;
        }

        try {
            JsonNode root = jsonUtils.fromJson(payload, JsonNode.class);
            String eventId = root.path("meta").path("id").asText(null);
            if (eventId == null || eventId.isBlank()) {
                return;
            }

            consumerInboxService.markDead(consumerGroupId, eventId.trim(), exception.getMessage());
        } catch (Exception ex) {
            log.warn("Failed to mark consumer inbox event as DEAD from recoverer: {}", ex.getMessage());
        }
    }
}
