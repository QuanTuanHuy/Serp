package serp.project.school_bus_service.ui.controller;

import jakarta.validation.Valid;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PatchMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import serp.project.school_bus_service.application.dto.request.RouteAssignmentRequest;
import serp.project.school_bus_service.application.dto.request.RoutePlanUpsertRequest;
import serp.project.school_bus_service.application.dto.response.GeneralResponse;
import serp.project.school_bus_service.application.dto.response.RouteAssignmentResponse;
import serp.project.school_bus_service.application.dto.response.RouteDetailResponse;
import serp.project.school_bus_service.application.dto.response.RoutePlanResponse;
import serp.project.school_bus_service.application.dto.response.RouteStopResponse;
import serp.project.school_bus_service.core.service.IRouteService;
import serp.project.school_bus_service.kernel.shared.auth.AuthUtils;
import serp.project.school_bus_service.kernel.shared.base.AbstractBaseController;

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
    // @PreAuthorize("@roleAuthorizer.hasPermission('school-bus.route.read')")
    public ResponseEntity<GeneralResponse<List<RoutePlanResponse>>> getRoutes() {
        return ok("Fetched routes", routeService.getRoutes(getCurrentTenantId()));
    }

    @PostMapping
    // @PreAuthorize("@roleAuthorizer.hasPermission('school-bus.route.write')")
    public ResponseEntity<GeneralResponse<RoutePlanResponse>> createRoute(@Valid @RequestBody RoutePlanUpsertRequest request) {
        return created("Created route", routeService.createRoute(request, getCurrentTenantId(), getCurrentUserId()));
    }

    @GetMapping("/{id}")
    // @PreAuthorize("@roleAuthorizer.hasPermission('school-bus.route.read')")
    public ResponseEntity<GeneralResponse<RouteDetailResponse>> getRoute(@PathVariable Long id) {
        return ok("Fetched route", routeService.getRoute(id, getCurrentTenantId()));
    }

    @PatchMapping("/{id}")
    // @PreAuthorize("@roleAuthorizer.hasPermission('school-bus.route.write')")
    public ResponseEntity<GeneralResponse<RoutePlanResponse>> updateRoute(@PathVariable Long id, @Valid @RequestBody RoutePlanUpsertRequest request) {
        return ok("Updated route", routeService.updateRoute(id, request, getCurrentTenantId(), getCurrentUserId()));
    }

    @PostMapping("/{id}/generate-greedy-plan")
    // @PreAuthorize("@roleAuthorizer.hasPermission('school-bus.route.generate-plan')")
    public ResponseEntity<GeneralResponse<List<RouteStopResponse>>> generateGreedyPlan(@PathVariable Long id) {
        return ok("Generated greedy route plan", routeService.generateGreedyPlan(id, getCurrentTenantId(), getCurrentUserId()));
    }

    @PostMapping("/{id}/assign")
    // @PreAuthorize("@roleAuthorizer.hasPermission('school-bus.route.assign')")
    public ResponseEntity<GeneralResponse<RouteAssignmentResponse>> assignRoute(@PathVariable Long id,
            @Valid @RequestBody RouteAssignmentRequest request) {
        return ok("Assigned route", routeService.assignRoute(id, request, getCurrentTenantId(), getCurrentUserId()));
    }

    @PostMapping("/{id}/start")
    // @PreAuthorize("@roleAuthorizer.hasPermission('school-bus.route.start')")
    public ResponseEntity<GeneralResponse<RoutePlanResponse>> startRoute(@PathVariable Long id) {
        return ok("Started route", routeService.startRoute(id, getCurrentTenantId(), getCurrentUserId()));
    }

    @PostMapping("/{id}/complete")
    // @PreAuthorize("@roleAuthorizer.hasPermission('school-bus.route.complete')")
    public ResponseEntity<GeneralResponse<RoutePlanResponse>> completeRoute(@PathVariable Long id) {
        return ok("Completed route", routeService.completeRoute(id, getCurrentTenantId(), getCurrentUserId()));
    }
}
