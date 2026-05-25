package serp.project.school_bus_service.service.impl;

import org.springframework.context.annotation.Lazy;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import serp.project.school_bus_service.dto.response.RoutePlanResponse;
import serp.project.school_bus_service.entity.RouteAssignmentEntity;
import serp.project.school_bus_service.entity.RoutePlanEntity;
import serp.project.school_bus_service.enums.RouteStatus;
import serp.project.school_bus_service.mapper.SchoolBusMapper;
import serp.project.school_bus_service.service.IAuditLogService;
import serp.project.school_bus_service.service.IRouteDispatchService;
import serp.project.school_bus_service.service.IRouteLifecycleService;
import serp.project.school_bus_service.service.IRouteService;
import serp.project.school_bus_service.service.ITripHistoryService;
import serp.project.school_bus_service.shared.exception.AppErrorCode;
import serp.project.school_bus_service.shared.exception.AppException;
import serp.project.school_bus_service.shared.i18n.MessageCommon;

import java.time.LocalDateTime;

@Service
public class RouteLifecycleServiceImpl implements IRouteLifecycleService {

    private final IRouteService routeService;
    private final IRouteDispatchService routeDispatchService;
    private final ITripHistoryService tripHistoryService;
    private final IAuditLogService auditLogService;
    private final SchoolBusMapper mapper;
    private final MessageCommon messageCommon;

    public RouteLifecycleServiceImpl(@Lazy IRouteService routeService,
                                      @Lazy IRouteDispatchService routeDispatchService,
                                      ITripHistoryService tripHistoryService,
                                      IAuditLogService auditLogService,
                                      SchoolBusMapper mapper,
                                      MessageCommon messageCommon) {
        this.routeService = routeService;
        this.routeDispatchService = routeDispatchService;
        this.tripHistoryService = tripHistoryService;
        this.auditLogService = auditLogService;
        this.mapper = mapper;
        this.messageCommon = messageCommon;
    }

    @Override
    @Transactional
    public RoutePlanResponse startRoute(Long routeId, Long tenantId, Long actorId) {
        RoutePlanEntity route = routeService.getRouteEntity(routeId, tenantId);
        if (route.getStatus() != RouteStatus.ASSIGNED) {
            throw new AppException(AppErrorCode.Route.INVALID_STATE, messageCommon.getMessage(AppErrorCode.Route.INVALID_STATE));
        }
        route.markUpdated(actor(actorId));
        route.setStatus(RouteStatus.IN_PROGRESS);
        route.setStartedAt(LocalDateTime.now());
        routeService.saveRouteEntity(route);
        auditLogService.log(tenantId, actorId, "RoutePlan", route.getId(), "START", "Started route");
        return mapper.toRoutePlanResponse(route);
    }

    @Override
    @Transactional
    public RoutePlanResponse completeRoute(Long routeId, Long tenantId, Long actorId) {
        RoutePlanEntity route = routeService.getRouteEntity(routeId, tenantId);
        if (route.getStatus() != RouteStatus.IN_PROGRESS) {
            throw new AppException(AppErrorCode.Route.INVALID_STATE, messageCommon.getMessage(AppErrorCode.Route.INVALID_STATE));
        }

        route.markUpdated(actor(actorId));
        route.setStatus(RouteStatus.COMPLETED);
        route.setCompletedAt(LocalDateTime.now());
        routeService.saveRouteEntity(route);

        RouteAssignmentEntity assignment = routeDispatchService
                .findAssignmentEntityByRoute(routeId, tenantId)
                .orElse(null);

        tripHistoryService.recordCompletedRoute(route, assignment, tenantId, actorId);

        auditLogService.log(tenantId, actorId, "RoutePlan", route.getId(), "COMPLETE",
                "Completed route and wrote trip history");
        return mapper.toRoutePlanResponse(route);
    }

    private String actor(Long actorId) {
        return actorId == null ? "SYSTEM" : String.valueOf(actorId);
    }
}
