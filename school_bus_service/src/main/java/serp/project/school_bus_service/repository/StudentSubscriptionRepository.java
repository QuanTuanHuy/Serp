package serp.project.school_bus_service.repository;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import serp.project.school_bus_service.dto.response.StudentSubscriptionResponse;
import serp.project.school_bus_service.enums.SubscriptionStatus;
import serp.project.school_bus_service.enums.TripOption;
import serp.project.school_bus_service.entity.StudentSubscriptionEntity;
import serp.project.school_bus_service.repository.projection.GreedyFillCandidateProjection;
import serp.project.school_bus_service.repository.projection.SubscriptionSummaryProjection;
import serp.project.school_bus_service.shared.base.BaseRepository;

import java.time.LocalDate;
import java.util.List;

public interface StudentSubscriptionRepository extends BaseRepository<StudentSubscriptionEntity, Long> {

    @Query(value = """
            SELECT new serp.project.school_bus_service.dto.response.StudentSubscriptionResponse(
                sub.id, sub.tenantId, sub.isActive, sub.isDeleted, sub.createdAt, sub.createdBy, sub.updatedAt, sub.updatedBy,
                sub.subscriptionCode,
                student.id, student.fullName, student.studentCode, parent.fullName,
                school.id, school.name, school.code,
                pickup.id, pickup.name, pickup.code,
                dropoff.id, dropoff.name, dropoff.code,
                sub.tripOption,
                sub.monday, sub.tuesday, sub.wednesday, sub.thursday, sub.friday, sub.saturday, sub.sunday,
                sub.effectiveFrom, sub.effectiveTo, sub.status,
                source.id, source.requestCode
            )
            FROM StudentSubscriptionEntity sub
            JOIN sub.student student
            JOIN student.school school
            JOIN student.parentProfile parent
            LEFT JOIN sub.pickupPoint pickup
            LEFT JOIN sub.dropoffPoint dropoff
            LEFT JOIN sub.sourceRequest source
            WHERE sub.tenantId = :tenantId
              AND sub.isDeleted = false
              AND (:parentProfileId IS NULL OR parent.id = :parentProfileId)
              AND (:schoolId IS NULL OR school.id = :schoolId)
              AND (:studentId IS NULL OR student.id = :studentId)
              AND (:status IS NULL OR sub.status = :status)
              AND (:tripOption IS NULL OR sub.tripOption = :tripOption)
              AND (
                  :keywordPattern IS NULL
                  OR LOWER(sub.subscriptionCode) LIKE :keywordPattern
                  OR LOWER(student.fullName) LIKE :keywordPattern
                  OR LOWER(school.name) LIKE :keywordPattern
                  OR LOWER(STR(sub.status)) LIKE :keywordPattern
                  OR LOWER(STR(sub.tripOption)) LIKE :keywordPattern
              )
            """,
            countQuery = """
            SELECT COUNT(sub)
            FROM StudentSubscriptionEntity sub
            JOIN sub.student student
            JOIN student.school school
            JOIN student.parentProfile parent
            WHERE sub.tenantId = :tenantId
              AND sub.isDeleted = false
              AND (:parentProfileId IS NULL OR parent.id = :parentProfileId)
              AND (:schoolId IS NULL OR school.id = :schoolId)
              AND (:studentId IS NULL OR student.id = :studentId)
              AND (:status IS NULL OR sub.status = :status)
              AND (:tripOption IS NULL OR sub.tripOption = :tripOption)
              AND (
                  :keywordPattern IS NULL
                  OR LOWER(sub.subscriptionCode) LIKE :keywordPattern
                  OR LOWER(student.fullName) LIKE :keywordPattern
                  OR LOWER(school.name) LIKE :keywordPattern
                  OR LOWER(STR(sub.status)) LIKE :keywordPattern
                  OR LOWER(STR(sub.tripOption)) LIKE :keywordPattern
              )
            """)
    Page<StudentSubscriptionResponse> findSubscriptionListItems(
            @Param("tenantId") Long tenantId,
            @Param("parentProfileId") Long parentProfileId,
            @Param("schoolId") Long schoolId,
            @Param("studentId") Long studentId,
            @Param("status") SubscriptionStatus status,
            @Param("tripOption") TripOption tripOption,
            @Param("keywordPattern") String keywordPattern,
            Pageable pageable);

    List<StudentSubscriptionEntity> findByStudentIdAndTenantIdAndIsDeletedFalse(Long studentId, Long tenantId);

    @Query("""
            SELECT s FROM StudentSubscriptionEntity s
             JOIN FETCH s.student
             LEFT JOIN FETCH s.pickupPoint
             LEFT JOIN FETCH s.dropoffPoint
             WHERE s.student.school.id = :schoolId
               AND s.tenantId = :tenantId
               AND s.isDeleted = false
            """)
    List<StudentSubscriptionEntity> findAllBySchoolIdAndTenantId(
            @Param("schoolId") Long schoolId,
            @Param("tenantId") Long tenantId);

