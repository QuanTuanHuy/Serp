package serp.project.school_bus_service.service.impl;

import org.springframework.stereotype.Service;
import serp.project.school_bus_service.service.IAuditLogService;

@Service
public class AuditLogServiceImpl implements IAuditLogService {

    @Override
    public void log(Long tenantId, Long actorId, String entityType, Long entityId, String action, String message) {
        // No-op after Phase 2 simplification. Will be re-implemented later.
    }

    @Override
    public long countByTenant(Long tenantId) {
        return 0L;
    }
}
