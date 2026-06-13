package serp.project.school_bus_service.service.algorithm;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import serp.project.school_bus_service.dto.request.GreedyFillRouteRequest;
import serp.project.school_bus_service.dto.response.GreedyFillRouteResponse;
import serp.project.school_bus_service.entity.PickupPointEntity;
import serp.project.school_bus_service.entity.RoutePlanEntity;
import serp.project.school_bus_service.entity.RoutePlanStudentEntity;
import serp.project.school_bus_service.entity.RoutePlanningSessionEntity;
import serp.project.school_bus_service.entity.RouteStopEntity;
import serp.project.school_bus_service.entity.SchoolPickupPointEntity;
import serp.project.school_bus_service.entity.StudentSubscriptionEntity;
import serp.project.school_bus_service.enums.PlanningSessionStatus;
import serp.project.school_bus_service.enums.RouteDirection;
import serp.project.school_bus_service.enums.RouteGeometrySource;
import serp.project.school_bus_service.enums.RouteLocationType;
import serp.project.school_bus_service.enums.RouteStatus;
import serp.project.school_bus_service.enums.RouteStopPurpose;
import serp.project.school_bus_service.service.IRouteGeometryService;
import serp.project.school_bus_service.service.IRoutePlanStudentService;
import serp.project.school_bus_service.service.IRoutePlanningSessionService;
import serp.project.school_bus_service.service.IRouteService;
import serp.project.school_bus_service.service.IRouteStopService;
import serp.project.school_bus_service.service.ISchoolPickupPointService;
import serp.project.school_bus_service.service.IStudentSubscriptionService;
import serp.project.school_bus_service.service.ITripExecutionService;
import serp.project.school_bus_service.service.algorithm.model.CandidateResolution;
import serp.project.school_bus_service.service.algorithm.model.CapacityState;
import serp.project.school_bus_service.service.algorithm.model.Coordinate;
import serp.project.school_bus_service.service.algorithm.model.GreedyChoice;
import serp.project.school_bus_service.service.algorithm.model.PersistenceResult;
import serp.project.school_bus_service.service.algorithm.model.RouteContext;
import serp.project.school_bus_service.service.algorithm.model.SelectedDemand;
import serp.project.school_bus_service.service.algorithm.model.SelectionResult;
import serp.project.school_bus_service.service.algorithm.model.StopDemand;
import serp.project.school_bus_service.service.algorithm.model.StudentCandidate;
import serp.project.school_bus_service.shared.exception.AppErrorCode;
import serp.project.school_bus_service.shared.exception.AppException;

import java.util.ArrayList;
import java.util.Comparator;
import java.util.HashMap;
import java.util.HashSet;
import java.util.LinkedHashMap;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.Set;
import java.util.function.Function;
import java.util.stream.Collectors;

/**
 * Applies a distance-per-student greedy fill to one existing route.
 *
 * This service owns no table. It coordinates the route, stop, subscription,
 * assignment, trip and geometry services that own the affected data.
 */
@Service
public class RouteGreedyFillService {

    private static final double EARTH_RADIUS_KM = 6371.0088;
    private static final double FALLBACK_SPEED_KMH = 30.0;

    private final IRoutePlanningSessionService planningSessionService;
    private final IRouteService routeService;
    private final IRouteStopService routeStopService;
    private final IRoutePlanStudentService routePlanStudentService;
    private final IStudentSubscriptionService subscriptionService;
    private final ISchoolPickupPointService schoolPickupPointService;
    private final ITripExecutionService tripExecutionService;
    private final IRouteGeometryService routeGeometryService;

    public RouteGreedyFillService(IRoutePlanningSessionService planningSessionService,
                                  IRouteService routeService,
                                  IRouteStopService routeStopService,
                                  IRoutePlanStudentService routePlanStudentService,
                                  IStudentSubscriptionService subscriptionService,
                                  ISchoolPickupPointService schoolPickupPointService,
                                  ITripExecutionService tripExecutionService,
                                  IRouteGeometryService routeGeometryService) {
        this.planningSessionService = planningSessionService;
        this.routeService = routeService;
        this.routeStopService = routeStopService;
        this.routePlanStudentService = routePlanStudentService;
        this.subscriptionService = subscriptionService;
        this.schoolPickupPointService = schoolPickupPointService;
        this.tripExecutionService = tripExecutionService;
        this.routeGeometryService = routeGeometryService;
    }

