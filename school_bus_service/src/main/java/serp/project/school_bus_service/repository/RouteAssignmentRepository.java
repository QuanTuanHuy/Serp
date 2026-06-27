package serp.project.school_bus_service.repository;

import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import serp.project.school_bus_service.entity.RouteAssignmentEntity;
import serp.project.school_bus_service.enums.RouteAssignmentStatus;
import serp.project.school_bus_service.repository.projection.RouteAssignmentSummaryProjection;
import serp.project.school_bus_service.shared.base.BaseRepository;

import java.time.LocalDate;
import java.time.LocalTime;
import java.util.Collection;
import java.util.List;
import java.util.Optional;

public interface RouteAssignmentRepository extends BaseRepository<RouteAssignmentEntity, Long> {

    Optional<RouteAssignmentEntity> findByRouteIdAndTenantIdAndIsDeletedFalse(Long routeId, Long tenantId);

    @Query("""
            SELECT a FROM RouteAssignmentEntity a
            WHERE a.route.id = :routeId
              AND a.tenantId = :tenantId
              AND a.isDeleted = false
              AND a.status IN :statuses
            ORDER BY a.assignedAt DESC, a.id DESC
            """)
    List<RouteAssignmentEntity> findCurrentByRoute(
            @Param("routeId") Long routeId,
            @Param("tenantId") Long tenantId,
            @Param("statuses") Collection<RouteAssignmentStatus> statuses,
            Pageable pageable);

    @Query("""
            SELECT count(a) > 0 FROM RouteAssignmentEntity a
            WHERE a.route.id = :routeId
              AND a.tenantId = :tenantId
              AND a.isDeleted = false
              AND a.status IN :statuses
              AND a.driver.id = :driverId
            """)
    boolean existsCurrentDriverAssignment(
            @Param("routeId") Long routeId,
            @Param("tenantId") Long tenantId,
            @Param("driverId") Long driverId,
            @Param("statuses") Collection<RouteAssignmentStatus> statuses);

    @Query("""
            SELECT count(a) > 0 FROM RouteAssignmentEntity a
            WHERE a.route.id = :routeId
              AND a.tenantId = :tenantId
              AND a.isDeleted = false
              AND a.status IN :statuses
              AND a.attendant.id = :attendantId
            """)
    boolean existsCurrentAttendantAssignment(
            @Param("routeId") Long routeId,
            @Param("tenantId") Long tenantId,
            @Param("attendantId") Long attendantId,
            @Param("statuses") Collection<RouteAssignmentStatus> statuses);

    List<RouteAssignmentEntity> findByBusIdAndTenantIdAndIsDeletedFalse(Long busId, Long tenantId);

    List<RouteAssignmentEntity> findByDriverIdAndTenantIdAndIsDeletedFalse(Long driverId, Long tenantId);

    List<RouteAssignmentEntity> findByAttendantIdAndTenantIdAndIsDeletedFalse(Long attendantId, Long tenantId);

    @Query(value = """
            SELECT ranked.route_id AS routeId,
                   ranked.bus_id AS busId,
                   b.plate_number AS busPlateNumber,
                   b.capacity AS busCapacity,
                   b.status AS busStatus,
                   ranked.driver_id AS driverId,
                   dp.full_name AS driverName,
                   ranked.attendant_id AS attendantId,
                   ap.full_name AS attendantName
              FROM (
                    SELECT a.*,
                           ROW_NUMBER() OVER (
                               PARTITION BY a.route_id
                               ORDER BY a.assigned_at DESC, a.id DESC
                           ) AS rn
                      FROM public.school_bus_route_assignment a
                     WHERE a.tenant_id = :tenantId
                       AND a.is_deleted = false
                       AND a.status IN ('ASSIGNED', 'CONFIRMED')
                       AND a.route_id IN (:routeIds)
                   ) ranked
              JOIN public.school_bus_bus b ON b.id = ranked.bus_id
              LEFT JOIN public.school_bus_driver_profile dp ON dp.id = ranked.driver_id
              LEFT JOIN public.school_bus_attendant_profile ap ON ap.id = ranked.attendant_id
             WHERE ranked.rn = 1
            """, nativeQuery = true)
    List<RouteAssignmentSummaryProjection> findCurrentSummariesByRouteIds(
            @Param("tenantId") Long tenantId,
            @Param("routeIds") Collection<Long> routeIds);

