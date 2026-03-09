/*
Author: QuanTuanHuy
Description: Part of Serp Project
*/

package io.github.serp.platform.kafka.autoconfigure;

import com.fasterxml.jackson.databind.ObjectMapper;
import org.apache.kafka.clients.consumer.ConsumerConfig;
import org.apache.kafka.clients.consumer.ConsumerRecord;
import org.apache.kafka.common.TopicPartition;
import org.apache.kafka.common.header.internals.RecordHeaders;
import org.apache.kafka.common.serialization.StringDeserializer;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.ObjectProvider;
import org.springframework.boot.autoconfigure.AutoConfiguration;
import org.springframework.boot.autoconfigure.AutoConfigureAfter;
import org.springframework.boot.autoconfigure.kafka.KafkaAutoConfiguration;
import org.springframework.boot.autoconfigure.condition.ConditionalOnBean;
import org.springframework.boot.autoconfigure.condition.ConditionalOnClass;
import org.springframework.boot.autoconfigure.condition.ConditionalOnMissingBean;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.boot.autoconfigure.kafka.KafkaProperties;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.context.annotation.Bean;
import org.springframework.kafka.config.ConcurrentKafkaListenerContainerFactory;
import org.springframework.kafka.core.ConsumerFactory;
import org.springframework.kafka.core.DefaultKafkaConsumerFactory;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.kafka.listener.CommonErrorHandler;
import org.springframework.kafka.listener.ConsumerRecordRecoverer;
import org.springframework.kafka.listener.ContainerProperties;
import org.springframework.kafka.listener.DeadLetterPublishingRecoverer;
import org.springframework.kafka.listener.DefaultErrorHandler;
import org.springframework.kafka.listener.RecordInterceptor;
import org.springframework.kafka.support.ExponentialBackOffWithMaxRetries;
import org.springframework.util.ClassUtils;

import java.nio.charset.StandardCharsets;
import java.util.HashMap;
import java.util.Map;

import io.github.serp.platform.kafka.consumer.DefaultSerpKafkaRecordParser;
import io.github.serp.platform.kafka.consumer.SerpKafkaAcknowledgmentHelper;
import io.github.serp.platform.kafka.consumer.SerpKafkaRecordParser;
import io.github.serp.platform.kafka.properties.SerpKafkaProperties;
import io.github.serp.platform.kafka.support.SerpKafkaRecordInterceptor;
import io.github.serp.platform.kafka.support.SerpKafkaTopicResolver;

@AutoConfiguration
@AutoConfigureAfter(KafkaAutoConfiguration.class)
@ConditionalOnClass(ConcurrentKafkaListenerContainerFactory.class)
@EnableConfigurationProperties(SerpKafkaProperties.class)
@ConditionalOnProperty(prefix = "serp.kafka", name = "enabled", havingValue = "true", matchIfMissing = true)
public class SerpKafkaConsumerAutoConfiguration {
    private static final Logger log = LoggerFactory.getLogger(SerpKafkaConsumerAutoConfiguration.class);

    @Bean
    @ConditionalOnMissingBean
    public SerpKafkaTopicResolver serpKafkaTopicResolver(SerpKafkaProperties properties) {
        return new SerpKafkaTopicResolver(properties);
    }

    @Bean
    @ConditionalOnMissingBean(SerpKafkaRecordParser.class)
    public SerpKafkaRecordParser serpKafkaRecordParser(ObjectMapper objectMapper) {
        return new DefaultSerpKafkaRecordParser(objectMapper);
    }

    @Bean
    @ConditionalOnMissingBean
    public SerpKafkaAcknowledgmentHelper serpKafkaAcknowledgmentHelper() {
        return new SerpKafkaAcknowledgmentHelper();
    }

    @Bean
    @ConditionalOnMissingBean(name = "serpKafkaRecordInterceptor")
    public RecordInterceptor<Object, Object> serpKafkaRecordInterceptor(SerpKafkaProperties properties) {
        return new SerpKafkaRecordInterceptor(properties.getConsumer().getHeaders().getCorrelationIdHeader());
    }

    @Bean
    @ConditionalOnMissingBean(ConsumerFactory.class)
    public ConsumerFactory<Object, Object> consumerFactory(KafkaProperties kafkaProperties) {
        Map<String, Object> consumerProperties = new HashMap<>(kafkaProperties.buildConsumerProperties());
        consumerProperties.putIfAbsent(ConsumerConfig.KEY_DESERIALIZER_CLASS_CONFIG, StringDeserializer.class);
        consumerProperties.putIfAbsent(ConsumerConfig.VALUE_DESERIALIZER_CLASS_CONFIG, StringDeserializer.class);
        return new DefaultKafkaConsumerFactory<>(consumerProperties);
    }

