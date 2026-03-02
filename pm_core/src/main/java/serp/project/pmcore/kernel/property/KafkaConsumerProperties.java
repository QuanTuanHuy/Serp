/**
 * Author: QuanTuanHuy
 * Description: Part of Serp Project
 */

package serp.project.pmcore.kernel.property;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.stereotype.Component;

@Component
@ConfigurationProperties(prefix = "app.kafka.consumer")
public class KafkaConsumerProperties {

    private boolean enabled = false;
    private List<String> topics = new ArrayList<>();
    private int concurrency = 1;

    private int maxRetryAttempts = 5;
    private long initialBackoffMs = 1000;
    private long maxBackoffMs = 30000;
    private double backoffMultiplier = 2.0;
    private String dlqSuffix = ".dlq";

    private int inboxRetentionDays = 14;
    private String inboxCleanupCron = "0 30 3 * * *";

    public boolean isEnabled() {
        return enabled;
    }

    public void setEnabled(boolean enabled) {
        this.enabled = enabled;
    }

    public List<String> getTopics() {
        return topics;
    }

    public void setTopics(List<String> topics) {
        this.topics = topics;
    }

    public int getConcurrency() {
        return concurrency;
    }

    public void setConcurrency(int concurrency) {
        this.concurrency = concurrency;
    }

    public int getMaxRetryAttempts() {
        return maxRetryAttempts;
    }

    public void setMaxRetryAttempts(int maxRetryAttempts) {
        this.maxRetryAttempts = maxRetryAttempts;
    }

    public long getInitialBackoffMs() {
        return initialBackoffMs;
    }

    public void setInitialBackoffMs(long initialBackoffMs) {
        this.initialBackoffMs = initialBackoffMs;
    }

    public long getMaxBackoffMs() {
        return maxBackoffMs;
    }

    public void setMaxBackoffMs(long maxBackoffMs) {
        this.maxBackoffMs = maxBackoffMs;
    }

    public double getBackoffMultiplier() {
        return backoffMultiplier;
    }

    public void setBackoffMultiplier(double backoffMultiplier) {
        this.backoffMultiplier = backoffMultiplier;
    }

    public String getDlqSuffix() {
        return dlqSuffix;
    }

    public void setDlqSuffix(String dlqSuffix) {
        this.dlqSuffix = dlqSuffix;
    }

    public int getInboxRetentionDays() {
        return inboxRetentionDays;
    }

    public void setInboxRetentionDays(int inboxRetentionDays) {
        this.inboxRetentionDays = inboxRetentionDays;
    }

    public String getInboxCleanupCron() {
        return inboxCleanupCron;
    }

    public void setInboxCleanupCron(String inboxCleanupCron) {
        this.inboxCleanupCron = inboxCleanupCron;
    }

    public String[] getResolvedTopics() {
        List<String> source = topics == null ? Collections.emptyList() : topics;
        List<String> resolved = source.stream()
                .filter(topic -> topic != null && !topic.isBlank())
                .map(String::trim)
                .toList();

        if (resolved.isEmpty()) {
            return new String[] { "serp.pm.placeholder.events" };
        }

        return resolved.toArray(new String[0]);
    }

    public String getResolvedDlqSuffix() {
        if (dlqSuffix == null || dlqSuffix.isBlank()) {
            return ".dlq";
        }
        return dlqSuffix.trim();
    }
}
