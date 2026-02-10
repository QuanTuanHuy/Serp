/*
Author: QuanTuanHuy
Description: Part of Serp Project
*/

package serp.project.mailservice.core.service;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import serp.project.mailservice.core.domain.entity.EmailEntity;
import serp.project.mailservice.core.domain.entity.EmailStatsEntity;
import serp.project.mailservice.core.domain.enums.EmailStatus;
import serp.project.mailservice.core.domain.enums.EmailType;
import serp.project.mailservice.core.port.store.IEmailStatsPort;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;

@Service
@RequiredArgsConstructor
@Slf4j
public class EmailStatsService implements IEmailStatsService {

    private final IEmailStatsPort emailStatsPort;

    @Override
    public void recordEmailSent(EmailEntity email, long responseTimeMs) {
        if (email == null) {
            log.warn("Email entity is null, skipping stats recording");
            return;
        }

        try {
            LocalDateTime now = LocalDateTime.now();
            LocalDate statDate = now.toLocalDate();
            int statHour = now.getHour();

            emailStatsPort.incrementStats(
                    email.getTenantId(),
                    email.getProvider(),
                    email.getType() != null ? email.getType() : EmailType.TRANSACTIONAL,
                    EmailStatus.SENT,
                    statDate,
                    statHour,
                    responseTimeMs,
                    0L,
                    0);

            log.debug("Recorded email sent stats for messageId: {}, responseTime: {}ms",
                    email.getMessageId(), responseTimeMs);
        } catch (Exception e) {
            log.error("Failed to record email sent stats for messageId: {}", email.getMessageId(), e);
        }
    }

    @Override
    public void recordEmailFailed(EmailEntity email) {
        if (email == null) {
            log.warn("Email entity is null, skipping stats recording");
            return;
        }

        try {
            LocalDateTime now = LocalDateTime.now();
            LocalDate statDate = now.toLocalDate();
            int statHour = now.getHour();

            EmailStatus status = email.getStatus() != null ? email.getStatus() : EmailStatus.FAILED;

            emailStatsPort.incrementStats(
                    email.getTenantId(),
                    email.getProvider(),
                    email.getType() != null ? email.getType() : EmailType.TRANSACTIONAL,
                    status,
                    statDate,
                    statHour,
                    0L,
                    0L,
                    0
            );

            log.debug("Recorded email failed stats for messageId: {}, status: {}",
                    email.getMessageId(), status);
        } catch (Exception e) {
            log.error("Failed to record email failed stats for messageId: {}", email.getMessageId(), e);
        }
    }

    @Override
    public void recordEmailRetry(EmailEntity email) {
        if (email == null) {
            return;
        }

        try {
            LocalDateTime now = LocalDateTime.now();
            LocalDate statDate = now.toLocalDate();
            int statHour = now.getHour();

            emailStatsPort.incrementStats(
                    email.getTenantId(),
                    email.getProvider(),
                    email.getType() != null ? email.getType() : EmailType.TRANSACTIONAL,
                    EmailStatus.RETRY,
                    statDate,
                    statHour,
                    0L,
                    0L,
                    0
            );

            log.debug("Recorded email retry stats for messageId: {}", email.getMessageId());
        } catch (Exception e) {
            log.error("Failed to record email retry stats for messageId: {}", email.getMessageId(), e);
        }
    }

    @Override
    public EmailStatsEntity aggregateStats(Long tenantId, LocalDate startDate, LocalDate endDate) {
        log.info("Aggregating stats for tenant: {}, from: {} to: {}", tenantId, startDate, endDate);

        List<EmailStatsEntity> statsList = emailStatsPort.findByTenantIdAndDateRange(tenantId, startDate, endDate);

        if (statsList.isEmpty()) {
            return null;
        }

        long totalCount = 0, successCount = 0, failedCount = 0, retryCount = 0;
        long totalSizeBytes = 0, attachmentCount = 0;
        long totalResponseTime = 0, responseTimeEntries = 0;
        Long minResponseTime = null, maxResponseTime = null;

        for (EmailStatsEntity stats : statsList) {
            totalCount += stats.getTotalCount() != null ? stats.getTotalCount() : 0;
            successCount += stats.getSuccessCount() != null ? stats.getSuccessCount() : 0;
            failedCount += stats.getFailedCount() != null ? stats.getFailedCount() : 0;
            retryCount += stats.getRetryCount() != null ? stats.getRetryCount() : 0;
            totalSizeBytes += stats.getTotalSizeBytes() != null ? stats.getTotalSizeBytes() : 0;
            attachmentCount += stats.getAttachmentCount() != null ? stats.getAttachmentCount() : 0;

            if (stats.getAvgResponseTimeMs() != null && stats.getTotalCount() != null && stats.getTotalCount() > 0) {
                totalResponseTime += stats.getAvgResponseTimeMs() * stats.getTotalCount();
                responseTimeEntries += stats.getTotalCount();
            }
            if (stats.getMinResponseTimeMs() != null) {
                minResponseTime = minResponseTime == null
                        ? stats.getMinResponseTimeMs()
                        : Math.min(minResponseTime, stats.getMinResponseTimeMs());
            }
            if (stats.getMaxResponseTimeMs() != null) {
                maxResponseTime = maxResponseTime == null
                        ? stats.getMaxResponseTimeMs()
                        : Math.max(maxResponseTime, stats.getMaxResponseTimeMs());
            }
        }

        return EmailStatsEntity.builder()
                .tenantId(tenantId)
                .statDate(startDate)
                .totalCount(totalCount)
                .successCount(successCount)
                .failedCount(failedCount)
                .retryCount(retryCount)
                .avgResponseTimeMs(responseTimeEntries > 0 ? totalResponseTime / responseTimeEntries : null)
                .minResponseTimeMs(minResponseTime)
                .maxResponseTimeMs(maxResponseTime)
                .totalSizeBytes(totalSizeBytes)
                .attachmentCount(attachmentCount)
                .build();
    }

    @Override
    public long cleanupOldStats(int retentionDays) {
        log.info("Cleaning up stats older than {} days", retentionDays);
        LocalDate cutoffDate = LocalDate.now().minusDays(retentionDays);
        long deletedCount = emailStatsPort.deleteByStatDateBefore(cutoffDate);
        log.info("Stats cleanup completed: deleted {} records before {}", deletedCount, cutoffDate);
        return deletedCount;
    }

    @Override
    public double getSuccessRate(Long tenantId, LocalDate startDate, LocalDate endDate) {
        EmailStatsEntity aggregated = aggregateStats(tenantId, startDate, endDate);
        if (aggregated == null) {
            return 0.0;
        }
        return aggregated.getSuccessRate();
    }

    @Override
    public long getAverageResponseTime(Long tenantId, LocalDate startDate, LocalDate endDate) {
        EmailStatsEntity aggregated = aggregateStats(tenantId, startDate, endDate);
        if (aggregated == null || aggregated.getAvgResponseTimeMs() == null) {
            return 0L;
        }
        return aggregated.getAvgResponseTimeMs();
    }
}
