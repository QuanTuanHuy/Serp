/*
Author: QuanTuanHuy
Description: Part of Serp Project
*/

package io.github.serp.platform.kafka.properties;

import org.springframework.boot.context.properties.ConfigurationProperties;

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
        private long maxAttempts = 3;
        private long retryIntervalMs = 1000;
        private boolean deadLetterEnabled = true;
        private String dltSuffix = ".dlt";

        public long getMaxAttempts() {
            return maxAttempts;
        }

        public void setMaxAttempts(long maxAttempts) {
            this.maxAttempts = maxAttempts;
        }

        public long getRetryIntervalMs() {
            return retryIntervalMs;
        }

        public void setRetryIntervalMs(long retryIntervalMs) {
            this.retryIntervalMs = retryIntervalMs;
        }

        public boolean isDeadLetterEnabled() {
            return deadLetterEnabled;
        }

        public void setDeadLetterEnabled(boolean deadLetterEnabled) {
            this.deadLetterEnabled = deadLetterEnabled;
        }

        public String getDltSuffix() {
            return dltSuffix;
        }

        public void setDltSuffix(String dltSuffix) {
            this.dltSuffix = dltSuffix;
        }
    }
}
