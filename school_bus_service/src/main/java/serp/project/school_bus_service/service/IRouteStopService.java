package serp.project.school_bus_service.service;

import serp.project.school_bus_service.shared.base.IBaseService;

import serp.project.school_bus_service.dto.request.AddRouteStopRequest;
import serp.project.school_bus_service.dto.request.AddStudentToStopRequest;
import serp.project.school_bus_service.dto.request.MoveStudentRequest;
import serp.project.school_bus_service.dto.request.ReorderStopsRequest;
import serp.project.school_bus_service.dto.response.RoutePlanStudentResponse;
import serp.project.school_bus_service.dto.response.RouteStopResponse;
import serp.project.school_bus_service.entity.RouteStopEntity;

import java.util.List;
import java.util.Optional;

public interface IRouteStopService extends IBaseService<RouteStopEntity, Long> {

    List<RouteStopResponse> reorderRouteStops(Long routeId, ReorderStopsRequest request, Long tenantId, Long actorId);

    RouteStopResponse addStop(Long routeId, AddRouteStopRequest request, Long tenantId, Long actorId);

    void removeStop(Long routeId, Long stopId, Long tenantId, Long actorId);

    /**
     * Assign a student to a route.
     * Auto-finds an existing stop at the student's relevant pickup/dropoff point,
     * or creates a new stop at the end of the route if none exists.
     * Validates: route editable, student eligible for session, not duplicate in session.
     */
    RoutePlanStudentResponse assignStudentToRoute(Long routeId,
                                                  AddStudentToStopRequest request,
                                                  Long tenantId, Long actorId);

    /**
     * Add a student to a specific stop on a route.
     * Auto-infers serviceAction from route direction (OUTBOUND→BOARD, RETURN→DROPOFF).
     * Validates: route editable, stop belongs to route, stop pickupPoint matches student's
     * relevantPoint for the direction, student is eligible for the session,
     * student is not already assigned in this session.
     * After success, refreshes session summary counters.
     */
    RoutePlanStudentResponse addStudentToStop(Long routeId, Long stopId,
                                              AddStudentToStopRequest request,
                                              Long tenantId, Long actorId);

    void moveStudent(Long sourceRouteId, MoveStudentRequest request, Long tenantId, Long actorId);

    void removeStudent(Long routeId, Long studentId, Long subscriptionId, Long tenantId, Long actorId);

    /** Internal: returns stops for a route ordered by stopOrder. */
    List<RouteStopEntity> findByRoute(Long routeId, Long tenantId);

    /** Internal: finds a single stop by id, returns empty if not found or deleted. */
    Optional<RouteStopEntity> findRouteStop(Long stopId, Long tenantId);

    /** Internal: persists a route stop entity (used by greedy planning). */
    RouteStopEntity saveRouteStop(RouteStopEntity entity);

    /** Internal: persists a list of route stop entities in a single batch. */
    List<RouteStopEntity> saveAllRouteStops(List<RouteStopEntity> entities);

    /** Internal: physically deletes a stop entity (used by greedy simulation). */
    void deletePhysical(Long id);

    /** Create or update terminal stops for a route plan and recalculate OSRM geometry. */
    void updateTerminalStops(serp.project.school_bus_service.entity.RoutePlanEntity route, Long tenantId, Long actorId);
}

