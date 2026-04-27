package serp.project.school_bus_service.core.service;

import serp.project.school_bus_service.application.dto.request.ReorderStopsRequest;
import serp.project.school_bus_service.application.dto.response.RoutePathResponse;
import serp.project.school_bus_service.application.dto.response.RouteStopResponse;

import java.util.List;

public interface IRouteStopService {

    List<RouteStopResponse> generateGreedyPlan(Long routeId, Long tenantId, Long actorId);

    List<RouteStopResponse> reorderRouteStops(Long routeId, ReorderStopsRequest request, Long tenantId, Long actorId);

    RoutePathResponse computePath(Long routeId, Long tenantId, Long actorId);
}
