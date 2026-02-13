/*
Author: QuanTuanHuy
Description: Part of Serp Project
*/

package io.github.serp.platform.kafka.autoconfigure;

import org.apache.kafka.common.TopicPartition;
import org.springframework.beans.factory.ObjectProvider;
import org.springframework.boot.autoconfigure.AutoConfiguration;
import org.springframework.boot.autoconfigure.condition.ConditionalOnClass;
import org.springframework.boot.autoconfigure.condition.ConditionalOnMissingBean;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.context.annotation.Bean;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.kafka.listener.CommonErrorHandler;
import org.springframework.kafka.listener.DeadLetterPublishingRecoverer;
import org.springframework.kafka.listener.DefaultErrorHandler;
import org.springframework.util.backoff.FixedBackOff;

import io.github.serp.platform.kafka.properties.SerpKafkaProperties;
import io.github.serp.platform.kafka.publisher.DefaultSerpKafkaPublisher;
import io.github.serp.platform.kafka.publisher.SerpKafkaPublisher;
import io.github.serp.platform.kafka.support.SerpKafkaTopicResolver;

@AutoConfiguration
@ConditionalOnClass(KafkaTemplate.class)
@EnableConfigurationProperties(SerpKafkaProperties.class)
@ConditionalOnProperty(prefix = "serp.kafka", name = "enabled", havingValue = "true", matchIfMissing = true)
public class SerpKafkaAutoConfiguration {

    @Bean
    @ConditionalOnMissingBean
    public SerpKafkaTopicResolver serpKafkaTopicResolver(SerpKafkaProperties properties) {
        return new SerpKafkaTopicResolver(properties);
    }

    @Bean
    @ConditionalOnMissingBean
    public SerpKafkaPublisher serpKafkaPublisher(
            KafkaTemplate<Object, Object> kafkaTemplate,
            SerpKafkaProperties properties) {
        return new DefaultSerpKafkaPublisher(kafkaTemplate, properties);
    }

    @Bean
    @ConditionalOnProperty(
            prefix = "serp.kafka.consumer",
            name = "dead-letter-enabled",
            havingValue = "true",
            matchIfMissing = true)
    @ConditionalOnMissingBean
    public DeadLetterPublishingRecoverer serpDeadLetterPublishingRecoverer(
            KafkaTemplate<Object, Object> kafkaTemplate,
            SerpKafkaTopicResolver topicResolver) {
        return new DeadLetterPublishingRecoverer(
                kafkaTemplate,
                (consumerRecord, exception) ->
                        new TopicPartition(topicResolver.resolveDeadLetterTopic(consumerRecord.topic()),
                                consumerRecord.partition()));
    }

    @Bean
    @ConditionalOnMissingBean(CommonErrorHandler.class)
    public CommonErrorHandler serpKafkaCommonErrorHandler(
            SerpKafkaProperties properties,
            ObjectProvider<DeadLetterPublishingRecoverer> recovererProvider) {
        long maxAttempts = Math.max(1, properties.getConsumer().getMaxAttempts());
        long retryCount = maxAttempts - 1;
        long retryIntervalMs = Math.max(0, properties.getConsumer().getRetryIntervalMs());
        FixedBackOff fixedBackOff = new FixedBackOff(retryIntervalMs, retryCount);

        DeadLetterPublishingRecoverer recoverer = recovererProvider.getIfAvailable();
        if (recoverer == null) {
            return new DefaultErrorHandler(fixedBackOff);
        }

        return new DefaultErrorHandler(recoverer, fixedBackOff);
    }
}