    @Query("""
            SELECT a FROM RouteAssignmentEntity a
            WHERE a.route.id IN :routeIds
              AND a.tenantId = :tenantId
              AND a.isDeleted = false
              AND a.status IN (serp.project.school_bus_service.enums.RouteAssignmentStatus.ASSIGNED,
                               serp.project.school_bus_service.enums.RouteAssignmentStatus.CONFIRMED)
            """)
    List<RouteAssignmentEntity> findDashboardAssignments(
            @Param("routeIds") Collection<Long> routeIds,
            @Param("tenantId") Long tenantId);

    /**
     * Find active assignments for a given bus on a service date whose planned time windows
     * overlap with [windowStart, windowEnd]. Used for precise conflict detection (2B).
     * Falls through to all assignments on same day when no time window is stored.
     */
    @Query("""
            SELECT a FROM RouteAssignmentEntity a
            JOIN a.route r
            WHERE a.tenantId = :tenantId
              AND a.isDeleted = false
              AND a.status IN ('ASSIGNED', 'CONFIRMED')
              AND a.bus.id = :busId
              AND r.id <> :excludeRouteId
              AND r.isDeleted = false
              AND r.planningSession.serviceDate = :serviceDate
              AND r.status NOT IN ('CANCELLED', 'COMPLETED')
              AND (
                    r.plannedStartTime IS NULL OR r.plannedEndTime IS NULL
                    OR NOT (r.plannedEndTime <= :windowStart OR r.plannedStartTime >= :windowEnd)
                  )
            """)
    List<RouteAssignmentEntity> findBusConflicts(
            @Param("tenantId") Long tenantId,
            @Param("busId") Long busId,
            @Param("excludeRouteId") Long excludeRouteId,
            @Param("serviceDate") LocalDate serviceDate,
            @Param("windowStart") LocalTime windowStart,
            @Param("windowEnd") LocalTime windowEnd);

    @Query("""
            SELECT a FROM RouteAssignmentEntity a
            JOIN a.route r
            WHERE a.tenantId = :tenantId
              AND a.isDeleted = false
              AND a.status IN ('ASSIGNED', 'CONFIRMED')
              AND a.driver.id = :driverId
              AND r.id <> :excludeRouteId
              AND r.isDeleted = false
              AND r.planningSession.serviceDate = :serviceDate
              AND r.status NOT IN ('CANCELLED', 'COMPLETED')
              AND (
                    r.plannedStartTime IS NULL OR r.plannedEndTime IS NULL
                    OR NOT (r.plannedEndTime <= :windowStart OR r.plannedStartTime >= :windowEnd)
                  )
            """)
    List<RouteAssignmentEntity> findDriverConflicts(
            @Param("tenantId") Long tenantId,
            @Param("driverId") Long driverId,
            @Param("excludeRouteId") Long excludeRouteId,
            @Param("serviceDate") LocalDate serviceDate,
            @Param("windowStart") LocalTime windowStart,
            @Param("windowEnd") LocalTime windowEnd);

    @Query("""
            SELECT a FROM RouteAssignmentEntity a
            JOIN a.route r
            WHERE a.tenantId = :tenantId
              AND a.isDeleted = false
              AND a.status IN ('ASSIGNED', 'CONFIRMED')
              AND a.attendant.id = :attendantId
              AND r.id <> :excludeRouteId
              AND r.isDeleted = false
              AND r.planningSession.serviceDate = :serviceDate
              AND r.status NOT IN ('CANCELLED', 'COMPLETED')
              AND (
                    r.plannedStartTime IS NULL OR r.plannedEndTime IS NULL
                    OR NOT (r.plannedEndTime <= :windowStart OR r.plannedStartTime >= :windowEnd)
                  )
            """)
    List<RouteAssignmentEntity> findAttendantConflicts(
            @Param("tenantId") Long tenantId,
            @Param("attendantId") Long attendantId,
            @Param("excludeRouteId") Long excludeRouteId,
            @Param("serviceDate") LocalDate serviceDate,
            @Param("windowStart") LocalTime windowStart,
            @Param("windowEnd") LocalTime windowEnd);
}
