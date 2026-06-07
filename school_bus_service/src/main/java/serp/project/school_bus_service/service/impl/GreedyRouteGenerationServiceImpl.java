package serp.project.school_bus_service.service.impl;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.context.annotation.Lazy;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import serp.project.school_bus_service.dto.request.GreedyGenerateRequest;
import serp.project.school_bus_service.dto.response.EligibleStudentResponse;
import serp.project.school_bus_service.dto.response.GreedyGenerateResponse;
import serp.project.school_bus_service.dto.response.PlanningIssueResponse;
import serp.project.school_bus_service.dto.response.PlanningPreviewResponse;
import serp.project.school_bus_service.dto.response.RouteQualityResponse;
import serp.project.school_bus_service.entity.DepotEntity;
import serp.project.school_bus_service.entity.PickupPointEntity;
import serp.project.school_bus_service.entity.RoutePlanEntity;
import serp.project.school_bus_service.entity.RoutePlanningIssueEntity;
import serp.project.school_bus_service.entity.RoutePlanningSessionEntity;
import serp.project.school_bus_service.entity.RoutePlanStudentEntity;
import serp.project.school_bus_service.entity.RouteStopEntity;
import serp.project.school_bus_service.entity.StudentSubscriptionEntity;
import serp.project.school_bus_service.enums.PlanningIssueSeverity;
import serp.project.school_bus_service.enums.PlanningSessionStatus;
import serp.project.school_bus_service.enums.RouteCalculationStatus;
import serp.project.school_bus_service.enums.RouteCalculationType;
import serp.project.school_bus_service.enums.RouteDirection;
import serp.project.school_bus_service.enums.RouteGenerationMethod;
import serp.project.school_bus_service.enums.RouteLocationType;
import serp.project.school_bus_service.enums.RoutePlanStudentAction;
import serp.project.school_bus_service.enums.RouteStatus;
import serp.project.school_bus_service.enums.RouteStopPurpose;
import serp.project.school_bus_service.enums.ShiftType;
import serp.project.school_bus_service.repository.RoutePlanningSessionRepository;
import serp.project.school_bus_service.dto.request.RouteCalculationTraceCreateCommand;
import serp.project.school_bus_service.service.ICodeGeneratorService;
import serp.project.school_bus_service.service.IDepotService;
import serp.project.school_bus_service.service.IGreedyRouteGenerationService;
import serp.project.school_bus_service.service.IRouteCalculationTraceService;
import serp.project.school_bus_service.service.IRouteObjectiveScoringService;
import serp.project.school_bus_service.service.IRoutePlanningIssueService;
import serp.project.school_bus_service.service.IRoutePlanningSessionService;
import serp.project.school_bus_service.service.IRoutePlanStudentService;
import serp.project.school_bus_service.service.IRouteService;
import serp.project.school_bus_service.service.IRouteStopService;
import serp.project.school_bus_service.service.ISchoolBusAppConfigService;
import serp.project.school_bus_service.service.domain.IRouteEligibilityService;
import serp.project.school_bus_service.service.domain.IRouteGeometryService;
import serp.project.school_bus_service.service.domain.RouteStopFactory;
import serp.project.school_bus_service.shared.code.AppConfigCode;
import serp.project.school_bus_service.shared.code.SchoolBusCode;
import serp.project.school_bus_service.shared.exception.AppErrorCode;
import serp.project.school_bus_service.shared.exception.AppException;
import serp.project.school_bus_service.shared.i18n.MessageCommon;
import tools.jackson.databind.ObjectMapper;

import java.math.BigDecimal;
import java.time.Duration;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;

/**
 * Builds an initial feasible set of routes using a greedy insertion heuristic.
 * The algorithm does not guarantee a global optimum; it prioritizes fast,
 * explainable route construction for dispatch planning.
 */
@Service
public class GreedyRouteGenerationServiceImpl implements IGreedyRouteGenerationService {

    private static final Logger log = LoggerFactory.getLogger(GreedyRouteGenerationServiceImpl.class);
    private static final int DEFAULT_CAPACITY = 30;

