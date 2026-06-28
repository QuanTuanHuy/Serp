package serp.project.school_bus_service.repository;

import serp.project.school_bus_service.entity.TripStopLogEntity;
import serp.project.school_bus_service.repository.projection.TripOperationStopProjection;
import serp.project.school_bus_service.repository.projection.TripStopOperationProjection;
import serp.project.school_bus_service.repository.projection.TripStopProgressRowProjection;
import serp.project.school_bus_service.shared.base.BaseRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.Collection;
import java.util.List;
import java.util.Optional;

public interface TripStopLogRepository extends BaseRepository<TripStopLogEntity, Long> {

    @Query("""
            SELECT log FROM TripStopLogEntity log
            JOIN log.routeStop stop
            WHERE log.trip.id = :tripId
              AND log.tenantId = :tenantId
              AND log.isDeleted = false
            ORDER BY stop.stopOrder ASC
            """)
    List<TripStopLogEntity> findByTripOrderedByRouteStopOrder(
            @Param("tripId") Long tripId,
            @Param("tenantId") Long tenantId);

    @Query("""
            SELECT log FROM TripStopLogEntity log
            JOIN log.routeStop stop
            WHERE log.trip.id IN :tripIds
              AND log.tenantId = :tenantId
              AND log.isDeleted = false
            ORDER BY log.trip.id ASC, stop.stopOrder ASC
            """)
    List<TripStopLogEntity> findByTripsOrderedByRouteStopOrder(
            @Param("tripIds") Collection<Long> tripIds,
            @Param("tenantId") Long tenantId);

    Optional<TripStopLogEntity> findByTripIdAndRouteStopIdAndTenantIdAndIsDeletedFalse(
            Long tripId,
            Long routeStopId,
            Long tenantId);

    @Query(value = """
            SELECT log.id AS tripStopLogId,
                   log.route_stop_id AS routeStopId,
                   stop.stop_order AS stopOrder,
                   stop.stop_purpose AS stopPurpose,
                   stop.location_type AS locationType,
                   log.status AS stopStatus,
                   log.actual_arrival_time AS actualArrivalTime,
                   log.actual_departure_time AS actualDepartureTime
              FROM public.school_bus_trip_stop_log log
              JOIN public.school_bus_route_stop stop
                ON stop.id = log.route_stop_id
               AND stop.is_deleted = false
             WHERE log.trip_id = :tripId
               AND log.route_stop_id = :routeStopId
               AND log.tenant_id = :tenantId
               AND log.is_deleted = false
             LIMIT 1
            """, nativeQuery = true)
    Optional<TripStopOperationProjection> findOperationStop(
            @Param("tripId") Long tripId,
            @Param("routeStopId") Long routeStopId,
            @Param("tenantId") Long tenantId);

    @Query(value = """
            SELECT log.id AS tripStopLogId,
                   log.route_stop_id AS routeStopId,
                   stop.stop_order AS stopOrder,
                   stop.stop_purpose AS stopPurpose,
                   stop.location_type AS locationType,
                   log.status AS stopStatus,
                   log.actual_arrival_time AS actualArrivalTime,
                   log.actual_departure_time AS actualDepartureTime
              FROM public.school_bus_trip_stop_log log
              JOIN public.school_bus_route_stop stop
                ON stop.id = log.route_stop_id
               AND stop.is_deleted = false
             WHERE log.trip_id = :tripId
               AND log.tenant_id = :tenantId
               AND log.is_deleted = false
             ORDER BY stop.stop_order ASC
             LIMIT 1
            """, nativeQuery = true)
    Optional<TripStopOperationProjection> findFirstOperationStop(
            @Param("tripId") Long tripId,
            @Param("tenantId") Long tenantId);

    @Query(value = """
            SELECT log.id AS tripStopLogId,
                   log.route_stop_id AS routeStopId,
                   stop.stop_order AS stopOrder,
                   stop.stop_purpose AS stopPurpose,
                   stop.location_type AS locationType,
                   log.status AS stopStatus,
                   log.actual_arrival_time AS actualArrivalTime,
                   log.actual_departure_time AS actualDepartureTime
              FROM public.school_bus_trip_stop_log log
              JOIN public.school_bus_route_stop stop
                ON stop.id = log.route_stop_id
               AND stop.is_deleted = false
             WHERE log.trip_id = :tripId
               AND log.tenant_id = :tenantId
               AND log.status = 'PENDING'
               AND log.is_deleted = false
             ORDER BY stop.stop_order ASC
             LIMIT 1
            """, nativeQuery = true)
    Optional<TripStopOperationProjection> findFirstPendingOperationStop(
            @Param("tripId") Long tripId,
            @Param("tenantId") Long tenantId);