    /**
     * Runs the complete greedy flow for one existing route.
     *
     * Input:
     * - sessionId and routeId identify the planning context and target route.
     * - request controls assignment preservation and the optional service-stop limit.
     *
     * Output:
     * - assignment, capacity, stop and distance totals after persistence.
     */
    @Transactional
    public GreedyFillRouteResponse greedyFillRoute(Long sessionId,
                                                    Long routeId,
                                                    GreedyFillRouteRequest request,
                                                    Long tenantId,
                                                    Long actorId) {
        GreedyFillRouteRequest resolvedRequest = request == null ? new GreedyFillRouteRequest() : request;
        RouteContext context = loadAndValidateRouteForGreedyFill(
                sessionId, routeId, resolvedRequest, tenantId);
        CapacityState capacity = calculateRemainingCapacity(context.getRoute());
        CandidateResolution candidates = resolveEligibleUnassignedCandidates(context, tenantId);
        List<StopDemand> demands = groupCandidatesByServicePoint(candidates.getAssignable(), context.getDirection());
        SelectionResult selection = selectStopDemands(
                context, demands, capacity.getRemainingCapacity(), resolvedRequest.getMaxStops());
        PersistenceResult persisted = persistGreedySelection(
                context, selection, capacity.getCapacity(), tenantId, actorId);
        recalculatePathAndSessionSummary(context, persisted.getOrderedStops(), tenantId);
        return buildResponse(context, persisted, candidates, capacity.getCapacity());
    }

    /**
     * Step 1 - Loads the route and validates whether Greedy Fill can be applied.
     *
     * Input:
     * - sessionId and routeId requested by the dispatcher.
     * - request options, tenant scope and current persisted route state.
     *
     * Output:
     * - RouteContext with editable session, route, terminals and service stops.
     *
     * Next:
     * - The caller calculates remaining capacity.
     */
    private RouteContext loadAndValidateRouteForGreedyFill(Long sessionId,
                                                           Long routeId,
                                                           GreedyFillRouteRequest request,
                                                           Long tenantId) {
        if (!request.preserveExistingAssignments()) {
            throw new AppException(AppErrorCode.REQUEST_VALIDATION_FAILED,
                    "Replace existing assignments is not supported yet.");
        }

        RoutePlanningSessionEntity session = planningSessionService.requireSession(sessionId, tenantId);
        RoutePlanEntity route = routeService.getRouteEntity(routeId, tenantId);
        if (route.getPlanningSession() == null
                || !sessionId.equals(route.getPlanningSession().getId())) {
            throw new AppException(AppErrorCode.REQUEST_VALIDATION_FAILED,
                    "Route does not belong to the requested planning session.");
        }
        if (session.getStatus() == PlanningSessionStatus.PUBLISHED
                || session.getStatus() == PlanningSessionStatus.CANCELLED) {
            throw new AppException(AppErrorCode.Session.FROZEN,
                    "Cannot run Greedy Fill on a " + session.getStatus().name().toLowerCase(Locale.ROOT)
                            + " planning session.");
        }
        if (route.getStatus() == RouteStatus.PUBLISHED
                || route.getStatus() == RouteStatus.ASSIGNED
                || route.getStatus() == RouteStatus.TRIP_CREATED
                || route.getStatus() == RouteStatus.CANCELLED) {
            throw new AppException(AppErrorCode.Route.INVALID_STATE,
                    "Cannot run Greedy Fill when route status is " + route.getStatus() + ".");
        }
        if (tripExecutionService.existsByRoute(routeId, tenantId)) {
            throw new AppException(AppErrorCode.Route.INVALID_STATE,
                    "Route already has a trip execution.");
        }
        if (route.getRouteDirection() == null) {
            throw new AppException(AppErrorCode.REQUEST_VALIDATION_FAILED,
                    "Route direction is required before running Greedy Fill.");
        }
        if (session.getRouteDirection() != route.getRouteDirection()) {
            throw new AppException(AppErrorCode.REQUEST_VALIDATION_FAILED,
                    "Route direction must match the planning session direction.");
        }
        if (!session.getSchool().getId().equals(route.getSchool().getId())
                || !session.getServiceDate().equals(route.getServiceDate())) {
            throw new AppException(AppErrorCode.REQUEST_VALIDATION_FAILED,
                    "Route school and service date must match the planning session.");
        }
        validateRouteEndpoints(route);

        List<RouteStopEntity> stops = routeStopService.findByRoute(routeId, tenantId);
        RouteStopEntity startTerminal = requireTerminal(stops, RouteStopPurpose.START_TERMINAL);
        RouteStopEntity endTerminal = requireTerminal(stops, RouteStopPurpose.END_TERMINAL);
        validateCoordinate(startTerminal, "Start terminal must have coordinates before running Greedy Fill.");
        validateCoordinate(endTerminal, "End terminal must have coordinates before running Greedy Fill.");

        List<RouteStopEntity> serviceStops = stops.stream()
                .filter(stop -> stop.getStopPurpose() != null && !stop.getStopPurpose().isTerminal())
                .toList();
        for (RouteStopEntity stop : serviceStops) {
            validateCoordinate(stop, "Existing service stops must have coordinates before running Greedy Fill.");
        }
        RouteContext context = new RouteContext();
        context.setSession(session);
        context.setRoute(route);
        context.setDirection(route.getRouteDirection());
        context.setStartTerminal(startTerminal);
        context.setEndTerminal(endTerminal);
        context.setServiceStops(new ArrayList<>(serviceStops));
        return context;
    }

