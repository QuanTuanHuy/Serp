package serp.project.school_bus_service.infrastructure.store.repository;

import serp.project.school_bus_service.kernel.shared.base.BaseRepository;
import serp.project.school_bus_service.infrastructure.store.model.BusEntity;

import java.util.List;

public interface BusRepository extends BaseRepository<BusEntity, Long> {
    List<BusEntity> findByTenantIdAndIsDeletedFalseOrderByPlateNumberAsc(Long tenantId);
}
