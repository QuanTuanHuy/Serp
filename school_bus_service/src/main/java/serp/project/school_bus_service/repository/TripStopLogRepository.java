package serp.project.school_bus_service.repository;

import serp.project.school_bus_service.entity.TripStopLogEntity;
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