    /**
     * Step 2 - Calculates usable and remaining bus capacity.
     *
     * Input:
     * - validated route with its selected bus and existing assignments.
     *
     * Output:
     * - total capacity, assigned count and remaining capacity.
     *
     * Next:
     * - The caller resolves eligible unassigned student demand.
     */
    private CapacityState calculateRemainingCapacity(RoutePlanEntity route) {
        if (route.getSelectedBus() == null) {
            throw new AppException(AppErrorCode.Bus.SELECTED_BUS_REQUIRED,
                    "Route must have a selected bus before running Greedy Fill.");
        }
        Integer capacity = route.getSelectedBus().getCapacity();
        if (capacity == null || capacity <= 0) {
            throw new AppException(AppErrorCode.Bus.CAPACITY_NOT_CONFIGURED,
                    "The selected bus must have a positive capacity.");
        }
        int assigned = Math.toIntExact(routePlanStudentService.countDistinctStudentsByRoute(route.getId()));
        int remaining = capacity - assigned;
        if (remaining <= 0) {
            throw new AppException(AppErrorCode.Bus.CAPACITY_EXCEEDED,
                    "Route is already full.");
        }
        CapacityState capacityState = new CapacityState();
        capacityState.setCapacity(capacity);
        capacityState.setAssignedStudents(assigned);
        capacityState.setRemainingCapacity(remaining);
        return capacityState;
    }

    /**
     * Step 3 - Resolves eligible demand that is not already assigned in the session.
     *
     * Input:
     * - school, service date and route direction from the validated context.
     *
     * Output:
     * - assignable subscriptions plus counts excluded by assignment or coordinates.
     *
     * Next:
     * - The caller groups assignable subscriptions by their physical service point.
     */
    private CandidateResolution resolveEligibleUnassignedCandidates(RouteContext context, Long tenantId) {
        List<StudentSubscriptionEntity> eligible = subscriptionService.findEligibleSubscriptions(
                context.getSession().getSchool().getId(),
                context.getDirection(),
                context.getSession().getServiceDate(),
                tenantId);

        Set<Long> currentRouteStudents = routePlanStudentService.findByRoute(context.getRoute().getId()).stream()
                .map(item -> item.getStudent().getId())
                .collect(Collectors.toSet());
        Set<Long> otherRouteStudents = routePlanStudentService
                .findStudentsInOtherRoutesOfSessionAndDirection(
                        context.getSession().getId(), context.getRoute().getId(), context.getDirection())
                .stream()
                .map(item -> item.getStudent().getId())
                .collect(Collectors.toSet());
        Set<Long> linkedPointIds = schoolPickupPointService
                .getPickupPointLinksForSchools(
                        List.of(context.getSession().getSchool().getId()), tenantId)
                .stream()
                .filter(link -> Boolean.TRUE.equals(link.getIsActive())
                        && !Boolean.TRUE.equals(link.getIsDeleted()))
                .map(SchoolPickupPointEntity::getPickupPoint)
                .map(PickupPointEntity::getId)
                .collect(Collectors.toSet());

        Map<Long, StudentCandidate> uniqueCandidates = new LinkedHashMap<>();
        Set<Long> eligibleStudentIds = new LinkedHashSet<>();
        int assignedElsewhere = 0;
        int missingCoordinates = 0;
        int invalidPoint = 0;

        for (StudentSubscriptionEntity subscription : eligible.stream()
                .sorted(Comparator.comparing(StudentSubscriptionEntity::getId))
                .toList()) {
            Long studentId = subscription.getStudent().getId();
            if (!eligibleStudentIds.add(studentId) || currentRouteStudents.contains(studentId)) {
                continue;
            }
            if (otherRouteStudents.contains(studentId)) {
                assignedElsewhere++;
                continue;
            }
            PickupPointEntity point = relevantPoint(subscription, context.getDirection());
            if (!isValidServicePoint(point, context.getDirection(), linkedPointIds)) {
                invalidPoint++;
                continue;
            }
            if (point.getLatitude() == null || point.getLongitude() == null) {
                missingCoordinates++;
                continue;
            }
            StudentCandidate candidate = new StudentCandidate();
            candidate.setSubscription(subscription);
            candidate.setPoint(point);
            uniqueCandidates.put(studentId, candidate);
        }
        CandidateResolution resolution = new CandidateResolution();
        resolution.setAssignable(new ArrayList<>(uniqueCandidates.values()));
        resolution.setEligibleStudentIds(eligibleStudentIds);
        resolution.setCurrentRouteStudents(currentRouteStudents);
        resolution.setOtherRouteStudents(otherRouteStudents);
        resolution.setAssignedElsewhere(assignedElsewhere);
        resolution.setMissingCoordinates(missingCoordinates);
        resolution.setInvalidPoint(invalidPoint);
        return resolution;
    }

