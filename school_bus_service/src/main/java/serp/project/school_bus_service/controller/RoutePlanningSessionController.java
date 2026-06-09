package serp.project.school_bus_service.controller;

import jakarta.validation.Valid;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.http.ResponseEntity;
import serp.project.school_bus_service.dto.request.PlanningSessionCreateRequest;
import serp.project.school_bus_service.dto.request.PlanningSessionPreviewRequest;
import serp.project.school_bus_service.dto.request.RoutePlanUpsertRequest;
import serp.project.school_bus_service.dto.response.EligibleStudentResponse;
import serp.project.school_bus_service.dto.response.GeneralResponse;
import serp.project.school_bus_service.dto.response.PlanningPreviewResponse;
import serp.project.school_bus_service.dto.response.PlanningSessionResponse;
import serp.project.school_bus_service.dto.response.RoutePlanResponse;
import serp.project.school_bus_service.service.IRoutePlanningSessionService;
import serp.project.school_bus_service.shared.auth.AuthUtils;
import serp.project.school_bus_service.shared.base.AbstractBaseController;

import java.util.List;

@RestController
@RequestMapping("/route-planning-sessions")
public class RoutePlanningSessionController extends AbstractBaseController {

    private final IRoutePlanningSessionService sessionService;

    public RoutePlanningSessionController(IRoutePlanningSessionService sessionService,
                                          AuthUtils authUtils) {
        super(authUtils);
        this.sessionService = sessionService;
    }

    @PostMapping("/preview")
    @PreAuthorize("@roleAuthorizer.hasPermission('school-bus.planning.write')")
    public ResponseEntity<GeneralResponse<PlanningPreviewResponse>> preview(
            @Valid @RequestBody PlanningSessionPreviewRequest request) {
        return ok("Eligible demand preview", sessionService.preview(request, getCurrentTenantId()));
    }

    @PostMapping
    @PreAuthorize("@roleAuthorizer.hasPermission('school-bus.planning.write')")
    public ResponseEntity<GeneralResponse<PlanningSessionResponse>> createSession(
            @Valid @RequestBody PlanningSessionCreateRequest request) {
        return created("Planning session created",
                sessionService.createSession(request, getCurrentTenantId(), getCurrentUserId()));
    }

    @GetMapping
    @PreAuthorize("@roleAuthorizer.hasPermission('school-bus.planning.read')")
    public ResponseEntity<GeneralResponse<List<PlanningSessionResponse>>> listSessions() {
        return ok("Planning sessions fetched", sessionService.listSessions(getCurrentTenantId()));
    }

    @GetMapping("/{id}")
    @PreAuthorize("@roleAuthorizer.hasPermission('school-bus.planning.read')")
    public ResponseEntity<GeneralResponse<PlanningSessionResponse>> getSession(
            @PathVariable Long id) {
        return ok("Planning session fetched", sessionService.getSession(id, getCurrentTenantId()));
    }

    @GetMapping("/{id}/routes")
    @PreAuthorize("@roleAuthorizer.hasPermission('school-bus.planning.read')")
    public ResponseEntity<GeneralResponse<List<RoutePlanResponse>>> listSessionRoutes(
            @PathVariable Long id) {
        return ok("Session routes fetched", sessionService.listRoutesBySession(id, getCurrentTenantId()));
    }

    @PostMapping("/{id}/routes")
    @PreAuthorize("@roleAuthorizer.hasPermission('school-bus.planning.write')")
    public ResponseEntity<GeneralResponse<RoutePlanResponse>> createSessionRoute(
            @PathVariable Long id,
            @Valid @RequestBody RoutePlanUpsertRequest request) {
        return created("Route created in session",
                sessionService.createRouteInSession(id, request, getCurrentTenantId(), getCurrentUserId()));
    }

    @GetMapping("/{id}/eligible-students")
    @PreAuthorize("@roleAuthorizer.hasPermission('school-bus.planning.read')")
    public ResponseEntity<GeneralResponse<List<EligibleStudentResponse>>> listEligibleStudents(
            @PathVariable Long id) {
        return ok("Eligible students fetched",
                sessionService.listEligibleStudents(id, getCurrentTenantId()));
    }

    @PostMapping("/{id}/publish")
    @PreAuthorize("@roleAuthorizer.hasPermission('school-bus.planning.publish')")
    public ResponseEntity<GeneralResponse<PlanningSessionResponse>> publishSession(
            @PathVariable Long id) {
        return ok("Session published",
                sessionService.publishSession(id, getCurrentTenantId(), getCurrentUserId()));
    }

    @PostMapping("/{id}/cancel")
    @PreAuthorize("@roleAuthorizer.hasPermission('school-bus.planning.write')")
    public ResponseEntity<GeneralResponse<PlanningSessionResponse>> cancelSession(
            @PathVariable Long id) {
        return ok("Session cancelled",
                sessionService.cancelSession(id, getCurrentTenantId(), getCurrentUserId()));
    }

    @DeleteMapping("/{sessionId}/routes/{routeId}")
    @PreAuthorize("@roleAuthorizer.hasPermission('school-bus.planning.write')")
    public ResponseEntity<GeneralResponse<Void>> deleteRoute(
            @PathVariable Long sessionId,
            @PathVariable Long routeId) {
        sessionService.deleteRouteInSession(sessionId, routeId, getCurrentTenantId(), getCurrentUserId());
        return ok("Route deleted successfully", null);
    }
}
