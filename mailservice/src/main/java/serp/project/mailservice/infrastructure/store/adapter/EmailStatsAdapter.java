/*
Author: QuanTuanHuy
Description: Part of Serp Project
*/

package serp.project.mailservice.infrastructure.store.adapter;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;
import serp.project.mailservice.core.domain.entity.EmailStatsEntity;
import serp.project.mailservice.core.domain.enums.EmailProvider;
import serp.project.mailservice.core.domain.enums.EmailStatus;
import serp.project.mailservice.core.domain.enums.EmailType;
import serp.project.mailservice.core.port.store.IEmailStatsPort;
import serp.project.mailservice.infrastructure.store.mapper.EmailStatsModelMapper;
import serp.project.mailservice.infrastructure.store.repository.EmailStatsRepository;

import java.time.LocalDate;
import java.util.List;
import java.util.Optional;

@Component
@RequiredArgsConstructor
@Slf4j
public class EmailStatsAdapter implements IEmailStatsPort {
    private final EmailStatsRepository emailStatsRepository;

    @Override
    public EmailStatsEntity save(EmailStatsEntity stats) {
        var model = EmailStatsModelMapper.toModel(stats);
        var savedModel = emailStatsRepository.save(model);
        return EmailStatsModelMapper.toEntity(savedModel);
    }

    @Override
    public Optional<EmailStatsEntity> findById(Long id) {
        return emailStatsRepository.findById(id)
                .map(EmailStatsModelMapper::toEntity);
    }

    @Override
    public Optional<EmailStatsEntity> findByKey(Long tenantId, EmailProvider provider, EmailType emailType,
                                                 EmailStatus status, LocalDate statDate, Integer statHour) {
        return emailStatsRepository.findByTenantIdAndProviderAndEmailTypeAndStatusAndStatDateAndStatHour(
                        tenantId, provider, emailType, status, statDate, statHour)
                .map(EmailStatsModelMapper::toEntity);
    }

    @Override
    public List<EmailStatsEntity> findByTenantIdAndDateRange(Long tenantId, LocalDate fromDate, LocalDate toDate) {
        var models = emailStatsRepository.findByTenantIdAndStatDateBetween(tenantId, fromDate, toDate);
        return EmailStatsModelMapper.toEntities(models);
    }

    @Override
    public List<EmailStatsEntity> findByProvider(EmailProvider provider, LocalDate fromDate, LocalDate toDate) {
        var models = emailStatsRepository.findByProviderAndStatDateBetween(provider, fromDate, toDate);
        return EmailStatsModelMapper.toEntities(models);
    }

    @Override
    public List<EmailStatsEntity> findByFilters(Long tenantId, EmailProvider provider, EmailType emailType,
                                                 EmailStatus status, LocalDate fromDate, LocalDate toDate) {
        var models = emailStatsRepository.findByFilters(tenantId, provider, emailType, status, fromDate, toDate);
        return EmailStatsModelMapper.toEntities(models);
    }

    @Override
    @Transactional
    public void incrementStats(Long tenantId, EmailProvider provider, EmailType emailType,
                               EmailStatus status, LocalDate statDate, Integer statHour,
                               Long responseTimeMs, Long sizeBytes, Integer attachmentCount) {
        var statsOpt = findByKey(tenantId, provider, emailType, status, statDate, statHour);

        if (statsOpt.isPresent()) {
            var stats = statsOpt.get();
            stats.setTotalCount(stats.getTotalCount() + 1);

            if (status == EmailStatus.SENT) {
                stats.setSuccessCount(stats.getSuccessCount() + 1);
            } else if (status == EmailStatus.FAILED) {
                stats.setFailedCount(stats.getFailedCount() + 1);
            } else if (status == EmailStatus.RETRY) {
                stats.setRetryCount(stats.getRetryCount() + 1);
            }

            if (responseTimeMs != null) {
                long totalTime = (stats.getAvgResponseTimeMs() != null ? stats.getAvgResponseTimeMs() : 0)
                        * (stats.getTotalCount() - 1) + responseTimeMs;
                stats.setAvgResponseTimeMs(totalTime / stats.getTotalCount());

                if (stats.getMinResponseTimeMs() == null || responseTimeMs < stats.getMinResponseTimeMs()) {
                    stats.setMinResponseTimeMs(responseTimeMs);
                }
                if (stats.getMaxResponseTimeMs() == null || responseTimeMs > stats.getMaxResponseTimeMs()) {
                    stats.setMaxResponseTimeMs(responseTimeMs);
                }
            }

            if (sizeBytes != null) {
                stats.setTotalSizeBytes((stats.getTotalSizeBytes() != null ? stats.getTotalSizeBytes() : 0) + sizeBytes);
            }

            if (attachmentCount != null && attachmentCount > 0) {
                stats.setAttachmentCount((stats.getAttachmentCount() != null ? stats.getAttachmentCount() : 0) + attachmentCount);
            }

            save(stats);
        } else {
            var newStats = EmailStatsEntity.builder()
                    .tenantId(tenantId)
                    .provider(provider)
                    .emailType(emailType)
                    .status(status)
                    .statDate(statDate)
                    .statHour(statHour)
                    .totalCount(1L)
                    .successCount(status == EmailStatus.SENT ? 1L : 0L)
                    .failedCount(status == EmailStatus.FAILED ? 1L : 0L)
                    .retryCount(status == EmailStatus.RETRY ? 1L : 0L)
                    .avgResponseTimeMs(responseTimeMs)
                    .minResponseTimeMs(responseTimeMs)
                    .maxResponseTimeMs(responseTimeMs)
                    .totalSizeBytes(sizeBytes != null ? sizeBytes : 0L)
                    .attachmentCount(attachmentCount != null ? (long) attachmentCount : 0L)
                    .build();
            save(newStats);
        }
    }
}
