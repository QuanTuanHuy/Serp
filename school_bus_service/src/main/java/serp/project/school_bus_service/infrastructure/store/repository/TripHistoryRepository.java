package serp.project.school_bus_service.infrastructure.store.repository;

import serp.project.school_bus_service.kernel.shared.base.BaseRepository;
import serp.project.school_bus_service.infrastructure.store.model.TripHistoryEntity;

import java.util.List;
import java.util.Optional;

public interface TripHistoryRepository extends BaseRepository<TripHistoryEntity, Long> {
    List<TripHistoryEntity> findByTenantIdAndIsDeletedFalseOrderByServiceDateDescCreatedAtDesc(Long tenantId);

    Optional<TripHistoryEntity> findByRouteIdAndTenantIdAndIsDeletedFalse(Long routeId, Long tenantId);

    long countByTenantIdAndIsDeletedFalse(Long tenantId);
}
