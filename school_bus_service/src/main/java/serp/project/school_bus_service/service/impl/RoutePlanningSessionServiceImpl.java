package serp.project.school_bus_service.service.impl;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import serp.project.school_bus_service.dto.request.GreedyGenerateRequest;
import serp.project.school_bus_service.dto.request.PlanningSessionCreateRequest;
import serp.project.school_bus_service.dto.request.PlanningSessionPreviewRequest;
import serp.project.school_bus_service.dto.request.RoutePlanUpsertRequest;
import serp.project.school_bus_service.dto.response.EligibleStudentResponse;
import serp.project.school_bus_service.dto.response.GreedyGenerateResponse;
import serp.project.school_bus_service.dto.response.PlanningIssueResponse;
import serp.project.school_bus_service.dto.response.PlanningPreviewResponse;
import serp.project.school_bus_service.dto.response.PlanningSessionResponse;
import serp.project.school_bus_service.dto.response.RouteQualityResponse;
import serp.project.school_bus_service.dto.response.RoutePlanResponse;
import serp.project.school_bus_service.entity.PickupPointEntity;
import serp.project.school_bus_service.entity.DepotEntity;
import serp.project.school_bus_service.entity.RoutePlanEntity;
import serp.project.school_bus_service.entity.RoutePlanningIssueEntity;
import serp.project.school_bus_service.entity.RoutePlanningSessionEntity;
import serp.project.school_bus_service.entity.RoutePlanStudentEntity;
import serp.project.school_bus_service.entity.RouteStopEntity;
import serp.project.school_bus_service.entity.SchoolEntity;
import serp.project.school_bus_service.entity.SchoolScheduleEntity;
import serp.project.school_bus_service.entity.StudentSubscriptionEntity;
import serp.project.school_bus_service.enums.PlanningIssueSeverity;
import serp.project.school_bus_service.enums.PlanningMethod;
import serp.project.school_bus_service.enums.PlanningSessionStatus;
import serp.project.school_bus_service.enums.RouteDirection;
import serp.project.school_bus_service.enums.RouteGenerationMethod;
import serp.project.school_bus_service.enums.RouteLocationType;
import serp.project.school_bus_service.enums.RoutePlanStudentAction;
import serp.project.school_bus_service.enums.RouteStatus;
import serp.project.school_bus_service.enums.RouteStopPurpose;
import serp.project.school_bus_service.enums.ShiftType;
import serp.project.school_bus_service.repository.RoutePlanningSessionRepository;
import serp.project.school_bus_service.service.algorithm.GreedyPlanInput;
import serp.project.school_bus_service.service.algorithm.GreedyPlanResult;
import serp.project.school_bus_service.service.algorithm.GreedyRouteBatch;
import serp.project.school_bus_service.service.algorithm.GreedyStopAssignment;
import serp.project.school_bus_service.service.algorithm.IGreedyRoutePlanningService;
import serp.project.school_bus_service.service.ICodeGeneratorService;
import serp.project.school_bus_service.service.IDepotService;
import serp.project.school_bus_service.service.domain.IRouteEligibilityService;
import serp.project.school_bus_service.service.domain.IRouteGeometryService;
import serp.project.school_bus_service.service.domain.RouteStopFactory;
import serp.project.school_bus_service.service.IRoutePlanningIssueService;
import serp.project.school_bus_service.service.IRoutePlanningSessionService;
import serp.project.school_bus_service.service.IRoutePlanStudentService;
import serp.project.school_bus_service.service.IRouteService;
import serp.project.school_bus_service.service.IRouteStopService;
import serp.project.school_bus_service.service.ISchoolScheduleService;
import serp.project.school_bus_service.service.ISchoolService;
import serp.project.school_bus_service.shared.code.SchoolBusCode;
import serp.project.school_bus_service.shared.exception.AppErrorCode;
import serp.project.school_bus_service.shared.exception.AppException;
import serp.project.school_bus_service.shared.i18n.MessageCommon;
import serp.project.school_bus_service.shared.base.AbstractBaseService;
import serp.project.school_bus_service.shared.base.BaseRepository;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