    /**
     * Step 4 - Groups eligible students by the physical stop used by this direction.
     *
     * OUTBOUND uses pickup points because students board the bus.
     * RETURN uses drop-off points because students leave the bus.
     *
     * Output:
     * - one StopDemand per point, containing all unassigned candidates at that point.
     */
    private List<StopDemand> groupCandidatesByServicePoint(List<StudentCandidate> candidates,
                                                           RouteDirection direction) {
        Map<Long, StopDemand> grouped = new LinkedHashMap<>();
        for (StudentCandidate candidate : candidates) {
            PickupPointEntity point = relevantPoint(candidate.getSubscription(), direction);
            StopDemand demand = grouped.computeIfAbsent(point.getId(), ignored -> {
                StopDemand newDemand = new StopDemand();
                newDemand.setPoint(point);
                newDemand.setCandidates(new ArrayList<>());
                return newDemand;
            });
            demand.getCandidates().add(candidate);
        }
        grouped.values().forEach(demand -> demand.getCandidates()
                .sort(Comparator.comparing(candidate -> candidate.getSubscription().getStudent().getId())));
        return new ArrayList<>(grouped.values());
    }

    /**
     * Step 5 - Selects stop demands using a greedy score.
     *
     * Input:
     * - current route terminals and service stops.
     * - grouped StopDemand list, remaining bus capacity and optional stop limit.
     *
     * Output:
     * - selected demands with the exact students to add at each stop.
     *
     * Greedy rule:
     * - choose the stop with the lowest added distance per assigned student.
     */
    private SelectionResult selectStopDemands(RouteContext context,
                                              List<StopDemand> demands,
                                              int remainingCapacity,
                                              Integer maxStops) {
        Map<Long, RouteStopEntity> existingStopsByPoint = context.getServiceStops().stream()
                .filter(stop -> stop.getPickupPoint() != null)
                .collect(Collectors.toMap(
                        stop -> stop.getPickupPoint().getId(),
                        Function.identity(),
                        (left, right) -> left));
        Coordinate current = context.getServiceStops().isEmpty()
                ? coordinateOf(context.getStartTerminal())
                : coordinateOf(context.getServiceStops().get(context.getServiceStops().size() - 1));
        Coordinate end = coordinateOf(context.getEndTerminal());
        List<StopDemand> remainingDemands = new ArrayList<>(demands);
        List<SelectedDemand> selected = new ArrayList<>();
        int newStopCount = 0;

        while (remainingCapacity > 0 && !remainingDemands.isEmpty()) {
            GreedyChoice best = null;
            for (StopDemand demand : remainingDemands) {
                boolean existingStop = existingStopsByPoint.containsKey(demand.getPoint().getId());
                if (!existingStop && maxStops != null
                        && context.getServiceStops().size() + newStopCount >= maxStops) {
                    continue;
                }
                int takeCount = Math.min(demand.getCandidates().size(), remainingCapacity);
                Coordinate point = coordinateOf(demand.getPoint());
                double addedDistance = existingStop ? 0.0
                        : addedDistanceKm(current, point, end);
                double score = addedDistance / takeCount;
                GreedyChoice choice = new GreedyChoice();
                choice.setDemand(demand);
                choice.setTakeCount(takeCount);
                choice.setAddedDistanceKm(addedDistance);
                choice.setScore(score);
                choice.setExistingStop(existingStop);
                if (best == null || GREEDY_CHOICE_COMPARATOR.compare(choice, best) < 0) {
                    best = choice;
                }
            }
            if (best == null) {
                break;
            }

            List<StudentCandidate> selectedStudents =
                    new ArrayList<>(best.getDemand().getCandidates().subList(0, best.getTakeCount()));
            SelectedDemand selectedDemand = new SelectedDemand();
            selectedDemand.setPoint(best.getDemand().getPoint());
            selectedDemand.setStudents(selectedStudents);
            selectedDemand.setExistingStop(best.isExistingStop());
            selected.add(selectedDemand);
            remainingCapacity -= best.getTakeCount();
            remainingDemands.remove(best.getDemand());
            if (!best.isExistingStop()) {
                current = coordinateOf(best.getDemand().getPoint());
                newStopCount++;
            }
        }
        SelectionResult result = new SelectionResult();
        result.setSelectedDemands(selected);
        result.setRemainingCapacity(remainingCapacity);
        return result;
    }

    private static final Comparator<GreedyChoice> GREEDY_CHOICE_COMPARATOR =
            Comparator.comparingDouble(GreedyChoice::getScore)
                    .thenComparingDouble(GreedyChoice::getAddedDistanceKm)
                    .thenComparing(choice -> choice.getDemand().getPoint().getId());

