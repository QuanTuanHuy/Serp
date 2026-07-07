package serp.project.school_bus_service.repository;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import serp.project.school_bus_service.dto.response.RoutePlanListItemResponse;
import serp.project.school_bus_service.repository.projection.RouteDispatchSummaryProjection;
import serp.project.school_bus_service.repository.projection.RouteReadinessCountProjection;
import serp.project.school_bus_service.shared.base.BaseRepository;
import serp.project.school_bus_service.entity.RoutePlanEntity;
import serp.project.school_bus_service.enums.RouteDirection;
import serp.project.school_bus_service.enums.RouteStatus;

import java.time.LocalDate;
import java.util.List;


public interface RoutePlanRepository extends BaseRepository<RoutePlanEntity, Long> {

    @Query(value = """
        SELECT new serp.project.school_bus_service.dto.response.RoutePlanListItemResponse(
            r.id,
            r.tenantId,
            r.isActive,
            r.isDeleted,
            r.createdAt,
            r.createdBy,
            r.updatedAt,
            r.updatedBy,
            ps.school.id,
            ps.school.name,
            ps.routeDirection,
            r.routeCode,
            r.routeName,
            ps.serviceDate,
            r.status,
            r.plannedDistanceKm,
            r.plannedDurationMin,
            r.plannedStudentCount,
            r.startedAt,
            r.completedAt,
            r.requiredCapacity,
            ps.id
        )
        FROM RoutePlanEntity r
        JOIN r.planningSession ps
        WHERE r.tenantId = :tenantId
          AND r.isDeleted = false
          AND (
              :keywordPattern IS NULL
              OR LOWER(r.routeCode) LIKE :keywordPattern
              OR LOWER(r.routeName) LIKE :keywordPattern
              OR LOWER(ps.school.name) LIKE :keywordPattern
              OR LOWER(STR(r.status)) LIKE :keywordPattern
              OR LOWER(STR(ps.routeDirection)) LIKE :keywordPattern
          )
        """,
        countQuery = """
        SELECT COUNT(r)
        FROM RoutePlanEntity r
        JOIN r.planningSession ps
        WHERE r.tenantId = :tenantId
          AND r.isDeleted = false
          AND (
              :keywordPattern IS NULL
              OR LOWER(r.routeCode) LIKE :keywordPattern
              OR LOWER(r.routeName) LIKE :keywordPattern
              OR LOWER(ps.school.name) LIKE :keywordPattern
              OR LOWER(STR(r.status)) LIKE :keywordPattern
              OR LOWER(STR(ps.routeDirection)) LIKE :keywordPattern
          )
        """)
    Page<RoutePlanListItemResponse> findRouteListItems(
            @Param("tenantId") Long tenantId,
            @Param("keywordPattern") String keywordPattern,
            Pageable pageable);

    @Query("""
        SELECT r FROM RoutePlanEntity r
        WHERE r.tenantId = :tenantId AND r.isDeleted = false
        ORDER BY r.planningSession.serviceDate DESC, r.id DESC
    """)
    List<RoutePlanEntity> findByTenantIdAndIsDeletedFalseOrderByServiceDateDescIdDesc(@Param("tenantId") Long tenantId);

    long countByTenantIdAndStatusAndIsDeletedFalse(Long tenantId, RouteStatus status);

    @Query(value = """
            SELECT
                COUNT(route.id) AS totalRoutes,
                COALESCE(SUM(CASE
                    WHEN route.status IN ('DRAFT', 'GENERATED', 'REVIEWING', 'PUBLISHED', 'ASSIGNED')
                    THEN 1 ELSE 0 END), 0) AS plannedRoutes,
                COALESCE(SUM(CASE WHEN route.status = 'TRIP_CREATED' THEN 1 ELSE 0 END), 0) AS tripCreatedRoutes
              FROM public.school_bus_route_plan route
             WHERE route.tenant_id = :tenantId
               AND route.is_deleted = false
            """, nativeQuery = true)
    RouteDispatchSummaryProjection getDispatchSummary(@Param("tenantId") Long tenantId);

