package serp.project.pmcore.core.domain.entity;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import serp.project.pmcore.core.domain.enums.OutboxEventStatus;

@Getter
@Setter
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class OutboxEventEntity {

    // Exponential backoff: 5s, 30s, 2m, 10m, 1h
    private static final long[] BACKOFF_DELAYS_MS = {5_000, 30_000, 120_000, 600_000, 3_600_000};

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
    private Integer maxRetries = BACKOFF_DELAYS_MS.length;
    private Long nextRetryAt;
    private Long publishedAt;
    private String errorMessage;
    @Builder.Default
    private Long createdAt = System.currentTimeMillis();
    private Long updatedAt;

    public void markPublished() {
        long now = System.currentTimeMillis();
        this.status = OutboxEventStatus.PUBLISHED;
        this.publishedAt = now;
        this.errorMessage = null;
        this.updatedAt = now;
    }

    public void markFailed(String error) {
        long now = System.currentTimeMillis();
        this.retryCount = this.retryCount == null ? 1 : this.retryCount + 1;
        if (this.retryCount >= this.maxRetries) {
            this.status = OutboxEventStatus.DEAD;
            this.nextRetryAt = null;
        } else {
            this.status = OutboxEventStatus.FAILED;
            int idx = Math.min(this.retryCount - 1, BACKOFF_DELAYS_MS.length - 1);
            this.nextRetryAt = now + BACKOFF_DELAYS_MS[idx];
        }
        this.errorMessage = error != null  && error.length() > 1000 ? error.substring(0, 1000) : error;
        this.updatedAt = now;
    }
}
