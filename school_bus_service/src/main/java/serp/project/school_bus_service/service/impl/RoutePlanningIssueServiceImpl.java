package serp.project.school_bus_service.service.impl;

import org.springframework.stereotype.Service;
import serp.project.school_bus_service.entity.RoutePlanningIssueEntity;
import serp.project.school_bus_service.enums.PlanningIssueSeverity;
import serp.project.school_bus_service.repository.RoutePlanningIssueRepository;
import serp.project.school_bus_service.service.IRoutePlanningIssueService;
import serp.project.school_bus_service.shared.base.AbstractBaseService;
import serp.project.school_bus_service.shared.base.BaseRepository;

import java.util.List;

@Service
public class RoutePlanningIssueServiceImpl extends AbstractBaseService<RoutePlanningIssueEntity, Long>
        implements IRoutePlanningIssueService {

    private final RoutePlanningIssueRepository routePlanningIssueRepository;

    public RoutePlanningIssueServiceImpl(RoutePlanningIssueRepository routePlanningIssueRepository) {
        this.routePlanningIssueRepository = routePlanningIssueRepository;
    }

    @Override
    protected BaseRepository<RoutePlanningIssueEntity, Long> getRepository() {
        return routePlanningIssueRepository;
    }

    @Override
    public void saveAll(List<RoutePlanningIssueEntity> issues) {
        routePlanningIssueRepository.saveAll(issues);
    }

    @Override
    public List<RoutePlanningIssueEntity> findByPlanningSession(Long planningSessionId) {
        return routePlanningIssueRepository
                .findByPlanningSessionIdAndIsDeletedFalseOrderBySeverityAscId(planningSessionId);
    }

    @Override
    public List<RoutePlanningIssueEntity> findByRoute(Long routeId) {
        return routePlanningIssueRepository.findByRouteIdAndIsDeletedFalseOrderBySeverityAscId(routeId);
    }

    @Override
    public long countBlockingByRoute(Long routeId) {
        return routePlanningIssueRepository.countByRouteIdAndSeverityAndIsDeletedFalseAndIsResolvedFalse(
                routeId, PlanningIssueSeverity.BLOCKING);
    }
}
