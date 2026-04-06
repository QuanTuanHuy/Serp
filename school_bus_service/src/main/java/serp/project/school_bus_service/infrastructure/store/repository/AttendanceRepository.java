package serp.project.school_bus_service.infrastructure.store.repository;

import serp.project.school_bus_service.kernel.shared.base.BaseRepository;
import serp.project.school_bus_service.infrastructure.store.model.AttendanceEntity;

import java.util.List;

public interface AttendanceRepository extends BaseRepository<AttendanceEntity, Long> {
    List<AttendanceEntity> findByTenantIdAndIsDeletedFalseOrderByRecordedAtDesc(Long tenantId);

    long countByTenantIdAndIsDeletedFalse(Long tenantId);
}
