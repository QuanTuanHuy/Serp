package serp.project.school_bus_service.service.impl;

import org.springframework.stereotype.Service;
import serp.project.school_bus_service.entity.TripStopLogEntity;
import serp.project.school_bus_service.repository.TripStopLogRepository;
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

    public TripStopLogServiceImpl(TripStopLogRepository tripStopLogRepository) {
        this.tripStopLogRepository = tripStopLogRepository;
    }

    @Override
    protected BaseRepository<TripStopLogEntity, Long> getRepository() {
        return tripStopLogRepository;
    }

    @Override
    public List<TripStopLogEntity> findByTrip(Long tripId, Long tenantId) {
        return tripStopLogRepository.findByTripIdAndTenantIdAndIsDeletedFalseOrderByStopOrderAsc(tripId, tenantId);
    }

    @Override
    public List<TripStopLogEntity> findByTrips(Collection<Long> tripIds, Long tenantId) {
        if (tripIds == null || tripIds.isEmpty()) {
            return List.of();
        }
        return tripStopLogRepository.findByTripIdInAndTenantIdAndIsDeletedFalseOrderByStopOrderAsc(tripIds, tenantId);
    }

    @Override
    public Optional<TripStopLogEntity> findByTripAndRouteStop(Long tripId, Long routeStopId, Long tenantId) {
        return tripStopLogRepository.findByTripIdAndRouteStopIdAndTenantIdAndIsDeletedFalse(tripId, routeStopId, tenantId);
    }

    @Override
    public TripStopLogEntity save(TripStopLogEntity entity) {
        return tripStopLogRepository.save(entity);
    }
}
