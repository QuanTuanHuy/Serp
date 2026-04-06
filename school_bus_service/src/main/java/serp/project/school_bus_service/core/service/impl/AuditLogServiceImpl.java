package serp.project.school_bus_service.core.service.impl;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import serp.project.school_bus_service.core.service.IAuditLogService;
import serp.project.school_bus_service.infrastructure.store.model.AuditLogEntity;
import serp.project.school_bus_service.infrastructure.store.repository.AuditLogRepository;

import java.time.LocalDateTime;

@Service
@RequiredArgsConstructor
public class AuditLogServiceImpl implements IAuditLogService {

    private final AuditLogRepository auditLogRepository;

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
}
