package serp.project.school_bus_service.service;
import serp.project.school_bus_service.entity.AuditLogEntity;

import serp.project.school_bus_service.shared.base.IBaseService;

public interface IAuditLogService extends IBaseService<AuditLogEntity, Long> {

    void log(Long tenantId, Long performedBy, String aggregateType, Long aggregateId, String actionType, String actionDetail);

    long countByTenant(Long tenantId);
}
