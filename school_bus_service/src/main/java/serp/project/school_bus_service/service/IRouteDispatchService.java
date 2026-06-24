package serp.project.school_bus_service.service;

import serp.project.school_bus_service.shared.base.IBaseService;

import serp.project.school_bus_service.dto.request.ManualDispatchRequest;
import serp.project.school_bus_service.dto.request.RouteAssignmentRequest;
import serp.project.school_bus_service.dto.response.RouteAssignmentResponse;
import serp.project.school_bus_service.entity.RouteAssignmentEntity;

import java.util.Optional;

public interface IRouteDispatchService extends IBaseService<RouteAssignmentEntity, Long> {

    RouteAssignmentResponse assignRoute(Long routeId, RouteAssignmentRequest request, Long tenantId, Long actorId);

    RouteAssignmentResponse manualDispatchRoute(Long routeId, ManualDispatchRequest request, Long tenantId,
            Long actorId);

    /** Internal: returns the active assignment entity for a route. */
    Optional<RouteAssignmentEntity> findAssignmentEntityByRoute(Long routeId, Long tenantId);
}
