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
import serp.project.school_bus_service.core.service.IRouteService;
import serp.project.school_bus_service.kernel.shared.auth.AuthUtils;
import serp.project.school_bus_service.kernel.shared.base.AbstractBaseController;

@RestController
@RequestMapping("/school-bus/api/v1/routes")
public class RouteController extends AbstractBaseController {

    private final IRouteService routeService;

    public RouteController(IRouteService routeService, AuthUtils authUtils) {
        super(authUtils);
        this.routeService = routeService;
    }

    @GetMapping
    public ResponseEntity<?> getRoutes() {
        return ok("Fetched routes", routeService.getRoutes(getCurrentTenantId()));
    }

    @PostMapping
    public ResponseEntity<?> createRoute(@Valid @RequestBody RoutePlanUpsertRequest request) {
        requireAnyRole("SCHOOL_BUS_ADMIN", "SCHOOL_BUS_DISPATCHER");
        return created("Created route", routeService.createRoute(request, getCurrentTenantId(), getCurrentUserId()));
    }

    @GetMapping("/{id}")
    public ResponseEntity<?> getRoute(@PathVariable Long id) {
        return ok("Fetched route", routeService.getRoute(id, getCurrentTenantId()));
    }

    @PatchMapping("/{id}")
    public ResponseEntity<?> updateRoute(@PathVariable Long id, @Valid @RequestBody RoutePlanUpsertRequest request) {
        requireAnyRole("SCHOOL_BUS_ADMIN", "SCHOOL_BUS_DISPATCHER");
        return ok("Updated route", routeService.updateRoute(id, request, getCurrentTenantId(), getCurrentUserId()));
    }

    @PostMapping("/{id}/generate-greedy-plan")
    public ResponseEntity<?> generateGreedyPlan(@PathVariable Long id) {
        requireAnyRole("SCHOOL_BUS_ADMIN", "SCHOOL_BUS_DISPATCHER");
        return ok("Generated greedy route plan", routeService.generateGreedyPlan(id, getCurrentTenantId(), getCurrentUserId()));
    }

    @PostMapping("/{id}/assign")
    public ResponseEntity<?> assignRoute(@PathVariable Long id,
            @Valid @RequestBody RouteAssignmentRequest request) {
        requireAnyRole("SCHOOL_BUS_ADMIN", "SCHOOL_BUS_DISPATCHER");
        return ok("Assigned route", routeService.assignRoute(id, request, getCurrentTenantId(), getCurrentUserId()));
    }

    @PostMapping("/{id}/start")
    public ResponseEntity<?> startRoute(@PathVariable Long id) {
        requireAnyRole("SCHOOL_BUS_ADMIN", "SCHOOL_BUS_DISPATCHER", "SCHOOL_BUS_DRIVER", "SCHOOL_BUS_ATTENDANT");
        return ok("Started route", routeService.startRoute(id, getCurrentTenantId(), getCurrentUserId()));
    }

    @PostMapping("/{id}/complete")
    public ResponseEntity<?> completeRoute(@PathVariable Long id) {
        requireAnyRole("SCHOOL_BUS_ADMIN", "SCHOOL_BUS_DISPATCHER", "SCHOOL_BUS_DRIVER", "SCHOOL_BUS_ATTENDANT");
        return ok("Completed route", routeService.completeRoute(id, getCurrentTenantId(), getCurrentUserId()));
    }
}