    @Query(value = """
            SELECT log.id AS tripStopLogId,
                   log.route_stop_id AS routeStopId,
                   stop.stop_order AS stopOrder,
                   stop.stop_purpose AS stopPurpose,
                   stop.location_type AS locationType,
                   log.status AS stopStatus,
                   log.actual_arrival_time AS actualArrivalTime,
                   log.actual_departure_time AS actualDepartureTime
              FROM public.school_bus_trip_stop_log log
              JOIN public.school_bus_route_stop stop
                ON stop.id = log.route_stop_id
               AND stop.is_deleted = false
             WHERE log.trip_id = :tripId
               AND log.tenant_id = :tenantId
               AND log.status NOT IN ('DEPARTED', 'SKIPPED')
               AND log.is_deleted = false
             ORDER BY stop.stop_order ASC
             LIMIT 1
            """, nativeQuery = true)
    Optional<TripStopOperationProjection> findFirstUnfinishedOperationStop(
            @Param("tripId") Long tripId,
            @Param("tenantId") Long tenantId);

    @Query(value = """
            SELECT log.id AS tripStopLogId,
                   log.route_stop_id AS routeStopId,
                   stop.stop_order AS stopOrder,
                   stop.stop_purpose AS stopPurpose,
                   stop.location_type AS locationType,
                   log.status AS stopStatus,
                   log.actual_arrival_time AS actualArrivalTime,
                   log.actual_departure_time AS actualDepartureTime
              FROM public.school_bus_trip_stop_log log
              JOIN public.school_bus_route_stop stop
                ON stop.id = log.route_stop_id
               AND stop.is_deleted = false
             WHERE log.trip_id = :tripId
               AND log.tenant_id = :tenantId
               AND log.is_deleted = false
             ORDER BY stop.stop_order DESC
             LIMIT 1
            """, nativeQuery = true)
    Optional<TripStopOperationProjection> findLastOperationStop(
            @Param("tripId") Long tripId,
            @Param("tenantId") Long tenantId);