@Service
public class RoutePlanningSessionServiceImpl extends AbstractBaseService<RoutePlanningSessionEntity, Long>
        implements IRoutePlanningSessionService {

    private static final Logger log = LoggerFactory.getLogger(RoutePlanningSessionServiceImpl.class);
    private static final int DEFAULT_CAPACITY = 30;

    private final RoutePlanningSessionRepository sessionRepository;
    private final IRouteService routeService;
    private final IRouteStopService routeStopService;
    private final IRoutePlanStudentService routePlanStudentService;
    private final IRoutePlanningIssueService issueService;
    private final ISchoolService schoolService;
    private final ISchoolScheduleService scheduleService;
    private final IRouteEligibilityService eligibilityService;
    private final IGreedyRoutePlanningService greedyService;
    private final IRouteGeometryService routeGeometryService;
    private final ICodeGeneratorService codeGeneratorService;
    private final IDepotService depotService;
    private final RouteStopFactory routeStopFactory;
    private final MessageCommon messageCommon;

    public RoutePlanningSessionServiceImpl(RoutePlanningSessionRepository sessionRepository,
                                            IRouteService routeService,
                                            IRouteStopService routeStopService,
                                            IRoutePlanStudentService routePlanStudentService,
                                            IRoutePlanningIssueService issueService,
                                            ISchoolService schoolService,
                                            ISchoolScheduleService scheduleService,
                                            IRouteEligibilityService eligibilityService,
                                            IGreedyRoutePlanningService greedyService,
                                            IRouteGeometryService routeGeometryService,
                                            ICodeGeneratorService codeGeneratorService,
                                            IDepotService depotService,
                                            RouteStopFactory routeStopFactory,
                                            MessageCommon messageCommon) {
        this.sessionRepository = sessionRepository;
        this.routeService = routeService;
        this.routeStopService = routeStopService;
        this.routePlanStudentService = routePlanStudentService;
        this.issueService = issueService;
        this.schoolService = schoolService;
        this.scheduleService = scheduleService;
        this.eligibilityService = eligibilityService;
        this.greedyService = greedyService;
        this.routeGeometryService = routeGeometryService;
        this.codeGeneratorService = codeGeneratorService;
        this.depotService = depotService;
        this.routeStopFactory = routeStopFactory;
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
        return eligibilityService.buildPreview(
                req.getSchoolId(), req.getSchoolScheduleId(),
                req.getRouteDirection(), req.getServiceDate(), tenantId);
    }

    // ── Create session ───────────────────────────────────────────────────────

    @Override
    @Transactional
    public PlanningSessionResponse createSession(PlanningSessionCreateRequest req,
                                                 Long tenantId, Long actorId) {
        // 409 if active session already exists for same context
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

        // Load eligible count
        List<StudentSubscriptionEntity> eligible = eligibilityService.findEligible(
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

    // ── Greedy generation ────────────────────────────────────────────────────

    @Override
    @Transactional
    public GreedyGenerateResponse generateGreedy(Long sessionId, GreedyGenerateRequest req,
                                                 Long tenantId, Long actorId) {
        RoutePlanningSessionEntity session = requireSession(sessionId, tenantId);
        if (session.getStatus() == PlanningSessionStatus.PUBLISHED ||
            session.getStatus() == PlanningSessionStatus.CANCELLED) {
            throw new AppException(AppErrorCode.Session.CANNOT_GENERATE,
                    messageCommon.getMessage(AppErrorCode.Session.CANNOT_GENERATE, session.getStatus()));
        }

        int capacity = (req.getDefaultBusCapacity() != null && req.getDefaultBusCapacity() > 0)
                ? req.getDefaultBusCapacity() : DEFAULT_CAPACITY;
        boolean usingDefault = req.getDefaultBusCapacity() == null || req.getDefaultBusCapacity() <= 0;

        // Resolve depot — required for OUTBOUND (start=DEPOT) and RETURN (end=DEPOT)
        if (req.getDepotId() == null) {
            throw new AppException(AppErrorCode.Route.DEPOT_REQUIRED,
                    messageCommon.getMessage(AppErrorCode.Route.DEPOT_REQUIRED, "depot"));
        }
        DepotEntity depot = depotService.getDepot(req.getDepotId(), tenantId);

        // Soft-delete all existing routes/stops/students/issues from this session
        softDeleteExistingRoutes(session, tenantId, actorId);

        // Load eligible subscriptions
        List<StudentSubscriptionEntity> eligible = eligibilityService.findEligible(
                session.getSchool().getId(), session.getSchoolSchedule().getId(),
                session.getRouteDirection().name(), session.getServiceDate(), tenantId);

        boolean isOutbound = session.getRouteDirection() == RouteDirection.OUTBOUND;

        // Delegate grouping + partitioning to the algorithm service
        GreedyPlanResult plan = greedyService.buildPlan(
                new GreedyPlanInput(eligible, isOutbound, capacity));

        List<RouteQualityResponse> routeQualityList = new ArrayList<>();
        List<RoutePlanningIssueEntity> sessionIssues = new ArrayList<>();
        int totalPlanned = 0;
        int routeIndex = 1;

        for (GreedyRouteBatch batch : plan.getBatches()) {
            String routeCode = codeGeneratorService.generate(
                    SchoolBusCode.ROUTE.sequenceKey(), SchoolBusCode.ROUTE.prefix(), tenantId, actorId);

            RoutePlanEntity route = new RoutePlanEntity();
            route.markCreated(tenantId, actor(actorId));
            route.setPlanningSession(session);
            route.setSchoolSchedule(session.getSchoolSchedule());
            route.setSchool(session.getSchool());
            route.setRouteCode(routeCode);
            route.setRouteName("Route " + routeIndex++);
            route.setRouteDirection(session.getRouteDirection());
            route.setServiceDate(session.getServiceDate());
            route.setStatus(RouteStatus.GENERATED);
            route.setRouteGenerationMethod(RouteGenerationMethod.GREEDY);
            route.setPlannedStudentCount(batch.getTotalStudents());
            route.setRequiredCapacity(batch.getTotalStudents());
            route.setStartLocationType(isOutbound ? RouteLocationType.DEPOT : RouteLocationType.SCHOOL);
            route.setEndLocationType(isOutbound ? RouteLocationType.SCHOOL : RouteLocationType.DEPOT);
            route.setEndSchool(isOutbound ? session.getSchool() : null);
            route.setStartSchool(!isOutbound ? session.getSchool() : null);
            route.setStartDepot(isOutbound ? depot : null);
            route.setEndDepot(!isOutbound ? depot : null);
            route.setShiftType(ShiftType.valueOf(
                    session.getSchoolSchedule().getShiftType() != null
                            ? session.getSchoolSchedule().getShiftType().toUpperCase() : "MORNING"));
            route.setIssueCount(0);
            route.setBlockingIssueCount(0);
            route.setVersionNo(1);

            LocalTime arrivalDeadline = session.getSchoolSchedule().getArrivalDeadline();
            LocalTime departureTime = session.getSchoolSchedule().getDepartureTime();
            if (isOutbound && arrivalDeadline != null) {
                route.setPlannedStartTime(arrivalDeadline.minusMinutes(90));
                route.setPlannedEndTime(arrivalDeadline);
            } else if (!isOutbound && departureTime != null) {
                route.setPlannedStartTime(departureTime);
                route.setPlannedEndTime(departureTime.plusMinutes(90));
            }
            route = routeService.saveRouteEntity(route);

            // ── Build full stop list in memory, assign final stopOrder, save once ──
            // 1. Build middle stop entities (no DB save yet — no IDs needed for RouteStopEntity)
            List<RouteStopEntity> middleStops = new ArrayList<>();
            for (GreedyStopAssignment assignment : batch.getStopAssignments()) {
                RouteStopEntity stop = routeStopFactory.buildMiddleStop(route, assignment.getPickupPoint(),
                        tenantId, actor(actorId));
                stop.setEstimatedStudentCount(assignment.getStudents().size());
                stop.setPlannedBoardingCount(isOutbound ? assignment.getStudents().size() : 0);
                stop.setPlannedDropoffCount(!isOutbound ? assignment.getStudents().size() : 0);
                middleStops.add(stop);
            }

            // 2. Build full ordered list: START_TERMINAL + middle + END_TERMINAL
            //    with final stopOrder assigned in one pass — no shifting needed
            List<RouteStopEntity> allStops = routeStopFactory.buildFullStopList(
                    route, middleStops, tenantId, actor(actorId));

            // 3. Save all stops in one batch (orders already final → no unique constraint violation)
            List<RouteStopEntity> savedStops = routeStopService.saveAllRouteStops(allStops);

            // 4. Map middle stops back from savedStops (need IDs for RoutePlanStudent)
            //    Middle stops in savedStops are at indices 1..N (index 0 = START, last = END)
            List<RouteStopEntity> savedMiddle = savedStops.stream()
                    .filter(s -> s.getStopPurpose() != null && !s.getStopPurpose().isTerminal())
                    .toList();

            // Resolve terminal stop for complementary action
            //   OUTBOUND: each student also gets DROPOFF at END_TERMINAL (school)
            //   RETURN:   each student also gets BOARD at START_TERMINAL (school)
            RouteStopEntity terminalStop = savedStops.stream()
                    .filter(s -> s.getStopPurpose() != null && (isOutbound
                            ? s.getStopPurpose() == RouteStopPurpose.END_TERMINAL
                            : s.getStopPurpose() == RouteStopPurpose.START_TERMINAL))
                    .findFirst()
                    .orElse(null);

            // 5. Save RoutePlanStudents: main action (middle stop) + complementary (terminal stop)
            for (int ai = 0; ai < batch.getStopAssignments().size(); ai++) {
                GreedyStopAssignment assignment = batch.getStopAssignments().get(ai);
                RouteStopEntity stop = savedMiddle.get(ai);
                for (StudentSubscriptionEntity sub : assignment.getStudents()) {
                    // Main: BOARD at pickup stop (OUTBOUND) or DROPOFF at dropoff stop (RETURN)
                    RoutePlanStudentEntity planStudent = new RoutePlanStudentEntity();
                    planStudent.markCreated(tenantId, actor(actorId));
                    planStudent.setRoute(route);
                    planStudent.setRouteStop(stop);
                    planStudent.setStudent(sub.getStudent());
                    planStudent.setSubscription(sub);
                    planStudent.setServiceAction(isOutbound ? RoutePlanStudentAction.BOARD : RoutePlanStudentAction.DROPOFF);
                    routePlanStudentService.save(planStudent);

                    // Complementary: DROPOFF at END_TERMINAL (OUTBOUND) or BOARD at START_TERMINAL (RETURN)
                    if (terminalStop != null) {
                        RoutePlanStudentEntity terminalEntry = new RoutePlanStudentEntity();
                        terminalEntry.markCreated(tenantId, actor(actorId));
                        terminalEntry.setRoute(route);
                        terminalEntry.setRouteStop(terminalStop);
                        terminalEntry.setStudent(sub.getStudent());
                        terminalEntry.setSubscription(sub);
                        terminalEntry.setServiceAction(isOutbound ? RoutePlanStudentAction.DROPOFF : RoutePlanStudentAction.BOARD);
                        routePlanStudentService.save(terminalEntry);
                    }
                }
            }

            // 6. Compute route geometry with full stop list (terminal → middle → terminal)
            routeGeometryService.computeAndUpdate(route, savedStops);
            route = routeService.saveRouteEntity(route);

            // Build per-route issues
            List<RoutePlanningIssueEntity> routeIssues = new ArrayList<>();
            for (GreedyStopAssignment assignment : batch.getStopAssignments()) {
                PickupPointEntity pt = assignment.getPickupPoint();
                if (pt.getLatitude() == null || pt.getLongitude() == null) {
                    routeIssues.add(buildIssue(session, route, null, null, tenantId,
                            "MISSING_COORDINATE", PlanningIssueSeverity.WARNING,
                            "Pickup point '" + pt.getName() + "' has no coordinates — straight-line estimate used"));
                }
            }
            // Capacity overflow: batch exceeds specified bus capacity → BLOCKING issue
            if (batch.getTotalStudents() > capacity) {
                routeIssues.add(buildIssue(session, route, null, null, tenantId,
                        "CAPACITY_OVERFLOW", PlanningIssueSeverity.BLOCKING,
                        "Route '" + route.getRouteName() + "' requires " + batch.getTotalStudents()
                                + " seats but the specified capacity is " + capacity
                                + " — assign a larger bus or split the route manually"));
            }
            if (usingDefault && routeIndex == 2) {
                sessionIssues.add(buildIssue(session, null, null, null, tenantId,
                        "DEFAULT_CAPACITY_USED", PlanningIssueSeverity.INFO,
                        "No bus capacity specified; using default of " + DEFAULT_CAPACITY + " seats per route"));
            }

            int blockingCount = (int) routeIssues.stream().filter(i -> i.getSeverity() == PlanningIssueSeverity.BLOCKING).count();
            int totalIssues = routeIssues.size();
            issueService.saveAll(routeIssues);

            route.setIssueCount(totalIssues);
            route.setBlockingIssueCount(blockingCount);
            route.setQualityScore(blockingCount == 0 ? (totalIssues == 0 ? 100.0 : 75.0) : 30.0);
            routeService.saveRouteEntity(route);

            totalPlanned += batch.getTotalStudents();
            routeQualityList.add(buildQuality(route, batch.getTotalStudents(), savedMiddle.size(), routeIssues));
        }

        issueService.saveAll(sessionIssues);

        List<StudentSubscriptionEntity> unassignedSubs = plan.getUnassignedStudents();

        // Update session counters
        session.setStatus(PlanningSessionStatus.GENERATED);
        session.setTotalPlannedStudents(totalPlanned);
        session.setTotalUnassignedStudents(eligible.size() - totalPlanned + unassignedSubs.size());
        session.setTotalRoutes(plan.getBatches().size());
        session.setGeneratedAt(LocalDateTime.now());
        session.setGeneratedBy(actorId);
        session.markUpdated(actor(actorId));
        sessionRepository.save(session);

        // Build response
        List<EligibleStudentResponse> unassigned = unassignedSubs.stream()
                .map(s -> eligibilityService.toEligibleStudentResponse(s,
                        session.getSchoolSchedule().getId(),
                        session.getRouteDirection().name(), tenantId))
                .toList();

        List<PlanningIssueResponse> sessionIssueResponses = sessionIssues.stream()
                .map(this::toIssueResponse).toList();

        // Collect unique pickup points covered by all generated routes (for map display)
        Map<Long, PlanningPreviewResponse.EligiblePickupPointResponse> ppMap = new LinkedHashMap<>();
        for (GreedyRouteBatch batch : plan.getBatches()) {
            for (GreedyStopAssignment assignment : batch.getStopAssignments()) {
                PickupPointEntity pt = assignment.getPickupPoint();
                if (!ppMap.containsKey(pt.getId())) {
                    PlanningPreviewResponse.EligiblePickupPointResponse ppResp =
                            new PlanningPreviewResponse.EligiblePickupPointResponse();
                    ppResp.setPickupPointId(pt.getId());
                    ppResp.setPickupPointName(pt.getName());
                    ppResp.setLatitude(pt.getLatitude());
                    ppResp.setLongitude(pt.getLongitude());
                    ppResp.setStudentCount(assignment.getStudents().size());
                    ppMap.put(pt.getId(), ppResp);
                } else {
                    // Accumulate student count across routes using the same stop
                    PlanningPreviewResponse.EligiblePickupPointResponse existing = ppMap.get(pt.getId());
                    existing.setStudentCount(existing.getStudentCount() + assignment.getStudents().size());
                }
            }
        }

        GreedyGenerateResponse response = new GreedyGenerateResponse();
        response.setSession(toResponse(session));
        response.setRoutes(routeQualityList);
        response.setTotalUnassignedStudents(unassignedSubs.size());
        response.setUnassignedStudents(unassigned);
        response.setSessionIssues(sessionIssueResponses);
        response.setEligiblePickupPoints(new ArrayList<>(ppMap.values()));
        return response;
    }

    // ── Publish ──────────────────────────────────────────────────────────────

    @Override
    @Transactional
    public PlanningSessionResponse publishSession(Long sessionId, Long tenantId, Long actorId) {
        RoutePlanningSessionEntity session = requireSession(sessionId, tenantId);
        if (session.getStatus() == PlanningSessionStatus.PUBLISHED) {
            throw new AppException(AppErrorCode.Session.ALREADY_PUBLISHED, messageCommon.getMessage(AppErrorCode.Session.ALREADY_PUBLISHED));
        }
        if (session.getStatus() == PlanningSessionStatus.CANCELLED) {
            throw new AppException(AppErrorCode.Session.CANNOT_PUBLISH_CANCELLED, messageCommon.getMessage(AppErrorCode.Session.CANNOT_PUBLISH_CANCELLED));
        }

        // Check routes for blocking issues
        List<RoutePlanEntity> routes = routeService.findRoutesBySession(sessionId, tenantId);

        if (routes.isEmpty()) {
            throw new AppException(AppErrorCode.Session.NO_ROUTES,
                    messageCommon.getMessage(AppErrorCode.Session.NO_ROUTES));
        }

        if (session.getTotalUnassignedStudents() != null && session.getTotalUnassignedStudents() > 0) {
            throw new AppException(AppErrorCode.Session.UNASSIGNED_STUDENTS,
                    messageCommon.getMessage(AppErrorCode.Session.UNASSIGNED_STUDENTS, session.getTotalUnassignedStudents()));
        }

        for (RoutePlanEntity route : routes) {
            if (route.getBlockingIssueCount() != null && route.getBlockingIssueCount() > 0) {
                throw new AppException(AppErrorCode.Session.BLOCKING_ISSUES,
                        messageCommon.getMessage(AppErrorCode.Session.BLOCKING_ISSUES, route.getRouteName(), route.getBlockingIssueCount()));
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

    // ── Session route / student listing (MANUAL + GREEDY refresh) ────────────

    @Override
    @Transactional(readOnly = true)
    public List<RoutePlanResponse> listRoutesBySession(Long sessionId, Long tenantId) {
        requireSession(sessionId, tenantId);
        return routeService.listRoutesBySession(sessionId, tenantId);
    }

    @Override
    @Transactional
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
        List<StudentSubscriptionEntity> eligible =
                eligibilityService.findEligible(
                        session.getSchool().getId(),
                        session.getSchoolSchedule().getId(),
                        session.getRouteDirection().name(),
                        session.getServiceDate(),
                        tenantId);
        return eligible.stream()
                .map(sub -> eligibilityService.toEligibleStudentResponse(
                        sub,
                        session.getSchoolSchedule().getId(),
                        session.getRouteDirection().name(),
                        tenantId))
                .toList();
    }

    // ── Cancel ───────────────────────────────────────────────────────────────

    @Override
    @Transactional
    public PlanningSessionResponse cancelSession(Long sessionId, Long tenantId, Long actorId) {
        RoutePlanningSessionEntity session = requireSession(sessionId, tenantId);
        if (session.getStatus() == PlanningSessionStatus.CANCELLED) {
            throw new AppException(AppErrorCode.Session.ALREADY_CANCELLED, messageCommon.getMessage(AppErrorCode.Session.ALREADY_CANCELLED));
        }
        softDeleteExistingRoutes(session, tenantId, actorId);
        session.setStatus(PlanningSessionStatus.CANCELLED);
        session.markUpdated(actor(actorId));
        return toResponse(sessionRepository.save(session));
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

    private RoutePlanningIssueEntity buildIssue(RoutePlanningSessionEntity session,
                                                RoutePlanEntity route, RouteStopEntity stop,
                                                StudentSubscriptionEntity sub, Long tenantId,
                                                String type, PlanningIssueSeverity severity, String message) {
        RoutePlanningIssueEntity issue = new RoutePlanningIssueEntity();
        issue.markCreated(tenantId, "SYSTEM");
        issue.setPlanningSession(session);
        issue.setRoute(route);
        issue.setRouteStop(stop);
        if (sub != null) {
            issue.setStudent(sub.getStudent());
            issue.setSubscription(sub);
        }
        issue.setIssueType(type);
        issue.setSeverity(severity);
        issue.setMessage(message);
        issue.setIsResolved(Boolean.FALSE);
        return issue;
    }

    private RouteQualityResponse buildQuality(RoutePlanEntity route, int studentCount,
                                              int stopCount, List<RoutePlanningIssueEntity> issues) {
        RouteQualityResponse q = new RouteQualityResponse();
        q.setRouteId(route.getId());
        q.setRouteCode(route.getRouteCode());
        q.setRouteName(route.getRouteName());
        q.setStatus(route.getStatus().name());
        q.setStudentCount(studentCount);
        q.setStopCount(stopCount);
        q.setRequiredCapacity(route.getRequiredCapacity());
        q.setQualityScore(route.getQualityScore());
        q.setBlockingIssueCount((int) issues.stream().filter(i -> i.getSeverity() == PlanningIssueSeverity.BLOCKING).count());
        q.setWarningIssueCount((int) issues.stream().filter(i -> i.getSeverity() == PlanningIssueSeverity.WARNING).count());
        q.setInfoIssueCount((int) issues.stream().filter(i -> i.getSeverity() == PlanningIssueSeverity.INFO).count());
        q.setIssues(issues.stream().map(this::toIssueResponse).toList());
        return q;
    }

    private PlanningIssueResponse toIssueResponse(RoutePlanningIssueEntity e) {
        PlanningIssueResponse r = new PlanningIssueResponse();
        r.setId(e.getId());
        r.setIssueType(e.getIssueType());
        r.setSeverity(e.getSeverity().name());
        r.setMessage(e.getMessage());
        r.setIsResolved(e.getIsResolved());
        if (e.getPlanningSession() != null) r.setPlanningSessionId(e.getPlanningSession().getId());
        if (e.getRoute() != null) r.setRouteId(e.getRoute().getId());
        if (e.getStudent() != null) {
            r.setStudentId(e.getStudent().getId());
            r.setStudentName(e.getStudent().getFullName());
        }
        if (e.getSubscription() != null) r.setSubscriptionId(e.getSubscription().getId());
        return r;
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

    // ── Terminal stop generation ─────────────────────────────────────────────

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

