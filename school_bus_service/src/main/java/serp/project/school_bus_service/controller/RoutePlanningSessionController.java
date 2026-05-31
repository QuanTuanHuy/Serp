package serp.project.school_bus_service.controller;

import jakarta.validation.Valid;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.http.ResponseEntity;
import serp.project.school_bus_service.dto.request.GreedyGenerateRequest;
import serp.project.school_bus_service.dto.request.PlanningSessionCreateRequest;
import serp.project.school_bus_service.dto.request.PlanningSessionPreviewRequest;
import serp.project.school_bus_service.dto.request.RoutePlanUpsertRequest;
import serp.project.school_bus_service.dto.response.EligibleStudentResponse;
import serp.project.school_bus_service.dto.response.GeneralResponse;
import serp.project.school_bus_service.dto.response.GreedyGenerateResponse;
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

    /** Preview eligible demand before creating a session. */
    @PostMapping("/preview")
    public ResponseEntity<GeneralResponse<PlanningPreviewResponse>> preview(
            @Valid @RequestBody PlanningSessionPreviewRequest request) {
        return ok("Eligible demand preview", sessionService.preview(request, getCurrentTenantId()));
    }

    /** Create a new planning session (409 if duplicate active session). */
    @PostMapping
    public ResponseEntity<GeneralResponse<PlanningSessionResponse>> createSession(
            @Valid @RequestBody PlanningSessionCreateRequest request) {
        return created("Planning session created",
                sessionService.createSession(request, getCurrentTenantId(), getCurrentUserId()));
    }

    /** List all planning sessions for the current tenant. */
    @GetMapping
    public ResponseEntity<GeneralResponse<List<PlanningSessionResponse>>> listSessions() {
        return ok("Planning sessions fetched", sessionService.listSessions(getCurrentTenantId()));
    }

    /** Get a single planning session by ID. */
    @GetMapping("/{id}")
    public ResponseEntity<GeneralResponse<PlanningSessionResponse>> getSession(
            @PathVariable Long id) {
        return ok("Planning session fetched", sessionService.getSession(id, getCurrentTenantId()));
    }

    /** Run greedy route generation on an existing session. */
    @PostMapping("/{id}/generate-greedy")
    public ResponseEntity<GeneralResponse<GreedyGenerateResponse>> generateGreedy(
            @PathVariable Long id,
            @RequestBody(required = false) GreedyGenerateRequest request) {
        GreedyGenerateRequest req = request != null ? request : new GreedyGenerateRequest();
        return ok("Greedy routes generated",
                sessionService.generateGreedy(id, req, getCurrentTenantId(), getCurrentUserId()));
    }

    /** List routes belonging to a planning session (works for both MANUAL and GREEDY). */
    @GetMapping("/{id}/routes")
    public ResponseEntity<GeneralResponse<List<RoutePlanResponse>>> listSessionRoutes(
            @PathVariable Long id) {
        return ok("Session routes fetched", sessionService.listRoutesBySession(id, getCurrentTenantId()));
    }

    /** Create a route manually in a MANUAL planning session. */
    @PostMapping("/{id}/routes")
    public ResponseEntity<GeneralResponse<RoutePlanResponse>> createSessionRoute(
            @PathVariable Long id,
            @Valid @RequestBody RoutePlanUpsertRequest request) {
        return created("Route created in session",
                sessionService.createRouteInSession(id, request, getCurrentTenantId(), getCurrentUserId()));
    }

    /** List eligible students for a session (for MANUAL assignment). */
    @GetMapping("/{id}/eligible-students")
    public ResponseEntity<GeneralResponse<List<EligibleStudentResponse>>> listEligibleStudents(
            @PathVariable Long id) {
        return ok("Eligible students fetched",
                sessionService.listEligibleStudents(id, getCurrentTenantId()));
    }

    /** Publish all routes in the session (fails if blocking issues exist or unassigned students remain). */
    @PostMapping("/{id}/publish")
    public ResponseEntity<GeneralResponse<PlanningSessionResponse>> publishSession(
            @PathVariable Long id) {
        return ok("Session published",
                sessionService.publishSession(id, getCurrentTenantId(), getCurrentUserId()));
    }

    /** Cancel the session and soft-delete its routes. */
    @PostMapping("/{id}/cancel")
    public ResponseEntity<GeneralResponse<PlanningSessionResponse>> cancelSession(
            @PathVariable Long id) {
        return ok("Session cancelled",
                sessionService.cancelSession(id, getCurrentTenantId(), getCurrentUserId()));
    }
}
