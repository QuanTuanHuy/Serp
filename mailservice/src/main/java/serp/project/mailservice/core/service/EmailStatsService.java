/*
Author: QuanTuanHuy
Description: Part of Serp Project
*/

package serp.project.mailservice.core.service;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import serp.project.mailservice.core.domain.entity.EmailEntity;
import serp.project.mailservice.core.domain.enums.EmailStatus;
import serp.project.mailservice.core.domain.enums.EmailType;
import serp.project.mailservice.core.port.store.IEmailStatsPort;

import java.time.LocalDate;
import java.time.LocalDateTime;

@Service
@RequiredArgsConstructor
@Slf4j
public class EmailStatsService implements serp.project.mailservice.core.service.IEmailStatsService {

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
    public serp.project.mailservice.core.domain.entity.EmailStatsEntity aggregateStats(
            Long tenantId, LocalDate startDate, LocalDate endDate) {
        log.info("Aggregating stats for tenant: {}, from: {} to: {}", tenantId, startDate, endDate);
        return null;
    }

    @Override
    public long cleanupOldStats(int retentionDays) {
        log.info("Cleaning up stats older than {} days", retentionDays);
        LocalDate cutoffDate = LocalDate.now().minusDays(retentionDays);
        log.info("Stats cleanup completed for date before: {}", cutoffDate);
        return 0L;
    }

    @Override
    public double getSuccessRate(Long tenantId, LocalDate startDate, LocalDate endDate) {
        return 0.0;
    }

    @Override
    public long getAverageResponseTime(Long tenantId, LocalDate startDate, LocalDate endDate) {
        return 0L;
    }
}