    @Bean
    @ConditionalOnProperty(
            prefix = "serp.kafka.consumer.dlt",
            name = "enabled",
            havingValue = "true",
            matchIfMissing = true)
    @ConditionalOnBean(KafkaTemplate.class)
    @ConditionalOnMissingBean
    public DeadLetterPublishingRecoverer serpDeadLetterPublishingRecoverer(
            KafkaTemplate<?, ?> kafkaTemplate,
            SerpKafkaTopicResolver topicResolver,
            SerpKafkaProperties properties) {
        DeadLetterPublishingRecoverer recoverer = new DeadLetterPublishingRecoverer(
                kafkaTemplate,
                (ConsumerRecord<?, ?> consumerRecord, Exception exception) ->
                        resolveDltTopicPartition(topicResolver, consumerRecord));
        recoverer.setFailIfSendResultIsError(properties.getConsumer().getDlt().isFailIfSendResultIsError());
        recoverer.setHeadersFunction((consumerRecord, exception) -> buildDltHeaders(consumerRecord, exception, properties));
        return recoverer;
    }

    @Bean
    @ConditionalOnMissingBean(CommonErrorHandler.class)
    public CommonErrorHandler serpKafkaCommonErrorHandler(
            SerpKafkaProperties properties,
            KafkaProperties kafkaProperties,
            ObjectProvider<DeadLetterPublishingRecoverer> recovererProvider) {
        int maxAttempts = Math.max(1, properties.getConsumer().getRetry().getMaxAttempts());
        ExponentialBackOffWithMaxRetries backOff = new ExponentialBackOffWithMaxRetries(maxAttempts - 1);
        backOff.setInitialInterval(Math.max(1, properties.getConsumer().getRetry().getInitialIntervalMs()));
        backOff.setMultiplier(Math.max(1.0d, properties.getConsumer().getRetry().getMultiplier()));
        backOff.setMaxInterval(Math.max(1, properties.getConsumer().getRetry().getMaxIntervalMs()));

        DefaultErrorHandler errorHandler;
        DeadLetterPublishingRecoverer recoverer = recovererProvider.getIfAvailable();
        String groupId = kafkaProperties.getConsumer().getGroupId();
        if (recoverer == null) {
            errorHandler = new DefaultErrorHandler(loggingRecoverer(groupId), backOff);
        } else {
            errorHandler = new DefaultErrorHandler(recoverer, backOff);
        }

        errorHandler.setAckAfterHandle(false);
        errorHandler.setCommitRecovered(true);
        errorHandler.setRetryListeners((record, exception, deliveryAttempt) -> {
            Throwable effectiveException = resolveEffectiveException(exception);
            log.warn(
                    "Kafka consume failed topic={}, partition={}, offset={}, key={}, group-id={}, delivery-attempt={}, exception={}",
                    record == null ? null : record.topic(),
                    record == null ? null : record.partition(),
                    record == null ? null : record.offset(),
                    record == null ? null : record.key(),
                    groupId,
                    deliveryAttempt,
                    effectiveException.getClass().getName());
        });
        applyNonRetryableExceptions(errorHandler, properties);
        return errorHandler;
    }

    @Bean(name = "kafkaListenerContainerFactory")
    @ConditionalOnMissingBean(name = "kafkaListenerContainerFactory")
    public ConcurrentKafkaListenerContainerFactory<Object, Object> kafkaListenerContainerFactory(
            ConsumerFactory<Object, Object> consumerFactory,
            CommonErrorHandler commonErrorHandler,
            ObjectProvider<RecordInterceptor<Object, Object>> recordInterceptorProvider,
            SerpKafkaProperties properties) {
        ConcurrentKafkaListenerContainerFactory<Object, Object> factory =
                new ConcurrentKafkaListenerContainerFactory<>();
        factory.setConsumerFactory(consumerFactory);
        factory.setCommonErrorHandler(commonErrorHandler);

        RecordInterceptor<Object, Object> interceptor = recordInterceptorProvider.getIfAvailable();
        if (interceptor != null) {
            factory.setRecordInterceptor(interceptor);
        }

        int concurrency = Math.max(1, properties.getConsumer().getListener().getConcurrency());
        factory.setConcurrency(concurrency);

        ContainerProperties containerProperties = factory.getContainerProperties();
        containerProperties.setAckMode(resolveAckMode(properties.getConsumer().getListener().getAckMode()));
        containerProperties.setSyncCommits(properties.getConsumer().getListener().isSyncCommits());
        containerProperties.setObservationEnabled(properties.getConsumer().isObservationEnabled());
        containerProperties.setDeliveryAttemptHeader(true);

        return factory;
    }

