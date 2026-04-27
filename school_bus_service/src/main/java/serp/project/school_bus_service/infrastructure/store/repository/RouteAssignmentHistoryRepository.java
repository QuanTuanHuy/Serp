package serp.project.school_bus_service.infrastructure.store.repository;

import serp.project.school_bus_service.infrastructure.store.model.RouteAssignmentHistoryEntity;
import serp.project.school_bus_service.kernel.shared.base.BaseRepository;

import java.util.List;

public interface RouteAssignmentHistoryRepository extends BaseRepository<RouteAssignmentHistoryEntity, Long> {

    List<RouteAssignmentHistoryEntity> findByRouteIdAndTenantIdAndIsDeletedFalseOrderByChangedAtDesc(
            Long routeId,
            Long tenantId);
}

