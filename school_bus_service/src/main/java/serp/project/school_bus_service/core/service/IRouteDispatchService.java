package serp.project.school_bus_service.core.service;

import serp.project.school_bus_service.application.dto.request.ManualDispatchRequest;
import serp.project.school_bus_service.application.dto.request.RouteAssignmentRequest;
import serp.project.school_bus_service.application.dto.response.RouteAssignmentResponse;

public interface IRouteDispatchService {

    RouteAssignmentResponse assignRoute(Long routeId, RouteAssignmentRequest request, Long tenantId, Long actorId);

    RouteAssignmentResponse manualDispatchRoute(Long routeId, ManualDispatchRequest request, Long tenantId,
            Long actorId);
}
