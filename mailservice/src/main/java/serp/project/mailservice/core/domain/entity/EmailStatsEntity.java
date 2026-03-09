/*
Author: QuanTuanHuy
Description: Part of Serp Project
*/

package serp.project.mailservice.core.domain.entity;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.experimental.SuperBuilder;
import serp.project.mailservice.core.domain.enums.EmailProvider;
import serp.project.mailservice.core.domain.enums.EmailStatus;
import serp.project.mailservice.core.domain.enums.EmailType;

import java.time.LocalDate;
import java.time.LocalDateTime;

@Data
@SuperBuilder
@NoArgsConstructor
@AllArgsConstructor
public class EmailStatsEntity {
    private Long id;

    private Long tenantId;
    private EmailProvider provider;
    private EmailType emailType;
    private EmailStatus status;

    private LocalDate statDate;
    private Integer statHour;

    private Long totalCount;
    private Long successCount;
    private Long failedCount;
    private Long retryCount;

    private Long avgResponseTimeMs;
    private Long minResponseTimeMs;
    private Long maxResponseTimeMs;

    private Long totalSizeBytes;
    private Long attachmentCount;

    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;

    // ==================== Factory Methods ====================

    public static EmailStatsEntity createNew(Long tenantId, EmailProvider provider, EmailType emailType,
                                              EmailStatus status, LocalDate statDate, Integer statHour) {
        LocalDateTime now = LocalDateTime.now();
        return EmailStatsEntity.builder()
                .tenantId(tenantId)
                .provider(provider)
                .emailType(emailType)
                .status(status)
                .statDate(statDate)
                .statHour(statHour)
                .totalCount(0L)
                .successCount(0L)
                .failedCount(0L)
                .retryCount(0L)
                .totalSizeBytes(0L)
                .attachmentCount(0L)
                .createdAt(now)
                .updatedAt(now)
                .build();
    }

    // ==================== Business Logic ====================

    public void incrementForStatus(EmailStatus eventStatus, Long responseTimeMs, Long sizeBytes, Integer attachCount) {
        this.totalCount = safeGet(this.totalCount) + 1;

        if (eventStatus == EmailStatus.SENT) {
            this.successCount = safeGet(this.successCount) + 1;
        } else if (eventStatus == EmailStatus.FAILED) {
            this.failedCount = safeGet(this.failedCount) + 1;
        } else if (eventStatus == EmailStatus.RETRY) {
            this.retryCount = safeGet(this.retryCount) + 1;
        }

        if (responseTimeMs != null) {
            long previousTotal = safeGet(this.avgResponseTimeMs) * (this.totalCount - 1);
            this.avgResponseTimeMs = (previousTotal + responseTimeMs) / this.totalCount;

            if (this.minResponseTimeMs == null || responseTimeMs < this.minResponseTimeMs) {
                this.minResponseTimeMs = responseTimeMs;
            }
            if (this.maxResponseTimeMs == null || responseTimeMs > this.maxResponseTimeMs) {
                this.maxResponseTimeMs = responseTimeMs;
            }
        }

        if (sizeBytes != null) {
            this.totalSizeBytes = safeGet(this.totalSizeBytes) + sizeBytes;
        }

        if (attachCount != null && attachCount > 0) {
            this.attachmentCount = safeGet(this.attachmentCount) + attachCount;
        }

        this.updatedAt = LocalDateTime.now();
    }

    // ==================== Query Methods ====================

    public double getSuccessRate() {
        long total = safeGet(totalCount);
        if (total == 0) {
            return 0.0;
        }
        return (double) safeGet(successCount) / total * 100.0;
    }

    public double getFailureRate() {
        long total = safeGet(totalCount);
        if (total == 0) {
            return 0.0;
        }
        return (double) safeGet(failedCount) / total * 100.0;
    }

    // ==================== Private Helpers ====================

    private static long safeGet(Long value) {
        return value != null ? value : 0L;
    }
}
