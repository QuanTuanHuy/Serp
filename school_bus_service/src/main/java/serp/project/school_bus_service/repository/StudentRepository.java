package serp.project.school_bus_service.repository;

import serp.project.school_bus_service.shared.base.BaseRepository;
import serp.project.school_bus_service.entity.StudentEntity;

import java.util.List;

public interface StudentRepository extends BaseRepository<StudentEntity, Long> {
    List<StudentEntity> findByTenantIdAndIsDeletedFalseOrderByFullNameAsc(Long tenantId);

    long countByTenantIdAndIsDeletedFalse(Long tenantId);
}