    private final RoutePlanningSessionRepository sessionRepository;
    private final IRouteService routeService;
    private final IRouteStopService routeStopService;
    private final IRoutePlanStudentService routePlanStudentService;
    private final IRoutePlanningIssueService issueService;
    private final IDepotService depotService;
    private final IRouteEligibilityService eligibilityService;
    private final IRouteGeometryService routeGeometryService;
    private final RouteStopFactory routeStopFactory;
    private final ICodeGeneratorService codeGeneratorService;
    private final ISchoolBusAppConfigService appConfigService;
    private final IRouteObjectiveScoringService objectiveScoringService;
    private final IRouteCalculationTraceService traceService;
    private final IRoutePlanningSessionService sessionService;
    private final MessageCommon messageCommon;
    private final ObjectMapper objectMapper;

    public GreedyRouteGenerationServiceImpl(
            RoutePlanningSessionRepository sessionRepository,
            IRouteService routeService,
            IRouteStopService routeStopService,
            IRoutePlanStudentService routePlanStudentService,
            IRoutePlanningIssueService issueService,
            IDepotService depotService,
            IRouteEligibilityService eligibilityService,
            IRouteGeometryService routeGeometryService,
            RouteStopFactory routeStopFactory,
            ICodeGeneratorService codeGeneratorService,
            ISchoolBusAppConfigService appConfigService,
            IRouteObjectiveScoringService objectiveScoringService,
            IRouteCalculationTraceService traceService,
            @Lazy IRoutePlanningSessionService sessionService,
            MessageCommon messageCommon,
            ObjectMapper objectMapper) {
        this.sessionRepository = sessionRepository;
        this.routeService = routeService;
        this.routeStopService = routeStopService;
        this.routePlanStudentService = routePlanStudentService;
        this.issueService = issueService;
        this.depotService = depotService;
        this.eligibilityService = eligibilityService;
        this.routeGeometryService = routeGeometryService;
        this.routeStopFactory = routeStopFactory;
        this.codeGeneratorService = codeGeneratorService;
        this.appConfigService = appConfigService;
        this.objectiveScoringService = objectiveScoringService;
        this.traceService = traceService;
        this.sessionService = sessionService;
        this.messageCommon = messageCommon;
        this.objectMapper = objectMapper;
    }

