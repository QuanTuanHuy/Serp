/**
 * Author: QuanTuanHuy
 * Description: Part of Serp Project
 */

package serp.project.account.core.domain.entity;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import serp.project.account.core.domain.enums.OutboxEventStatus;

@NoArgsConstructor
@AllArgsConstructor
@Builder
@Setter
@Getter
public class OutboxEventEntity {

    // Exponential backoff: 5s, 30s, 2m, 10m, 1h
    private static final long[] BACKOFF_DELAYS_MS = { 5_000, 30_000, 120_000, 600_000, 3_600_000 };
    private static final int DEFAULT_MAX_RETRIES = BACKOFF_DELAYS_MS.length;

    private Long id;
    private Long tenantId;
    private String aggregateType;
    private Long aggregateId;
    private String eventType;
    private String topic;
    private String partitionKey;
    private String payload;
    private OutboxEventStatus status;
    @Builder.Default
    private Integer retryCount = 0;
    @Builder.Default
    private Integer maxRetries = DEFAULT_MAX_RETRIES;
    private Long nextRetryAt;
    private Long publishedAt;
    private String errorMessage;
    @Builder.Default
    private Long createdAt = System.currentTimeMillis();
    private Long updatedAt;

    public static OutboxEventEntity createNew(Long tenantId, String aggregateType, Long aggregateId,
            String eventType, String topic, String partitionKey, String payload) {
        long now = System.currentTimeMillis();
        OutboxEventEntity event = new OutboxEventEntity();
        event.setTenantId(tenantId);
        event.setAggregateType(aggregateType);
        event.setAggregateId(aggregateId);
        event.setEventType(eventType);
        event.setTopic(topic);
        event.setPartitionKey(partitionKey);
        event.setPayload(payload);
        event.setStatus(OutboxEventStatus.PENDING);
        event.setRetryCount(0);
        event.setMaxRetries(DEFAULT_MAX_RETRIES);
        event.setCreatedAt(now);
        event.setUpdatedAt(now);
        return event;
    }

    public static int defaultMaxRetries() {
        return DEFAULT_MAX_RETRIES;
    }

    public void markPublished() {
        long now = System.currentTimeMillis();
        this.status = OutboxEventStatus.PUBLISHED;
        this.publishedAt = now;
        this.nextRetryAt = null;
        this.errorMessage = null;
        this.updatedAt = now;
    }

    public void markFailed(String error) {
        long now = System.currentTimeMillis();
        this.retryCount = this.retryCount == null ? 1 : this.retryCount + 1;
        int allowedRetries = this.maxRetries == null || this.maxRetries < 1
                ? DEFAULT_MAX_RETRIES
                : this.maxRetries;
        if (this.retryCount > allowedRetries) {
            this.status = OutboxEventStatus.DEAD;
            this.nextRetryAt = null;
        } else {
            this.status = OutboxEventStatus.FAILED;
            int idx = Math.min(this.retryCount - 1, BACKOFF_DELAYS_MS.length - 1);
            this.nextRetryAt = now + BACKOFF_DELAYS_MS[idx];
        }
        this.errorMessage = error != null && error.length() > 1000 ? error.substring(0, 1000) : error;
        this.updatedAt = now;
    }
}
