package serp.project.school_bus_service.infrastructure.store.repository;

import serp.project.school_bus_service.kernel.shared.base.BaseRepository;
import serp.project.school_bus_service.infrastructure.store.model.ParentProfileEntity;

import java.util.List;

public interface ParentProfileRepository extends BaseRepository<ParentProfileEntity, Long> {
    List<ParentProfileEntity> findByTenantIdAndIsDeletedFalseOrderByFullNameAsc(Long tenantId);
}
