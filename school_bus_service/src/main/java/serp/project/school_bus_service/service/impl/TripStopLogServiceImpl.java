package serp.project.school_bus_service.service.impl;

import org.springframework.stereotype.Service;
import serp.project.school_bus_service.entity.TripStopLogEntity;
import serp.project.school_bus_service.repository.TripStopLogRepository;
import serp.project.school_bus_service.repository.projection.TripStopProgressRowProjection;
import serp.project.school_bus_service.service.IRouteStopService;
import serp.project.school_bus_service.service.ITripStopLogService;
import serp.project.school_bus_service.shared.base.AbstractBaseService;
import serp.project.school_bus_service.shared.base.BaseRepository;

import java.util.Collection;
import java.util.List;
import java.util.Optional;

@Service
public class TripStopLogServiceImpl extends AbstractBaseService<TripStopLogEntity, Long>
        implements ITripStopLogService {

    private final TripStopLogRepository tripStopLogRepository;
    private final IRouteStopService routeStopService;

    public TripStopLogServiceImpl(TripStopLogRepository tripStopLogRepository,
                                  IRouteStopService routeStopService) {
        this.tripStopLogRepository = tripStopLogRepository;
        this.routeStopService = routeStopService;
    }

    @Override
    protected BaseRepository<TripStopLogEntity, Long> getRepository() {
        return tripStopLogRepository;
    }

    @Override
    public List<TripStopLogEntity> findByTrip(Long tripId, Long tenantId) {
        return hydrateRouteStops(
                tripStopLogRepository.findByTripOrderedByRouteStopOrder(tripId, tenantId),
                tenantId);
    }

    @Override
    public List<TripStopLogEntity> findByTrips(Collection<Long> tripIds, Long tenantId) {
        if (tripIds == null || tripIds.isEmpty()) {
            return List.of();
        }
        return hydrateRouteStops(
                tripStopLogRepository.findByTripsOrderedByRouteStopOrder(tripIds, tenantId),
                tenantId);
    }

    @Override
    public List<TripStopProgressRowProjection> findProgressRowsByTripIds(Collection<Long> tripIds, Long tenantId) {
        if (tripIds == null || tripIds.isEmpty()) {
            return List.of();
        }
        return tripStopLogRepository.findProgressRowsByTripIds(tripIds, tenantId);
    }

    @Override
    public Optional<TripStopLogEntity> findByTripAndRouteStop(Long tripId, Long routeStopId, Long tenantId) {
        return tripStopLogRepository.findByTripIdAndRouteStopIdAndTenantIdAndIsDeletedFalse(tripId, routeStopId, tenantId)
                .map(log -> hydrateRouteStop(log, tenantId));
    }

    @Override
    public TripStopLogEntity save(TripStopLogEntity entity) {
        return tripStopLogRepository.save(entity);
    }

    private List<TripStopLogEntity> hydrateRouteStops(List<TripStopLogEntity> logs, Long tenantId) {
        return logs.stream()
                .map(log -> hydrateRouteStop(log, tenantId))
                .toList();
    }

    private TripStopLogEntity hydrateRouteStop(TripStopLogEntity log, Long tenantId) {
        if (log == null || log.getRouteStop() == null || log.getRouteStop().getId() == null) {
            return log;
        }
        routeStopService.findRouteStop(log.getRouteStop().getId(), tenantId)
                .ifPresent(log::setRouteStop);
        return log;
    }
}
