/*
Author: QuanTuanHuy
Description: Part of Serp Project
*/

package io.github.serp.platform.kafka.properties;

import org.springframework.boot.context.properties.ConfigurationProperties;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@ConfigurationProperties(prefix = "serp.kafka")
public class SerpKafkaProperties {
    private boolean enabled = true;
    private Producer producer = new Producer();
    private Consumer consumer = new Consumer();

    public boolean isEnabled() {
        return enabled;
    }

    public void setEnabled(boolean enabled) {
        this.enabled = enabled;
    }

    public Producer getProducer() {
        return producer;
    }

    public void setProducer(Producer producer) {
        this.producer = producer;
    }

    public Consumer getConsumer() {
        return consumer;
    }

    public void setConsumer(Consumer consumer) {
        this.consumer = consumer;
    }

    public static class Producer {
        private String defaultTopic;
        private boolean headerPropagationEnabled = true;
        private String correlationIdHeader = "X-Correlation-Id";
        private boolean failOnPublishError = true;

        public String getDefaultTopic() {
            return defaultTopic;
        }

        public void setDefaultTopic(String defaultTopic) {
            this.defaultTopic = defaultTopic;
        }

        public boolean isHeaderPropagationEnabled() {
            return headerPropagationEnabled;
        }

        public void setHeaderPropagationEnabled(boolean headerPropagationEnabled) {
            this.headerPropagationEnabled = headerPropagationEnabled;
        }

        public String getCorrelationIdHeader() {
            return correlationIdHeader;
        }

        public void setCorrelationIdHeader(String correlationIdHeader) {
            this.correlationIdHeader = correlationIdHeader;
        }

        public boolean isFailOnPublishError() {
            return failOnPublishError;
        }

        public void setFailOnPublishError(boolean failOnPublishError) {
            this.failOnPublishError = failOnPublishError;
        }
    }

    public static class Consumer {
        private Listener listener = new Listener();
        private Retry retry = new Retry();
        private Dlt dlt = new Dlt();
        private Headers headers = new Headers();
        private boolean observationEnabled = true;

        public Listener getListener() {
            return listener;
        }

        public void setListener(Listener listener) {
            this.listener = listener;
        }

        public Retry getRetry() {
            return retry;
        }

        public void setRetry(Retry retry) {
            this.retry = retry;
        }

        public Dlt getDlt() {
            return dlt;
        }

        public void setDlt(Dlt dlt) {
            this.dlt = dlt;
        }

        public Headers getHeaders() {
            return headers;
        }

        public void setHeaders(Headers headers) {
            this.headers = headers;
        }

        public boolean isObservationEnabled() {
            return observationEnabled;
        }

        public void setObservationEnabled(boolean observationEnabled) {
            this.observationEnabled = observationEnabled;
        }
    }

    public static class Listener {
        private String ackMode = "RECORD";
        private int concurrency = 1;
        private boolean syncCommits = true;

        public String getAckMode() {
            return ackMode;
        }

        public void setAckMode(String ackMode) {
            this.ackMode = ackMode;
        }

        public int getConcurrency() {
            return concurrency;
        }

        public void setConcurrency(int concurrency) {
            this.concurrency = concurrency;
        }

        public boolean isSyncCommits() {
            return syncCommits;
        }

        public void setSyncCommits(boolean syncCommits) {
            this.syncCommits = syncCommits;
        }
    }

    public static class Retry {
        private int maxAttempts = 3;
        private long initialIntervalMs = 1000;
        private double multiplier = 2.0d;
        private long maxIntervalMs = 10000;
        private List<String> nonRetryableExceptions = new ArrayList<>(List.of(
                "org.springframework.kafka.support.serializer.DeserializationException",
                "com.fasterxml.jackson.core.JsonProcessingException",
                "java.lang.IllegalArgumentException",
                "org.springframework.web.bind.MethodArgumentNotValidException"));

        public int getMaxAttempts() {
            return maxAttempts;
        }

        public void setMaxAttempts(int maxAttempts) {
            this.maxAttempts = maxAttempts;
        }

        public long getInitialIntervalMs() {
            return initialIntervalMs;
        }