    @Query("SELECT r FROM RoutePlanEntity r WHERE r.planningSession.id = :sessionId AND r.tenantId = :tenantId AND r.isDeleted = false ORDER BY r.id ASC")
    List<RoutePlanEntity> findByPlanningSessionIdAndTenantId(@Param("sessionId") Long sessionId,
                                                              @Param("tenantId") Long tenantId);

    @Query("""
        SELECT CASE WHEN COUNT(r) > 0 THEN true ELSE false END FROM RoutePlanEntity r
        WHERE r.tenantId = :tenantId
          AND r.isDeleted = false
          AND r.isActive = true
          AND r.planningSession.id = :sessionId
          AND EXISTS (
              SELECT a FROM RouteAssignmentEntity a
              WHERE a.route.id = r.id
                AND a.bus.id = :busId
                AND a.tenantId = :tenantId
                AND a.isDeleted = false
          )
          AND (:excludeRouteId IS NULL OR r.id <> :excludeRouteId)
    """)
    boolean existsActiveRouteUsingSelectedBusInSession(
        @Param("tenantId") Long tenantId,
        @Param("sessionId") Long sessionId,
        @Param("busId") Long busId,
        @Param("excludeRouteId") Long excludeRouteId
    );

    @Query("""
        SELECT r FROM RoutePlanEntity r
        WHERE r.tenantId = :tenantId AND r.isDeleted = false
          AND r.planningSession.serviceDate = :serviceDate
          AND r.status IN (serp.project.school_bus_service.enums.RouteStatus.PUBLISHED,
                           serp.project.school_bus_service.enums.RouteStatus.ASSIGNED,
                           serp.project.school_bus_service.enums.RouteStatus.TRIP_CREATED)
          AND (:schoolId IS NULL OR r.planningSession.school.id = :schoolId)
          AND (:direction IS NULL OR r.planningSession.routeDirection = :direction)
    """)
    List<RoutePlanEntity> findOperationalRoutes(
        @Param("tenantId") Long tenantId,
        @Param("serviceDate") LocalDate serviceDate,
        @Param("schoolId") Long schoolId,
        @Param("direction") RouteDirection direction
    );

    @Query("""
        SELECT r FROM RoutePlanEntity r
        WHERE r.tenantId = :tenantId AND r.isDeleted = false
          AND r.planningSession.serviceDate = :serviceDate
          AND EXISTS (
              SELECT rps FROM RoutePlanStudentEntity rps
              WHERE rps.route.id = r.id
                AND rps.subscription.student.parentProfile.id = :parentProfileId
                AND rps.isDeleted = false
          )
    """)
    List<RoutePlanEntity> findRoutePlansByParentAndDate(
        @Param("tenantId") Long tenantId,
        @Param("parentProfileId") Long parentProfileId,
        @Param("serviceDate") LocalDate serviceDate
    );

    @Query("""
        SELECT r FROM RoutePlanEntity r
        WHERE r.tenantId = :tenantId AND r.isDeleted = false
          AND r.planningSession.serviceDate = :serviceDate
          AND EXISTS (
              SELECT a FROM RouteAssignmentEntity a
              WHERE a.route.id = r.id
                AND a.driver.id = :driverId
                AND a.isDeleted = false
          )
    """)
    List<RoutePlanEntity> findRoutePlansByDriverAndDate(
        @Param("tenantId") Long tenantId,
        @Param("driverId") Long driverId,
        @Param("serviceDate") LocalDate serviceDate
    );

    @Query("""
        SELECT r FROM RoutePlanEntity r
        WHERE r.tenantId = :tenantId AND r.isDeleted = false
          AND r.planningSession.serviceDate = :serviceDate
          AND EXISTS (
              SELECT a FROM RouteAssignmentEntity a
              WHERE a.route.id = r.id
                AND a.attendant.id = :attendantId
                AND a.isDeleted = false
          )
    """)
    List<RoutePlanEntity> findRoutePlansByAttendantAndDate(
        @Param("tenantId") Long tenantId,
        @Param("attendantId") Long attendantId,
        @Param("serviceDate") LocalDate serviceDate
    );

