package serp.project.school_bus_service.service.impl;

import org.springframework.context.annotation.Lazy;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import serp.project.school_bus_service.dto.response.PlanningIssueResponse;
import serp.project.school_bus_service.dto.response.RouteManualValidationResponse;
import serp.project.school_bus_service.dto.response.RouteManualValidationResponse.StopValidationResponse;
import serp.project.school_bus_service.entity.RoutePlanEntity;
import serp.project.school_bus_service.entity.RoutePlanningIssueEntity;
import serp.project.school_bus_service.entity.RouteStopEntity;
import serp.project.school_bus_service.service.IRouteManualValidationService;
import serp.project.school_bus_service.service.IRoutePlanningIssueService;
import serp.project.school_bus_service.service.IRouteService;
import serp.project.school_bus_service.service.IRouteStopService;
import serp.project.school_bus_service.service.domain.IRouteGeometryService;
import serp.project.school_bus_service.shared.exception.AppErrorCode;
import serp.project.school_bus_service.shared.exception.AppException;
import serp.project.school_bus_service.shared.i18n.MessageCommon;

import java.util.ArrayList;
import java.util.List;

@Service
public class RouteManualValidationServiceImpl implements IRouteManualValidationService {

    private final IRouteService routeService;
    private final IRouteStopService routeStopService;
    private final IRouteGeometryService routeGeometryService;
    private final IRoutePlanningIssueService issueService;
    private final MessageCommon messageCommon;

    public RouteManualValidationServiceImpl(
            @Lazy IRouteService routeService,
            @Lazy IRouteStopService routeStopService,
            @Lazy IRouteGeometryService routeGeometryService,
            @Lazy IRoutePlanningIssueService issueService,
            MessageCommon messageCommon) {
        this.routeService = routeService;
        this.routeStopService = routeStopService;
        this.routeGeometryService = routeGeometryService;
        this.issueService = issueService;
        this.messageCommon = messageCommon;
    }

    @Override
    @Transactional
    public RouteManualValidationResponse validateRoute(Long routePlanId, Long tenantId) {
        RoutePlanEntity route = routeService.getRouteEntity(routePlanId, tenantId);
        List<RouteStopEntity> stops = routeStopService.findByRoute(routePlanId, tenantId);

        // Calculate travel metrics, stop arrival/departure times, and generate issues
        routeGeometryService.computeAndUpdate(route, stops);

        // Persist updated stop schedules and route details
        routeStopService.saveAllRouteStops(stops);
        routeService.saveRouteEntity(route);

        // Load persisted issues
        List<RoutePlanningIssueEntity> issueEntities = issueService.findByRoute(routePlanId);
        List<PlanningIssueResponse> issues = issueEntities.stream().map(this::mapIssue).toList();

        // Build stop validation details
        List<StopValidationResponse> stopResponses = stops.stream()
                .map(s -> mapStop(s, issues))
                .toList();

        int blockingCount = route.getBlockingIssueCount() != null ? route.getBlockingIssueCount() : 0;
        int warningCount = (int) issues.stream().filter(i -> "WARNING".equals(i.getSeverity())).count();

        RouteManualValidationResponse response = new RouteManualValidationResponse();
        response.setRoutePlanId(routePlanId);
        response.setRouteCode(route.getRouteCode());
        response.setRouteName(route.getRouteName());
        response.setValid(blockingCount == 0);
        response.setIssueCount(issues.size());
        response.setBlockingIssueCount(blockingCount);
        response.setWarningIssueCount(warningCount);
        response.setIssues(issues);
        response.setStops(stopResponses);

        return response;
    }

    @Override
    @Transactional(readOnly = true)
    public void validateBeforeAssignResources(Long routePlanId, Long tenantId) {
        RoutePlanEntity route = routeService.getRouteEntity(routePlanId, tenantId);
        if (route.getBlockingIssueCount() != null && route.getBlockingIssueCount() > 0) {
            throw new AppException(AppErrorCode.Route.ROUTE_HAS_BLOCKING_ISSUES,
                    messageCommon.getMessage(AppErrorCode.Route.ROUTE_HAS_BLOCKING_ISSUES));
        }
    }

    private PlanningIssueResponse mapIssue(RoutePlanningIssueEntity e) {
        PlanningIssueResponse r = new PlanningIssueResponse();
        r.setId(e.getId());
        r.setIssueType(e.getIssueType());
        r.setSeverity(e.getSeverity().name());
        r.setMessage(e.getMessage());
        r.setIsResolved(e.getIsResolved());
        if (e.getPlanningSession() != null) r.setPlanningSessionId(e.getPlanningSession().getId());
        if (e.getRoute() != null) r.setRouteId(e.getRoute().getId());
        if (e.getRouteStop() != null) r.setRouteStopId(e.getRouteStop().getId());
        if (e.getStudent() != null) {
            r.setStudentId(e.getStudent().getId());
            r.setStudentName(e.getStudent().getFullName());
        }
        if (e.getSubscription() != null) r.setSubscriptionId(e.getSubscription().getId());
        return r;
    }

    private StopValidationResponse mapStop(RouteStopEntity stop, List<PlanningIssueResponse> allIssues) {
        StopValidationResponse r = new StopValidationResponse();
        r.setStopId(stop.getId());
        r.setDisplayName(stop.getDisplayName());
        r.setStopOrder(stop.getStopOrder());
        r.setLocationType(stop.getLocationType() != null ? stop.getLocationType().name() : null);
        r.setPlannedArrivalTime(stop.getPlannedArrivalTime() != null ? stop.getPlannedArrivalTime().toString() : null);
        r.setPlannedDepartureTime(stop.getPlannedDepartureTime() != null ? stop.getPlannedDepartureTime().toString() : null);
        r.setTerminal(stop.getStopPurpose() != null && stop.getStopPurpose().isTerminal());

        List<PlanningIssueResponse> stopIssues = allIssues.stream()
                .filter(i -> stop.getId().equals(i.getRouteStopId()))
                .toList();
        r.setIssues(stopIssues);
        r.setIssueCount(stopIssues.size());
        r.setHasBlockingIssue(stopIssues.stream().anyMatch(i -> "BLOCKING".equals(i.getSeverity())));
        return r;
    }
}
