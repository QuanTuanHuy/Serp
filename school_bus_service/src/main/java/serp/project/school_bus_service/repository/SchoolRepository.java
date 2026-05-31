package serp.project.school_bus_service.repository;

import serp.project.school_bus_service.shared.base.BaseRepository;
import serp.project.school_bus_service.entity.SchoolEntity;

import java.util.List;

public interface SchoolRepository extends BaseRepository<SchoolEntity, Long> {
    List<SchoolEntity> findByTenantIdAndIsDeletedFalseOrderByNameAsc(Long tenantId);

    long countByTenantIdAndIsDeletedFalse(Long tenantId);
}
