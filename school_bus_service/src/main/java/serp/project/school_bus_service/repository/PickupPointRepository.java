package serp.project.school_bus_service.repository;

import serp.project.school_bus_service.shared.base.BaseRepository;
import serp.project.school_bus_service.entity.PickupPointEntity;

import java.util.List;

public interface PickupPointRepository extends BaseRepository<PickupPointEntity, Long> {
    List<PickupPointEntity> findByTenantIdAndIsDeletedFalseOrderByNameAsc(Long tenantId);
}
