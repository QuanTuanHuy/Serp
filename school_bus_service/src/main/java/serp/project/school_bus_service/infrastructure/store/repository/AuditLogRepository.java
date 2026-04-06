package serp.project.school_bus_service.infrastructure.store.repository;

import serp.project.school_bus_service.kernel.shared.base.BaseRepository;
import serp.project.school_bus_service.infrastructure.store.model.AuditLogEntity;

public interface AuditLogRepository extends BaseRepository<AuditLogEntity, Long> {
    long countByTenantIdAndIsDeletedFalse(Long tenantId);
}
