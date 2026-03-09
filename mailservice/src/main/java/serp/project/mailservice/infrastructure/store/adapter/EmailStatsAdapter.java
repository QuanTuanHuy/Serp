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

        EmailStatsEntity stats;
        if (statsOpt.isPresent()) {
            stats = statsOpt.get();
        } else {
            stats = EmailStatsEntity.createNew(tenantId, provider, emailType, status, statDate, statHour);
        }

        stats.incrementForStatus(status, responseTimeMs, sizeBytes, attachmentCount);
        save(stats);
    }

    @Override
    @Transactional
    public long deleteByStatDateBefore(LocalDate cutoffDate) {
        return emailStatsRepository.deleteByStatDateBefore(cutoffDate);
    }
}
