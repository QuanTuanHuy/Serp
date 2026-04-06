package serp.project.school_bus_service.infrastructure.store.repository;

import serp.project.school_bus_service.kernel.shared.base.BaseRepository;
import serp.project.school_bus_service.infrastructure.store.model.PickupPointEntity;

import java.util.List;

public interface PickupPointRepository extends BaseRepository<PickupPointEntity, Long> {
    List<PickupPointEntity> findByTenantIdAndIsDeletedFalseOrderByNameAsc(Long tenantId);
}
