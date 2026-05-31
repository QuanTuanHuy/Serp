package serp.project.school_bus_service.repository;

import serp.project.school_bus_service.shared.base.BaseRepository;
import serp.project.school_bus_service.entity.AuditLogEntity;

public interface AuditLogRepository extends BaseRepository<AuditLogEntity, Long> {
    long countByTenantIdAndIsDeletedFalse(Long tenantId);
}