    @Query("""
            SELECT s FROM StudentSubscriptionEntity s
             JOIN FETCH s.student st
             WHERE st.school.id = :schoolId
               AND s.tenantId = :tenantId
               AND s.status = :status
               AND s.isDeleted = false
            """)
    List<StudentSubscriptionEntity> findBySchoolIdAndTenantIdAndStatusAndIsDeletedFalse(
            @Param("schoolId") Long schoolId,
            @Param("tenantId") Long tenantId,
            @Param("status") SubscriptionStatus status);

    List<StudentSubscriptionEntity> findByStudentIdAndTripOptionAndTenantIdAndStatusAndIsDeletedFalse(
            Long studentId,
            TripOption tripOption,
            Long tenantId,
            SubscriptionStatus status);

    long countByTenantIdAndStatusAndIsDeletedFalse(Long tenantId, SubscriptionStatus status);

    @Query(value = """
            SELECT
                COUNT(sub.id) AS totalSubscriptions,
                COALESCE(SUM(CASE WHEN sub.status = 'ACTIVE' THEN 1 ELSE 0 END), 0) AS activeSubscriptions,
                COALESCE(SUM(CASE WHEN sub.status <> 'ACTIVE' THEN 1 ELSE 0 END), 0) AS inactiveSubscriptions
              FROM public.school_bus_student_subscription sub
              JOIN public.school_bus_student student
                ON student.id = sub.student_id
               AND student.is_deleted = false
             WHERE sub.tenant_id = :tenantId
               AND sub.is_deleted = false
               AND (CAST(:parentProfileId AS bigint) IS NULL OR student.parent_profile_id = :parentProfileId)
            """, nativeQuery = true)
    SubscriptionSummaryProjection getSubscriptionSummary(
            @Param("tenantId") Long tenantId,
            @Param("parentProfileId") Long parentProfileId);

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
             WHERE st.school.id = :schoolId
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
     * Projection for route greedy-fill. It keeps the candidate read path small and
     * avoids loading full subscription/student/point entity graphs.
     */
    @Query(value = """
            SELECT sub.id AS subscriptionId,
                   st.id AS studentId,
                   point.id AS pointId,
                   point.name AS pointName,
                   point.latitude AS latitude,
                   point.longitude AS longitude,
                   point.usage_type AS usageType,
                   point.is_active AS pointActive,
                   point.is_deleted AS pointDeleted,
                   CASE WHEN spp.id IS NULL THEN false ELSE true END AS linkedToSchool
              FROM public.school_bus_student_subscription sub
              JOIN public.school_bus_student st ON st.id = sub.student_id
              LEFT JOIN public.school_bus_pickup_point point
                     ON point.id = CASE
                         WHEN :isOutbound = true THEN sub.pickup_point_id
                         ELSE sub.dropoff_point_id
                     END
              LEFT JOIN public.school_bus_school_pickup_point spp
                     ON spp.school_id = st.school_id
                    AND spp.pickup_point_id = point.id
                    AND spp.tenant_id = :tenantId
                    AND spp.is_deleted = false
                    AND spp.is_active = true
             WHERE st.school_id = :schoolId
               AND sub.tenant_id = :tenantId
               AND sub.is_deleted = false
               AND sub.status = 'ACTIVE'
               AND sub.effective_from <= :serviceDate
               AND (sub.effective_to IS NULL OR sub.effective_to >= :serviceDate)
               AND st.is_deleted = false
               AND st.is_active = true
               AND (:dayIndex = 1 AND sub.is_monday = true
                 OR :dayIndex = 2 AND sub.is_tuesday = true
                 OR :dayIndex = 3 AND sub.is_wednesday = true
                 OR :dayIndex = 4 AND sub.is_thursday = true
                 OR :dayIndex = 5 AND sub.is_friday = true
                 OR :dayIndex = 6 AND sub.is_saturday = true
                 OR :dayIndex = 7 AND sub.is_sunday = true)
               AND sub.trip_option IN (:allowedTripOptions)
               AND ((:isOutbound = true AND sub.pickup_point_id IS NOT NULL)
                 OR (:isOutbound = false AND sub.dropoff_point_id IS NOT NULL))
             ORDER BY sub.id ASC
            """, nativeQuery = true)
    List<GreedyFillCandidateProjection> findGreedyFillCandidates(
            @Param("schoolId") Long schoolId,
            @Param("tenantId") Long tenantId,
            @Param("serviceDate") LocalDate serviceDate,
            @Param("dayIndex") int dayIndex,
            @Param("allowedTripOptions") List<String> allowedTripOptions,
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

