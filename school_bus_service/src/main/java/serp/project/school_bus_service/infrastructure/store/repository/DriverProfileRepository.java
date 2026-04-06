package serp.project.school_bus_service.infrastructure.store.repository;

import serp.project.school_bus_service.kernel.shared.base.BaseRepository;
import serp.project.school_bus_service.infrastructure.store.model.DriverProfileEntity;

import java.util.List;

public interface DriverProfileRepository extends BaseRepository<DriverProfileEntity, Long> {
    List<DriverProfileEntity> findByTenantIdAndIsDeletedFalseOrderByFullNameAsc(Long tenantId);
}
