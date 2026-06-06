package serp.project.school_bus_service.service;

import serp.project.school_bus_service.shared.base.IBaseService;

import serp.project.school_bus_service.entity.RoutePlanningIssueEntity;
import serp.project.school_bus_service.enums.PlanningIssueSeverity;

import java.util.List;

public interface IRoutePlanningIssueService extends IBaseService<RoutePlanningIssueEntity, Long> {

    void saveAll(List<RoutePlanningIssueEntity> issues);

    List<RoutePlanningIssueEntity> findByPlanningSession(Long planningSessionId);

    List<RoutePlanningIssueEntity> findByRoute(Long routeId);

    long countBlockingByRoute(Long routeId);
}