    /**
     * Step 6 and 7 - Creates or updates stops, orders them, and persists students.
     *
     * Input:
     * - selected stop demands and the validated route context.
     *
     * Output:
     * - ordered persisted stops and updated route/student totals.
     *
     * Next:
     * - The caller recalculates geometry and the planning-session summary.
     */
    private PersistenceResult persistGreedySelection(RouteContext context,
                                                     SelectionResult selection,
                                                     int capacity,
                                                     Long tenantId,
                                                     Long actorId) {
        RouteStopPurpose servicePurpose = context.getDirection() == RouteDirection.OUTBOUND
                ? RouteStopPurpose.PICKUP : RouteStopPurpose.DROPOFF;
        Map<Long, RouteStopEntity> stopsByPoint = context.getServiceStops().stream()
                .filter(stop -> stop.getPickupPoint() != null
                        && stop.getStopPurpose() == servicePurpose)
                .collect(Collectors.toMap(
                        stop -> stop.getPickupPoint().getId(),
                        Function.identity(),
                        (left, right) -> left,
                        LinkedHashMap::new));
        Map<Long, RouteStopEntity> selectedStops = new HashMap<>();
        Map<Long, List<StudentCandidate>> acceptedCandidatesByPoint = new LinkedHashMap<>();
        Set<Long> routeStudentIds = routePlanStudentService.findByRoute(context.getRoute().getId()).stream()
                .map(item -> item.getStudent().getId())
                .collect(Collectors.toSet());
        Set<String> batchAssignmentKeys = new HashSet<>();
        int createdStopCount = 0;

        for (SelectedDemand demand : selection.getSelectedDemands()) {
            List<StudentCandidate> acceptedCandidates = new ArrayList<>();
            for (StudentCandidate candidate : demand.getStudents()) {
                StudentSubscriptionEntity subscription = candidate.getSubscription();
                Long studentId = subscription.getStudent().getId();
                Long subscriptionId = subscription.getId();
                String assignmentKey = studentId + ":" + subscriptionId;
                if (routeStudentIds.contains(studentId)
                        || !batchAssignmentKeys.add(assignmentKey)
                        || routePlanStudentService.existsByRouteAndStudent(
                                context.getRoute().getId(), studentId)
                        || routePlanStudentService.existsByRouteStudentAndSubscription(
                                context.getRoute().getId(), studentId, subscriptionId)
                        || routePlanStudentService.existsInOtherRoutesOfSessionAndDirection(
                                context.getSession().getId(),
                                context.getRoute().getId(),
                                studentId,
                                context.getDirection())) {
                    continue;
                }
                routeStudentIds.add(studentId);
                acceptedCandidates.add(candidate);
            }
            if (acceptedCandidates.isEmpty()) {
                continue;
            }

            acceptedCandidatesByPoint.put(demand.getPoint().getId(), acceptedCandidates);
            RouteStopEntity stop = stopsByPoint.get(demand.getPoint().getId());
            if (stop == null) {
                stop = buildServiceStop(context.getRoute(), demand.getPoint(), tenantId, actorId);
                stopsByPoint.put(demand.getPoint().getId(), stop);
                context.getServiceStops().add(stop);
                createdStopCount++;
            }
            selectedStops.put(demand.getPoint().getId(), stop);
        }

        normalizeTerminalMetadata(context, actorId);
        List<RouteStopEntity> orderedStops = orderStopsByNearestNeighbor(context);
        routeStopService.saveAllRouteStops(orderedStops);

        List<RoutePlanStudentEntity> newAssignments = new ArrayList<>();
        for (SelectedDemand demand : selection.getSelectedDemands()) {
            RouteStopEntity serviceStop = selectedStops.get(demand.getPoint().getId());
            List<StudentCandidate> acceptedCandidates = acceptedCandidatesByPoint.get(demand.getPoint().getId());
            if (serviceStop == null || acceptedCandidates == null) {
                continue;
            }
            for (StudentCandidate candidate : acceptedCandidates) {
                newAssignments.add(buildRouteStudent(
                        context, candidate.getSubscription(), serviceStop, tenantId, actorId));
            }
        }
        routePlanStudentService.saveAll(newAssignments);
        routePlanStudentService.flush();
        orderedStops = recalculateRouteStopPlannedCounts(
                context.getRoute().getId(), tenantId, actorId);

        int totalAssigned = Math.toIntExact(
                routePlanStudentService.countDistinctStudentsByRoute(context.getRoute().getId()));
        RoutePlanEntity route = context.getRoute();
        route.setPlannedStudentCount(totalAssigned);
        route.setRequiredCapacity(totalAssigned);
        route.setAssignedBusCapacity(capacity);
        route.markUpdated(actor(actorId));
        routeService.saveRouteEntity(route);
        PersistenceResult result = new PersistenceResult();
        result.setAddedStudents(newAssignments.size());
        result.setAddedStops(createdStopCount);
        result.setTotalAssignedStudents(totalAssigned);
        result.setOrderedStops(orderedStops);
        return result;
    }

