package serp.project.school_bus_service.service.impl;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import serp.project.school_bus_service.dto.request.PlanningSessionCreateRequest;
import serp.project.school_bus_service.dto.request.PlanningSessionPreviewRequest;
import serp.project.school_bus_service.dto.request.RoutePlanUpsertRequest;
import serp.project.school_bus_service.dto.response.EligibleStudentResponse;
import serp.project.school_bus_service.dto.response.PlanningDemandResponse;
import serp.project.school_bus_service.dto.response.PlanningPointResponse;
import serp.project.school_bus_service.dto.response.PlanningPreviewResponse;
import serp.project.school_bus_service.dto.response.PlanningReadinessSummary;
import serp.project.school_bus_service.dto.response.PlanningSessionResponse;
import serp.project.school_bus_service.dto.response.RoutePlanResponse;
import serp.project.school_bus_service.entity.PickupPointEntity;
import serp.project.school_bus_service.entity.RoutePlanEntity;
import serp.project.school_bus_service.entity.RoutePlanStudentEntity;
import serp.project.school_bus_service.entity.RoutePlanningSessionEntity;
import serp.project.school_bus_service.entity.SchoolEntity;
import serp.project.school_bus_service.entity.SchoolScheduleEntity;
import serp.project.school_bus_service.entity.StudentSubscriptionEntity;
import serp.project.school_bus_service.enums.PlanningMethod;
import serp.project.school_bus_service.enums.PlanningSessionStatus;
import serp.project.school_bus_service.enums.RouteDirection;
import serp.project.school_bus_service.enums.RouteStatus;
import serp.project.school_bus_service.repository.RoutePlanningSessionRepository;
import serp.project.school_bus_service.service.IRoutePlanStudentService;
import serp.project.school_bus_service.service.IRoutePlanningSessionService;
import serp.project.school_bus_service.service.IRouteService;
import serp.project.school_bus_service.service.IRouteStopService;
import serp.project.school_bus_service.service.ISchoolScheduleService;
import serp.project.school_bus_service.service.ISchoolService;
import serp.project.school_bus_service.shared.exception.AppErrorCode;
import serp.project.school_bus_service.shared.exception.AppException;
import serp.project.school_bus_service.shared.i18n.MessageCommon;
import serp.project.school_bus_service.shared.base.AbstractBaseService;
import serp.project.school_bus_service.shared.base.BaseRepository;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Service
public class RoutePlanningSessionServiceImpl extends AbstractBaseService<RoutePlanningSessionEntity, Long>
        implements IRoutePlanningSessionService {

    private static final Logger log = LoggerFactory.getLogger(RoutePlanningSessionServiceImpl.class);

    private final RoutePlanningSessionRepository sessionRepository;
    private final IRouteService routeService;
    private final IRouteStopService routeStopService;
    private final IRoutePlanStudentService routePlanStudentService;
    private final ISchoolService schoolService;
    private final ISchoolScheduleService scheduleService;
    private final MessageCommon messageCommon;

    public RoutePlanningSessionServiceImpl(RoutePlanningSessionRepository sessionRepository,
                                            IRouteService routeService,
                                            IRouteStopService routeStopService,
                                            IRoutePlanStudentService routePlanStudentService,
                                            ISchoolService schoolService,
                                            ISchoolScheduleService scheduleService,
                                            MessageCommon messageCommon) {
        this.sessionRepository = sessionRepository;
        this.routeService = routeService;
        this.routeStopService = routeStopService;
        this.routePlanStudentService = routePlanStudentService;
        this.schoolService = schoolService;
        this.scheduleService = scheduleService;
        this.messageCommon = messageCommon;
    }

    @Override
    protected BaseRepository<RoutePlanningSessionEntity, Long> getRepository() {
        return sessionRepository;
    }

    // ── Preview ─────────────────────────────────────────────────────────────

    @Override
    @Transactional(readOnly = true)
    public PlanningPreviewResponse preview(PlanningSessionPreviewRequest req, Long tenantId) {
        SchoolEntity school = schoolService.getSchool(req.getSchoolId(), tenantId);
        SchoolScheduleEntity schedule = scheduleService.getSchedule(req.getSchoolScheduleId(), tenantId);
        boolean isOutbound = "OUTBOUND".equalsIgnoreCase(req.getRouteDirection());

        List<StudentSubscriptionEntity> eligible = routePlanStudentService.findEligibleSubscriptions(
                req.getSchoolId(), req.getSchoolScheduleId(),
                req.getRouteDirection(), req.getServiceDate(), tenantId);

        // Build demands list
        List<PlanningDemandResponse> eligibleDemands = new ArrayList<>();
        Map<Long, PlanningPointResponse> pointMap = new HashMap<>();

        for (StudentSubscriptionEntity sub : eligible) {
            PlanningDemandResponse demand = new PlanningDemandResponse();
            demand.setSubscriptionId(sub.getId());
            demand.setSubscriptionCode(sub.getSubscriptionCode());
            demand.setStudentId(sub.getStudent().getId());
            demand.setStudentCode(sub.getStudent().getStudentCode());
            demand.setStudentName(sub.getStudent().getFullName());
            demand.setSchoolId(school.getId());
            demand.setSchoolName(school.getName());
            demand.setSchoolScheduleId(schedule.getId());
            demand.setScheduleCode(schedule.getScheduleCode());
            demand.setScheduleName(schedule.getScheduleName());
            demand.setTripOption(sub.getTripOption().name());

            // Resolve the relevant point based on direction
            PickupPointEntity point = isOutbound ? sub.getPickupPoint() : sub.getDropoffPoint();
            if (point != null) {
                demand.setPointId(point.getId());
                demand.setPointCode(point.getCode());
                demand.setPointName(point.getName());
                if (point.getLatitude() != null) demand.setLatitude(BigDecimal.valueOf(point.getLatitude()));
                if (point.getLongitude() != null) demand.setLongitude(BigDecimal.valueOf(point.getLongitude()));

                // Aggregate point info
                pointMap.computeIfAbsent(point.getId(), id -> {
                    PlanningPointResponse pr = new PlanningPointResponse();
                    pr.setPointId(point.getId());
                    pr.setPointCode(point.getCode());
                    pr.setPointName(point.getName());
                    if (point.getLatitude() != null) pr.setLatitude(BigDecimal.valueOf(point.getLatitude()));
                    if (point.getLongitude() != null) pr.setLongitude(BigDecimal.valueOf(point.getLongitude()));
                    pr.setPointRole(isOutbound ? "PICKUP" : "DROPOFF");
                    pr.setStudentCount(0);
                    return pr;
                });
                pointMap.get(point.getId()).setStudentCount(pointMap.get(point.getId()).getStudentCount() + 1);
            }

            eligibleDemands.add(demand);
        }

        // Build summary
        PlanningReadinessSummary summary = new PlanningReadinessSummary();
        summary.setTotalSubscriptions(eligible.size());
        summary.setEligibleStudents(eligible.size());
        summary.setPointCount(pointMap.size());
        summary.setPickupPointCount(isOutbound ? pointMap.size() : 0);
        summary.setDropoffPointCount(isOutbound ? 0 : pointMap.size());

        // Build response
        PlanningPreviewResponse response = new PlanningPreviewResponse();
        response.setSchoolId(school.getId());
        response.setSchoolCode(school.getCode());
        response.setSchoolName(school.getName());
        response.setSchoolAddress(school.getAddress());
        response.setSchoolScheduleId(schedule.getId());
        response.setScheduleCode(schedule.getScheduleCode());
        response.setScheduleName(schedule.getScheduleName());
        response.setShiftType(schedule.getShiftType());
        response.setArrivalDeadline(schedule.getArrivalDeadline());
        response.setDepartureTime(schedule.getDepartureTime());
        response.setEffectiveFrom(schedule.getEffectiveFrom());
        response.setEffectiveTo(schedule.getEffectiveTo());
        response.setServiceDate(req.getServiceDate());
        response.setServiceDayOfWeek(req.getServiceDate().getDayOfWeek());
        response.setDirection(req.getRouteDirection());
        response.setRouteDirection(req.getRouteDirection());
        response.setPlanningMethod(req.getPlanningMethod());
        response.setSummary(summary);
        response.setEligibleDemands(eligibleDemands);
        response.setPoints(new ArrayList<>(pointMap.values()));
        response.setTotalEligibleStudents(eligible.size());
        response.setTotalEligiblePickupPoints(pointMap.size());
        return response;
    }

    // ── Create session ───────────────────────────────────────────────────────

    @Override
    @Transactional
    public PlanningSessionResponse createSession(PlanningSessionCreateRequest req,
                                                 Long tenantId, Long actorId) {
        List<RoutePlanningSessionEntity> existing = sessionRepository.findActiveByContext(
                tenantId, req.getSchoolId(), req.getSchoolScheduleId(),
                req.getServiceDate(), RouteDirection.parse(req.getRouteDirection()));
        if (!existing.isEmpty()) {
            throw new AppException(AppErrorCode.Session.CONFLICT,
                    messageCommon.getMessage(AppErrorCode.Session.CONFLICT));
        }

        SchoolEntity school = schoolService.getSchool(req.getSchoolId(), tenantId);
        SchoolScheduleEntity schedule = scheduleService.getSchedule(req.getSchoolScheduleId(), tenantId);
        if (!schedule.getSchool().getId().equals(school.getId())) {
            throw new AppException(AppErrorCode.Session.SCHEDULE_MISMATCH,
                    messageCommon.getMessage(AppErrorCode.Session.SCHEDULE_MISMATCH));
        }

        RoutePlanningSessionEntity session = new RoutePlanningSessionEntity();
        session.markCreated(tenantId, actor(actorId));
        session.setSchool(school);
        session.setSchoolSchedule(schedule);
        session.setServiceDate(req.getServiceDate());
        session.setRouteDirection(RouteDirection.parse(req.getRouteDirection()));
        session.setPlanningMethod(PlanningMethod.parse(req.getPlanningMethod()));
        session.setStatus(PlanningSessionStatus.DRAFT);
        session.setPlanningNotes(req.getPlanningNotes());

        List<StudentSubscriptionEntity> eligible = routePlanStudentService.findEligibleSubscriptions(
                req.getSchoolId(), req.getSchoolScheduleId(),
                req.getRouteDirection(), req.getServiceDate(), tenantId);
        session.setTotalEligibleStudents(eligible.size());
        session.setTotalPlannedStudents(0);
        session.setTotalUnassignedStudents(eligible.size());

        return toResponse(sessionRepository.save(session));
    }

    // ── List / Get ───────────────────────────────────────────────────────────

    @Override
    @Transactional(readOnly = true)
    public List<PlanningSessionResponse> listSessions(Long tenantId) {
        return sessionRepository
                .findByTenantIdAndIsDeletedFalseOrderByServiceDateDescIdDesc(tenantId)
                .stream().map(this::toResponse).toList();
    }

    @Override
    @Transactional(readOnly = true)
    public PlanningSessionResponse getSession(Long sessionId, Long tenantId) {
        return toResponse(requireSession(sessionId, tenantId));
    }

    // ── Publish ──────────────────────────────────────────────────────────────

    @Override
    @Transactional
    public PlanningSessionResponse publishSession(Long sessionId, Long tenantId, Long actorId) {
        RoutePlanningSessionEntity session = requireSession(sessionId, tenantId);
        if (session.getStatus() == PlanningSessionStatus.PUBLISHED) {
            throw new AppException(AppErrorCode.Session.ALREADY_PUBLISHED,
                    messageCommon.getMessage(AppErrorCode.Session.ALREADY_PUBLISHED));
        }
        if (session.getStatus() == PlanningSessionStatus.CANCELLED) {
            throw new AppException(AppErrorCode.Session.CANNOT_PUBLISH_CANCELLED,
                    messageCommon.getMessage(AppErrorCode.Session.CANNOT_PUBLISH_CANCELLED));
        }

        List<RoutePlanEntity> routes = routeService.findRoutesBySession(sessionId, tenantId);
        if (routes.isEmpty()) {
            throw new AppException(AppErrorCode.Session.NO_ROUTES,
                    messageCommon.getMessage(AppErrorCode.Session.NO_ROUTES));
        }

        // Validate each route has at least 1 student
        for (RoutePlanEntity route : routes) {
            List<RoutePlanStudentEntity> students = routePlanStudentService.findByRoute(route.getId());
            if (students.isEmpty()) {
                throw new AppException(AppErrorCode.Session.ROUTE_NO_STUDENTS,
                        "Route '" + route.getRouteName() + "' has no assigned students");
            }
        }

        LocalDateTime now = LocalDateTime.now();
        for (RoutePlanEntity route : routes) {
            if (route.getStatus() != RouteStatus.CANCELLED) {
                route.setStatus(RouteStatus.PUBLISHED);
                route.setPublishedAt(now);
                route.setPublishedBy(actorId);
                route.markUpdated(actor(actorId));
                routeService.saveRouteEntity(route);
            }
        }

        session.setStatus(PlanningSessionStatus.PUBLISHED);
        session.setPublishedAt(now);
        session.setPublishedBy(actorId);
        session.markUpdated(actor(actorId));
        return toResponse(sessionRepository.save(session));
    }

    // ── Session route / student listing ──────────────────────────────────────

    @Override
    @Transactional(readOnly = true)
    public List<RoutePlanResponse> listRoutesBySession(Long sessionId, Long tenantId) {
        requireSession(sessionId, tenantId);
        return routeService.listRoutesBySession(sessionId, tenantId);
    }

    @Override
    public RoutePlanResponse createRouteInSession(Long sessionId, RoutePlanUpsertRequest request,
                                                   Long tenantId, Long actorId) {
        RoutePlanningSessionEntity session = requireSession(sessionId, tenantId);
        if (session.getPlanningMethod() != PlanningMethod.MANUAL) {
            throw new AppException(AppErrorCode.Session.NOT_MANUAL,
                    messageCommon.getMessage(AppErrorCode.Session.NOT_MANUAL));
        }
        requireSessionEditable(session);
        RoutePlanResponse response = routeService.createRouteInSession(request, sessionId, tenantId, actorId);
        refreshSessionSummary(sessionId, tenantId);
        return response;
    }

    @Override
    @Transactional(readOnly = true)
    public List<EligibleStudentResponse> listEligibleStudents(Long sessionId, Long tenantId) {
        RoutePlanningSessionEntity session = requireSession(sessionId, tenantId);
        List<StudentSubscriptionEntity> eligible = routePlanStudentService.findEligibleSubscriptions(
                session.getSchool().getId(),
                session.getSchoolSchedule().getId(),
                session.getRouteDirection().name(),
                session.getServiceDate(),
                tenantId);

        List<RoutePlanEntity> routes = routeService.findRoutesBySession(sessionId, tenantId);
        Map<Long, Long> studentRouteMap = new HashMap<>();
        for (RoutePlanEntity route : routes) {
            if (Boolean.TRUE.equals(route.getIsDeleted()) || Boolean.FALSE.equals(route.getIsActive())) {
                continue;
            }
            List<RoutePlanStudentEntity> rpsList = routePlanStudentService.findByRoute(route.getId());
            for (RoutePlanStudentEntity rps : rpsList) {
                if (rps.getStudent() != null && !Boolean.TRUE.equals(rps.getIsDeleted())) {
                    studentRouteMap.put(rps.getStudent().getId(), route.getId());
                }
            }
        }

        boolean isOutbound = session.getRouteDirection() == RouteDirection.OUTBOUND;

        return eligible.stream()
                .map(sub -> {
                    EligibleStudentResponse resp = new EligibleStudentResponse();
                    resp.setStudentId(sub.getStudent().getId());
                    resp.setStudentName(sub.getStudent().getFullName());
                    resp.setStudentCode(sub.getStudent().getStudentCode());
                    resp.setSubscriptionId(sub.getId());
                    resp.setSubscriptionCode(sub.getSubscriptionCode());
                    resp.setTripOption(sub.getTripOption().name());

                    // Pickup point
                    if (sub.getPickupPoint() != null) {
                        resp.setPickupPointId(sub.getPickupPoint().getId());
                        resp.setPickupPointName(sub.getPickupPoint().getName());
                        resp.setPickupPointLatitude(sub.getPickupPoint().getLatitude());
                        resp.setPickupPointLongitude(sub.getPickupPoint().getLongitude());
                    }

                    // Dropoff point
                    if (sub.getDropoffPoint() != null) {
                        resp.setDropoffPointId(sub.getDropoffPoint().getId());
                        resp.setDropoffPointName(sub.getDropoffPoint().getName());
                        resp.setDropoffPointLatitude(sub.getDropoffPoint().getLatitude());
                        resp.setDropoffPointLongitude(sub.getDropoffPoint().getLongitude());
                    }

                    // Relevant point based on direction
                    var relevantPoint = isOutbound ? sub.getPickupPoint() : sub.getDropoffPoint();
                    if (relevantPoint != null) {
                        resp.setRelevantPointId(relevantPoint.getId());
                        resp.setRelevantPointName(relevantPoint.getName());
                        resp.setRelevantPointLatitude(relevantPoint.getLatitude());
                        resp.setRelevantPointLongitude(relevantPoint.getLongitude());
                    }

                    resp.setSpecialNote(sub.getStudent().getSpecialNote());

                    Long routeId = studentRouteMap.get(sub.getStudent().getId());
                    resp.setAssigned(routeId != null);
                    resp.setAssignedRouteId(routeId);
                    return resp;
                })
                .toList();
    }

    // ── Cancel ───────────────────────────────────────────────────────────────

    @Override
    @Transactional
    public PlanningSessionResponse cancelSession(Long sessionId, Long tenantId, Long actorId) {
        RoutePlanningSessionEntity session = requireSession(sessionId, tenantId);
        if (session.getStatus() == PlanningSessionStatus.CANCELLED) {
            throw new AppException(AppErrorCode.Session.ALREADY_CANCELLED,
                    messageCommon.getMessage(AppErrorCode.Session.ALREADY_CANCELLED));
        }
        softDeleteExistingRoutes(session, tenantId, actorId);
        session.setStatus(PlanningSessionStatus.CANCELLED);
        session.markUpdated(actor(actorId));
        return toResponse(sessionRepository.save(session));
    }

    @Override
    @Transactional
    public void deleteRouteInSession(Long sessionId, Long routeId, Long tenantId, Long actorId) {
        RoutePlanningSessionEntity session = requireSession(sessionId, tenantId);
        requireSessionEditable(session);
        routeService.deleteRoute(sessionId, routeId, tenantId, actorId);
        refreshSessionSummary(sessionId, tenantId);
    }

    // ── Helpers ──────────────────────────────────────────────────────────────

    private void requireSessionEditable(RoutePlanningSessionEntity session) {
        if (session.getStatus() == PlanningSessionStatus.PUBLISHED
                || session.getStatus() == PlanningSessionStatus.CANCELLED) {
            throw new AppException(AppErrorCode.Session.FROZEN,
                    messageCommon.getMessage(AppErrorCode.Session.FROZEN, session.getStatus()));
        }
    }

    private void softDeleteExistingRoutes(RoutePlanningSessionEntity session, Long tenantId, Long actorId) {
        List<RoutePlanEntity> old = routeService.findRoutesBySession(session.getId(), tenantId);
        for (RoutePlanEntity r : old) {
            r.markSoftDeleted(actor(actorId));
            routeService.saveRouteEntity(r);
        }
    }

    @Override
    @Transactional(readOnly = true)
    public RoutePlanningSessionEntity requireSession(Long id, Long tenantId) {
        return sessionRepository.findByIdAndTenantIdAndIsDeletedFalse(id, tenantId)
                .orElseThrow(() -> new AppException(AppErrorCode.NOT_FOUND,
                        "Planning session not found: " + id));
    }

    private PlanningSessionResponse toResponse(RoutePlanningSessionEntity e) {
        PlanningSessionResponse r = new PlanningSessionResponse();
        r.setId(e.getId());
        r.setSchoolId(e.getSchool().getId());
        r.setSchoolName(e.getSchool().getName());
        r.setSchoolScheduleId(e.getSchoolSchedule().getId());
        r.setSchoolScheduleName(e.getSchoolSchedule().getScheduleName());
        r.setServiceDate(e.getServiceDate());
        r.setRouteDirection(e.getRouteDirection().name());
        r.setPlanningMethod(e.getPlanningMethod().name());
        r.setStatus(e.getStatus().name());
        r.setTotalEligibleStudents(e.getTotalEligibleStudents());
        r.setTotalPlannedStudents(e.getTotalPlannedStudents());
        r.setTotalUnassignedStudents(e.getTotalUnassignedStudents());
        r.setTotalRoutes(e.getTotalRoutes());
        r.setTotalStops(e.getTotalStops());
        r.setTotalDistanceKm(e.getTotalDistanceKm());
        r.setTotalDurationMin(e.getTotalDurationMin());
        r.setGeneratedAt(e.getGeneratedAt());
        r.setPublishedAt(e.getPublishedAt());
        r.setPlanningNotes(e.getPlanningNotes());
        r.setCreatedAt(e.getCreatedAt());
        r.setUpdatedAt(e.getUpdatedAt());
        return r;
    }

    // ── Refresh session summary ──────────────────────────────────────────────

    @Override
    @Transactional
    public void refreshSessionSummary(Long sessionId, Long tenantId) {
        RoutePlanningSessionEntity session = requireSession(sessionId, tenantId);
        long planned = routePlanStudentService.countDistinctStudentsBySession(sessionId);
        long routes = routePlanStudentService.countRoutesBySession(sessionId);
        long stops = routePlanStudentService.countStopsBySession(sessionId);
        int eligible = session.getTotalEligibleStudents() != null ? session.getTotalEligibleStudents() : 0;
        long unassigned = Math.max(0, eligible - planned);
        List<RoutePlanEntity> activeRoutes = routeService.findRoutesBySession(sessionId, tenantId);
        double totalDistKm = activeRoutes.stream()
                .mapToDouble(r -> r.getPlannedDistanceKm() != null ? r.getPlannedDistanceKm() : 0.0)
                .sum();
        int totalDurMin = activeRoutes.stream()
                .mapToInt(r -> r.getPlannedDurationMin() != null ? r.getPlannedDurationMin() : 0)
                .sum();
        session.setTotalPlannedStudents((int) planned);
        session.setTotalUnassignedStudents((int) unassigned);
        session.setTotalRoutes((int) routes);
        session.setTotalStops((int) stops);
        session.setTotalDistanceKm(totalDistKm > 0 ? totalDistKm : null);
        session.setTotalDurationMin(totalDurMin > 0 ? totalDurMin : null);
        sessionRepository.save(session);
    }
}
