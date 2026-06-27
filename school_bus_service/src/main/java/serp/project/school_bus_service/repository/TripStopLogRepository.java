package serp.project.school_bus_service.repository;

import serp.project.school_bus_service.entity.TripStopLogEntity;
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
}