    @Query(value = """
            WITH trip_context AS (
                SELECT t.id AS trip_id,
                       r.id AS route_id,
                       ps.route_direction AS route_direction
                  FROM public.school_bus_trip_execution t
                  JOIN public.school_bus_route_plan r
                    ON r.id = t.route_id
                   AND r.is_deleted = false
                  JOIN public.school_bus_route_planning_session ps
                    ON ps.id = r.planning_session_id
                   AND ps.is_deleted = false
                 WHERE t.id = :tripId
                   AND t.tenant_id = :tenantId
                   AND t.is_deleted = false
            ),
            scoped_students AS (
                SELECT ts.id,
                       ts.status,
                       ts.pickup_stop_id,
                       ts.dropoff_stop_id
                  FROM public.school_bus_trip_student ts
                  JOIN public.school_bus_student_subscription sub
                    ON sub.id = ts.subscription_id
                   AND sub.is_deleted = false
                  JOIN public.school_bus_student student
                    ON student.id = sub.student_id
                   AND student.is_deleted = false
                 WHERE ts.trip_id = :tripId
                   AND ts.tenant_id = :tenantId
                   AND ts.is_deleted = false
                   AND (:parentProfileId IS NULL OR student.parent_profile_id = :parentProfileId)
            )
            SELECT stop.id AS routeStopId,
                   stop.stop_order AS stopOrder,
                   stop.location_type AS locationType,
                   stop.stop_purpose AS stopPurpose,
                   stop.location_id AS locationId,
                   COALESCE(pp.name, school.name, depot.name) AS locationName,
                   COALESCE(pp.address, school.address, depot.address) AS locationAddress,
                   COALESCE(pp.latitude, school.latitude, depot.latitude) AS latitude,
                   COALESCE(pp.longitude, school.longitude, depot.longitude) AS longitude,
                   log.status AS stopStatus,
                   COALESCE(log.actual_boarded_count, 0) AS actualBoardedCount,
                   COALESCE(log.actual_dropped_count, 0) AS actualDroppedCount,
                   CAST(log.actual_arrival_time AS varchar) AS actualArrivalTime,
                   CAST(log.actual_departure_time AS varchar) AS actualDepartureTime,
                   CAST(
                       CASE
                           WHEN stop.stop_purpose = 'START_TERMINAL'
                            AND stop.location_type = 'SCHOOL'
                            AND trip_context.route_direction = 'RETURN'
                               THEN (SELECT COUNT(*) FROM scoped_students)
                           ELSE (
                               SELECT COUNT(*)
                                 FROM scoped_students scoped
                                WHERE scoped.pickup_stop_id = stop.id
                           )
                       END AS INTEGER
                   ) AS plannedBoardingCount,
                   CAST(
                       CASE
                           WHEN stop.stop_purpose = 'END_TERMINAL'
                            AND stop.location_type = 'SCHOOL'
                            AND trip_context.route_direction = 'OUTBOUND'
                               THEN (
                                   SELECT COUNT(*)
                                     FROM scoped_students scoped
                                    WHERE scoped.status IN ('BOARDED', 'DROPPED_OFF')
                               )
                           ELSE (
                               SELECT COUNT(*)
                                 FROM scoped_students scoped
                                WHERE scoped.dropoff_stop_id = stop.id
                           )
                       END AS INTEGER
                   ) AS plannedDropoffCount
              FROM public.school_bus_trip_stop_log log
              JOIN public.school_bus_route_stop stop
                ON stop.id = log.route_stop_id
               AND stop.is_deleted = false
              JOIN trip_context
                ON trip_context.trip_id = log.trip_id
              LEFT JOIN public.school_bus_pickup_point pp
                ON stop.location_type = 'PICKUP_POINT'
               AND pp.id = stop.location_id
               AND pp.is_deleted = false
              LEFT JOIN public.school_bus_school school
                ON stop.location_type = 'SCHOOL'
               AND school.id = stop.location_id
               AND school.is_deleted = false
              LEFT JOIN public.school_bus_depot depot
                ON stop.location_type = 'DEPOT'
               AND depot.id = stop.location_id
               AND depot.is_deleted = false
             WHERE log.trip_id = :tripId
               AND log.tenant_id = :tenantId
               AND log.is_deleted = false
             ORDER BY stop.stop_order ASC
            """, nativeQuery = true)
    List<TripOperationStopProjection> findOperationStopsByTripId(
            @Param("tripId") Long tripId,
            @Param("tenantId") Long tenantId,
            @Param("parentProfileId") Long parentProfileId);

    @Query(value = """
            SELECT log.trip_id AS tripId,
                   log.route_stop_id AS routeStopId,
                   stop.stop_order AS stopOrder,
                   log.status AS status,
                   COALESCE(pp.name, school.name, depot.name) AS stopName,
                   stop.location_type AS locationType
              FROM public.school_bus_trip_stop_log log
              JOIN public.school_bus_route_stop stop ON stop.id = log.route_stop_id
              LEFT JOIN public.school_bus_pickup_point pp
                     ON stop.location_type = 'PICKUP_POINT'
                    AND pp.id = stop.location_id
                    AND pp.is_deleted = false
              LEFT JOIN public.school_bus_school school
                     ON stop.location_type = 'SCHOOL'
                    AND school.id = stop.location_id
                    AND school.is_deleted = false
              LEFT JOIN public.school_bus_depot depot
                     ON stop.location_type = 'DEPOT'
                    AND depot.id = stop.location_id
                    AND depot.is_deleted = false
             WHERE log.trip_id IN (:tripIds)
               AND log.tenant_id = :tenantId
               AND log.is_deleted = false
               AND stop.is_deleted = false
             ORDER BY log.trip_id ASC, stop.stop_order ASC
            """, nativeQuery = true)
    List<TripStopProgressRowProjection> findProgressRowsByTripIds(
            @Param("tripIds") Collection<Long> tripIds,
            @Param("tenantId") Long tenantId);
}

