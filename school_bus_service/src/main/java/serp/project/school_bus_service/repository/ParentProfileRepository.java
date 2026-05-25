package serp.project.school_bus_service.repository;

import serp.project.school_bus_service.shared.base.BaseRepository;
import serp.project.school_bus_service.entity.ParentProfileEntity;

import java.util.List;

public interface ParentProfileRepository extends BaseRepository<ParentProfileEntity, Long> {
    List<ParentProfileEntity> findByTenantIdAndIsDeletedFalseOrderByFullNameAsc(Long tenantId);

    long countByTenantIdAndIsDeletedFalse(Long tenantId);
}
