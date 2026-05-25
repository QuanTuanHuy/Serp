package serp.project.school_bus_service.service.impl;

import org.springframework.stereotype.Service;
import serp.project.school_bus_service.entity.AuditLogEntity;
import serp.project.school_bus_service.repository.AuditLogRepository;
import serp.project.school_bus_service.service.IAuditLogService;
import serp.project.school_bus_service.shared.base.AbstractBaseService;
import serp.project.school_bus_service.shared.base.BaseRepository;

import java.time.LocalDateTime;

@Service
public class AuditLogServiceImpl extends AbstractBaseService<AuditLogEntity, Long>
        implements IAuditLogService {

    private final AuditLogRepository auditLogRepository;

    public AuditLogServiceImpl(AuditLogRepository auditLogRepository) {
        this.auditLogRepository = auditLogRepository;
    }

    @Override
    protected BaseRepository<AuditLogEntity, Long> getRepository() {
        return auditLogRepository;
    }

    @Override
    public void log(Long tenantId, Long performedBy, String aggregateType, Long aggregateId, String actionType,
            String actionDetail) {
        AuditLogEntity auditLog = new AuditLogEntity();
        auditLog.markCreated(tenantId, performedBy == null ? "SYSTEM" : String.valueOf(performedBy));
        auditLog.setPerformedBy(performedBy);
        auditLog.setAggregateType(aggregateType);
        auditLog.setAggregateId(aggregateId);
        auditLog.setActionType(actionType);
        auditLog.setActionDetail(actionDetail);
        auditLog.setPerformedAt(LocalDateTime.now());
        auditLogRepository.save(auditLog);
    }

    @Override
    public long countByTenant(Long tenantId) {
        return auditLogRepository.countByTenantIdAndIsDeletedFalse(tenantId);
    }
}