    /**
     * Step 8 - Stores a straight-line fallback, tries OSRM, and refreshes session totals.
     *
     * Input:
     * - the final ordered stop list and persisted route.
     *
     * Output:
     * - route path/distance/duration and current planning-session summary.
     *
     * OSRM may replace the fallback. A routing failure leaves the Haversine path usable.
     */
    private void recalculatePathAndSessionSummary(RouteContext context,
                                                  List<RouteStopEntity> orderedStops,
                                                  Long tenantId) {
        applyHaversineFallback(context.getRoute(), orderedStops);
        routeService.saveRouteEntity(context.getRoute());
        routeGeometryService.recalculateGeometry(context.getRoute(), tenantId);
        routeService.saveRouteEntity(context.getRoute());
        planningSessionService.refreshSessionSummary(context.getSession().getId(), tenantId);
    }

    /**
     * Step 9 - Builds the API response from persisted state.
     *
     * Input:
     * - route/session context, persistence totals and candidate exclusion counts.
     *
     * Output:
     * - concise result for route-planning UI refresh and toast feedback.
     */
    private GreedyFillRouteResponse buildResponse(RouteContext context,
                                                  PersistenceResult persisted,
                                                  CandidateResolution candidates,
                                                  int capacity) {
        Set<Long> assignedAfterFill = new HashSet<>(candidates.getCurrentRouteStudents());
        assignedAfterFill.addAll(candidates.getOtherRouteStudents());
        routePlanStudentService.findByRoute(context.getRoute().getId()).stream()
                .map(item -> item.getStudent().getId())
                .forEach(assignedAfterFill::add);
        int unassigned = (int) candidates.getEligibleStudentIds().stream()
                .filter(studentId -> !assignedAfterFill.contains(studentId))
                .count();
        int remainingCapacity = Math.max(0, capacity - persisted.getTotalAssignedStudents());

        GreedyFillRouteResponse response = new GreedyFillRouteResponse();
        response.setRouteId(context.getRoute().getId());
        response.setSessionId(context.getSession().getId());
        response.setAddedStudents(persisted.getAddedStudents());
        response.setAddedStops(persisted.getAddedStops());
        response.setTotalAssignedStudents(persisted.getTotalAssignedStudents());
        response.setRemainingCapacity(remainingCapacity);
        response.setPlannedDistanceKm(context.getRoute().getPlannedDistanceKm());
        response.setPlannedDurationMin(context.getRoute().getPlannedDurationMin());
        response.setUnassignedCandidates(unassigned);
        response.setSkippedAssignedElsewhere(candidates.getAssignedElsewhere());
        response.setSkippedMissingCoordinates(candidates.getMissingCoordinates());
        response.setSkippedInvalidPoint(candidates.getInvalidPoint());
        response.setMessage("Added " + persisted.getAddedStudents() + " students across "
                + persisted.getAddedStops() + " stops. " + unassigned
                + " eligible students remain unassigned.");
        return response;
    }

    private void validateRouteEndpoints(RoutePlanEntity route) {
        if (route.getStartLocationType() == null || route.getEndLocationType() == null) {
            throw new AppException(AppErrorCode.REQUEST_VALIDATION_FAILED,
                    "Route must have start and end terminals before running Greedy Fill.");
        }
        boolean startMissing = route.getStartLocationType() == RouteLocationType.SCHOOL
                ? route.getStartSchool() == null : route.getStartDepot() == null;
        boolean endMissing = route.getEndLocationType() == RouteLocationType.SCHOOL
                ? route.getEndSchool() == null : route.getEndDepot() == null;
        if (startMissing || endMissing) {
            throw new AppException(AppErrorCode.REQUEST_VALIDATION_FAILED,
                    "Route must have start and end terminals before running Greedy Fill.");
        }
        if (route.getRouteDirection() == RouteDirection.OUTBOUND
                && (route.getStartLocationType() != RouteLocationType.DEPOT
                || route.getEndLocationType() != RouteLocationType.SCHOOL)) {
            throw new AppException(AppErrorCode.REQUEST_VALIDATION_FAILED,
                    "OUTBOUND Greedy Fill requires Depot to School terminals.");
        }
        if (route.getRouteDirection() == RouteDirection.RETURN
                && (route.getStartLocationType() != RouteLocationType.SCHOOL
                || route.getEndLocationType() != RouteLocationType.DEPOT)) {
            throw new AppException(AppErrorCode.REQUEST_VALIDATION_FAILED,
                    "RETURN Greedy Fill requires School to Depot terminals.");
        }
    }

    private RouteStopEntity requireTerminal(List<RouteStopEntity> stops, RouteStopPurpose purpose) {
        return stops.stream()
                .filter(stop -> stop.getStopPurpose() == purpose)
                .findFirst()
                .orElseThrow(() -> new AppException(AppErrorCode.REQUEST_VALIDATION_FAILED,
                        "Route is missing its " + purpose.name().toLowerCase(Locale.ROOT) + " stop."));
    }

