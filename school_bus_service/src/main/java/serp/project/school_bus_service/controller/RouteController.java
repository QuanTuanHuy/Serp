package serp.project.school_bus_service.controller;

import jakarta.validation.Valid;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.PatchMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import serp.project.school_bus_service.dto.params.RoutePlanParamsRequest;
import serp.project.school_bus_service.dto.request.AddRouteStopRequest;
import serp.project.school_bus_service.dto.request.AddStudentToStopRequest;
import serp.project.school_bus_service.dto.request.ManualDispatchRequest;
import serp.project.school_bus_service.dto.request.MoveStudentRequest;
import serp.project.school_bus_service.dto.request.ReorderStopsRequest;
import serp.project.school_bus_service.dto.request.RouteAssignmentRequest;
import serp.project.school_bus_service.dto.request.RoutePlanUpsertRequest;
import serp.project.school_bus_service.dto.response.GeneralResponse;
import serp.project.school_bus_service.dto.response.PageResponse;
import serp.project.school_bus_service.dto.response.RouteAssignmentResponse;
import serp.project.school_bus_service.dto.response.RouteDetailResponse;
import serp.project.school_bus_service.dto.response.RoutePlanResponse;
import serp.project.school_bus_service.dto.response.RoutePlanStudentResponse;
import serp.project.school_bus_service.dto.response.RoutePathResponse;
import serp.project.school_bus_service.dto.response.RouteStopResponse;
import serp.project.school_bus_service.service.IRouteService;
import serp.project.school_bus_service.shared.auth.AuthUtils;
import serp.project.school_bus_service.shared.base.AbstractBaseController;

import java.util.List;

@RestController
@RequestMapping("/routes")
public class RouteController extends AbstractBaseController {

    private final IRouteService routeService;

    public RouteController(IRouteService routeService, AuthUtils authUtils) {
        super(authUtils);
        this.routeService = routeService;
    }

    @GetMapping
    @PreAuthorize("@roleAuthorizer.hasPermission('school-bus.planning.read')")
    public ResponseEntity<GeneralResponse<PageResponse<RoutePlanResponse>>> getRoutes(
            @ModelAttribute RoutePlanParamsRequest params) {
        return ok("Fetched routes", routeService.getRoutes(params, getCurrentTenantId()));
    }

    @GetMapping("/{id}")
    @PreAuthorize("@roleAuthorizer.hasPermission('school-bus.planning.read')")
    public ResponseEntity<GeneralResponse<RouteDetailResponse>> getRoute(@PathVariable Long id) {
        return ok("Fetched route", routeService.getRoute(id, getCurrentTenantId()));
    }

    @GetMapping("/{id}/path")
    @PreAuthorize("@roleAuthorizer.hasPermission('school-bus.planning.read')")
    public ResponseEntity<GeneralResponse<RoutePathResponse>> getRoutePath(@PathVariable Long id) {
        return ok("Fetched route path", routeService.getRoutePath(id, getCurrentTenantId()));
    }

    @PatchMapping("/{id}")
    @PreAuthorize("@roleAuthorizer.hasPermission('school-bus.planning.write')")
    public ResponseEntity<GeneralResponse<RoutePlanResponse>> updateRoute(
            @PathVariable Long id,
            @Valid @RequestBody RoutePlanUpsertRequest request) {
        return ok("Updated route", routeService.updateRoute(id, request, getCurrentTenantId(), getCurrentUserId()));
    }

    @PostMapping("/{id}/assign")
    @PreAuthorize("@roleAuthorizer.hasPermission('school-bus.dispatch.assign')")
    public ResponseEntity<GeneralResponse<RouteAssignmentResponse>> assignRoute(
            @PathVariable Long id,
            @Valid @RequestBody RouteAssignmentRequest request) {
        return ok("Assigned route", routeService.assignRoute(id, request, getCurrentTenantId(), getCurrentUserId()));
    }

