package serp.project.school_bus_service.repository;

import serp.project.school_bus_service.shared.base.BaseRepository;
import serp.project.school_bus_service.entity.TripHistoryEntity;

import java.util.List;
import java.util.Optional;

public interface TripHistoryRepository extends BaseRepository<TripHistoryEntity, Long> {
    List<TripHistoryEntity> findByTenantIdAndIsDeletedFalseOrderByServiceDateDescCreatedAtDesc(Long tenantId);

    Optional<TripHistoryEntity> findByRouteIdAndTenantIdAndIsDeletedFalse(Long routeId, Long tenantId);

    long countByTenantIdAndIsDeletedFalse(Long tenantId);
}
