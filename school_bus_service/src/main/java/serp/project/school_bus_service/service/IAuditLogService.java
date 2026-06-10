package serp.project.school_bus_service.service;

/**
 * Audit log service interface. Currently a no-op after Phase 2 simplification.
 * Will be re-implemented in a future phase.
 */
public interface IAuditLogService {
    void log(Long tenantId, Long actorId, String entityType, Long entityId, String action, String message);

    long countByTenant(Long tenantId);
}