        public void setInitialIntervalMs(long initialIntervalMs) {
            this.initialIntervalMs = initialIntervalMs;
        }

        public double getMultiplier() {
            return multiplier;
        }

        public void setMultiplier(double multiplier) {
            this.multiplier = multiplier;
        }

        public long getMaxIntervalMs() {
            return maxIntervalMs;
        }

        public void setMaxIntervalMs(long maxIntervalMs) {
            this.maxIntervalMs = maxIntervalMs;
        }

        public List<String> getNonRetryableExceptions() {
            return nonRetryableExceptions;
        }

        public void setNonRetryableExceptions(List<String> nonRetryableExceptions) {
            this.nonRetryableExceptions = nonRetryableExceptions;
        }
    }

    public static class Dlt {
        private boolean enabled = true;
        private String suffix = ".dlt";
        private boolean samePartition = true;
        private boolean failIfSendResultIsError = true;
        private Map<String, String> topicMappings = new HashMap<>();

        public boolean isEnabled() {
            return enabled;
        }

        public void setEnabled(boolean enabled) {
            this.enabled = enabled;
        }

        public String getSuffix() {
            return suffix;
        }

        public void setSuffix(String suffix) {
            this.suffix = suffix;
        }

        public boolean isSamePartition() {
            return samePartition;
        }

        public void setSamePartition(boolean samePartition) {
            this.samePartition = samePartition;
        }

        public boolean isFailIfSendResultIsError() {
            return failIfSendResultIsError;
        }

        public void setFailIfSendResultIsError(boolean failIfSendResultIsError) {
            this.failIfSendResultIsError = failIfSendResultIsError;
        }

        public Map<String, String> getTopicMappings() {
            return topicMappings;
        }

        public void setTopicMappings(Map<String, String> topicMappings) {
            this.topicMappings = topicMappings;
        }
    }

    public static class Headers {
        private String correlationIdHeader = "X-Correlation-Id";
        private String exceptionClassHeader = "x-serp-exception-class";
        private String exceptionMessageHeader = "x-serp-exception-message";
        private String exceptionStackSummaryHeader = "x-serp-exception-stack";
        private String originalTopicHeader = "x-serp-original-topic";
        private String originalPartitionHeader = "x-serp-original-partition";
        private String originalOffsetHeader = "x-serp-original-offset";
        private String originalTimestampHeader = "x-serp-original-timestamp";

        public String getCorrelationIdHeader() {
            return correlationIdHeader;
        }

        public void setCorrelationIdHeader(String correlationIdHeader) {
            this.correlationIdHeader = correlationIdHeader;
        }

        public String getExceptionClassHeader() {
            return exceptionClassHeader;
        }

        public void setExceptionClassHeader(String exceptionClassHeader) {
            this.exceptionClassHeader = exceptionClassHeader;
        }

        public String getExceptionMessageHeader() {
            return exceptionMessageHeader;
        }

        public void setExceptionMessageHeader(String exceptionMessageHeader) {
            this.exceptionMessageHeader = exceptionMessageHeader;
        }

        public String getExceptionStackSummaryHeader() {
            return exceptionStackSummaryHeader;
        }

        public void setExceptionStackSummaryHeader(String exceptionStackSummaryHeader) {
            this.exceptionStackSummaryHeader = exceptionStackSummaryHeader;
        }

        public String getOriginalTopicHeader() {
            return originalTopicHeader;
        }

        public void setOriginalTopicHeader(String originalTopicHeader) {
            this.originalTopicHeader = originalTopicHeader;
        }

        public String getOriginalPartitionHeader() {
            return originalPartitionHeader;
        }

        public void setOriginalPartitionHeader(String originalPartitionHeader) {
            this.originalPartitionHeader = originalPartitionHeader;
        }

        public String getOriginalOffsetHeader() {
            return originalOffsetHeader;
        }

        public void setOriginalOffsetHeader(String originalOffsetHeader) {
            this.originalOffsetHeader = originalOffsetHeader;
        }

        public String getOriginalTimestampHeader() {
            return originalTimestampHeader;
        }

        public void setOriginalTimestampHeader(String originalTimestampHeader) {
            this.originalTimestampHeader = originalTimestampHeader;
        }
    }
}
