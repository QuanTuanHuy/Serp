package serp.project.school_bus_service.service;
import serp.project.school_bus_service.entity.RoutePlanningSessionEntity;

import serp.project.school_bus_service.shared.base.IBaseService;

import serp.project.school_bus_service.dto.request.PlanningSessionCreateRequest;
import serp.project.school_bus_service.dto.request.PlanningSessionPreviewRequest;
import serp.project.school_bus_service.dto.request.RoutePlanUpsertRequest;
import serp.project.school_bus_service.dto.response.EligibleStudentResponse;
import serp.project.school_bus_service.dto.response.PlanningPreviewResponse;
import serp.project.school_bus_service.dto.response.PlanningSessionResponse;
import serp.project.school_bus_service.dto.response.RoutePlanResponse;

import java.util.List;

public interface IRoutePlanningSessionService extends IBaseService<RoutePlanningSessionEntity, Long> {

    PlanningPreviewResponse preview(PlanningSessionPreviewRequest request, Long tenantId);

    PlanningSessionResponse createSession(PlanningSessionCreateRequest request, Long tenantId, Long actorId);

    List<PlanningSessionResponse> listSessions(Long tenantId);

    PlanningSessionResponse getSession(Long sessionId, Long tenantId);

    List<RoutePlanResponse> listRoutesBySession(Long sessionId, Long tenantId);

    RoutePlanResponse createRouteInSession(Long sessionId, RoutePlanUpsertRequest request,
                                           Long tenantId, Long actorId);

    List<EligibleStudentResponse> listEligibleStudents(Long sessionId, Long tenantId);

    PlanningSessionResponse publishSession(Long sessionId, Long tenantId, Long actorId);

    PlanningSessionResponse cancelSession(Long sessionId, Long tenantId, Long actorId);

    void refreshSessionSummary(Long sessionId, Long tenantId);

    RoutePlanningSessionEntity requireSession(Long sessionId, Long tenantId);
}

