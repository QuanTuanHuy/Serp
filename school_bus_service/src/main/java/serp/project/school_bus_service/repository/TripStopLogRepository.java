package serp.project.school_bus_service.repository;

import serp.project.school_bus_service.entity.TripStopLogEntity;
import serp.project.school_bus_service.shared.base.BaseRepository;

import java.util.List;
import java.util.Optional;

public interface TripStopLogRepository extends BaseRepository<TripStopLogEntity, Long> {

    List<TripStopLogEntity> findByTripIdAndTenantIdAndIsDeletedFalseOrderByStopOrderAsc(Long tripId, Long tenantId);

    Optional<TripStopLogEntity> findByTripIdAndRouteStopIdAndTenantIdAndIsDeletedFalse(
            Long tripId,
            Long routeStopId,
            Long tenantId);
}

