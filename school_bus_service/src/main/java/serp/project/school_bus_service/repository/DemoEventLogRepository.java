package serp.project.school_bus_service.repository;

import serp.project.school_bus_service.entity.DemoEventLogEntity;
import serp.project.school_bus_service.shared.base.BaseRepository;

import java.util.List;

public interface DemoEventLogRepository extends BaseRepository<DemoEventLogEntity, Long> {

    List<DemoEventLogEntity> findByDemoSessionIdAndTenantIdAndIsDeletedFalseOrderByEventTimeAsc(
            Long demoSessionId,
            Long tenantId);

    List<DemoEventLogEntity> findByDemoSessionIdAndTenantIdAndIsDeletedFalseOrderByEventTimeDesc(
            Long demoSessionId,
            Long tenantId);
}