    @Query("""
        SELECT r FROM RoutePlanEntity r
        WHERE r.tenantId = :tenantId
          AND r.isDeleted = false
          AND r.planningSession.serviceDate = :serviceDate
          AND r.status IN (serp.project.school_bus_service.enums.RouteStatus.PUBLISHED,
                           serp.project.school_bus_service.enums.RouteStatus.ASSIGNED,
                           serp.project.school_bus_service.enums.RouteStatus.TRIP_CREATED)
          AND (:schoolId IS NULL OR r.planningSession.school.id = :schoolId)
          AND (:direction IS NULL OR r.planningSession.routeDirection = :direction)
          AND (
              :tenantWide = true
              OR (:driverProfileId IS NOT NULL AND (
                  EXISTS (
                      SELECT a FROM RouteAssignmentEntity a
                      WHERE a.route.id = r.id
                        AND a.driver.id = :driverProfileId
                        AND a.tenantId = :tenantId
                        AND a.isDeleted = false
                        AND a.status IN (serp.project.school_bus_service.enums.RouteAssignmentStatus.ASSIGNED,
                                         serp.project.school_bus_service.enums.RouteAssignmentStatus.CONFIRMED)
                  )
                  OR EXISTS (
                      SELECT t FROM TripExecutionEntity t
                      WHERE t.route.id = r.id
                        AND EXISTS (
                            SELECT a2 FROM RouteAssignmentEntity a2
                            WHERE a2.route.id = t.route.id
                              AND a2.driver.id = :driverProfileId
                              AND a2.tenantId = :tenantId
                              AND a2.isDeleted = false
                        )
                        AND t.tenantId = :tenantId
                        AND t.isDeleted = false
                  )
              ))
              OR (:attendantProfileId IS NOT NULL AND (
                  EXISTS (
                      SELECT a FROM RouteAssignmentEntity a
                      WHERE a.route.id = r.id
                        AND a.attendant.id = :attendantProfileId
                        AND a.tenantId = :tenantId
                        AND a.isDeleted = false
                        AND a.status IN (serp.project.school_bus_service.enums.RouteAssignmentStatus.ASSIGNED,
                                         serp.project.school_bus_service.enums.RouteAssignmentStatus.CONFIRMED)
                  )
                  OR EXISTS (
                      SELECT t FROM TripExecutionEntity t
                      WHERE t.route.id = r.id
                        AND EXISTS (
                            SELECT a3 FROM RouteAssignmentEntity a3
                            WHERE a3.route.id = t.route.id
                              AND a3.attendant.id = :attendantProfileId
                              AND a3.tenantId = :tenantId
                              AND a3.isDeleted = false
                        )
                        AND t.tenantId = :tenantId
                        AND t.isDeleted = false
                  )
              ))
              OR (:parentProfileId IS NOT NULL AND EXISTS (
                  SELECT rps FROM RoutePlanStudentEntity rps
                  WHERE rps.route.id = r.id
                    AND rps.subscription.student.parentProfile.id = :parentProfileId
                    AND rps.tenantId = :tenantId
                    AND rps.isDeleted = false
                    AND rps.subscription.student.isDeleted = false
                    AND rps.subscription.student.isActive = true
              ))
          )
    """)
    List<RoutePlanEntity> findDashboardRoutes(
            @Param("tenantId") Long tenantId,
            @Param("serviceDate") LocalDate serviceDate,
            @Param("schoolId") Long schoolId,
            @Param("direction") RouteDirection direction,
            @Param("tenantWide") boolean tenantWide,
            @Param("driverProfileId") Long driverProfileId,
            @Param("attendantProfileId") Long attendantProfileId,
            @Param("parentProfileId") Long parentProfileId);

