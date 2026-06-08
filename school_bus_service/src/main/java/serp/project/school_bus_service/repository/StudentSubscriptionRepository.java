package serp.project.school_bus_service.repository;

import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import serp.project.school_bus_service.enums.SubscriptionStatus;
import serp.project.school_bus_service.enums.TripOption;
import serp.project.school_bus_service.entity.StudentSubscriptionEntity;
import serp.project.school_bus_service.shared.base.BaseRepository;

import java.time.LocalDate;
import java.util.List;

public interface StudentSubscriptionRepository extends BaseRepository<StudentSubscriptionEntity, Long> {

    List<StudentSubscriptionEntity> findByStudentIdAndTenantIdAndIsDeletedFalse(Long studentId, Long tenantId);

    @Query("""
            SELECT s FROM StudentSubscriptionEntity s
             JOIN FETCH s.student
             LEFT JOIN FETCH s.pickupPoint
             LEFT JOIN FETCH s.dropoffPoint
             WHERE s.school.id = :schoolId
               AND s.tenantId = :tenantId
               AND s.isDeleted = false
            """)
    List<StudentSubscriptionEntity> findAllBySchoolIdAndTenantId(
            @Param("schoolId") Long schoolId,
            @Param("tenantId") Long tenantId);

    List<StudentSubscriptionEntity> findBySchoolIdAndTenantIdAndStatusAndIsDeletedFalse(
            Long schoolId,
            Long tenantId,
            SubscriptionStatus status);

    List<StudentSubscriptionEntity> findByStudentIdAndTripOptionAndTenantIdAndStatusAndIsDeletedFalse(
            Long studentId,
            TripOption tripOption,
            Long tenantId,
            SubscriptionStatus status);

    long countByTenantIdAndStatusAndIsDeletedFalse(Long tenantId, SubscriptionStatus status);

    /**
     * Single query that pushes most eligibility filters into the DB:
     * - status = ACTIVE, not deleted
     * - effectiveFrom <= serviceDate AND (effectiveTo IS NULL OR effectiveTo >= serviceDate)
     * - day-of-week flag is true for the given dayIndex (1=MON..7=SUN)
     * - tripOption IN the allowed set for the route direction
     * - relevant point (pickup for OUTBOUND, dropoff for RETURN) is not null
     */
    @Query("""
            SELECT s FROM StudentSubscriptionEntity s
             JOIN FETCH s.student st
             LEFT JOIN FETCH s.pickupPoint
             LEFT JOIN FETCH s.dropoffPoint
             WHERE s.school.id = :schoolId
               AND s.tenantId = :tenantId
               AND s.isDeleted = false
               AND s.status = serp.project.school_bus_service.enums.SubscriptionStatus.ACTIVE
               AND s.effectiveFrom <= :serviceDate
               AND (s.effectiveTo IS NULL OR s.effectiveTo >= :serviceDate)
               AND st.isDeleted = false
               AND st.isActive = true
               AND (:dayIndex = 1 AND s.monday = true
                 OR :dayIndex = 2 AND s.tuesday = true
                 OR :dayIndex = 3 AND s.wednesday = true
                 OR :dayIndex = 4 AND s.thursday = true
                 OR :dayIndex = 5 AND s.friday = true
                 OR :dayIndex = 6 AND s.saturday = true
                 OR :dayIndex = 7 AND s.sunday = true)
               AND s.tripOption IN :allowedTripOptions
               AND ((:isOutbound = true AND s.pickupPoint IS NOT NULL)
                 OR (:isOutbound = false AND s.dropoffPoint IS NOT NULL))
            """)
    List<StudentSubscriptionEntity> findEligibleForPlanning(
            @Param("schoolId") Long schoolId,
            @Param("tenantId") Long tenantId,
            @Param("serviceDate") LocalDate serviceDate,
            @Param("dayIndex") int dayIndex,
            @Param("allowedTripOptions") List<TripOption> allowedTripOptions,
            @Param("isOutbound") boolean isOutbound);

    /**
     * Checks whether an overlapping ACTIVE subscription exists for the same student + tripOption.
     * The overlap condition: effectiveFrom <= otherEnd AND otherFrom <= effectiveTo (with NULL = infinity).
     * Excludes a specific subscription ID when updating.
     */
    @Query("""
            SELECT CASE WHEN COUNT(s) > 0 THEN true ELSE false END
              FROM StudentSubscriptionEntity s
             WHERE s.student.id = :studentId
               AND s.tripOption = :tripOption
               AND s.tenantId = :tenantId
               AND s.isDeleted = false
               AND s.status = serp.project.school_bus_service.enums.SubscriptionStatus.ACTIVE
               AND (:excludingId IS NULL OR s.id <> :excludingId)
               AND s.effectiveFrom <= COALESCE(:effectiveTo, CAST('9999-12-31' AS LocalDate))
               AND COALESCE(s.effectiveTo, CAST('9999-12-31' AS LocalDate)) >= :effectiveFrom
            """)
    boolean existsOverlappingActiveSubscription(
            @Param("studentId") Long studentId,
            @Param("tripOption") TripOption tripOption,
            @Param("effectiveFrom") LocalDate effectiveFrom,
            @Param("effectiveTo") LocalDate effectiveTo,
            @Param("tenantId") Long tenantId,
            @Param("excludingId") Long excludingId);
}

