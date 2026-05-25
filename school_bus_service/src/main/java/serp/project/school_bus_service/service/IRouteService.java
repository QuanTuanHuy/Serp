package serp.project.school_bus_service.service;

import serp.project.school_bus_service.shared.base.IBaseService;

import serp.project.school_bus_service.dto.params.RoutePlanParamsRequest;
import serp.project.school_bus_service.entity.RoutePlanEntity;
import serp.project.school_bus_service.dto.request.AddRouteStopRequest;
import serp.project.school_bus_service.dto.request.AddStudentToStopRequest;
import serp.project.school_bus_service.dto.request.ManualDispatchRequest;
import serp.project.school_bus_service.dto.request.MoveStudentRequest;
import serp.project.school_bus_service.dto.request.ReorderStopsRequest;
import serp.project.school_bus_service.dto.request.RouteAssignmentRequest;
import serp.project.school_bus_service.dto.request.RoutePlanUpsertRequest;
import serp.project.school_bus_service.dto.response.PageResponse;
import serp.project.school_bus_service.dto.response.AssignmentHistoryResponse;
import serp.project.school_bus_service.dto.response.RouteAssignmentResponse;
import serp.project.school_bus_service.dto.response.RouteAttendanceManifestResponse;
import serp.project.school_bus_service.dto.response.RouteDetailResponse;
import serp.project.school_bus_service.dto.response.RoutePathResponse;
import serp.project.school_bus_service.dto.response.RoutePlanResponse;
import serp.project.school_bus_service.dto.response.RoutePlanStudentResponse;
import serp.project.school_bus_service.dto.response.RouteStopResponse;
import serp.project.school_bus_service.enums.RouteStatus;

import java.util.List;

public interface IRouteService extends IBaseService<RoutePlanEntity, Long> {

    RoutePlanEntity getRouteEntity(Long id, Long tenantId);

    PageResponse<RoutePlanResponse> getRoutes(RoutePlanParamsRequest params, Long tenantId);

    RouteDetailResponse getRoute(Long id, Long tenantId);

    RoutePathResponse getRoutePath(Long routeId, Long tenantId);

    RouteAttendanceManifestResponse getAttendanceManifest(Long routeId, Long tenantId);

    List<RouteStopResponse> getRouteStops(Long routeId, Long tenantId);

    RouteAssignmentResponse getRouteAssignment(Long routeId, Long tenantId);

    RoutePlanResponse updateRoute(Long id, RoutePlanUpsertRequest request, Long tenantId, Long actorId);

    List<RouteStopResponse> reorderRouteStops(Long routeId, ReorderStopsRequest request, Long tenantId, Long actorId);

    RouteAssignmentResponse assignRoute(Long routeId, RouteAssignmentRequest request, Long tenantId, Long actorId);

    RouteAssignmentResponse manualDispatchRoute(Long routeId, ManualDispatchRequest request, Long tenantId,
            Long actorId);

    RoutePathResponse computePath(Long routeId, Long tenantId, Long actorId);

    RoutePlanResponse startRoute(Long routeId, Long tenantId, Long actorId);

    RoutePlanResponse completeRoute(Long routeId, Long tenantId, Long actorId);

    List<AssignmentHistoryResponse> getAssignmentHistory(Long routeId, Long tenantId);

    // Manual editing
    RouteStopResponse addStop(Long routeId, AddRouteStopRequest request, Long tenantId, Long actorId);

    void removeStop(Long routeId, Long stopId, Long tenantId, Long actorId);

    /** Manually assign student to route — auto-creates stop if needed. */
    RoutePlanStudentResponse assignStudentToRoute(Long routeId,
                                                  AddStudentToStopRequest request,
                                                  Long tenantId, Long actorId);

    RoutePlanStudentResponse addStudentToStop(Long routeId, Long stopId,
                                              AddStudentToStopRequest request,
                                              Long tenantId, Long actorId);

    void moveStudent(Long sourceRouteId, MoveStudentRequest request, Long tenantId, Long actorId);

    void removeStudent(Long routeId, Long studentId, Long subscriptionId, Long tenantId, Long actorId);


    // Internal: persist entity changes from sub-services
    RoutePlanEntity saveRouteEntity(RoutePlanEntity entity);

    // Internal: find routes belonging to a planning session
    List<RoutePlanEntity> findRoutesBySession(Long sessionId, Long tenantId);

    // List routes by session as DTOs
    List<RoutePlanResponse> listRoutesBySession(Long sessionId, Long tenantId);

    // Create a route linked to a planning session (MANUAL mode)
    RoutePlanResponse createRouteInSession(RoutePlanUpsertRequest request, Long sessionId, Long tenantId, Long actorId);

    long countByTenantAndStatus(Long tenantId, RouteStatus status);
}