    private void validateCoordinate(RouteStopEntity stop, String message) {
        if (stop.getLatitude() == null || stop.getLongitude() == null) {
            throw new AppException(AppErrorCode.REQUEST_VALIDATION_FAILED, message);
        }
    }

    private PickupPointEntity relevantPoint(StudentSubscriptionEntity subscription,
                                            RouteDirection direction) {
        return direction == RouteDirection.OUTBOUND
                ? subscription.getPickupPoint()
                : subscription.getDropoffPoint();
    }

    private boolean isValidServicePoint(PickupPointEntity point,
                                        RouteDirection direction,
                                        Set<Long> linkedPointIds) {
        if (point == null
                || Boolean.TRUE.equals(point.getIsDeleted())
                || Boolean.FALSE.equals(point.getIsActive())
                || !linkedPointIds.contains(point.getId())) {
            return false;
        }
        String usageType = point.getUsageType();
        if (usageType == null || "PICKUP_DROPOFF".equalsIgnoreCase(usageType)) {
            return true;
        }
        return direction == RouteDirection.OUTBOUND
                ? "PICKUP_ONLY".equalsIgnoreCase(usageType)
                : "DROPOFF_ONLY".equalsIgnoreCase(usageType);
    }

    private double addedDistanceKm(Coordinate current, Coordinate candidate, Coordinate end) {
        return Math.max(0.0,
                haversineKm(current, candidate)
                        + haversineKm(candidate, end)
                        - haversineKm(current, end));
    }

    private RouteStopEntity buildServiceStop(RoutePlanEntity route,
                                             PickupPointEntity point,
                                             Long tenantId,
                                             Long actorId) {
        RouteStopEntity stop = new RouteStopEntity();
        stop.markCreated(tenantId, actor(actorId));
        stop.setRoute(route);
        stop.setPickupPoint(point);
        stop.setLocationType(RouteLocationType.PICKUP_POINT);
        stop.setStopPurpose(route.getRouteDirection() == RouteDirection.OUTBOUND
                ? RouteStopPurpose.PICKUP : RouteStopPurpose.DROPOFF);
        stop.setEstimatedStudentCount(0);
        stop.setPlannedBoardingCount(0);
        stop.setPlannedDropoffCount(0);
        return stop;
    }

    /**
     * Recalculates service-stop counters from active route assignments.
     * Terminal assignment references are intentionally excluded from displayed
     * boarding/drop-off counters.
     */
    private List<RouteStopEntity> recalculateRouteStopPlannedCounts(Long routeId,
                                                                    Long tenantId,
                                                                    Long actorId) {
        List<RoutePlanStudentEntity> assignments = routePlanStudentService.findByRoute(routeId);
        Map<Long, Integer> boardingByStop = assignments.stream()
                .filter(item -> item.getPickupStop() != null)
                .collect(Collectors.groupingBy(
                        item -> item.getPickupStop().getId(),
                        Collectors.summingInt(item -> 1)));
        Map<Long, Integer> dropoffByStop = assignments.stream()
                .filter(item -> item.getDropoffStop() != null)
                .collect(Collectors.groupingBy(
                        item -> item.getDropoffStop().getId(),
                        Collectors.summingInt(item -> 1)));

        List<RouteStopEntity> stops = routeStopService.findByRoute(routeId, tenantId);
        for (RouteStopEntity stop : stops) {
            boolean terminal = stop.getStopPurpose() != null && stop.getStopPurpose().isTerminal();
            int boarding = terminal ? 0 : boardingByStop.getOrDefault(stop.getId(), 0);
            int dropoff = terminal ? 0 : dropoffByStop.getOrDefault(stop.getId(), 0);
            stop.setPlannedBoardingCount(boarding);
            stop.setPlannedDropoffCount(dropoff);
            stop.setEstimatedStudentCount(boarding + dropoff);
            stop.markUpdated(actor(actorId));
        }
        return routeStopService.saveAllRouteStops(stops);
    }

    private void normalizeTerminalMetadata(RouteContext context, Long actorId) {
        applyTerminalLocation(context.getStartTerminal(), context.getRoute(), true, actorId);
        applyTerminalLocation(context.getEndTerminal(), context.getRoute(), false, actorId);
    }

    private void applyTerminalLocation(RouteStopEntity terminal,
                                       RoutePlanEntity route,
                                       boolean start,
                                       Long actorId) {
        RouteLocationType type = start ? route.getStartLocationType() : route.getEndLocationType();
        terminal.setLocationType(type);
        terminal.setStopPurpose(start ? RouteStopPurpose.START_TERMINAL : RouteStopPurpose.END_TERMINAL);
        terminal.setPickupPoint(null);
        terminal.setSchool(type == RouteLocationType.SCHOOL
                ? (start ? route.getStartSchool() : route.getEndSchool()) : null);
        terminal.setDepot(type == RouteLocationType.DEPOT
                ? (start ? route.getStartDepot() : route.getEndDepot()) : null);
        terminal.setEstimatedStudentCount(0);
        terminal.setPlannedBoardingCount(0);
        terminal.setPlannedDropoffCount(0);
        terminal.markUpdated(actor(actorId));
    }