    @Override
    @Transactional
    public GreedyGenerateResponse generateRoutes(Long sessionId, GreedyGenerateRequest request, Long tenantId, Long actorId) {
        RoutePlanningSessionEntity session = sessionService.requireSession(sessionId, tenantId);
        if (session.getStatus() == PlanningSessionStatus.PUBLISHED ||
                session.getStatus() == PlanningSessionStatus.CANCELLED) {
            throw new AppException(AppErrorCode.Session.CANNOT_GENERATE,
                    messageCommon.getMessage(AppErrorCode.Session.CANNOT_GENERATE, session.getStatus()));
        }

        int capacity = (request.getDefaultBusCapacity() != null && request.getDefaultBusCapacity() > 0)
                ? request.getDefaultBusCapacity() : DEFAULT_CAPACITY;

        if (request.getDepotId() == null) {
            throw new AppException(AppErrorCode.Route.DEPOT_REQUIRED,
                    messageCommon.getMessage(AppErrorCode.Route.DEPOT_REQUIRED, "depot"));
        }
        DepotEntity depot = depotService.getDepot(request.getDepotId(), tenantId);

        // 1. Soft-delete all existing routes
        softDeleteExistingRoutes(session, tenantId, actorId);

        // 2. Load eligible subscriptions
        List<StudentSubscriptionEntity> eligible = eligibilityService.findEligible(
                session.getSchool().getId(), session.getSchoolSchedule().getId(),
                session.getRouteDirection().name(), session.getServiceDate(), tenantId);

        boolean isOutbound = session.getRouteDirection() == RouteDirection.OUTBOUND;

        // 3. Group students by point
        Map<Long, PointAggregate> pointMap = new LinkedHashMap<>();
        List<StudentSubscriptionEntity> unassignedSubs = new ArrayList<>();

        for (StudentSubscriptionEntity sub : eligible) {
            PickupPointEntity point = isOutbound ? sub.getPickupPoint() : sub.getDropoffPoint();
            if (point == null) {
                unassignedSubs.add(sub);
                continue;
            }
            pointMap.compute(point.getId(), (id, agg) -> {
                if (agg == null) return new PointAggregate(point);
                return agg;
            }).addStudent(sub);
        }

        // Sort point aggregates by student size descending
        List<PointAggregate> sortedCandidates = pointMap.values().stream()
                .sorted(Comparator.comparingInt(PointAggregate::studentCount).reversed()
                        .thenComparingLong(a -> a.point.getId()))
                .toList();

        List<RoutePlanEntity> activeRoutes = new ArrayList<>();
        int routeIndex = 1;

        Map<String, BigDecimal> weights = loadWeights();
        BigDecimal wDistance = weights.get(AppConfigCode.ROUTING_WEIGHT_DISTANCE);
        BigDecimal wDuration = weights.get(AppConfigCode.ROUTING_WEIGHT_DURATION);
        BigDecimal wWaitTime = weights.get(AppConfigCode.ROUTING_WEIGHT_WAIT_TIME);

        // 4. Greedy Nearest Feasible Insertion
        for (PointAggregate candidate : sortedCandidates) {
            boolean placed = false;
            double bestCost = Double.MAX_VALUE;
            RoutePlanEntity bestRoute = null;
            int bestIdx = -1;

            // Try inserting in existing active routes
            for (RoutePlanEntity route : activeRoutes) {
                List<RouteStopEntity> stops = routeStopService.findByRoute(route.getId(), tenantId);
                int numPositions = stops.size() - 1; // Between terminal stops

                for (int i = 1; i <= numPositions; i++) {
                    // Try chèn tạm thời
                    InsertionResult tempResult = tryInsertTemporary(route, stops, candidate, i, capacity, wDistance, wDuration, wWaitTime, isOutbound, tenantId, actorId);
                    if (tempResult.feasible && tempResult.cost < bestCost) {
                        bestCost = tempResult.cost;
                        bestRoute = route;
                        bestIdx = i;
                    }
                }
            }

            if (bestRoute != null) {
                // Perform official permanent insertion
                insertPermanently(bestRoute, candidate, bestIdx, isOutbound, tenantId, actorId);
                placed = true;
            }

            if (!placed) {
                // Open a new route
                RoutePlanEntity newRoute = createEmptyRoute(session, depot, routeIndex++, isOutbound, tenantId, actorId);
                List<RouteStopEntity> stops = routeStopService.findByRoute(newRoute.getId(), tenantId);

                // Try inserting into position 1 of the new route
                InsertionResult tempResult = tryInsertTemporary(newRoute, stops, candidate, 1, capacity, wDistance, wDuration, wWaitTime, isOutbound, tenantId, actorId);
                if (tempResult.feasible) {
                    insertPermanently(newRoute, candidate, 1, isOutbound, tenantId, actorId);
                    activeRoutes.add(newRoute);
                } else {
                    // Even a new route is infeasible -> push to unassigned
                    unassignedSubs.addAll(candidate.students);
                    // Cleanup new empty route since it's not used
                    newRoute.setIsDeleted(true);
                    newRoute.setIsActive(false);
                    routeService.saveRouteEntity(newRoute);
                }
            }
        }

        // Final recalculation for all active routes
        int totalPlanned = 0;
        List<RouteQualityResponse> routeQualityList = new ArrayList<>();
        List<RoutePlanningIssueEntity> allRouteIssues = new ArrayList<>();

        for (RoutePlanEntity route : activeRoutes) {
            // Re-load to get latest persisted values
            RoutePlanEntity loaded = routeService.getRouteEntity(route.getId(), tenantId);
            List<RouteStopEntity> stops = routeStopService.findByRoute(loaded.getId(), tenantId);
            routeGeometryService.computeAndUpdate(loaded, stops);
            loaded = routeService.saveRouteEntity(loaded);

            List<RoutePlanningIssueEntity> issues = issueService.findByRoute(loaded.getId());
            allRouteIssues.addAll(issues);

            totalPlanned += loaded.getPlannedStudentCount() != null ? loaded.getPlannedStudentCount() : 0;
            routeQualityList.add(buildQuality(loaded, stops.size(), issues));
        }

        // Update session counters
        session.setStatus(PlanningSessionStatus.GENERATED);
        session.setTotalPlannedStudents(totalPlanned);
        session.setTotalUnassignedStudents(eligible.size() - totalPlanned + unassignedSubs.size());
        session.setTotalRoutes(activeRoutes.size());
        session.setGeneratedAt(LocalDateTime.now());
        session.setGeneratedBy(actorId);
        session.markUpdated(actor(actorId));
        sessionRepository.save(session);

        // Ghi Greedy Solution Trace
        saveGreedyGenerationTrace(session, eligible, sortedCandidates, activeRoutes, unassignedSubs, tenantId);

        // Build Response
        List<EligibleStudentResponse> unassigned = unassignedSubs.stream()
                .map(s -> eligibilityService.toEligibleStudentResponse(s,
                        session.getSchoolSchedule().getId(),
                        session.getRouteDirection().name(), tenantId))
                .toList();

        List<PlanningIssueResponse> sessionIssueResponses = allRouteIssues.stream()
                .map(this::toIssueResponse).toList();

        Map<Long, PlanningPreviewResponse.EligiblePickupPointResponse> ppMap = new LinkedHashMap<>();
        for (RoutePlanEntity route : activeRoutes) {
            List<RouteStopEntity> stops = routeStopService.findByRoute(route.getId(), tenantId);
            for (RouteStopEntity stop : stops) {
                if (stop.getStopPurpose() != null && stop.getStopPurpose().isTerminal()) {
                    continue;
                }
                PickupPointEntity pt = stop.getPickupPoint();
                if (pt != null) {
                    ppMap.compute(pt.getId(), (id, existing) -> {
                        if (existing == null) {
                            PlanningPreviewResponse.EligiblePickupPointResponse ppResp = new PlanningPreviewResponse.EligiblePickupPointResponse();
                            ppResp.setPickupPointId(pt.getId());
                            ppResp.setPickupPointName(pt.getName());
                            ppResp.setLatitude(pt.getLatitude());
                            ppResp.setLongitude(pt.getLongitude());
                            ppResp.setStudentCount(stop.getEstimatedStudentCount());
                            return ppResp;
                        } else {
                            existing.setStudentCount(existing.getStudentCount() + stop.getEstimatedStudentCount());
                            return existing;
                        }
                    });
                }
            }
        }

        GreedyGenerateResponse response = new GreedyGenerateResponse();
        response.setSession(sessionService.getSession(sessionId, tenantId));
        response.setRoutes(routeQualityList);
        response.setTotalUnassignedStudents(unassignedSubs.size());
        response.setUnassignedStudents(unassigned);
        response.setSessionIssues(sessionIssueResponses);
        response.setEligiblePickupPoints(new ArrayList<>(ppMap.values()));
        return response;
    }

