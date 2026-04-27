package serp.project.school_bus_service.infrastructure.store.repository;

import serp.project.school_bus_service.infrastructure.store.model.DemoEventLogEntity;
import serp.project.school_bus_service.kernel.shared.base.BaseRepository;

import java.util.List;

public interface DemoEventLogRepository extends BaseRepository<DemoEventLogEntity, Long> {

    List<DemoEventLogEntity> findByDemoSessionIdAndTenantIdAndIsDeletedFalseOrderByEventTimeAsc(
            Long demoSessionId,
            Long tenantId);

    List<DemoEventLogEntity> findByDemoSessionIdAndTenantIdAndIsDeletedFalseOrderByEventTimeDesc(
            Long demoSessionId,
            Long tenantId);
}