    private List<RouteStopEntity> orderStopsByNearestNeighbor(RouteContext context) {
        List<RouteStopEntity> remaining = new ArrayList<>(context.getServiceStops());
        List<RouteStopEntity> ordered = new ArrayList<>();
        ordered.add(context.getStartTerminal());
        Coordinate current = coordinateOf(context.getStartTerminal());
        while (!remaining.isEmpty()) {
            Coordinate origin = current;
            RouteStopEntity nearest = remaining.stream()
                    .min(Comparator
                            .comparingDouble((RouteStopEntity stop) ->
                                    haversineKm(origin, coordinateOf(stop)))
                            .thenComparing(stop -> stop.getPickupPoint().getId()))
                    .orElseThrow();
            ordered.add(nearest);
            remaining.remove(nearest);
            current = coordinateOf(nearest);
        }
        ordered.add(context.getEndTerminal());
        for (int index = 0; index < ordered.size(); index++) {
            ordered.get(index).setStopOrder(index);
        }
        return ordered;
    }

    private RoutePlanStudentEntity buildRouteStudent(RouteContext context,
                                                     StudentSubscriptionEntity subscription,
                                                     RouteStopEntity serviceStop,
                                                     Long tenantId,
                                                     Long actorId) {
        RoutePlanStudentEntity assignment = new RoutePlanStudentEntity();
        assignment.markCreated(tenantId, actor(actorId));
        assignment.setRoute(context.getRoute());
        assignment.setStudent(subscription.getStudent());
        assignment.setSubscription(subscription);
        if (context.getDirection() == RouteDirection.OUTBOUND) {
            assignment.setPickupStop(serviceStop);
            assignment.setDropoffStop(context.getEndTerminal());
        } else {
            assignment.setPickupStop(context.getStartTerminal());
            assignment.setDropoffStop(serviceStop);
        }
        return assignment;
    }

    private void applyHaversineFallback(RoutePlanEntity route, List<RouteStopEntity> orderedStops) {
        double totalDistance = 0.0;
        for (int index = 1; index < orderedStops.size(); index++) {
            double legDistance = haversineKm(
                    coordinateOf(orderedStops.get(index - 1)),
                    coordinateOf(orderedStops.get(index)));
            orderedStops.get(index).setDistanceFromPreviousKm(roundTwoDecimals(legDistance));
            totalDistance += legDistance;
        }
        if (!orderedStops.isEmpty()) {
            orderedStops.get(0).setDistanceFromPreviousKm(null);
        }
        routeStopService.saveAllRouteStops(orderedStops);
        route.setGeometryPath(orderedStops.stream()
                .map(stop -> "[" + formatCoordinate(stop.getLongitude())
                        + "," + formatCoordinate(stop.getLatitude()) + "]")
                .collect(Collectors.joining(",", "[", "]")));
        route.setGeometrySource(RouteGeometrySource.HAVERSINE_FALLBACK);
        route.setPlannedDistanceKm(roundTwoDecimals(totalDistance));
        route.setPlannedDurationMin((int) Math.ceil(totalDistance / FALLBACK_SPEED_KMH * 60.0));
    }

    private Coordinate coordinateOf(RouteStopEntity stop) {
        Coordinate coordinate = new Coordinate();
        coordinate.setLatitude(stop.getLatitude());
        coordinate.setLongitude(stop.getLongitude());
        return coordinate;
    }

    private Coordinate coordinateOf(PickupPointEntity point) {
        Coordinate coordinate = new Coordinate();
        coordinate.setLatitude(point.getLatitude());
        coordinate.setLongitude(point.getLongitude());
        return coordinate;
    }

    private double haversineKm(Coordinate from, Coordinate to) {
        double latDelta = Math.toRadians(to.getLatitude() - from.getLatitude());
        double lonDelta = Math.toRadians(to.getLongitude() - from.getLongitude());
        double fromLat = Math.toRadians(from.getLatitude());
        double toLat = Math.toRadians(to.getLatitude());
        double a = Math.sin(latDelta / 2) * Math.sin(latDelta / 2)
                + Math.cos(fromLat) * Math.cos(toLat)
                * Math.sin(lonDelta / 2) * Math.sin(lonDelta / 2);
        return EARTH_RADIUS_KM * 2 * Math.atan2(Math.sqrt(a), Math.sqrt(1 - a));
    }

    private double roundTwoDecimals(double value) {
        return Math.round(value * 100.0) / 100.0;
    }

    private String formatCoordinate(Double value) {
        return String.format(Locale.ROOT, "%.7f", value);
    }

    private String actor(Long actorId) {
        return actorId == null ? "system" : String.valueOf(actorId);
    }

}