    @Query(value = """
            SELECT
                COALESCE(SUM(CASE
                    WHEN current_assignment.bus_id IS NOT NULL
                     AND current_assignment.driver_id IS NOT NULL
                     AND current_assignment.attendant_id IS NOT NULL
                    THEN 1 ELSE 0 END), 0) AS readyCount,
                COALESCE(SUM(CASE WHEN current_assignment.bus_id IS NULL THEN 1 ELSE 0 END), 0) AS missingBusCount,
                COALESCE(SUM(CASE WHEN current_assignment.driver_id IS NULL THEN 1 ELSE 0 END), 0) AS missingDriverCount,
                COALESCE(SUM(CASE WHEN current_assignment.attendant_id IS NULL THEN 1 ELSE 0 END), 0) AS missingAttendantCount
              FROM public.school_bus_route_plan route
              JOIN public.school_bus_route_planning_session session
                ON session.id = route.planning_session_id
               AND session.is_deleted = false
              LEFT JOIN LATERAL (
                    SELECT assignment.bus_id,
                           assignment.driver_id,
                           assignment.attendant_id
                      FROM public.school_bus_route_assignment assignment
                     WHERE assignment.route_id = route.id
                       AND assignment.tenant_id = :tenantId
                       AND assignment.is_deleted = false
                       AND assignment.status IN ('ASSIGNED', 'CONFIRMED')
                     ORDER BY assignment.assigned_at DESC, assignment.id DESC
                     LIMIT 1
              ) current_assignment ON true
             WHERE route.tenant_id = :tenantId
               AND route.is_deleted = false
               AND session.service_date = :serviceDate
               AND route.status IN ('PUBLISHED', 'ASSIGNED', 'TRIP_CREATED')
               AND (CAST(:schoolId AS bigint) IS NULL OR session.school_id = :schoolId)
               AND (CAST(:direction AS varchar) IS NULL OR session.route_direction = :direction)
               AND (
                    :tenantWide = true
                    OR (CAST(:driverProfileId AS bigint) IS NOT NULL AND EXISTS (
                        SELECT 1
                          FROM public.school_bus_route_assignment driver_assignment
                         WHERE driver_assignment.route_id = route.id
                           AND driver_assignment.driver_id = :driverProfileId
                           AND driver_assignment.tenant_id = :tenantId
                           AND driver_assignment.is_deleted = false
                           AND driver_assignment.status IN ('ASSIGNED', 'CONFIRMED')
                    ))
                    OR (CAST(:attendantProfileId AS bigint) IS NOT NULL AND EXISTS (
                        SELECT 1
                          FROM public.school_bus_route_assignment attendant_assignment
                         WHERE attendant_assignment.route_id = route.id
                           AND attendant_assignment.attendant_id = :attendantProfileId
                           AND attendant_assignment.tenant_id = :tenantId
                           AND attendant_assignment.is_deleted = false
                           AND attendant_assignment.status IN ('ASSIGNED', 'CONFIRMED')
                    ))
                    OR (CAST(:parentProfileId AS bigint) IS NOT NULL AND EXISTS (
                        SELECT 1
                          FROM public.school_bus_route_plan_student route_student
                          JOIN public.school_bus_student_subscription subscription
                            ON subscription.id = route_student.subscription_id
                           AND subscription.is_deleted = false
                          JOIN public.school_bus_student student
                            ON student.id = subscription.student_id
                           AND student.is_deleted = false
                           AND student.is_active = true
                         WHERE route_student.route_id = route.id
                           AND route_student.tenant_id = :tenantId
                           AND route_student.is_deleted = false
                           AND student.parent_profile_id = :parentProfileId
                    ))
               )
            """, nativeQuery = true)
    RouteReadinessCountProjection countDashboardRouteReadiness(
            @Param("tenantId") Long tenantId,
            @Param("serviceDate") LocalDate serviceDate,
            @Param("schoolId") Long schoolId,
            @Param("direction") String direction,
            @Param("tenantWide") boolean tenantWide,
            @Param("driverProfileId") Long driverProfileId,
            @Param("attendantProfileId") Long attendantProfileId,
            @Param("parentProfileId") Long parentProfileId);
}
