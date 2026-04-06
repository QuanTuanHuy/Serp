package serp.project.school_bus_service.infrastructure.store.repository;

import serp.project.school_bus_service.kernel.shared.base.BaseRepository;
import serp.project.school_bus_service.infrastructure.store.model.SchoolEntity;

import java.util.List;

public interface SchoolRepository extends BaseRepository<SchoolEntity, Long> {
    List<SchoolEntity> findByTenantIdAndIsDeletedFalseOrderByNameAsc(Long tenantId);
}
