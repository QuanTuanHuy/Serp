package serp.project.school_bus_service.repository;

import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import serp.project.school_bus_service.entity.RouteAssignmentEntity;
import serp.project.school_bus_service.shared.base.BaseRepository;

import java.time.LocalDate;
import java.time.LocalTime;
import java.util.List;
import java.util.Optional;

public interface RouteAssignmentRepository extends BaseRepository<RouteAssignmentEntity, Long> {

    Optional<RouteAssignmentEntity> findByRouteIdAndTenantIdAndIsDeletedFalse(Long routeId, Long tenantId);

    List<RouteAssignmentEntity> findByBusIdAndTenantIdAndIsDeletedFalse(Long busId, Long tenantId);

    List<RouteAssignmentEntity> findByDriverIdAndTenantIdAndIsDeletedFalse(Long driverId, Long tenantId);

    List<RouteAssignmentEntity> findByAttendantIdAndTenantIdAndIsDeletedFalse(Long attendantId, Long tenantId);

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
              AND r.serviceDate = :serviceDate
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
              AND r.serviceDate = :serviceDate
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
              AND r.serviceDate = :serviceDate
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