    private void applyNonRetryableExceptions(DefaultErrorHandler errorHandler, SerpKafkaProperties properties) {
        for (String exceptionClassName : properties.getConsumer().getRetry().getNonRetryableExceptions()) {
            Class<? extends Exception> exceptionClass = resolveExceptionClass(exceptionClassName);
            if (exceptionClass != null) {
                errorHandler.addNotRetryableExceptions(exceptionClass);
            }
        }
    }

    private Class<? extends Exception> resolveExceptionClass(String className) {
        if (className == null || className.isBlank()) {
            return null;
        }

        try {
            Class<?> clazz = ClassUtils.forName(className.trim(), SerpKafkaConsumerAutoConfiguration.class.getClassLoader());
            if (!Exception.class.isAssignableFrom(clazz)) {
                return null;
            }

            @SuppressWarnings("unchecked")
            Class<? extends Exception> casted = (Class<? extends Exception>) clazz;
            return casted;
        } catch (ClassNotFoundException ex) {
            return null;
        }
    }

    private ContainerProperties.AckMode resolveAckMode(String configuredAckMode) {
        if (configuredAckMode == null || configuredAckMode.isBlank()) {
            return ContainerProperties.AckMode.RECORD;
        }

        try {
            return ContainerProperties.AckMode.valueOf(configuredAckMode.trim().toUpperCase());
        } catch (IllegalArgumentException ex) {
            return ContainerProperties.AckMode.RECORD;
        }
    }

    private TopicPartition resolveDltTopicPartition(
            SerpKafkaTopicResolver topicResolver,
            ConsumerRecord<?, ?> consumerRecord) {
        return topicResolver.resolveDeadLetterTopicPartition(consumerRecord);
    }

    private RecordHeaders buildDltHeaders(
            ConsumerRecord<?, ?> consumerRecord,
            Exception exception,
            SerpKafkaProperties properties) {
        SerpKafkaProperties.Headers headersProperties = properties.getConsumer().getHeaders();
        RecordHeaders headers = new RecordHeaders();
        Throwable effectiveException = resolveEffectiveException(exception);

        addHeader(headers, headersProperties.getExceptionClassHeader(), effectiveException.getClass().getName());
        addHeader(headers, headersProperties.getExceptionMessageHeader(), effectiveException.getMessage());
        addHeader(headers, headersProperties.getExceptionStackSummaryHeader(), summarizeStackTrace(effectiveException));
        addHeader(headers, headersProperties.getOriginalTopicHeader(), consumerRecord.topic());
        addHeader(headers, headersProperties.getOriginalPartitionHeader(), String.valueOf(consumerRecord.partition()));
        addHeader(headers, headersProperties.getOriginalOffsetHeader(), String.valueOf(consumerRecord.offset()));
        addHeader(headers, headersProperties.getOriginalTimestampHeader(), String.valueOf(consumerRecord.timestamp()));

        return headers;
    }

    private ConsumerRecordRecoverer loggingRecoverer(String groupId) {
        return (record, exception) -> {
            Throwable effectiveException = resolveEffectiveException(exception);
            log.error(
                    "Kafka consume exhausted retries without DLT topic={}, partition={}, offset={}, key={}, group-id={}, exception={}",
                    record == null ? null : record.topic(),
                    record == null ? null : record.partition(),
                    record == null ? null : record.offset(),
                    record == null ? null : record.key(),
                    groupId,
                    effectiveException.getClass().getName(),
                    effectiveException);
        };
    }

    private void addHeader(RecordHeaders headers, String key, String value) {
        if (key == null || key.isBlank() || value == null) {
            return;
        }
        headers.add(key, value.getBytes(StandardCharsets.UTF_8));
    }

    private String summarizeStackTrace(Throwable exception) {
        StringBuilder summary = new StringBuilder(exception.getClass().getName());
        if (exception.getMessage() != null && !exception.getMessage().isBlank()) {
            summary.append(": ").append(exception.getMessage());
        }

        StackTraceElement[] trace = exception.getStackTrace();
        int maxFrames = Math.min(3, trace.length);
        for (int i = 0; i < maxFrames; i++) {
            summary.append(" | at ").append(trace[i]);
        }

        String summaryText = summary.toString();
        int maxLength = 512;
        if (summaryText.length() > maxLength) {
            return summaryText.substring(0, maxLength);
        }
        return summaryText;
    }

    private Throwable resolveEffectiveException(Throwable exception) {
        if (exception == null) {
            return new IllegalStateException("Unknown Kafka consumer exception");
        }

        Throwable current = exception;
        while (current.getCause() != null && current.getCause() != current) {
            current = current.getCause();
        }
        return current;
    }
}
