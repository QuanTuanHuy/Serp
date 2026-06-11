package serp.project.school_bus_service.service;

import serp.project.school_bus_service.shared.base.IBaseService;

import serp.project.school_bus_service.entity.TripStopLogEntity;

import java.util.Collection;
import java.util.List;
import java.util.Optional;

public interface ITripStopLogService extends IBaseService<TripStopLogEntity, Long> {

    List<TripStopLogEntity> findByTrip(Long tripId, Long tenantId);

    List<TripStopLogEntity> findByTrips(Collection<Long> tripIds, Long tenantId);

    Optional<TripStopLogEntity> findByTripAndRouteStop(Long tripId, Long routeStopId, Long tenantId);

    TripStopLogEntity save(TripStopLogEntity entity);
}
