package serp.project.school_bus_service.repository;

import serp.project.school_bus_service.entity.RouteAssignmentHistoryEntity;
import serp.project.school_bus_service.shared.base.BaseRepository;

import java.util.List;

public interface RouteAssignmentHistoryRepository extends BaseRepository<RouteAssignmentHistoryEntity, Long> {

    List<RouteAssignmentHistoryEntity> findByRouteIdAndTenantIdAndIsDeletedFalseOrderByChangedAtDesc(
            Long routeId,
            Long tenantId);
}

