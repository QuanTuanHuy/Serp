/*
Author: QuanTuanHuy
Description: Part of Serp Project
*/

package serp.project.mailservice.infrastructure.store.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;
import serp.project.mailservice.core.domain.enums.EmailProvider;
import serp.project.mailservice.core.domain.enums.EmailStatus;
import serp.project.mailservice.core.domain.enums.EmailType;
import serp.project.mailservice.infrastructure.store.model.EmailStatsModel;

import java.time.LocalDate;
import java.util.List;
import java.util.Optional;

@Repository
public interface EmailStatsRepository extends JpaRepository<EmailStatsModel, Long> {
    Optional<EmailStatsModel> findByTenantIdAndProviderAndEmailTypeAndStatusAndStatDateAndStatHour(
            Long tenantId, EmailProvider provider, EmailType emailType,
            EmailStatus status, LocalDate statDate, Integer statHour);

    List<EmailStatsModel> findByTenantIdAndStatDateBetween(Long tenantId, LocalDate fromDate, LocalDate toDate);

    List<EmailStatsModel> findByProviderAndStatDateBetween(EmailProvider provider, LocalDate fromDate, LocalDate toDate);

    @Query("SELECT e FROM EmailStatsModel e WHERE " +
           "(:tenantId IS NULL OR e.tenantId = :tenantId) AND " +
           "(:provider IS NULL OR e.provider = :provider) AND " +
           "(:emailType IS NULL OR e.emailType = :emailType) AND " +
           "(:status IS NULL OR e.status = :status) AND " +
           "e.statDate BETWEEN :fromDate AND :toDate")
    List<EmailStatsModel> findByFilters(@Param("tenantId") Long tenantId,
                                        @Param("provider") EmailProvider provider,
                                        @Param("emailType") EmailType emailType,
                                        @Param("status") EmailStatus status,
                                        @Param("fromDate") LocalDate fromDate,
                                        @Param("toDate") LocalDate toDate);

    long deleteByStatDateBefore(LocalDate cutoffDate);
}
