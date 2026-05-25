package serp.project.school_bus_service.service;
import serp.project.school_bus_service.entity.RoutePlanningSessionEntity;

import serp.project.school_bus_service.shared.base.IBaseService;

import serp.project.school_bus_service.dto.request.PlanningSessionCreateRequest;
import serp.project.school_bus_service.dto.request.PlanningSessionPreviewRequest;
import serp.project.school_bus_service.dto.request.GreedyGenerateRequest;
import serp.project.school_bus_service.dto.request.RoutePlanUpsertRequest;
import serp.project.school_bus_service.dto.response.EligibleStudentResponse;
import serp.project.school_bus_service.dto.response.GreedyGenerateResponse;
import serp.project.school_bus_service.dto.response.PlanningPreviewResponse;
import serp.project.school_bus_service.dto.response.PlanningSessionResponse;
import serp.project.school_bus_service.dto.response.RoutePlanResponse;

import java.util.List;

public interface IRoutePlanningSessionService extends IBaseService<RoutePlanningSessionEntity, Long> {

    /** Preview eligible demand without creating a session. */
    PlanningPreviewResponse preview(PlanningSessionPreviewRequest request, Long tenantId);

    /** Create a new planning session; throws 409 if active session already exists for same context. */
    PlanningSessionResponse createSession(PlanningSessionCreateRequest request, Long tenantId, Long actorId);

    /** List all active sessions for tenant. */
    List<PlanningSessionResponse> listSessions(Long tenantId);

    /** Get session detail. */
    PlanningSessionResponse getSession(Long sessionId, Long tenantId);

    /**
     * Run greedy route generation on the given session.
     * Clears any previously generated DRAFT/GENERATED routes in this session before regenerating.
     */
    GreedyGenerateResponse generateGreedy(Long sessionId, GreedyGenerateRequest request,
                                          Long tenantId, Long actorId);

    /** List routes belonging to a planning session. */
    List<RoutePlanResponse> listRoutesBySession(Long sessionId, Long tenantId);

    /** Create a new route linked to a MANUAL planning session. */
    RoutePlanResponse createRouteInSession(Long sessionId, RoutePlanUpsertRequest request,
                                           Long tenantId, Long actorId);

    /** List eligible students for a session (to populate the unassigned list for MANUAL mode). */
    List<EligibleStudentResponse> listEligibleStudents(Long sessionId, Long tenantId);

    /** Publish a session — marks all routes PUBLISHED (if no blocking issues and no unassigned students). */
    PlanningSessionResponse publishSession(Long sessionId, Long tenantId, Long actorId);

    /** Cancel a session — marks it CANCELLED and soft-deletes generated routes. */
    PlanningSessionResponse cancelSession(Long sessionId, Long tenantId, Long actorId);

    /**
     * Reload and persist summary counters for a session after manual edits.
     * Updates: totalPlannedStudents, totalUnassignedStudents, totalRoutes, totalStops.
     */
    void refreshSessionSummary(Long sessionId, Long tenantId);

    /** Internal: resolve session entity and validate it belongs to tenant; throws NOT_FOUND if missing. */
    RoutePlanningSessionEntity requireSession(Long sessionId, Long tenantId);
}

