package serp.project.school_bus_service.repository;

import serp.project.school_bus_service.entity.RoutePlanningIssueEntity;
import serp.project.school_bus_service.enums.PlanningIssueSeverity;
import serp.project.school_bus_service.shared.base.BaseRepository;

import java.util.List;

public interface RoutePlanningIssueRepository extends BaseRepository<RoutePlanningIssueEntity, Long> {

    List<RoutePlanningIssueEntity> findByPlanningSessionIdAndIsDeletedFalseOrderBySeverityAscId(
            Long planningSessionId);

    List<RoutePlanningIssueEntity> findByRouteIdAndIsDeletedFalseOrderBySeverityAscId(Long routeId);

    List<RoutePlanningIssueEntity> findByRouteIdAndSeverityAndIsDeletedFalse(
            Long routeId, PlanningIssueSeverity severity);

    long countByRouteIdAndSeverityAndIsDeletedFalseAndIsResolvedFalse(
            Long routeId, PlanningIssueSeverity severity);

    long countByRouteIdAndIsDeletedFalseAndIsResolvedFalse(Long routeId);

    List<RoutePlanningIssueEntity> findByRouteId(Long routeId);
}