    private InsertionResult tryInsertTemporary(
            RoutePlanEntity route,
            List<RouteStopEntity> currentStops,
            PointAggregate candidate,
            int index,
            int capacity,
            BigDecimal wDistance,
            BigDecimal wDuration,
            BigDecimal wWaitTime,
            boolean isOutbound,
            Long tenantId,
            Long actorId) {

        // Clone/create list of stops for simulation
        List<RouteStopEntity> simulatedStops = new ArrayList<>();
        RouteStopEntity tempStop = null;

        // 1. Build stop sequence order temporarily
        int order = 0;
        for (int i = 0; i < currentStops.size(); i++) {
            if (i == index) {
                tempStop = routeStopFactory.buildMiddleStop(route, candidate.point, tenantId, "SYSTEM");
                tempStop.setEstimatedStudentCount(candidate.students.size());
                tempStop.setPlannedBoardingCount(isOutbound ? candidate.students.size() : 0);
                tempStop.setPlannedDropoffCount(!isOutbound ? candidate.students.size() : 0);
                tempStop.setStopOrder(order++);
                simulatedStops.add(tempStop);
            }
            RouteStopEntity oldStop = currentStops.get(i);
            oldStop.setStopOrder(order++);
            simulatedStops.add(oldStop);
        }

        // Save simulated stops to database to get tempStop ID for validation
        List<RouteStopEntity> savedSimulated = safeSaveStops(simulatedStops, currentStops, tenantId);
        RouteStopEntity persistedTempStop = savedSimulated.stream()
                .filter(s -> s.getPickupPoint() != null && s.getPickupPoint().getId().equals(candidate.point.getId()))
                .findFirst()
                .orElse(null);

        // 2. Temporarily assign students
        List<RoutePlanStudentEntity> tempPlanStudents = new ArrayList<>();
        RouteStopEntity terminalStop = savedSimulated.stream()
                .filter(s -> s.getStopPurpose() != null && (isOutbound
                        ? s.getStopPurpose() == RouteStopPurpose.END_TERMINAL
                        : s.getStopPurpose() == RouteStopPurpose.START_TERMINAL))
                .findFirst()
                .orElse(null);

        if (persistedTempStop != null) {
            for (StudentSubscriptionEntity sub : candidate.students) {
                RoutePlanStudentEntity planStudent = new RoutePlanStudentEntity();
                planStudent.markCreated(tenantId, "SYSTEM");
                planStudent.setRoute(route);
                planStudent.setRouteStop(persistedTempStop);
                planStudent.setStudent(sub.getStudent());
                planStudent.setSubscription(sub);
                planStudent.setServiceAction(isOutbound ? RoutePlanStudentAction.BOARD : RoutePlanStudentAction.DROPOFF);
                tempPlanStudents.add(routePlanStudentService.save(planStudent));

                if (terminalStop != null) {
                    RoutePlanStudentEntity terminalEntry = new RoutePlanStudentEntity();
                    terminalEntry.markCreated(tenantId, "SYSTEM");
                    terminalEntry.setRoute(route);
                    terminalEntry.setRouteStop(terminalStop);
                    terminalEntry.setStudent(sub.getStudent());
                    terminalEntry.setSubscription(sub);
                    terminalEntry.setServiceAction(isOutbound ? RoutePlanStudentAction.DROPOFF : RoutePlanStudentAction.BOARD);
                    tempPlanStudents.add(routePlanStudentService.save(terminalEntry));
                }
            }
        }

        // 3. Compute geometry & timeline
        routeGeometryService.computeAndUpdate(route, savedSimulated);

        double distance = route.getPlannedDistanceKm() != null ? route.getPlannedDistanceKm() : 0.0;
        int duration = route.getPlannedDurationMin() != null ? route.getPlannedDurationMin() : 0;
        int blockingCount = route.getBlockingIssueCount() != null ? route.getBlockingIssueCount() : 0;
        long studentCount = routePlanStudentService.countDistinctStudentsByRoute(route.getId());

        double waitTime = calculateRouteWaitTime(route, savedSimulated);
        boolean feasible = blockingCount == 0 && studentCount <= capacity;

        double cost = wDistance.doubleValue() * distance
                + wDuration.doubleValue() * duration
                + wWaitTime.doubleValue() * waitTime;

        // 4. Delete temporary issues created during simulation FIRST
        List<RoutePlanningIssueEntity> tempIssues = issueService.findAllByRoute(route.getId());
        if (tempIssues != null && !tempIssues.isEmpty()) {
            for (RoutePlanningIssueEntity issue : tempIssues) {
                issue.setRouteStop(null);
                issueService.deletePhysical(issue.getId());
            }
        }

        // 5. CLEANUP simulated stops & students from DB
        for (RoutePlanStudentEntity ps : tempPlanStudents) {
            routePlanStudentService.deletePhysical(ps.getId());
        }
        if (persistedTempStop != null) {
            routeStopService.deletePhysical(persistedTempStop.getId());
        }
        routeStopService.flush();

        // Restore original stop orders
        int origOrder = 0;
        for (RouteStopEntity orig : currentStops) {
            orig.setStopOrder(origOrder++);
        }
        routeStopService.saveAllRouteStops(currentStops);
        routeGeometryService.computeAndUpdate(route, currentStops);

        return new InsertionResult(feasible, cost);
    }

