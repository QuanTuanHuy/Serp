package serp.project.school_bus_service.repository;

import serp.project.school_bus_service.shared.base.BaseRepository;
import serp.project.school_bus_service.entity.ParentProfileEntity;

import java.util.List;
import java.util.Optional;

public interface ParentProfileRepository extends BaseRepository<ParentProfileEntity, Long> {
    List<ParentProfileEntity> findByTenantIdAndIsDeletedFalseOrderByFullNameAsc(Long tenantId);

    long countByTenantIdAndIsDeletedFalse(Long tenantId);

    Optional<ParentProfileEntity> findByTenantIdAndUserIdAndIsDeletedFalse(Long tenantId, Long userId);
}
