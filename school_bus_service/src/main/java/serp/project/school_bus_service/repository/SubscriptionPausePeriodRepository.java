package serp.project.school_bus_service.repository;

import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import serp.project.school_bus_service.enums.PausePeriodStatus;
import serp.project.school_bus_service.entity.SubscriptionPausePeriodEntity;
import serp.project.school_bus_service.shared.base.BaseRepository;

import java.time.LocalDate;
import java.util.List;

public interface SubscriptionPausePeriodRepository
        extends BaseRepository<SubscriptionPausePeriodEntity, Long> {

    List<SubscriptionPausePeriodEntity> findBySubscriptionIdAndTenantIdAndIsDeletedFalseOrderByPauseFromDesc(
            Long subscriptionId, Long tenantId);

    @Query("""
            select entity
              from SubscriptionPausePeriodEntity entity
             where entity.subscription.id = :subscriptionId
               and entity.tenantId = :tenantId
               and entity.isDeleted = false
               and entity.status in (:statuses)
             order by entity.pauseFrom desc
            """)
    List<SubscriptionPausePeriodEntity> findBySubscriptionIdAndStatusIn(
            @Param("subscriptionId") Long subscriptionId,
            @Param("tenantId") Long tenantId,
            @Param("statuses") List<PausePeriodStatus> statuses);

    /**
     * Batch query: returns subscription IDs (from the given set) that have an active/scheduled
     * pause period covering the specified date. Eliminates N+1 per-subscription lookups.
     */
    @Query("""
            SELECT DISTINCT p.subscription.id
              FROM SubscriptionPausePeriodEntity p
             WHERE p.subscription.id IN :subscriptionIds
               AND p.tenantId = :tenantId
               AND p.isDeleted = false
               AND p.status IN ('ACTIVE', 'SCHEDULED')
               AND p.pauseFrom <= :serviceDate
               AND (p.pauseTo IS NULL OR p.pauseTo >= :serviceDate)
            """)
    List<Long> findPausedSubscriptionIds(
            @Param("subscriptionIds") List<Long> subscriptionIds,
            @Param("tenantId") Long tenantId,
            @Param("serviceDate") LocalDate serviceDate);
}