    @PostMapping("/{id}/manual-dispatch")
    @PreAuthorize("@roleAuthorizer.hasPermission('school-bus.dispatch.assign')")
    public ResponseEntity<GeneralResponse<RouteAssignmentResponse>> manualDispatchRoute(
            @PathVariable Long id,
            @Valid @RequestBody ManualDispatchRequest request) {
        return ok("Manually dispatched route",
                routeService.manualDispatchRoute(id, request, getCurrentTenantId(), getCurrentUserId()));
    }

    @PatchMapping("/{id}/stops/reorder")
    @PreAuthorize("@roleAuthorizer.hasPermission('school-bus.planning.write')")
    public ResponseEntity<GeneralResponse<List<RouteStopResponse>>> reorderRouteStops(
            @PathVariable Long id,
            @Valid @RequestBody ReorderStopsRequest request) {
        return ok("Reordered route stops",
                routeService.reorderRouteStops(id, request, getCurrentTenantId(), getCurrentUserId()));
    }

    // ── Manual editing endpoints ─────────────────────────────────────────

    @PostMapping("/{id}/stops")
    @PreAuthorize("@roleAuthorizer.hasPermission('school-bus.planning.write')")
    public ResponseEntity<GeneralResponse<RouteStopResponse>> addStop(
            @PathVariable Long id,
            @Valid @RequestBody AddRouteStopRequest request) {
        return created("Added stop", routeService.addStop(id, request, getCurrentTenantId(), getCurrentUserId()));
    }

    @DeleteMapping("/{id}/stops/{stopId}")
    @PreAuthorize("@roleAuthorizer.hasPermission('school-bus.planning.write')")
    public ResponseEntity<GeneralResponse<Void>> removeStop(
            @PathVariable Long id,
            @PathVariable Long stopId) {
        routeService.removeStop(id, stopId, getCurrentTenantId(), getCurrentUserId());
        return ok("Removed stop", null);
    }

    @PostMapping("/{id}/stops/{stopId}/students")
    @PreAuthorize("@roleAuthorizer.hasPermission('school-bus.planning.write')")
    public ResponseEntity<GeneralResponse<RoutePlanStudentResponse>> addStudentToStop(
            @PathVariable Long id,
            @PathVariable Long stopId,
            @Valid @RequestBody AddStudentToStopRequest request) {
        return created("Added student to stop",
                routeService.addStudentToStop(id, stopId, request, getCurrentTenantId(), getCurrentUserId()));
    }

    @PostMapping("/{id}/students/assign")
    @PreAuthorize("@roleAuthorizer.hasPermission('school-bus.planning.write')")
    public ResponseEntity<GeneralResponse<RoutePlanStudentResponse>> assignStudentToRoute(
            @PathVariable Long id,
            @Valid @RequestBody AddStudentToStopRequest request) {
        return created("Assigned student to route",
                routeService.assignStudentToRoute(id, request, getCurrentTenantId(), getCurrentUserId()));
    }

    @PostMapping("/{id}/students/move")
    @PreAuthorize("@roleAuthorizer.hasPermission('school-bus.planning.write')")
    public ResponseEntity<GeneralResponse<Void>> moveStudent(
            @PathVariable Long id,
            @Valid @RequestBody MoveStudentRequest request) {
        routeService.moveStudent(id, request, getCurrentTenantId(), getCurrentUserId());
        return ok("Moved student", null);
    }

    @DeleteMapping("/{id}/students/{studentId}")
    @PreAuthorize("@roleAuthorizer.hasPermission('school-bus.planning.write')")
    public ResponseEntity<GeneralResponse<Void>> removeStudent(
            @PathVariable Long id,
            @PathVariable Long studentId,
            @RequestParam Long subscriptionId) {
        routeService.removeStudent(id, studentId, subscriptionId, getCurrentTenantId(), getCurrentUserId());
        return ok("Removed student", null);
    }
}