    private void insertPermanently(RoutePlanEntity route, PointAggregate candidate, int index, boolean isOutbound, Long tenantId, Long actorId) {
        List<RouteStopEntity> currentStops = routeStopService.findByRoute(route.getId(), tenantId);

        List<RouteStopEntity> stops = new ArrayList<>();
        int order = 0;
        RouteStopEntity newStop = null;
        for (int i = 0; i < currentStops.size(); i++) {
            if (i == index) {
                newStop = routeStopFactory.buildMiddleStop(route, candidate.point, tenantId, actor(actorId));
                newStop.setEstimatedStudentCount(candidate.students.size());
                newStop.setPlannedBoardingCount(isOutbound ? candidate.students.size() : 0);
                newStop.setPlannedDropoffCount(!isOutbound ? candidate.students.size() : 0);
                newStop.setStopOrder(order++);
                stops.add(newStop);
            }
            RouteStopEntity oldStop = currentStops.get(i);
            oldStop.setStopOrder(order++);
            stops.add(oldStop);
        }

        List<RouteStopEntity> savedStops = safeSaveStops(stops, currentStops, tenantId);
        RouteStopEntity persistedStop = savedStops.stream()
                .filter(s -> s.getPickupPoint() != null && s.getPickupPoint().getId().equals(candidate.point.getId()))
                .findFirst()
                .orElse(null);

        RouteStopEntity terminalStop = savedStops.stream()
                .filter(s -> s.getStopPurpose() != null && (isOutbound
                        ? s.getStopPurpose() == RouteStopPurpose.END_TERMINAL
                        : s.getStopPurpose() == RouteStopPurpose.START_TERMINAL))
                .findFirst()
                .orElse(null);

        if (persistedStop != null) {
            for (StudentSubscriptionEntity sub : candidate.students) {
                RoutePlanStudentEntity planStudent = new RoutePlanStudentEntity();
                planStudent.markCreated(tenantId, actor(actorId));
                planStudent.setRoute(route);
                planStudent.setRouteStop(persistedStop);
                planStudent.setStudent(sub.getStudent());
                planStudent.setSubscription(sub);
                planStudent.setServiceAction(isOutbound ? RoutePlanStudentAction.BOARD : RoutePlanStudentAction.DROPOFF);
                routePlanStudentService.save(planStudent);

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

        routeGeometryService.computeAndUpdate(route, savedStops);
        routeService.saveRouteEntity(route);
    }

    private RoutePlanEntity createEmptyRoute(RoutePlanningSessionEntity session, DepotEntity depot, int routeIndex, boolean isOutbound, Long tenantId, Long actorId) {
        String routeCode = codeGeneratorService.generate(
                SchoolBusCode.ROUTE.sequenceKey(), SchoolBusCode.ROUTE.prefix(), tenantId, actorId);

        RoutePlanEntity route = new RoutePlanEntity();
        route.markCreated(tenantId, actor(actorId));
        route.setPlanningSession(session);
        route.setSchoolSchedule(session.getSchoolSchedule());
        route.setSchool(session.getSchool());
        route.setRouteCode(routeCode);
        route.setRouteName("Route " + routeIndex);
        route.setRouteDirection(session.getRouteDirection());
        route.setServiceDate(session.getServiceDate());
        route.setStatus(RouteStatus.GENERATED);
        route.setRouteGenerationMethod(RouteGenerationMethod.GREEDY);
        route.setPlannedStudentCount(0);
        route.setRequiredCapacity(0);
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

        RoutePlanEntity savedRoute = routeService.saveRouteEntity(route);

        // Build terminals
        List<RouteStopEntity> terminals = routeStopFactory.buildFullStopList(savedRoute, new ArrayList<>(), tenantId, actor(actorId));
        routeStopService.saveAllRouteStops(terminals);

        routeGeometryService.computeAndUpdate(savedRoute, terminals);
        return routeService.saveRouteEntity(savedRoute);
    }

    private double calculateRouteWaitTime(RoutePlanEntity route, List<RouteStopEntity> stops) {
        if (stops == null || stops.size() < 2) {
            return 0.0;
        }
        RouteStopEntity terminalStop = stops.stream()
                .filter(s -> s.getStopPurpose() != null && (route.getRouteDirection() == RouteDirection.OUTBOUND
                        ? s.getStopPurpose() == RouteStopPurpose.END_TERMINAL
                        : s.getStopPurpose() == RouteStopPurpose.START_TERMINAL))
                .findFirst()
                .orElse(null);

        if (terminalStop == null || terminalStop.getPlannedArrivalTime() == null) {
            return 0.0;
        }

        LocalTime refTime = route.getRouteDirection() == RouteDirection.OUTBOUND
                ? terminalStop.getPlannedArrivalTime()
                : terminalStop.getPlannedDepartureTime();

        if (refTime == null) {
            return 0.0;
        }

        double totalTransitTime = 0.0;
        for (RouteStopEntity stop : stops) {
            if (stop.getStopPurpose() != null && stop.getStopPurpose().isTerminal()) {
                continue;
            }
            int boardingCount = stop.getEstimatedStudentCount() != null ? stop.getEstimatedStudentCount() : 0;
            if (boardingCount <= 0) {
                continue;
            }

            LocalTime stopTime = route.getRouteDirection() == RouteDirection.OUTBOUND
                    ? stop.getPlannedDepartureTime()
                    : stop.getPlannedArrivalTime();

            if (stopTime != null) {
                long minutes = Math.abs(Duration.between(refTime, stopTime).toMinutes());
                totalTransitTime += boardingCount * minutes;
            }
        }
        return totalTransitTime;
    }

    private void saveGreedyGenerationTrace(
            RoutePlanningSessionEntity session,
            List<StudentSubscriptionEntity> eligible,
            List<PointAggregate> sortedCandidates,
            List<RoutePlanEntity> generatedRoutes,
            List<StudentSubscriptionEntity> unassigned,
            Long tenantId) {

        try {
            RouteCalculationTraceCreateCommand command = new RouteCalculationTraceCreateCommand();
            command.setPlanningSessionId(session.getId());
            command.setTenantId(tenantId);
            command.setCalculationType(RouteCalculationType.GREEDY_GENERATION);
            command.setCalculationStatus(RouteCalculationStatus.SUCCESS);
            command.setSourceSummary("Greedy insertion algorithm running trace");

            Map<String, Object> inputMap = new HashMap<>();
            inputMap.put("eligibleStudentCount", eligible.size());
            inputMap.put("pointCount", sortedCandidates.size());
            inputMap.put("direction", session.getRouteDirection().name());
            inputMap.put("sessionDate", session.getServiceDate().toString());

            List<Map<String, Object>> routesList = new ArrayList<>();
            for (RoutePlanEntity r : generatedRoutes) {
                Map<String, Object> rMap = new HashMap<>();
                rMap.put("routeId", r.getId());
                rMap.put("routeCode", r.getRouteCode());
                rMap.put("studentCount", r.getPlannedStudentCount());
                rMap.put("distanceKm", r.getPlannedDistanceKm());
                rMap.put("durationMin", r.getPlannedDurationMin());
                rMap.put("blockingIssueCount", r.getBlockingIssueCount());
                routesList.add(rMap);
            }

            List<Map<String, Object>> unassignedList = new ArrayList<>();
            for (StudentSubscriptionEntity sub : unassigned) {
                Map<String, Object> uMap = new HashMap<>();
                uMap.put("studentId", sub.getStudent().getId());
                uMap.put("fullName", sub.getStudent().getFullName());
                uMap.put("reason", "No feasible insertion found (constraints violated or capacity overflow)");
                unassignedList.add(uMap);
            }

            Map<String, Object> traceMap = new HashMap<>();
            traceMap.put("planningSessionId", session.getId());
            traceMap.put("method", "GREEDY_NEAREST_FEASIBLE_INSERTION");
            traceMap.put("input", inputMap);
            traceMap.put("generatedRoutes", routesList);
            traceMap.put("unassigned", unassignedList);

            command.setInputJson(objectMapper.writeValueAsString(traceMap));
            traceService.saveTrace(command);
        } catch (Exception ex) {
            log.error("Failed to save greedy generation trace", ex);
        }
    }

    private void softDeleteExistingRoutes(RoutePlanningSessionEntity session, Long tenantId, Long actorId) {
        List<RoutePlanEntity> old = routeService.findRoutesBySession(session.getId(), tenantId);
        for (RoutePlanEntity r : old) {
            r.markSoftDeleted(actor(actorId));
            routeService.saveRouteEntity(r);
        }
    }

    private RouteQualityResponse buildQuality(RoutePlanEntity route, int stopCount, List<RoutePlanningIssueEntity> issues) {
        RouteQualityResponse q = new RouteQualityResponse();
        q.setRouteId(route.getId());
        q.setRouteCode(route.getRouteCode());
        q.setRouteName(route.getRouteName());
        q.setStatus(route.getStatus().name());
        q.setStudentCount(route.getPlannedStudentCount());
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
        r.setRouteStopId(e.getRouteStop() != null ? e.getRouteStop().getId() : null);
        r.setStudentId(e.getStudent() != null ? e.getStudent().getId() : null);
        r.setStudentName(e.getStudent() != null ? e.getStudent().getFullName() : null);
        return r;
    }

    private Map<String, BigDecimal> loadWeights() {
        Map<String, BigDecimal> weights = new HashMap<>();
        weights.put(AppConfigCode.ROUTING_WEIGHT_DISTANCE, appConfigService.getDecimal(AppConfigCode.ROUTING_WEIGHT_DISTANCE, BigDecimal.valueOf(1.0)));
        weights.put(AppConfigCode.ROUTING_WEIGHT_DURATION, appConfigService.getDecimal(AppConfigCode.ROUTING_WEIGHT_DURATION, BigDecimal.valueOf(1.0)));
        weights.put(AppConfigCode.ROUTING_WEIGHT_WAIT_TIME, appConfigService.getDecimal(AppConfigCode.ROUTING_WEIGHT_WAIT_TIME, BigDecimal.valueOf(0.5)));
        return weights;
    }

    private List<RouteStopEntity> safeSaveStops(List<RouteStopEntity> stops, List<RouteStopEntity> currentStops, Long tenantId) {
        // Preserve original stopOrder of the output stops using their indices to prevent memory corruption
        Map<RouteStopEntity, Integer> originalOrders = new HashMap<>();
        for (int i = 0; i < stops.size(); i++) {
            originalOrders.put(stops.get(i), stops.get(i).getStopOrder());
        }

        // Phase 1: park all already-persisted stops at temporary negative orders
        List<RouteStopEntity> persisted = currentStops.stream()
                .filter(s -> s.getId() != null)
                .collect(java.util.stream.Collectors.toList());
        for (int i = 0; i < persisted.size(); i++) {
            persisted.get(i).setStopOrder(-(1000 + i));
        }
        routeStopService.saveAllRouteStops(persisted);
        routeStopService.flush();

        // Phase 2: restore correct stopOrder before saving
        for (RouteStopEntity stop : stops) {
            Integer originalOrder = originalOrders.get(stop);
            if (originalOrder != null) {
                stop.setStopOrder(originalOrder);
            }
        }
        return routeStopService.saveAllRouteStops(stops);
    }

    private String actor(Long actorId) {
        return actorId != null ? actorId.toString() : "SYSTEM";
    }

    // ── Simulated aggregates & helper classes ────────────────────────────────
    private static final class PointAggregate {
        private final PickupPointEntity point;
        private final List<StudentSubscriptionEntity> students = new ArrayList<>();

        private PointAggregate(PickupPointEntity point) {
            this.point = point;
        }

        private void addStudent(StudentSubscriptionEntity sub) {
            students.add(sub);
        }

        private int studentCount() {
            return students.size();
        }
    }

    private static final class InsertionResult {
        private final boolean feasible;
        private final double cost;

        private InsertionResult(boolean feasible, double cost) {
            this.feasible = feasible;
            this.cost = cost;
        }
    }
}
