/*
Author: QuanTuanHuy
Description: Part of Serp Project
*/

package serp.project.mailservice.core.port.store;

import serp.project.mailservice.core.domain.entity.EmailStatsEntity;
import serp.project.mailservice.core.domain.enums.EmailProvider;
import serp.project.mailservice.core.domain.enums.EmailStatus;
import serp.project.mailservice.core.domain.enums.EmailType;

import java.time.LocalDate;
import java.util.List;
import java.util.Optional;

public interface IEmailStatsPort {
    EmailStatsEntity save(EmailStatsEntity stats);

    Optional<EmailStatsEntity> findById(Long id);

    Optional<EmailStatsEntity> findByKey(Long tenantId, EmailProvider provider, EmailType emailType,
            EmailStatus status, LocalDate statDate, Integer statHour);

    List<EmailStatsEntity> findByTenantIdAndDateRange(Long tenantId, LocalDate fromDate, LocalDate toDate);

    List<EmailStatsEntity> findByProvider(EmailProvider provider, LocalDate fromDate, LocalDate toDate);

    List<EmailStatsEntity> findByFilters(Long tenantId, EmailProvider provider, EmailType emailType,
            EmailStatus status, LocalDate fromDate, LocalDate toDate);

    void incrementStats(Long tenantId, EmailProvider provider, EmailType emailType,
            EmailStatus status, LocalDate statDate, Integer statHour,
            Long responseTimeMs, Long sizeBytes, Integer attachmentCount);
}
