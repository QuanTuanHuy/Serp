package serp.project.school_bus_service.core.service;

public interface IAuditLogService {

    void log(Long tenantId, Long performedBy, String aggregateType, Long aggregateId, String actionType, String actionDetail);
}
