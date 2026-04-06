package serp.project.school_bus_service.infrastructure.store.repository;

import serp.project.school_bus_service.kernel.shared.base.BaseRepository;
import serp.project.school_bus_service.infrastructure.store.model.StudentEntity;

import java.util.List;

public interface StudentRepository extends BaseRepository<StudentEntity, Long> {
    List<StudentEntity> findByTenantIdAndIsDeletedFalseOrderByFullNameAsc(Long tenantId);
}
