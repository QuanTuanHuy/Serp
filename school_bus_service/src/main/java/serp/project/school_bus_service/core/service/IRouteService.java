package serp.project.school_bus_service.core.service;

import serp.project.school_bus_service.application.dto.params.RoutePlanParamsRequest;
import serp.project.school_bus_service.infrastructure.store.model.RoutePlanEntity;
import serp.project.school_bus_service.application.dto.request.ManualDispatchRequest;
import serp.project.school_bus_service.application.dto.request.ReorderStopsRequest;
import serp.project.school_bus_service.application.dto.request.RouteAssignmentRequest;
import serp.project.school_bus_service.application.dto.request.RoutePlanUpsertRequest;
import serp.project.school_bus_service.application.dto.response.PageResponse;
import serp.project.school_bus_service.application.dto.response.RouteAssignmentResponse;
import serp.project.school_bus_service.application.dto.response.RouteAttendanceManifestResponse;
import serp.project.school_bus_service.application.dto.response.RouteDetailResponse;
import serp.project.school_bus_service.application.dto.response.RoutePathResponse;
import serp.project.school_bus_service.application.dto.response.RoutePlanResponse;
import serp.project.school_bus_service.application.dto.response.RouteStopResponse;

import java.util.List;

public interface IRouteService {

    RoutePlanEntity getRouteEntity(Long id, Long tenantId);

    PageResponse<RoutePlanResponse> getRoutes(RoutePlanParamsRequest params, Long tenantId);

    RouteDetailResponse getRoute(Long id, Long tenantId);

    RoutePathResponse getRoutePath(Long routeId, Long tenantId);

    RouteAttendanceManifestResponse getAttendanceManifest(Long routeId, Long tenantId);

    List<RouteStopResponse> getRouteStops(Long routeId, Long tenantId);

    RouteAssignmentResponse getRouteAssignment(Long routeId, Long tenantId);

    RoutePlanResponse createRoute(RoutePlanUpsertRequest request, Long tenantId, Long actorId);

    RoutePlanResponse updateRoute(Long id, RoutePlanUpsertRequest request, Long tenantId, Long actorId);

    List<RouteStopResponse> generateGreedyPlan(Long routeId, Long tenantId, Long actorId);

    List<RouteStopResponse> reorderRouteStops(Long routeId, ReorderStopsRequest request, Long tenantId, Long actorId);

    RouteAssignmentResponse assignRoute(Long routeId, RouteAssignmentRequest request, Long tenantId, Long actorId);

    RouteAssignmentResponse manualDispatchRoute(Long routeId, ManualDispatchRequest request, Long tenantId,
            Long actorId);

    RoutePathResponse computePath(Long routeId, Long tenantId, Long actorId);

    RoutePlanResponse startRoute(Long routeId, Long tenantId, Long actorId);

    RoutePlanResponse completeRoute(Long routeId, Long tenantId, Long actorId);
}

