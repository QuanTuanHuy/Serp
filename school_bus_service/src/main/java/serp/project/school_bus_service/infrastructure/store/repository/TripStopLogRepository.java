package serp.project.school_bus_service.infrastructure.store.repository;

import serp.project.school_bus_service.infrastructure.store.model.TripStopLogEntity;
import serp.project.school_bus_service.kernel.shared.base.BaseRepository;

import java.util.List;
import java.util.Optional;

public interface TripStopLogRepository extends BaseRepository<TripStopLogEntity, Long> {

    List<TripStopLogEntity> findByTripIdAndTenantIdAndIsDeletedFalseOrderByStopOrderAsc(Long tripId, Long tenantId);

    Optional<TripStopLogEntity> findByTripIdAndRouteStopIdAndTenantIdAndIsDeletedFalse(
            Long tripId,
            Long routeStopId,
            Long tenantId);
}

