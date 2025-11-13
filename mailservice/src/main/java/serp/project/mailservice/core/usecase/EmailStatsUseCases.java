/*
Author: QuanTuanHuy
Description: Part of Serp Project
*/

package serp.project.mailservice.core.usecase;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import serp.project.mailservice.core.domain.dto.request.EmailStatsFilterRequest;
import serp.project.mailservice.core.domain.dto.response.EmailStatsResponse;
import serp.project.mailservice.core.domain.entity.EmailStatsEntity;
import serp.project.mailservice.core.domain.enums.EmailProvider;
import serp.project.mailservice.core.domain.mapper.EmailStatsMapper;
import serp.project.mailservice.core.port.store.IEmailStatsPort;

import java.time.LocalDate;
import java.util.List;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
@Slf4j
public class EmailStatsUseCases {

    private final IEmailStatsPort emailStatsPort;

    @Transactional(readOnly = true)
    public List<EmailStatsResponse> getStatsByTenant(Long tenantId, LocalDate startDate, LocalDate endDate) {
        log.debug("Getting stats for tenant: {}, from: {} to: {}", tenantId, startDate, endDate);

        List<EmailStatsEntity> stats = emailStatsPort.findByTenantIdAndDateRange(tenantId, startDate, endDate);
        return stats.stream()
                .map(EmailStatsMapper::toResponse)
                .collect(Collectors.toList());
    }

    @Transactional(readOnly = true)
    public List<EmailStatsResponse> getStatsByProvider(EmailProvider provider, LocalDate startDate, LocalDate endDate) {
        log.debug("Getting stats for provider: {}, from: {} to: {}", provider, startDate, endDate);

        List<EmailStatsEntity> stats = emailStatsPort.findByProvider(provider, startDate, endDate);
        return stats.stream()
                .map(EmailStatsMapper::toResponse)
                .collect(Collectors.toList());
    }

    @Transactional(readOnly = true)
    public List<EmailStatsResponse> getStatsByFilters(EmailStatsFilterRequest filter) {
        log.debug("Getting stats with filters: {}", filter);

        List<EmailStatsEntity> stats = emailStatsPort.findByFilters(
                filter.getTenantId(),
                filter.getProvider(),
                filter.getEmailType(),
                filter.getStatus(),
                filter.getFromDate(),
                filter.getToDate()
        );

        return stats.stream()
                .map(EmailStatsMapper::toResponse)
                .collect(Collectors.toList());
    }

    @Transactional(readOnly = true)
    public EmailStatsResponse getAggregatedStats(Long tenantId, LocalDate startDate, LocalDate endDate) {
        log.debug("Getting aggregated stats for tenant: {}", tenantId);

        List<EmailStatsEntity> stats = emailStatsPort.findByTenantIdAndDateRange(tenantId, startDate, endDate);

        if (stats.isEmpty()) {
            return EmailStatsResponse.builder()
                    .tenantId(tenantId)
                    .totalCount(0L)
                    .successCount(0L)
                    .failedCount(0L)
                    .retryCount(0L)
                    .build();
        }

        long totalCount = stats.stream().mapToLong(EmailStatsEntity::getTotalCount).sum();
        long successCount = stats.stream().mapToLong(EmailStatsEntity::getSuccessCount).sum();
        long failedCount = stats.stream().mapToLong(EmailStatsEntity::getFailedCount).sum();
        long retryCount = stats.stream().mapToLong(EmailStatsEntity::getRetryCount).sum();

        long totalResponseTime = stats.stream()
                .mapToLong(s -> s.getAvgResponseTimeMs() * s.getTotalCount())
                .sum();
        long avgResponseTimeMs = totalCount > 0 ? totalResponseTime / totalCount : 0L;

        long minResponseTimeMs = stats.stream()
                .mapToLong(EmailStatsEntity::getMinResponseTimeMs)
                .filter(time -> time > 0)
                .min()
                .orElse(0L);

        long maxResponseTimeMs = stats.stream()
                .mapToLong(EmailStatsEntity::getMaxResponseTimeMs)
                .max()
                .orElse(0L);

        long totalSizeBytes = stats.stream().mapToLong(EmailStatsEntity::getTotalSizeBytes).sum();
        long attachmentCount = stats.stream().mapToLong(EmailStatsEntity::getAttachmentCount).sum();

        return EmailStatsResponse.builder()
                .tenantId(tenantId)
                .totalCount(totalCount)
                .successCount(successCount)
                .failedCount(failedCount)
                .retryCount(retryCount)
                .avgResponseTimeMs(avgResponseTimeMs)
                .minResponseTimeMs(minResponseTimeMs)
                .maxResponseTimeMs(maxResponseTimeMs)
                .totalSizeBytes(totalSizeBytes)
                .attachmentCount(attachmentCount)
                .build();
    }
}
