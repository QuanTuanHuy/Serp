/*
Author: QuanTuanHuy
Description: Part of Serp Project
*/

package serp.project.first_mile.service.impl;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.locationtech.jts.geom.Point;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.data.domain.PageRequest;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import serp.project.first_mile.caller.DistanceMatrixCaller;
import serp.project.first_mile.domain.Order;
import serp.project.first_mile.domain.PostOffice;
import serp.project.first_mile.domain.PostOfficeStaff;
import serp.project.first_mile.domain.PostOfficeStaffAssignment;
import serp.project.first_mile.domain.Trip;
import serp.project.first_mile.domain.TripOrder;
import serp.project.first_mile.domain.Vehicle;
import serp.project.first_mile.dto.request.AutoAssignPickupPlanRequest;
import serp.project.first_mile.dto.request.ManualAssignPickupOrdersRequest;
import serp.project.first_mile.dto.request.OptimizePickupPlanRequest;
import serp.project.first_mile.dto.response.PickupAssignmentResponse;
import serp.project.first_mile.dto.response.PickupOptimizationResponse;
import serp.project.first_mile.enums.OrderStatus;
import serp.project.first_mile.enums.PostOfficeStaffRole;
import serp.project.first_mile.enums.PostOfficeStaffStatus;
import serp.project.first_mile.enums.PickupDestroyOperator;
import serp.project.first_mile.enums.PickupShift;
import serp.project.first_mile.enums.PickupRepairOperator;
import serp.project.first_mile.enums.RoutingVehicle;
import serp.project.first_mile.enums.TripStatus;
import serp.project.first_mile.enums.VehicleStatus;
import serp.project.first_mile.exception.AppException;
import serp.project.first_mile.exception.ErrorCode;
import serp.project.first_mile.kernel.utils.AuthUtils;
import serp.project.first_mile.repository.OrderRepository;
import serp.project.first_mile.repository.PostOfficeRepository;
import serp.project.first_mile.repository.PostOfficeStaffAssignmentRepository;
import serp.project.first_mile.repository.PostOfficeStaffRepository;
import serp.project.first_mile.repository.TripOrderRepository;
import serp.project.first_mile.repository.TripRepository;
import serp.project.first_mile.repository.VehicleRepository;
import serp.project.first_mile.service.PickupOptimizationService;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.time.temporal.ChronoUnit;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.Comparator;
import java.util.EnumMap;
import java.util.HashMap;
import java.util.HashSet;
import java.util.LinkedHashMap;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.Objects;
import java.util.Random;
import java.util.Set;
import java.util.UUID;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
@Slf4j
public class PickupOptimizationServiceImpl implements PickupOptimizationService {

    private static final String ROLE_TMS_ADMIN = "TMS_ADMIN";
    private static final String ROLE_TMS_POSTOFFICER_MANAGER = "TMS_POSTOFFICER_MANAGER";

    private static final int DEFAULT_ORDER_LIMIT = 300;
    private static final double DEFAULT_AVERAGE_SPEED_KMPH = 25.0;
    private static final int DEFAULT_SERVICE_MINUTES_PER_STOP = 8;
    private static final int DEFAULT_MAX_ITERATIONS = 300;
    private static final long DEFAULT_MAX_RUNTIME_MILLIS = 1500L;
    private static final double DEFAULT_DESTROY_RATE = 0.20;
    private static final double DEFAULT_INITIAL_TEMPERATURE = 50.0;
    private static final double DEFAULT_COOLING_RATE = 0.995;
    private static final boolean DEFAULT_ALLOW_LATENESS = true;
    private static final boolean DEFAULT_ENFORCE_PLANNING_END = false;
    private static final boolean DEFAULT_ENFORCE_CAPACITY = true;

    private static final double DEFAULT_DISTANCE_WEIGHT = 1.0;
    private static final double DEFAULT_LATENESS_WEIGHT = 0.5;
    private static final double DEFAULT_UNASSIGNED_PENALTY = 500.0;
    private static final double DEFAULT_USED_ROUTE_PENALTY = 3.0;
    private static final int DEFAULT_DISTANCE_MATRIX_BATCH_SIZE = 20;
    private static final int DEFAULT_DISTANCE_MATRIX_MAX_NODES = 120;
    private static final int DEFAULT_TRIP_CODE_RANDOM_LENGTH = 8;

    private static final int OPERATOR_UPDATE_SEGMENT = 25;
    private static final double OPERATOR_REACTION_FACTOR = 0.2;
    private static final double OPERATOR_MIN_WEIGHT = 0.1;

    private static final double DEFAULT_MAX_WEIGHT_IF_MISSING = 100000.0;
    private static final double DEFAULT_MAX_VOLUME_IF_MISSING = 1000.0;
    private static final double EARTH_RADIUS_KM = 6371.0;
    private static final double EPSILON = 1e-9;
    private static final double HUGE_OBJECTIVE = 1e15;

    private static final String REASON_UNASSIGNED = "UNASSIGNED";
    private static final String REASON_NO_FEASIBLE_INSERTION = "NO_FEASIBLE_INSERTION";
    private static final String REASON_ALNS_REMOVAL = "REMOVED_BY_ALNS";
    private static final String REASON_MISSING_SENDER_LOCATION = "MISSING_SENDER_LOCATION";
    private static final String REASON_INVALID_SENDER_LOCATION = "INVALID_SENDER_LOCATION";
    private static final String REASON_ALREADY_ASSIGNED_TO_ACTIVE_TRIP = "ALREADY_ASSIGNED_TO_ACTIVE_TRIP";
    private static final String REASON_ORDER_NOT_ASSIGNABLE = "ORDER_NOT_ASSIGNABLE";

    private static final LocalTime SHIFT_MORNING_START = LocalTime.of(7, 30);
    private static final LocalTime SHIFT_MORNING_END = LocalTime.of(12, 0);
    private static final LocalTime SHIFT_AFTERNOON_START = LocalTime.of(13, 30);
    private static final LocalTime SHIFT_AFTERNOON_END = LocalTime.of(18, 0);
    private static final LocalTime SHIFT_EVENING_START = LocalTime.of(18, 30);
    private static final LocalTime SHIFT_EVENING_END = LocalTime.of(22, 0);

    private static final List<TripStatus> ACTIVE_ASSIGNMENT_TRIP_STATUSES = List.of(
            TripStatus.PLANNED,
            TripStatus.IN_PROGRESS
    );

    private static final List<TripStatus> REPLANNABLE_TRIP_STATUSES = List.of(
            TripStatus.PLANNED
    );

    private static final List<OrderStatus> DEFAULT_CANDIDATE_STATUSES = List.of(
            OrderStatus.CREATED,
            OrderStatus.PICKUP_FAILED
    );

    private final OrderRepository orderRepository;
    private final PostOfficeRepository postOfficeRepository;
    private final PostOfficeStaffRepository postOfficeStaffRepository;
    private final PostOfficeStaffAssignmentRepository postOfficeStaffAssignmentRepository;
    private final VehicleRepository vehicleRepository;
    private final TripRepository tripRepository;
    private final TripOrderRepository tripOrderRepository;
    private final DistanceMatrixCaller distanceMatrixCaller;
    private final AuthUtils authUtils;

    @Value("${distance-matrix.batch-size:20}")
    private Integer distanceMatrixBatchSize;

    @Value("${distance-matrix.max-nodes:120}")
    private Integer distanceMatrixMaxNodes;

    @Override
    public PickupOptimizationResponse optimizePickupPlan(OptimizePickupPlanRequest request) {
        Long tenantId = getCurrentTenantIdOrThrow();
        PostOffice postOffice = postOfficeRepository.findByIdAndTenantId(request.getPostOfficeId(), tenantId)
                .orElseThrow(() -> new AppException(ErrorCode.POST_OFFICE_NOT_FOUND));

        Point location = postOffice.getLocation();
        if (location == null) {
            throw new AppException(ErrorCode.INVALID_REQUEST);
        }

        validateManagerScope(postOffice.getId(), tenantId);

        AlgorithmConfig config = buildConfig(request);
        double depotLatitude = location.getY();
        double depotLongitude = location.getX();

        List<CourierResource> couriers = loadActiveCouriers(
                postOffice.getId(),
                tenantId,
                request.getCourierIds(),
                config.planningDate()
        );
        if (couriers.isEmpty()) {
            throw new AppException(ErrorCode.INVALID_REQUEST);
        }

        List<Vehicle> activeVehicles = vehicleRepository.findByTenantIdAndPostOffice_IdAndStatusIn(
                tenantId,
                postOffice.getId(),
                List.of(VehicleStatus.ACTIVE)
        );

        List<RouteState> initialRoutes = initializeRoutes(couriers, activeVehicles, depotLatitude, depotLongitude);
        if (initialRoutes.isEmpty()) {
            throw new AppException(ErrorCode.INVALID_REQUEST);
        }

        List<OrderStatus> statuses = resolveCandidateStatuses(request.getCandidateStatuses());
        List<Order> candidateOrders = orderRepository.findPickupCandidateOrders(
                tenantId,
                statuses,
                postOffice.getCode(),
                config.planningStartTime(),
                config.planningEndTime(),
                PageRequest.of(0, config.orderLimit())
        );

        PreparedOrderData preparedOrderData = prepareOrders(candidateOrders);
        TravelMetricProvider travelMetricProvider = buildTravelMetricProvider(
            preparedOrderData.assignableOrders(),
            depotLatitude,
            depotLongitude,
            config
        );
        AlgorithmConfig runtimeConfig = config.withTravelMetricProvider(travelMetricProvider);

        SolutionState initialSolution = buildInitialSolution(initialRoutes, preparedOrderData, runtimeConfig);
        SolutionState optimizedSolution = runAlns(initialSolution, runtimeConfig);

        return toResponse(postOffice, runtimeConfig, optimizedSolution);
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public PickupAssignmentResponse autoAssignPickupPlan(AutoAssignPickupPlanRequest request) {
        Long tenantId = getCurrentTenantIdOrThrow();
        ShiftPlanningWindow shiftPlanningWindow = resolveShiftPlanningWindow(
                request.getShift(),
                request.getTripDate(),
                request.getPlanningStartTime(),
                request.getPlanningEndTime()
        );

        PostOffice postOffice = getPostOfficeAndValidateScope(request.getPostOfficeId(), tenantId);
        Point location = postOffice.getLocation();
        if (location == null) {
            throw new AppException(ErrorCode.INVALID_REQUEST);
        }

        AlgorithmConfig config = buildConfig(request, shiftPlanningWindow);
        double depotLatitude = location.getY();
        double depotLongitude = location.getX();

        List<CourierResource> couriers = loadActiveCouriers(
                postOffice.getId(),
                tenantId,
                request.getCourierIds(),
                shiftPlanningWindow.tripDate()
        );
        if (couriers.isEmpty()) {
            throw new AppException(ErrorCode.INVALID_REQUEST);
        }

        List<Vehicle> activeVehicles = vehicleRepository.findByTenantIdAndPostOffice_IdAndStatusIn(
                tenantId,
                postOffice.getId(),
                List.of(VehicleStatus.ACTIVE)
        );

        List<RouteState> routes = initializeRoutes(couriers, activeVehicles, depotLatitude, depotLongitude);
        if (routes.isEmpty()) {
            throw new AppException(ErrorCode.INVALID_REQUEST);
        }

        Map<Long, Trip> existingTripByCourier = loadReplannableTripsByCourier(
                tenantId,
                postOffice.getId(),
                shiftPlanningWindow.tripDate(),
                request.getShift(),
                routes
        );

        ExistingRouteData existingRouteData = loadExistingRouteData(existingTripByCourier, tenantId);
        applyExistingRouteData(routes, existingTripByCourier, existingRouteData);

        List<OrderStatus> statuses = resolveCandidateStatuses(request.getCandidateStatuses());
        List<Order> candidateOrders = orderRepository.findPickupCandidateOrders(
                tenantId,
                statuses,
                postOffice.getCode(),
                config.planningStartTime(),
                config.planningEndTime(),
                PageRequest.of(0, config.orderLimit())
        );

        PreparedOrderData preparedOrderData = prepareOrders(candidateOrders);
        PreparedOrderData filteredPreparedOrderData = excludeOrdersAlreadyAssigned(preparedOrderData, tenantId, null);

        List<PickupOrderNode> matrixNodes = new ArrayList<>();
        for (List<PickupOrderNode> routeNodes : existingRouteData.routeNodesByTripId().values()) {
            matrixNodes.addAll(routeNodes);
        }
        matrixNodes.addAll(filteredPreparedOrderData.assignableOrders());

        TravelMetricProvider travelMetricProvider = buildTravelMetricProvider(
                matrixNodes,
                depotLatitude,
                depotLongitude,
                config
        );
        AlgorithmConfig runtimeConfig = config.withTravelMetricProvider(travelMetricProvider);

        List<UnassignedOrderState> unassignedOrders = new ArrayList<>();
        for (UnassignedOrderState state : existingRouteData.invalidOrderStates()) {
            unassignedOrders.add(state.copy());
        }
        for (UnassignedOrderState state : filteredPreparedOrderData.initialUnassignedOrders()) {
            unassignedOrders.add(state.copy());
        }
        for (PickupOrderNode order : filteredPreparedOrderData.assignableOrders()) {
            unassignedOrders.add(new UnassignedOrderState(order, REASON_UNASSIGNED, true));
        }

        SolutionState solution = new SolutionState(routes, unassignedOrders);
        applyGreedyRepair(solution, runtimeConfig);
        markNoFeasibleUnassigned(solution);
        sanitizeSolution(solution);

        SolutionEvaluation solutionEvaluation = evaluateSolution(solution, runtimeConfig);
        if (solutionEvaluation.objectiveScore() >= HUGE_OBJECTIVE) {
            throw new AppException(ErrorCode.INVALID_REQUEST);
        }

        AssignmentPersistResult persistResult = persistAssignments(
                postOffice,
                request.getShift(),
                shiftPlanningWindow.tripDate(),
                solution,
                solutionEvaluation,
                existingTripByCourier,
                tenantId
        );

        Set<Long> candidateOrderIds = extractOrderIds(candidateOrders);
        return buildAssignmentResponse(
                postOffice,
                request.getShift(),
                shiftPlanningWindow.tripDate(),
                candidateOrderIds.size(),
                candidateOrderIds,
                persistResult,
                solution.unassignedOrders()
        );
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public PickupAssignmentResponse manualAssignPickupOrders(ManualAssignPickupOrdersRequest request) {
        Long tenantId = getCurrentTenantIdOrThrow();
        ShiftPlanningWindow shiftPlanningWindow = resolveShiftPlanningWindow(
                request.getShift(),
                request.getTripDate(),
                request.getPlanningStartTime(),
                request.getPlanningEndTime()
        );

        PostOffice postOffice = getPostOfficeAndValidateScope(request.getPostOfficeId(), tenantId);
        Point location = postOffice.getLocation();
        if (location == null) {
            throw new AppException(ErrorCode.INVALID_REQUEST);
        }

        PostOfficeStaff courier = postOfficeStaffRepository.findByIdAndTenantId(request.getCourierStaffId(), tenantId)
                .orElseThrow(() -> new AppException(ErrorCode.POST_OFFICE_STAFF_NOT_FOUND));
        validateCourierAssignableForManual(courier, postOffice.getId(), tenantId, shiftPlanningWindow.tripDate());

        AlgorithmConfig config = buildConfig(request, shiftPlanningWindow);

        List<Vehicle> activeVehicles = vehicleRepository.findByTenantIdAndPostOffice_IdAndStatusIn(
                tenantId,
                postOffice.getId(),
                List.of(VehicleStatus.ACTIVE)
        );

        CourierResource courierResource = new CourierResource(
                courier.getId(),
                courier.getCode(),
                courier.getFullName(),
                resolveMaxStops(courier)
        );

        List<RouteState> routes = initializeRoutes(
                List.of(courierResource),
                activeVehicles,
                location.getY(),
                location.getX()
        );
        if (routes.isEmpty()) {
            throw new AppException(ErrorCode.INVALID_REQUEST);
        }

        Map<Long, Trip> existingTripByCourier = loadReplannableTripsByCourier(
                tenantId,
                postOffice.getId(),
                shiftPlanningWindow.tripDate(),
                request.getShift(),
                routes
        );
        Trip existingTrip = existingTripByCourier.get(courier.getId());

        Set<Long> requestedOrderIds = normalizeDistinctOrderIds(request.getOrderIds());

        List<TripOrder> existingTripOrders;
        if (existingTrip == null || existingTrip.getId() == null) {
            existingTripOrders = List.of();
        } else {
            existingTripOrders = tripOrderRepository.findByTrip_IdOrderBySequenceNoAsc(existingTrip.getId());
        }

        Set<Long> existingTripOrderIds = existingTripOrders.stream()
                .map(TripOrder::getOrderId)
                .filter(Objects::nonNull)
                .collect(Collectors.toCollection(LinkedHashSet::new));

        LinkedHashSet<Long> allOrderIds = new LinkedHashSet<>(existingTripOrderIds);
        allOrderIds.addAll(requestedOrderIds);

        Map<Long, Order> orderById = loadOrdersByIdMapOrThrow(allOrderIds, tenantId);

        Long excludeTripId = existingTrip == null ? null : existingTrip.getId();
        for (Long orderId : requestedOrderIds) {
            Order order = orderById.get(orderId);
            if (order == null) {
                throw new AppException(ErrorCode.ORDER_NOT_FOUND);
            }

            boolean alreadyInCurrentTrip = existingTripOrderIds.contains(orderId);
            validateManualOrder(order, postOffice.getCode(), alreadyInCurrentTrip);

            boolean assignedInAnotherTrip = tripOrderRepository.existsByTenantIdAndOrderIdAndTripStatusIn(
                    tenantId,
                    orderId,
                    ACTIVE_ASSIGNMENT_TRIP_STATUSES,
                    excludeTripId
            );
            if (assignedInAnotherTrip) {
                throw new AppException(ErrorCode.ORDER_ALREADY_ASSIGNED_TO_PICKUP_TRIP);
            }
        }

        ExistingRouteData existingRouteData = loadExistingRouteData(existingTripByCourier, tenantId);
        applyExistingRouteData(routes, existingTripByCourier, existingRouteData);

        List<PickupOrderNode> manualCandidateNodes = new ArrayList<>();
        for (Long orderId : requestedOrderIds) {
            if (existingTripOrderIds.contains(orderId)) {
                continue;
            }
            manualCandidateNodes.add(toOrderNodeOrThrow(orderById.get(orderId), ErrorCode.ORDER_NOT_ASSIGNABLE));
        }

        manualCandidateNodes.sort(Comparator
                .comparing(PickupOrderNode::pickupTimeEnd, Comparator.nullsLast(Comparator.naturalOrder()))
                .thenComparing(PickupOrderNode::orderId, Comparator.nullsLast(Comparator.naturalOrder()))
        );

        List<PickupOrderNode> matrixNodes = new ArrayList<>();
        for (List<PickupOrderNode> routeNodes : existingRouteData.routeNodesByTripId().values()) {
            matrixNodes.addAll(routeNodes);
        }
        matrixNodes.addAll(manualCandidateNodes);

        TravelMetricProvider travelMetricProvider = buildTravelMetricProvider(
                matrixNodes,
                location.getY(),
                location.getX(),
                config
        );
        AlgorithmConfig runtimeConfig = config.withTravelMetricProvider(travelMetricProvider);

        List<UnassignedOrderState> unassignedOrders = new ArrayList<>();
        for (PickupOrderNode manualCandidateNode : manualCandidateNodes) {
            unassignedOrders.add(new UnassignedOrderState(manualCandidateNode, REASON_UNASSIGNED, true));
        }

        SolutionState solution = new SolutionState(routes, unassignedOrders);
        applyGreedyRepair(solution, runtimeConfig);
        markNoFeasibleUnassigned(solution);
        sanitizeSolution(solution);

        SolutionEvaluation solutionEvaluation = evaluateSolution(solution, runtimeConfig);
        if (solutionEvaluation.objectiveScore() >= HUGE_OBJECTIVE) {
            throw new AppException(ErrorCode.ORDER_NOT_ASSIGNABLE);
        }

        AssignmentPersistResult persistResult = persistAssignments(
                postOffice,
                request.getShift(),
                shiftPlanningWindow.tripDate(),
                solution,
                solutionEvaluation,
                existingTripByCourier,
                tenantId
        );

        return buildAssignmentResponse(
                postOffice,
                request.getShift(),
                shiftPlanningWindow.tripDate(),
                requestedOrderIds.size(),
                requestedOrderIds,
                persistResult,
                solution.unassignedOrders()
        );
    }

    private AssignmentPersistResult persistAssignments(
            PostOffice postOffice,
            PickupShift shift,
            LocalDate tripDate,
            SolutionState solution,
            SolutionEvaluation solutionEvaluation,
            Map<Long, Trip> existingTripByCourier,
            Long tenantId
    ) {
        Set<Long> previousAssignedOrderIds = loadAssignedOrderIdsFromTrips(existingTripByCourier.values());
        Set<Long> assignedOrderIds = extractAssignedOrderIds(solution.routes());

        List<PickupAssignmentResponse.AssignedTripResponse> tripResponses = new ArrayList<>();
        for (int routeIndex = 0; routeIndex < solution.routes().size(); routeIndex++) {
            RouteState route = solution.routes().get(routeIndex);
            RouteEvaluation routeEvaluation = solutionEvaluation.routeEvaluations().get(routeIndex);
            Trip trip = existingTripByCourier.get(route.courierStaffId());

            if (route.stops().isEmpty()) {
                if (trip != null && trip.getId() != null) {
                    tripOrderRepository.deleteByTrip_Id(trip.getId());
                    trip.setVehicleId(route.vehicleId());
                    trip.setStatus(TripStatus.CANCELLED);
                    trip.setPlannedStartTime(null);
                    trip.setPlannedEndTime(null);
                    trip.setTotalOrders(0);
                    trip.setTotalDistanceKm(0.0);
                    trip.setTotalTravelMinutes(0L);
                    tripRepository.save(trip);
                }
                continue;
            }

            if (trip == null) {
                trip = new Trip();
                trip.setTripCode(generateTripCode(tripDate, shift, route.courierCode()));
            } else if (trip.getTripCode() == null || trip.getTripCode().isBlank()) {
                trip.setTripCode(generateTripCode(tripDate, shift, route.courierCode()));
            }

            trip.setTenantId(tenantId);
            trip.setPostOfficeId(postOffice.getId());
            trip.setCourierStaffId(route.courierStaffId());
            trip.setVehicleId(route.vehicleId());
            trip.setShift(shift);
            trip.setTripDate(tripDate);
            trip.setPlannedStartTime(routeEvaluation.routeStartTime());
            trip.setPlannedEndTime(routeEvaluation.routeEndTime());
            trip.setTotalOrders(route.stops().size());
            trip.setTotalDistanceKm(round3(routeEvaluation.totalDistanceKm()));
            trip.setTotalTravelMinutes(routeEvaluation.totalTravelMinutes());
            trip.setStatus(TripStatus.PLANNED);

            Trip savedTrip = tripRepository.save(trip);
            existingTripByCourier.put(route.courierStaffId(), savedTrip);

            tripOrderRepository.deleteByTrip_Id(savedTrip.getId());
            saveTripOrders(savedTrip, routeEvaluation.stopDetails(), tenantId);

            tripResponses.add(toAssignedTripResponse(savedTrip, route, routeEvaluation));
        }

        syncOrderStatuses(previousAssignedOrderIds, assignedOrderIds, tenantId);
        return new AssignmentPersistResult(tripResponses, assignedOrderIds);
    }

    private PickupAssignmentResponse buildAssignmentResponse(
            PostOffice postOffice,
            PickupShift shift,
            LocalDate tripDate,
            int totalRequestedOrders,
            Set<Long> scopedOrderIds,
            AssignmentPersistResult persistResult,
            List<UnassignedOrderState> unassignedOrderStates
    ) {
        List<PickupAssignmentResponse.UnassignedAssignmentOrderResponse> unassignedResponses =
                buildAssignmentUnassignedResponses(unassignedOrderStates, scopedOrderIds);

        int assignedOrders = countMatchedOrderIds(persistResult.assignedOrderIds(), scopedOrderIds);
        return new PickupAssignmentResponse(
                postOffice.getId(),
                postOffice.getCode(),
                postOffice.getName(),
                shift,
                tripDate,
                totalRequestedOrders,
                assignedOrders,
                unassignedResponses.size(),
                persistResult.tripResponses().size(),
                persistResult.tripResponses(),
                unassignedResponses
        );
    }

    private List<PickupAssignmentResponse.UnassignedAssignmentOrderResponse> buildAssignmentUnassignedResponses(
            List<UnassignedOrderState> unassignedOrderStates,
            Set<Long> scopedOrderIds
    ) {
        List<PickupAssignmentResponse.UnassignedAssignmentOrderResponse> responses = new ArrayList<>();
        for (UnassignedOrderState state : unassignedOrderStates) {
            PickupOrderNode order = state.order();
            if (order == null) {
                continue;
            }

            Long orderId = order.orderId();
            if (orderId != null && scopedOrderIds != null && !scopedOrderIds.contains(orderId)) {
                continue;
            }

            responses.add(new PickupAssignmentResponse.UnassignedAssignmentOrderResponse(
                    order.orderId(),
                    order.orderCode(),
                    order.customerOrderCode(),
                    state.reason()
            ));
        }

        responses.sort(Comparator
                .comparing(PickupAssignmentResponse.UnassignedAssignmentOrderResponse::orderId,
                        Comparator.nullsLast(Comparator.naturalOrder()))
                .thenComparing(PickupAssignmentResponse.UnassignedAssignmentOrderResponse::customerOrderCode,
                        Comparator.nullsLast(String::compareToIgnoreCase))
        );
        return responses;
    }

    private PickupAssignmentResponse.AssignedTripResponse toAssignedTripResponse(
            Trip trip,
            RouteState route,
            RouteEvaluation routeEvaluation
    ) {
        List<PickupAssignmentResponse.AssignedStopResponse> stopResponses = new ArrayList<>();
        for (StopEvaluationData stop : routeEvaluation.stopDetails()) {
            PickupOrderNode order = stop.order();
            stopResponses.add(new PickupAssignmentResponse.AssignedStopResponse(
                    stop.sequence(),
                    order.orderId(),
                    order.orderCode(),
                    order.customerOrderCode(),
                    stop.arrivalTime(),
                    stop.startServiceTime(),
                    stop.departureTime(),
                    round3(stop.distanceFromPreviousKm()),
                    stop.travelMinutes(),
                    stop.latenessMinutes()
            ));
        }

        return new PickupAssignmentResponse.AssignedTripResponse(
                trip.getId(),
                trip.getTripCode(),
                route.courierStaffId(),
                route.courierCode(),
                route.courierName(),
                route.vehicleId(),
                route.vehicleLicensePlate(),
                route.stops().size(),
                round3(routeEvaluation.totalDistanceKm()),
                routeEvaluation.totalTravelMinutes(),
                routeEvaluation.totalServiceMinutes(),
                routeEvaluation.totalLatenessMinutes(),
                routeEvaluation.routeStartTime(),
                routeEvaluation.routeEndTime(),
                stopResponses
        );
    }

    private void saveTripOrders(Trip trip, List<StopEvaluationData> stopDetails, Long tenantId) {
        if (trip == null || trip.getId() == null || stopDetails == null || stopDetails.isEmpty()) {
            return;
        }

        List<TripOrder> tripOrders = new ArrayList<>();
        for (StopEvaluationData stop : stopDetails) {
            TripOrder tripOrder = new TripOrder();
            tripOrder.setTenantId(tenantId);
            tripOrder.setTrip(trip);
            tripOrder.setOrderId(stop.order().orderId());
            tripOrder.setSequenceNo(stop.sequence());
            tripOrder.setDistanceFromPreviousKm(round3(stop.distanceFromPreviousKm()));
            tripOrder.setTravelMinutes(stop.travelMinutes());
            tripOrder.setPlannedArrivalTime(stop.arrivalTime());
            tripOrder.setPlannedStartServiceTime(stop.startServiceTime());
            tripOrder.setPlannedDepartureTime(stop.departureTime());
            tripOrder.setLatenessMinutes(stop.latenessMinutes());
            tripOrders.add(tripOrder);
        }

        tripOrderRepository.saveAll(tripOrders);
    }

    private void syncOrderStatuses(Set<Long> previousAssignedOrderIds, Set<Long> assignedOrderIds, Long tenantId) {
        if (!assignedOrderIds.isEmpty()) {
            List<Order> assignedOrders = orderRepository.findByIdInAndTenantId(assignedOrderIds, tenantId);
            for (Order assignedOrder : assignedOrders) {
                assignedOrder.setStatus(OrderStatus.ASSIGNED_TO_PICKUP);
            }
            orderRepository.saveAll(assignedOrders);
        }

        Set<Long> releasedOrderIds = new HashSet<>(previousAssignedOrderIds);
        releasedOrderIds.removeAll(assignedOrderIds);
        if (releasedOrderIds.isEmpty()) {
            return;
        }

        List<Order> releasedOrders = orderRepository.findByIdInAndTenantId(releasedOrderIds, tenantId);
        for (Order releasedOrder : releasedOrders) {
            if (releasedOrder.getId() == null) {
                continue;
            }

            boolean stillAssignedToActiveTrip = tripOrderRepository.existsByTenantIdAndOrderIdAndTripStatusIn(
                    tenantId,
                    releasedOrder.getId(),
                    ACTIVE_ASSIGNMENT_TRIP_STATUSES,
                    null
            );
            if (!stillAssignedToActiveTrip && OrderStatus.ASSIGNED_TO_PICKUP.equals(releasedOrder.getStatus())) {
                releasedOrder.setStatus(OrderStatus.CREATED);
            }
        }
        orderRepository.saveAll(releasedOrders);
    }

    private Map<Long, Trip> loadReplannableTripsByCourier(
            Long tenantId,
            Long postOfficeId,
            LocalDate tripDate,
            PickupShift shift,
            List<RouteState> routes
    ) {
        Set<Long> courierIds = routes.stream()
                .map(RouteState::courierStaffId)
                .filter(Objects::nonNull)
                .collect(Collectors.toSet());

        if (courierIds.isEmpty()) {
            return new HashMap<>();
        }

        List<Trip> replannableTrips = tripRepository.findByTenantIdAndPostOfficeIdAndTripDateAndShiftAndStatusIn(
                tenantId,
                postOfficeId,
                tripDate,
                shift,
                REPLANNABLE_TRIP_STATUSES
        );

        Map<Long, Trip> tripByCourier = new HashMap<>();
        for (Trip trip : replannableTrips) {
            Long courierId = trip.getCourierStaffId();
            if (courierId == null || !courierIds.contains(courierId)) {
                continue;
            }

            tripByCourier.compute(courierId, (key, currentValue) -> {
                if (currentValue == null) {
                    return trip;
                }
                if (trip.getId() == null) {
                    return currentValue;
                }
                if (currentValue.getId() == null || trip.getId() > currentValue.getId()) {
                    return trip;
                }
                return currentValue;
            });
        }

        return tripByCourier;
    }

    private ExistingRouteData loadExistingRouteData(Map<Long, Trip> existingTripByCourier, Long tenantId) {
        Map<Long, List<PickupOrderNode>> routeNodesByTripId = new HashMap<>();
        List<UnassignedOrderState> invalidOrderStates = new ArrayList<>();

        if (existingTripByCourier == null || existingTripByCourier.isEmpty()) {
            return new ExistingRouteData(routeNodesByTripId, invalidOrderStates);
        }

        Map<Long, List<TripOrder>> tripOrdersByTripId = new HashMap<>();
        LinkedHashSet<Long> existingOrderIds = new LinkedHashSet<>();
        for (Trip trip : existingTripByCourier.values()) {
            if (trip == null || trip.getId() == null) {
                continue;
            }

            List<TripOrder> tripOrders = tripOrderRepository.findByTrip_IdOrderBySequenceNoAsc(trip.getId());
            tripOrdersByTripId.put(trip.getId(), tripOrders);
            for (TripOrder tripOrder : tripOrders) {
                if (tripOrder.getOrderId() != null) {
                    existingOrderIds.add(tripOrder.getOrderId());
                }
            }
        }

        Map<Long, Order> orderById = new HashMap<>();
        if (!existingOrderIds.isEmpty()) {
            List<Order> existingOrders = orderRepository.findByIdInAndTenantId(existingOrderIds, tenantId);
            for (Order existingOrder : existingOrders) {
                orderById.put(existingOrder.getId(), existingOrder);
            }
        }

        for (Map.Entry<Long, List<TripOrder>> tripOrderEntry : tripOrdersByTripId.entrySet()) {
            Long tripId = tripOrderEntry.getKey();
            List<PickupOrderNode> routeNodes = new ArrayList<>();

            for (TripOrder tripOrder : tripOrderEntry.getValue()) {
                Long orderId = tripOrder.getOrderId();
                if (orderId == null) {
                    continue;
                }

                Order order = orderById.get(orderId);
                if (order == null) {
                    continue;
                }

                PickupOrderNode orderNode = toOrderNodeIfValid(order);
                if (orderNode == null) {
                    invalidOrderStates.add(new UnassignedOrderState(
                            toOrderNodeWithoutLocation(order),
                            REASON_INVALID_SENDER_LOCATION,
                            false
                    ));
                    continue;
                }

                routeNodes.add(orderNode);
            }

            routeNodesByTripId.put(tripId, routeNodes);
        }

        return new ExistingRouteData(routeNodesByTripId, invalidOrderStates);
    }

    private void applyExistingRouteData(
            List<RouteState> routes,
            Map<Long, Trip> existingTripByCourier,
            ExistingRouteData existingRouteData
    ) {
        if (routes == null || routes.isEmpty() || existingTripByCourier == null || existingTripByCourier.isEmpty()) {
            return;
        }

        for (RouteState route : routes) {
            Trip trip = existingTripByCourier.get(route.courierStaffId());
            if (trip == null || trip.getId() == null) {
                continue;
            }

            List<PickupOrderNode> routeNodes = existingRouteData.routeNodesByTripId().get(trip.getId());
            if (routeNodes != null && !routeNodes.isEmpty()) {
                route.stops().addAll(routeNodes);
            }
        }
    }

    private PreparedOrderData excludeOrdersAlreadyAssigned(
            PreparedOrderData preparedOrderData,
            Long tenantId,
            Long excludeTripId
    ) {
        List<PickupOrderNode> assignableOrders = new ArrayList<>();
        List<UnassignedOrderState> unassignedOrders = new ArrayList<>();

        for (UnassignedOrderState initialUnassignedOrder : preparedOrderData.initialUnassignedOrders()) {
            unassignedOrders.add(initialUnassignedOrder.copy());
        }

        for (PickupOrderNode assignableOrder : preparedOrderData.assignableOrders()) {
            if (assignableOrder.orderId() == null) {
                unassignedOrders.add(new UnassignedOrderState(assignableOrder, REASON_ORDER_NOT_ASSIGNABLE, false));
                continue;
            }

            boolean assignedInAnotherTrip = tripOrderRepository.existsByTenantIdAndOrderIdAndTripStatusIn(
                    tenantId,
                    assignableOrder.orderId(),
                    ACTIVE_ASSIGNMENT_TRIP_STATUSES,
                    excludeTripId
            );

            if (assignedInAnotherTrip) {
                unassignedOrders.add(new UnassignedOrderState(assignableOrder, REASON_ALREADY_ASSIGNED_TO_ACTIVE_TRIP, false));
                continue;
            }

            assignableOrders.add(assignableOrder);
        }

        return new PreparedOrderData(assignableOrders, unassignedOrders);
    }

    private Map<Long, Order> loadOrdersByIdMapOrThrow(Collection<Long> orderIds, Long tenantId) {
        Map<Long, Order> orderById = new HashMap<>();
        List<Order> orders = orderRepository.findByIdInAndTenantId(orderIds, tenantId);
        for (Order order : orders) {
            orderById.put(order.getId(), order);
        }

        for (Long orderId : orderIds) {
            if (orderId != null && !orderById.containsKey(orderId)) {
                throw new AppException(ErrorCode.ORDER_NOT_FOUND);
            }
        }

        return orderById;
    }

    private void validateManualOrder(Order order, String postOfficeCode, boolean alreadyInCurrentTrip) {
        OrderStatus status = order.getStatus();
        if (alreadyInCurrentTrip) {
            boolean validExistingStatus = OrderStatus.ASSIGNED_TO_PICKUP.equals(status)
                    || DEFAULT_CANDIDATE_STATUSES.contains(status);
            if (!validExistingStatus) {
                throw new AppException(ErrorCode.ORDER_NOT_ASSIGNABLE);
            }
        } else if (!DEFAULT_CANDIDATE_STATUSES.contains(status)) {
            throw new AppException(ErrorCode.ORDER_NOT_ASSIGNABLE);
        }

        String originPostOfficeCode = order.getOriginPostOfficeCode();
        if (postOfficeCode != null
                && originPostOfficeCode != null
                && !postOfficeCode.equalsIgnoreCase(originPostOfficeCode)) {
            throw new AppException(ErrorCode.ORDER_NOT_ASSIGNABLE);
        }

        if (toOrderNodeIfValid(order) == null) {
            throw new AppException(ErrorCode.ORDER_NOT_ASSIGNABLE);
        }
    }

    private void validateCourierAssignableForManual(
            PostOfficeStaff courier,
            Long postOfficeId,
            Long tenantId,
            LocalDate planningDate
    ) {
        if (!PostOfficeStaffRole.COURIER.equals(courier.getRole())
                || !PostOfficeStaffStatus.ACTIVE.equals(courier.getStatus())) {
            throw new AppException(ErrorCode.COURIER_NOT_ASSIGNED_TO_POST_OFFICE);
        }

        boolean assigned = postOfficeStaffAssignmentRepository.existsActiveAssignmentByStaffIdAndPostOfficeIdAndTenantId(
                courier.getId(),
                postOfficeId,
                tenantId,
                planningDate
        );
        if (!assigned) {
            throw new AppException(ErrorCode.COURIER_NOT_ASSIGNED_TO_POST_OFFICE);
        }
    }

    private Set<Long> normalizeDistinctOrderIds(List<Long> orderIds) {
        LinkedHashSet<Long> normalizedOrderIds = new LinkedHashSet<>();
        if (orderIds != null) {
            for (Long orderId : orderIds) {
                if (orderId != null && orderId > 0) {
                    normalizedOrderIds.add(orderId);
                }
            }
        }

        if (normalizedOrderIds.isEmpty()) {
            throw new AppException(ErrorCode.INVALID_REQUEST);
        }

        return normalizedOrderIds;
    }

    private ShiftPlanningWindow resolveShiftPlanningWindow(
            PickupShift shift,
            LocalDate tripDate,
            LocalDateTime planningStartTime,
            LocalDateTime planningEndTime
    ) {
        LocalDate effectiveTripDate = tripDate == null ? LocalDate.now() : tripDate;
        LocalDateTime defaultPlanningStartTime = LocalDateTime.of(effectiveTripDate, resolveShiftStartTime(shift));
        LocalDateTime defaultPlanningEndTime = LocalDateTime.of(effectiveTripDate, resolveShiftEndTime(shift));

        LocalDateTime effectivePlanningStartTime = planningStartTime == null
                ? defaultPlanningStartTime
                : planningStartTime;
        LocalDateTime effectivePlanningEndTime = planningEndTime == null
                ? defaultPlanningEndTime
                : planningEndTime;

        if (effectivePlanningEndTime.isBefore(effectivePlanningStartTime)) {
            throw new AppException(ErrorCode.INVALID_REQUEST);
        }

        if (!effectivePlanningStartTime.toLocalDate().equals(effectiveTripDate)
                || !effectivePlanningEndTime.toLocalDate().equals(effectiveTripDate)) {
            throw new AppException(ErrorCode.INVALID_REQUEST);
        }

        return new ShiftPlanningWindow(
                effectiveTripDate,
                effectivePlanningStartTime,
                effectivePlanningEndTime
        );
    }

    private LocalTime resolveShiftStartTime(PickupShift shift) {
        return switch (shift) {
            case MORNING -> SHIFT_MORNING_START;
            case AFTERNOON -> SHIFT_AFTERNOON_START;
            case EVENING -> SHIFT_EVENING_START;
        };
    }

    private LocalTime resolveShiftEndTime(PickupShift shift) {
        return switch (shift) {
            case MORNING -> SHIFT_MORNING_END;
            case AFTERNOON -> SHIFT_AFTERNOON_END;
            case EVENING -> SHIFT_EVENING_END;
        };
    }

    private AlgorithmConfig buildConfig(AutoAssignPickupPlanRequest request, ShiftPlanningWindow shiftPlanningWindow) {
        int orderLimit = resolvePositiveInt(request.getOrderLimit(), DEFAULT_ORDER_LIMIT);
        double averageSpeedKmph = resolvePositiveDouble(request.getAverageSpeedKmph(), DEFAULT_AVERAGE_SPEED_KMPH);
        int serviceMinutesPerStop = resolvePositiveInt(request.getServiceMinutesPerStop(), DEFAULT_SERVICE_MINUTES_PER_STOP);
        int maxIterations = resolvePositiveInt(request.getMaxIterations(), DEFAULT_MAX_ITERATIONS);
        long maxRuntimeMillis = resolvePositiveLong(request.getMaxRuntimeMillis(), DEFAULT_MAX_RUNTIME_MILLIS);

        double destroyRate = clamp(resolvePositiveDouble(request.getDestroyRate(), DEFAULT_DESTROY_RATE), 0.01, 0.90);
        double initialTemperature = resolvePositiveDouble(request.getInitialTemperature(), DEFAULT_INITIAL_TEMPERATURE);
        double coolingRate = clamp(resolvePositiveDouble(request.getCoolingRate(), DEFAULT_COOLING_RATE), 0.80, 0.9999);

        boolean allowLateness = request.getAllowLateness() == null ? DEFAULT_ALLOW_LATENESS : request.getAllowLateness();
        boolean enforcePlanningEnd = request.getEnforcePlanningEnd() == null
                ? DEFAULT_ENFORCE_PLANNING_END
                : request.getEnforcePlanningEnd();
        boolean enforceCapacity = request.getEnforceCapacity() == null
                ? DEFAULT_ENFORCE_CAPACITY
                : request.getEnforceCapacity();

        double distanceWeight = resolveNonNegativeDouble(request.getDistanceWeight(), DEFAULT_DISTANCE_WEIGHT);
        double latenessWeight = resolveNonNegativeDouble(request.getLatenessWeight(), DEFAULT_LATENESS_WEIGHT);
        double unassignedPenalty = resolveNonNegativeDouble(request.getUnassignedPenalty(), DEFAULT_UNASSIGNED_PENALTY);
        double usedRoutePenalty = resolveNonNegativeDouble(request.getUsedRoutePenalty(), DEFAULT_USED_ROUTE_PENALTY);
        RoutingVehicle routingVehicle = RoutingVehicle.fromValue(request.getVehicle());
        int matrixBatchSize = resolvePositiveInt(distanceMatrixBatchSize, DEFAULT_DISTANCE_MATRIX_BATCH_SIZE);
        int matrixMaxNodes = resolvePositiveInt(distanceMatrixMaxNodes, DEFAULT_DISTANCE_MATRIX_MAX_NODES);

        return new AlgorithmConfig(
                shiftPlanningWindow.planningStartTime(),
                shiftPlanningWindow.planningEndTime(),
                shiftPlanningWindow.tripDate(),
                orderLimit,
                averageSpeedKmph,
                serviceMinutesPerStop,
                maxIterations,
                maxRuntimeMillis,
                destroyRate,
                initialTemperature,
                coolingRate,
                allowLateness,
                enforcePlanningEnd,
                enforceCapacity,
                distanceWeight,
                latenessWeight,
                unassignedPenalty,
                usedRoutePenalty,
                routingVehicle,
                matrixBatchSize,
                matrixMaxNodes,
                null
        );
    }

    private AlgorithmConfig buildConfig(ManualAssignPickupOrdersRequest request, ShiftPlanningWindow shiftPlanningWindow) {
        double averageSpeedKmph = resolvePositiveDouble(request.getAverageSpeedKmph(), DEFAULT_AVERAGE_SPEED_KMPH);
        int serviceMinutesPerStop = resolvePositiveInt(request.getServiceMinutesPerStop(), DEFAULT_SERVICE_MINUTES_PER_STOP);
        boolean allowLateness = request.getAllowLateness() == null ? DEFAULT_ALLOW_LATENESS : request.getAllowLateness();
        boolean enforcePlanningEnd = request.getEnforcePlanningEnd() == null
                ? DEFAULT_ENFORCE_PLANNING_END
                : request.getEnforcePlanningEnd();
        boolean enforceCapacity = request.getEnforceCapacity() == null
                ? DEFAULT_ENFORCE_CAPACITY
                : request.getEnforceCapacity();
        int matrixBatchSize = resolvePositiveInt(distanceMatrixBatchSize, DEFAULT_DISTANCE_MATRIX_BATCH_SIZE);
        int matrixMaxNodes = resolvePositiveInt(distanceMatrixMaxNodes, DEFAULT_DISTANCE_MATRIX_MAX_NODES);

        return new AlgorithmConfig(
                shiftPlanningWindow.planningStartTime(),
                shiftPlanningWindow.planningEndTime(),
                shiftPlanningWindow.tripDate(),
                DEFAULT_ORDER_LIMIT,
                averageSpeedKmph,
                serviceMinutesPerStop,
                DEFAULT_MAX_ITERATIONS,
                DEFAULT_MAX_RUNTIME_MILLIS,
                DEFAULT_DESTROY_RATE,
                DEFAULT_INITIAL_TEMPERATURE,
                DEFAULT_COOLING_RATE,
                allowLateness,
                enforcePlanningEnd,
                enforceCapacity,
                DEFAULT_DISTANCE_WEIGHT,
                DEFAULT_LATENESS_WEIGHT,
                DEFAULT_UNASSIGNED_PENALTY,
                DEFAULT_USED_ROUTE_PENALTY,
                RoutingVehicle.fromValue(request.getVehicle()),
                matrixBatchSize,
                matrixMaxNodes,
                null
        );
    }

    private PostOffice getPostOfficeAndValidateScope(Long postOfficeId, Long tenantId) {
        PostOffice postOffice = postOfficeRepository.findByIdAndTenantId(postOfficeId, tenantId)
                .orElseThrow(() -> new AppException(ErrorCode.POST_OFFICE_NOT_FOUND));
        validateManagerScope(postOffice.getId(), tenantId);
        return postOffice;
    }

    private PickupOrderNode toOrderNodeIfValid(Order order) {
        if (order == null) {
            return null;
        }

        Point senderLocation = order.getSenderLocation();
        if (senderLocation == null) {
            return null;
        }

        double latitude = senderLocation.getY();
        double longitude = senderLocation.getX();
        if (!isValidCoordinate(latitude, longitude)) {
            return null;
        }

        return new PickupOrderNode(
                order.getId(),
                order.getOrderCode(),
                order.getCustomerOrderCode(),
                order.getSenderName(),
                order.getSenderPhone(),
                latitude,
                longitude,
                safePositive(order.getTotalWeight()),
                safePositive(order.getTotalVolume()),
                order.getPickupTimeStart(),
                order.getPickupTimeEnd()
        );
    }

    private PickupOrderNode toOrderNodeOrThrow(Order order, ErrorCode errorCode) {
        PickupOrderNode orderNode = toOrderNodeIfValid(order);
        if (orderNode == null) {
            throw new AppException(errorCode);
        }
        return orderNode;
    }

    private Set<Long> extractAssignedOrderIds(List<RouteState> routes) {
        Set<Long> assignedOrderIds = new HashSet<>();
        for (RouteState route : routes) {
            for (PickupOrderNode stop : route.stops()) {
                if (stop.orderId() != null) {
                    assignedOrderIds.add(stop.orderId());
                }
            }
        }
        return assignedOrderIds;
    }

    private Set<Long> loadAssignedOrderIdsFromTrips(Collection<Trip> trips) {
        Set<Long> assignedOrderIds = new HashSet<>();
        for (Trip trip : trips) {
            if (trip == null || trip.getId() == null) {
                continue;
            }

            List<TripOrder> tripOrders = tripOrderRepository.findByTrip_IdOrderBySequenceNoAsc(trip.getId());
            for (TripOrder tripOrder : tripOrders) {
                if (tripOrder.getOrderId() != null) {
                    assignedOrderIds.add(tripOrder.getOrderId());
                }
            }
        }
        return assignedOrderIds;
    }

    private int countMatchedOrderIds(Set<Long> assignedOrderIds, Set<Long> scopedOrderIds) {
        if (scopedOrderIds == null) {
            return assignedOrderIds.size();
        }

        int count = 0;
        for (Long assignedOrderId : assignedOrderIds) {
            if (scopedOrderIds.contains(assignedOrderId)) {
                count++;
            }
        }
        return count;
    }

    private Set<Long> extractOrderIds(List<Order> orders) {
        Set<Long> orderIds = new HashSet<>();
        for (Order order : orders) {
            if (order.getId() != null) {
                orderIds.add(order.getId());
            }
        }
        return orderIds;
    }

    private String generateTripCode(LocalDate tripDate, PickupShift shift, String courierCode) {
        String datePart = tripDate.format(java.time.format.DateTimeFormatter.BASIC_ISO_DATE);
        String shiftPart = shift.name();
        String courierPart = courierCode == null
                ? "COURIER"
                : courierCode.replaceAll("[^A-Za-z0-9]", "").toUpperCase(Locale.ROOT);
        if (courierPart.isBlank()) {
            courierPart = "COURIER";
        }

        String randomPart = UUID.randomUUID().toString().replace("-", "").toUpperCase(Locale.ROOT);
        if (randomPart.length() > DEFAULT_TRIP_CODE_RANDOM_LENGTH) {
            randomPart = randomPart.substring(0, DEFAULT_TRIP_CODE_RANDOM_LENGTH);
        }

        return "TRP-" + datePart + "-" + shiftPart + "-" + courierPart + "-" + randomPart;
    }

    private SolutionState buildInitialSolution(
            List<RouteState> initialRoutes,
            PreparedOrderData preparedOrderData,
            AlgorithmConfig config
    ) {
        List<RouteState> routes = new ArrayList<>();
        for (RouteState route : initialRoutes) {
            routes.add(route.copy());
        }

        List<UnassignedOrderState> unassignedOrders = new ArrayList<>();
        for (UnassignedOrderState state : preparedOrderData.initialUnassignedOrders()) {
            unassignedOrders.add(state.copy());
        }

        for (PickupOrderNode order : preparedOrderData.assignableOrders()) {
            unassignedOrders.add(new UnassignedOrderState(order, REASON_UNASSIGNED, true));
        }

        SolutionState solution = new SolutionState(routes, unassignedOrders);
        applyGreedyRepair(solution, config);
        markNoFeasibleUnassigned(solution);
        return solution;
    }

    private SolutionState runAlns(SolutionState initialSolution, AlgorithmConfig config) {
        Random random = new Random();
        SolutionState currentSolution = deepCopySolution(initialSolution);
        SolutionState bestSolution = deepCopySolution(initialSolution);

        SolutionEvaluation currentEvaluation = evaluateSolution(currentSolution, config);
        SolutionEvaluation bestEvaluation = currentEvaluation;

        EnumMap<PickupDestroyOperator, Double> destroyWeights = initializeDestroyWeights();
        EnumMap<PickupDestroyOperator, Double> destroyScores = initializeDestroyWeights();
        EnumMap<PickupDestroyOperator, Integer> destroyUsages = initializeDestroyUsages();

        EnumMap<PickupRepairOperator, Double> repairWeights = initializeRepairWeights();
        EnumMap<PickupRepairOperator, Double> repairScores = initializeRepairWeights();
        EnumMap<PickupRepairOperator, Integer> repairUsages = initializeRepairUsages();

        long deadlineNanos = System.nanoTime() + config.maxRuntimeMillis() * 1_000_000L;
        double temperature = config.initialTemperature();

        for (int iteration = 1; iteration <= config.maxIterations(); iteration++) {
            if (System.nanoTime() > deadlineNanos) {
                break;
            }

            PickupDestroyOperator destroyOperator = selectDestroyOperator(destroyWeights, random);
            PickupRepairOperator repairOperator = selectRepairOperator(repairWeights, random);

            destroyUsages.put(destroyOperator, destroyUsages.get(destroyOperator) + 1);
            repairUsages.put(repairOperator, repairUsages.get(repairOperator) + 1);

            SolutionState candidateSolution = deepCopySolution(currentSolution);
            applyDestroy(candidateSolution, destroyOperator, random, config);
            applyRepair(candidateSolution, repairOperator, config);
            markNoFeasibleUnassigned(candidateSolution);

            SolutionEvaluation candidateEvaluation = evaluateSolution(candidateSolution, config);
            double previousCurrentObjective = currentEvaluation.objectiveScore();
            boolean accepted = shouldAccept(
                    candidateEvaluation.objectiveScore(),
                    previousCurrentObjective,
                    temperature,
                    random
            );

            if (accepted) {
                currentSolution = candidateSolution;
                currentEvaluation = candidateEvaluation;
            }

            double reward = 0.0;
            if (candidateEvaluation.objectiveScore() + EPSILON < bestEvaluation.objectiveScore()) {
                bestSolution = candidateSolution;
                bestEvaluation = candidateEvaluation;
                reward = 8.0;
            } else if (candidateEvaluation.objectiveScore() + EPSILON < previousCurrentObjective) {
                reward = 4.0;
            } else if (accepted) {
                reward = 1.0;
            }

            destroyScores.put(destroyOperator, destroyScores.get(destroyOperator) + reward);
            repairScores.put(repairOperator, repairScores.get(repairOperator) + reward);

            if (iteration % OPERATOR_UPDATE_SEGMENT == 0) {
                updateDestroyWeights(destroyWeights, destroyScores, destroyUsages);
                updateRepairWeights(repairWeights, repairScores, repairUsages);
            }

            temperature = Math.max(0.0001, temperature * config.coolingRate());
        }

        return bestSolution;
    }

    private void applyDestroy(
            SolutionState solution,
            PickupDestroyOperator destroyOperator,
            Random random,
            AlgorithmConfig config
    ) {
        int totalAssignedOrders = countAssignedOrders(solution.routes());
        if (totalAssignedOrders <= 0) {
            return;
        }

        int removeCount = Math.max(1, (int) Math.round(totalAssignedOrders * config.destroyRate()));
        removeCount = Math.min(removeCount, totalAssignedOrders);

        if (destroyOperator == PickupDestroyOperator.WORST) {
            applyWorstDestroy(solution, removeCount, random, config);
            return;
        }

        applyRandomDestroy(solution, removeCount, random);
    }

    private void applyRandomDestroy(SolutionState solution, int removeCount, Random random) {
        List<RouteOrderRef> orderRefs = collectAssignedOrderRefs(solution.routes());
        if (orderRefs.isEmpty()) {
            return;
        }

        Collections.shuffle(orderRefs, random);
        List<RouteOrderRef> selected = new ArrayList<>(orderRefs.subList(0, Math.min(removeCount, orderRefs.size())));
        selected.sort(Comparator
                .comparingInt(RouteOrderRef::routeIndex)
                .thenComparing(RouteOrderRef::stopIndex)
                .reversed());

        for (RouteOrderRef ref : selected) {
            removeAssignedOrder(solution, ref.routeIndex(), ref.stopIndex(), REASON_ALNS_REMOVAL);
        }
    }

    private void applyWorstDestroy(SolutionState solution, int removeCount, Random random, AlgorithmConfig config) {
        List<RouteOrderContribution> contributions = new ArrayList<>();

        for (int routeIndex = 0; routeIndex < solution.routes().size(); routeIndex++) {
            RouteState route = solution.routes().get(routeIndex);
            for (int stopIndex = 0; stopIndex < route.stops().size(); stopIndex++) {
                double contribution = estimateMarginalDistanceContribution(route, stopIndex, config);
                contribution += random.nextDouble() * 0.0001;
                contributions.add(new RouteOrderContribution(routeIndex, stopIndex, contribution));
            }
        }

        if (contributions.isEmpty()) {
            return;
        }

        contributions.sort(Comparator.comparingDouble(RouteOrderContribution::contribution).reversed());

        List<RouteOrderRef> selected = new ArrayList<>();
        int upperBound = Math.min(removeCount, contributions.size());
        for (int i = 0; i < upperBound; i++) {
            RouteOrderContribution contribution = contributions.get(i);
            selected.add(new RouteOrderRef(contribution.routeIndex(), contribution.stopIndex()));
        }

        selected.sort(Comparator
                .comparingInt(RouteOrderRef::routeIndex)
                .thenComparing(RouteOrderRef::stopIndex)
                .reversed());

        for (RouteOrderRef ref : selected) {
            removeAssignedOrder(solution, ref.routeIndex(), ref.stopIndex(), REASON_ALNS_REMOVAL);
        }
    }

    private void removeAssignedOrder(SolutionState solution, int routeIndex, int stopIndex, String reason) {
        if (routeIndex < 0 || routeIndex >= solution.routes().size()) {
            return;
        }

        RouteState route = solution.routes().get(routeIndex);
        if (stopIndex < 0 || stopIndex >= route.stops().size()) {
            return;
        }

        PickupOrderNode removedOrder = route.stops().remove(stopIndex);
        removeUnassignedOrder(solution.unassignedOrders(), removedOrder.orderId());
        solution.unassignedOrders().add(new UnassignedOrderState(removedOrder, reason, true));
    }

    private void applyRepair(
            SolutionState solution,
            PickupRepairOperator repairOperator,
            AlgorithmConfig config
    ) {
        if (repairOperator == PickupRepairOperator.REGRET_2) {
            applyRegret2Repair(solution, config);
            return;
        }

        applyGreedyRepair(solution, config);
    }

    private void applyGreedyRepair(SolutionState solution, AlgorithmConfig config) {
        while (true) {
            List<UnassignedOrderState> reinsertableOrders = getReinsertableUnassigned(solution.unassignedOrders());
            if (reinsertableOrders.isEmpty()) {
                return;
            }

            Map<Integer, Double> routeCosts = computeRouteCosts(solution.routes(), config);
            InsertionDecision bestDecision = null;

            for (UnassignedOrderState state : reinsertableOrders) {
                InsertionCandidate candidate = findBestInsertion(state.order(), solution.routes(), routeCosts, config);
                if (candidate == null) {
                    continue;
                }

                if (bestDecision == null || candidate.deltaCost() < bestDecision.candidate().deltaCost()) {
                    bestDecision = new InsertionDecision(state.order(), candidate);
                }
            }

            if (bestDecision == null) {
                return;
            }

            applyInsertion(solution, bestDecision.order(), bestDecision.candidate());
        }
    }

    private void applyRegret2Repair(SolutionState solution, AlgorithmConfig config) {
        while (true) {
            List<UnassignedOrderState> reinsertableOrders = getReinsertableUnassigned(solution.unassignedOrders());
            if (reinsertableOrders.isEmpty()) {
                return;
            }

            Map<Integer, Double> routeCosts = computeRouteCosts(solution.routes(), config);
            RegretDecision bestDecision = null;

            for (UnassignedOrderState state : reinsertableOrders) {
                List<InsertionCandidate> topCandidates = findTopInsertionCandidates(
                        state.order(),
                        solution.routes(),
                        routeCosts,
                        config,
                        2
                );
                if (topCandidates.isEmpty()) {
                    continue;
                }

                InsertionCandidate bestCandidate = topCandidates.get(0);
                double secondCost = topCandidates.size() > 1 ? topCandidates.get(1).deltaCost() : bestCandidate.deltaCost() + 100.0;
                double regretValue = secondCost - bestCandidate.deltaCost();

                RegretDecision currentDecision = new RegretDecision(state.order(), bestCandidate, regretValue);
                if (bestDecision == null) {
                    bestDecision = currentDecision;
                    continue;
                }

                if (currentDecision.regretValue() > bestDecision.regretValue() + EPSILON) {
                    bestDecision = currentDecision;
                    continue;
                }

                if (Math.abs(currentDecision.regretValue() - bestDecision.regretValue()) <= EPSILON
                        && currentDecision.candidate().deltaCost() < bestDecision.candidate().deltaCost()) {
                    bestDecision = currentDecision;
                }
            }

            if (bestDecision == null) {
                return;
            }

            applyInsertion(solution, bestDecision.order(), bestDecision.candidate());
        }
    }

    private void applyInsertion(SolutionState solution, PickupOrderNode order, InsertionCandidate candidate) {
        RouteState route = solution.routes().get(candidate.routeIndex());
        route.stops().add(candidate.insertPosition(), order);
        removeUnassignedOrder(solution.unassignedOrders(), order.orderId());
    }

    private void markNoFeasibleUnassigned(SolutionState solution) {
        for (UnassignedOrderState state : solution.unassignedOrders()) {
            if (!state.reinsertable()) {
                continue;
            }
            if (state.reason() == null || state.reason().isBlank() || REASON_UNASSIGNED.equals(state.reason())) {
                state.setReason(REASON_NO_FEASIBLE_INSERTION);
            }
        }
    }

    private InsertionCandidate findBestInsertion(
            PickupOrderNode order,
            List<RouteState> routes,
            Map<Integer, Double> routeCosts,
            AlgorithmConfig config
    ) {
        InsertionCandidate bestCandidate = null;

        for (int routeIndex = 0; routeIndex < routes.size(); routeIndex++) {
            RouteState route = routes.get(routeIndex);
            double oldCost = routeCosts.getOrDefault(routeIndex, HUGE_OBJECTIVE);
            int maxInsertionPosition = route.stops().size();

            for (int insertPosition = 0; insertPosition <= maxInsertionPosition; insertPosition++) {
                List<PickupOrderNode> newStops = new ArrayList<>(route.stops());
                newStops.add(insertPosition, order);

                RouteEvaluation newEvaluation = evaluateRoute(route, newStops, config);
                if (!newEvaluation.feasible()) {
                    continue;
                }

                boolean usedRoute = !newStops.isEmpty();
                double newCost = calculateRouteCost(newEvaluation, usedRoute, config);
                double deltaCost = newCost - oldCost;

                if (bestCandidate == null || deltaCost < bestCandidate.deltaCost()) {
                    bestCandidate = new InsertionCandidate(routeIndex, insertPosition, deltaCost);
                }
            }
        }

        return bestCandidate;
    }

    private List<InsertionCandidate> findTopInsertionCandidates(
            PickupOrderNode order,
            List<RouteState> routes,
            Map<Integer, Double> routeCosts,
            AlgorithmConfig config,
            int topN
    ) {
        List<InsertionCandidate> candidates = new ArrayList<>();

        for (int routeIndex = 0; routeIndex < routes.size(); routeIndex++) {
            RouteState route = routes.get(routeIndex);
            double oldCost = routeCosts.getOrDefault(routeIndex, HUGE_OBJECTIVE);
            int maxInsertionPosition = route.stops().size();

            for (int insertPosition = 0; insertPosition <= maxInsertionPosition; insertPosition++) {
                List<PickupOrderNode> newStops = new ArrayList<>(route.stops());
                newStops.add(insertPosition, order);

                RouteEvaluation newEvaluation = evaluateRoute(route, newStops, config);
                if (!newEvaluation.feasible()) {
                    continue;
                }

                boolean usedRoute = !newStops.isEmpty();
                double newCost = calculateRouteCost(newEvaluation, usedRoute, config);
                double deltaCost = newCost - oldCost;
                candidates.add(new InsertionCandidate(routeIndex, insertPosition, deltaCost));
            }
        }

        candidates.sort(Comparator.comparingDouble(InsertionCandidate::deltaCost));
        if (candidates.size() <= topN) {
            return candidates;
        }

        return new ArrayList<>(candidates.subList(0, topN));
    }

    private List<UnassignedOrderState> getReinsertableUnassigned(List<UnassignedOrderState> unassignedOrders) {
        List<UnassignedOrderState> result = new ArrayList<>();
        for (UnassignedOrderState state : unassignedOrders) {
            if (state.reinsertable()) {
                result.add(state);
            }
        }
        return result;
    }

    private SolutionState deepCopySolution(SolutionState source) {
        List<RouteState> copiedRoutes = new ArrayList<>();
        for (RouteState route : source.routes()) {
            copiedRoutes.add(route.copy());
        }

        List<UnassignedOrderState> copiedUnassigned = new ArrayList<>();
        for (UnassignedOrderState unassignedOrder : source.unassignedOrders()) {
            copiedUnassigned.add(unassignedOrder.copy());
        }

        return new SolutionState(copiedRoutes, copiedUnassigned);
    }

    private SolutionEvaluation evaluateSolution(SolutionState solution, AlgorithmConfig config) {
        List<RouteEvaluation> routeEvaluations = new ArrayList<>();
        double totalDistanceKm = 0.0;
        long totalTravelMinutes = 0;
        long totalServiceMinutes = 0;
        long totalLatenessMinutes = 0;
        int assignedOrders = 0;
        int usedRoutes = 0;

        for (RouteState route : solution.routes()) {
            RouteEvaluation routeEvaluation = evaluateRoute(route, route.stops(), config);
            routeEvaluations.add(routeEvaluation);

            if (!routeEvaluation.feasible()) {
                return new SolutionEvaluation(
                        HUGE_OBJECTIVE,
                        totalDistanceKm,
                        totalTravelMinutes,
                        totalServiceMinutes,
                        totalLatenessMinutes,
                        assignedOrders,
                        solution.unassignedOrders().size(),
                        usedRoutes,
                        routeEvaluations
                );
            }

            totalDistanceKm += routeEvaluation.totalDistanceKm();
            totalTravelMinutes += routeEvaluation.totalTravelMinutes();
            totalServiceMinutes += routeEvaluation.totalServiceMinutes();
            totalLatenessMinutes += routeEvaluation.totalLatenessMinutes();
            assignedOrders += route.stops().size();
            if (!route.stops().isEmpty()) {
                usedRoutes++;
            }
        }

        int unassignedCount = solution.unassignedOrders().size();
        double objectiveScore = config.distanceWeight() * totalDistanceKm
                + config.latenessWeight() * totalLatenessMinutes
                + config.unassignedPenalty() * unassignedCount
                + config.usedRoutePenalty() * usedRoutes;

        return new SolutionEvaluation(
                objectiveScore,
                totalDistanceKm,
                totalTravelMinutes,
                totalServiceMinutes,
                totalLatenessMinutes,
                assignedOrders,
                unassignedCount,
                usedRoutes,
                routeEvaluations
        );
    }

    private RouteEvaluation evaluateRoute(RouteState route, List<PickupOrderNode> stops, AlgorithmConfig config) {
        if (route.maxStops() != null && route.maxStops() > 0 && stops.size() > route.maxStops()) {
            return RouteEvaluation.infeasible();
        }

        PickupOrderNode previousOrder = null;
        LocalDateTime currentTime = config.planningStartTime();

        double totalDistanceKm = 0.0;
        long totalTravelMinutes = 0;
        long totalServiceMinutes = 0;
        long totalLatenessMinutes = 0;
        double totalWeight = 0.0;
        double totalVolume = 0.0;

        List<StopEvaluationData> stopDetails = new ArrayList<>();

        for (int index = 0; index < stops.size(); index++) {
            PickupOrderNode order = stops.get(index);
            if (order.latitude() == null || order.longitude() == null) {
                return RouteEvaluation.infeasible();
            }

            LegMetric legMetric = resolveLegMetric(previousOrder, order, route, config);
            double distanceFromPreviousKm = legMetric.distanceKm();
            long travelMinutes = legMetric.travelMinutes();

            LocalDateTime arrivalTime = currentTime.plusMinutes(travelMinutes);
            LocalDateTime startServiceTime = arrivalTime;
            if (order.pickupTimeStart() != null && arrivalTime.isBefore(order.pickupTimeStart())) {
                startServiceTime = order.pickupTimeStart();
            }

            long latenessMinutes = 0;
            if (order.pickupTimeEnd() != null && startServiceTime.isAfter(order.pickupTimeEnd())) {
                latenessMinutes = ChronoUnit.MINUTES.between(order.pickupTimeEnd(), startServiceTime);
            }

            if (!config.allowLateness() && latenessMinutes > 0) {
                return RouteEvaluation.infeasible();
            }

            LocalDateTime departureTime = startServiceTime.plusMinutes(config.serviceMinutesPerStop());

            totalWeight += safePositive(order.weight());
            totalVolume += safePositive(order.volume());
            if (config.enforceCapacity()) {
                if (totalWeight > route.maxWeight() + EPSILON || totalVolume > route.maxVolume() + EPSILON) {
                    return RouteEvaluation.infeasible();
                }
            }

            totalDistanceKm += distanceFromPreviousKm;
            totalTravelMinutes += travelMinutes;
            totalServiceMinutes += config.serviceMinutesPerStop();
            totalLatenessMinutes += latenessMinutes;

            stopDetails.add(new StopEvaluationData(
                    index + 1,
                    order,
                    distanceFromPreviousKm,
                    travelMinutes,
                    arrivalTime,
                    startServiceTime,
                    departureTime,
                    latenessMinutes
            ));

            currentTime = departureTime;
            previousOrder = order;
        }

        if (!stops.isEmpty()) {
            LegMetric backLegMetric = resolveLegMetric(previousOrder, null, route, config);
            double backDistanceKm = backLegMetric.distanceKm();
            long backTravelMinutes = backLegMetric.travelMinutes();
            totalDistanceKm += backDistanceKm;
            totalTravelMinutes += backTravelMinutes;
            currentTime = currentTime.plusMinutes(backTravelMinutes);
        }

        if (config.enforcePlanningEnd() && currentTime.isAfter(config.planningEndTime())) {
            return RouteEvaluation.infeasible();
        }

        return new RouteEvaluation(
                true,
                totalDistanceKm,
                totalTravelMinutes,
                totalServiceMinutes,
                totalLatenessMinutes,
                totalWeight,
                totalVolume,
                config.planningStartTime(),
                currentTime,
                stopDetails
        );
    }

    private Map<Integer, Double> computeRouteCosts(List<RouteState> routes, AlgorithmConfig config) {
        Map<Integer, Double> routeCosts = new HashMap<>();
        for (int index = 0; index < routes.size(); index++) {
            RouteState route = routes.get(index);
            RouteEvaluation evaluation = evaluateRoute(route, route.stops(), config);
            boolean usedRoute = !route.stops().isEmpty();
            routeCosts.put(index, calculateRouteCost(evaluation, usedRoute, config));
        }
        return routeCosts;
    }

    private double calculateRouteCost(RouteEvaluation routeEvaluation, boolean usedRoute, AlgorithmConfig config) {
        if (!routeEvaluation.feasible()) {
            return HUGE_OBJECTIVE;
        }

        double cost = config.distanceWeight() * routeEvaluation.totalDistanceKm()
                + config.latenessWeight() * routeEvaluation.totalLatenessMinutes();
        if (usedRoute) {
            cost += config.usedRoutePenalty();
        }
        return cost;
    }

    private boolean shouldAccept(double candidateObjective, double currentObjective, double temperature, Random random) {
        if (candidateObjective + EPSILON < currentObjective) {
            return true;
        }

        if (temperature <= EPSILON) {
            return false;
        }

        double probability = Math.exp((currentObjective - candidateObjective) / temperature);
        return random.nextDouble() < probability;
    }

    private List<RouteOrderRef> collectAssignedOrderRefs(List<RouteState> routes) {
        List<RouteOrderRef> refs = new ArrayList<>();
        for (int routeIndex = 0; routeIndex < routes.size(); routeIndex++) {
            RouteState route = routes.get(routeIndex);
            for (int stopIndex = 0; stopIndex < route.stops().size(); stopIndex++) {
                refs.add(new RouteOrderRef(routeIndex, stopIndex));
            }
        }
        return refs;
    }

    private int countAssignedOrders(List<RouteState> routes) {
        int total = 0;
        for (RouteState route : routes) {
            total += route.stops().size();
        }
        return total;
    }

    private void removeUnassignedOrder(List<UnassignedOrderState> unassignedOrders, Long orderId) {
        if (orderId == null) {
            return;
        }

        for (int i = 0; i < unassignedOrders.size(); i++) {
            if (Objects.equals(unassignedOrders.get(i).order().orderId(), orderId)) {
                unassignedOrders.remove(i);
                return;
            }
        }
    }

    private double estimateMarginalDistanceContribution(RouteState route, int stopIndex, AlgorithmConfig config) {
        PickupOrderNode current = route.stops().get(stopIndex);

        PickupOrderNode previous;
        if (stopIndex == 0) {
            previous = null;
        } else {
            previous = route.stops().get(stopIndex - 1);
        }

        PickupOrderNode next;
        if (stopIndex == route.stops().size() - 1) {
            next = null;
        } else {
            next = route.stops().get(stopIndex + 1);
        }

        double viaCurrent = resolveLegMetric(previous, current, route, config).distanceKm()
                + resolveLegMetric(current, next, route, config).distanceKm();
        double direct = resolveLegMetric(previous, next, route, config).distanceKm();
        return viaCurrent - direct;
    }

    private TravelMetricProvider buildTravelMetricProvider(
            List<PickupOrderNode> assignableOrders,
            double depotLatitude,
            double depotLongitude,
            AlgorithmConfig config
    ) {
        Map<Long, Integer> orderNodeIndexByOrderId = new HashMap<>();
        List<NodePoint> nodes = new ArrayList<>();

        nodes.add(new NodePoint(null, depotLatitude, depotLongitude));
        for (PickupOrderNode order : assignableOrders) {
            if (order.orderId() == null || orderNodeIndexByOrderId.containsKey(order.orderId())) {
                continue;
            }
            int nodeIndex = nodes.size();
            orderNodeIndexByOrderId.put(order.orderId(), nodeIndex);
            nodes.add(new NodePoint(order.orderId(), order.latitude(), order.longitude()));
        }

        int nodeCount = nodes.size();
        double[][] distanceKm = new double[nodeCount][nodeCount];
        long[][] travelMinutes = new long[nodeCount][nodeCount];

        TravelMetricProvider metricProvider = new TravelMetricProvider(
                orderNodeIndexByOrderId,
                nodes,
                distanceKm,
                travelMinutes
        );

        populateFallbackMetrics(metricProvider, config.averageSpeedKmph());
        populateDistanceMatrixMetrics(metricProvider, config);
        return metricProvider;
    }

    private void populateDistanceMatrixMetrics(TravelMetricProvider metricProvider, AlgorithmConfig config) {
        int nodeCount = metricProvider.nodeCount();
        if (nodeCount <= 1) {
            return;
        }

        if (nodeCount > config.distanceMatrixMaxNodes()) {
            log.info(
                    "Skip Goong Distance Matrix due to node count {} > max-nodes {}",
                    nodeCount,
                    config.distanceMatrixMaxNodes()
            );
            return;
        }

        int batchSize = Math.max(1, config.distanceMatrixBatchSize());
        List<DistanceMatrixCaller.GeoPoint> points = metricProvider.nodes().stream()
                .map(node -> new DistanceMatrixCaller.GeoPoint(node.latitude(), node.longitude()))
                .toList();

        for (int originStart = 0; originStart < nodeCount; originStart += batchSize) {
            int originEnd = Math.min(originStart + batchSize, nodeCount);
            List<DistanceMatrixCaller.GeoPoint> originBatch = points.subList(originStart, originEnd);

            for (int destinationStart = 0; destinationStart < nodeCount; destinationStart += batchSize) {
                int destinationEnd = Math.min(destinationStart + batchSize, nodeCount);
                List<DistanceMatrixCaller.GeoPoint> destinationBatch = points.subList(destinationStart, destinationEnd);

                DistanceMatrixCaller.DistanceMatrixResult matrixResult = distanceMatrixCaller.calculateDistanceMatrix(
                        originBatch,
                        destinationBatch,
                        config.routingVehicle()
                );

                if (!hasValidMatrixShape(matrixResult, originBatch.size(), destinationBatch.size())) {
                    log.debug(
                            "Invalid matrix shape for batch origins={} destinations={}",
                            originBatch.size(),
                            destinationBatch.size()
                    );
                    continue;
                }

                for (int originOffset = 0; originOffset < originBatch.size(); originOffset++) {
                    List<DistanceMatrixCaller.DistanceMatrixElement> row = matrixResult.rows().get(originOffset);
                    for (int destinationOffset = 0; destinationOffset < destinationBatch.size(); destinationOffset++) {
                        DistanceMatrixCaller.DistanceMatrixElement element = row.get(destinationOffset);
                        if (element == null || !element.isOk()) {
                            continue;
                        }

                        double distanceValueKm = element.distanceMeters() / 1000.0;
                        long travelMinuteValue = convertDurationToMinutes(
                                element.durationSeconds(),
                                distanceValueKm,
                                config.averageSpeedKmph()
                        );

                        metricProvider.distanceKm()[originStart + originOffset][destinationStart + destinationOffset] = distanceValueKm;
                        metricProvider.travelMinutes()[originStart + originOffset][destinationStart + destinationOffset] = travelMinuteValue;
                    }
                }
            }
        }
    }

    private boolean hasValidMatrixShape(
            DistanceMatrixCaller.DistanceMatrixResult matrixResult,
            int expectedRows,
            int expectedColumns
    ) {
        if (matrixResult == null || matrixResult.rows() == null || matrixResult.rows().size() != expectedRows) {
            return false;
        }

        for (List<DistanceMatrixCaller.DistanceMatrixElement> row : matrixResult.rows()) {
            if (row == null || row.size() != expectedColumns) {
                return false;
            }
        }

        return true;
    }

    private void populateFallbackMetrics(TravelMetricProvider metricProvider, double averageSpeedKmph) {
        int nodeCount = metricProvider.nodeCount();
        for (int fromIndex = 0; fromIndex < nodeCount; fromIndex++) {
            NodePoint fromNode = metricProvider.nodes().get(fromIndex);
            for (int toIndex = 0; toIndex < nodeCount; toIndex++) {
                NodePoint toNode = metricProvider.nodes().get(toIndex);
                if (fromIndex == toIndex) {
                    metricProvider.distanceKm()[fromIndex][toIndex] = 0.0;
                    metricProvider.travelMinutes()[fromIndex][toIndex] = 0;
                    continue;
                }

                double distanceValueKm = distanceKm(
                        fromNode.latitude(),
                        fromNode.longitude(),
                        toNode.latitude(),
                        toNode.longitude()
                );
                long travelMinuteValue = estimateTravelMinutes(distanceValueKm, averageSpeedKmph);

                metricProvider.distanceKm()[fromIndex][toIndex] = distanceValueKm;
                metricProvider.travelMinutes()[fromIndex][toIndex] = travelMinuteValue;
            }
        }
    }

    private LegMetric resolveLegMetric(
            PickupOrderNode fromOrder,
            PickupOrderNode toOrder,
            RouteState route,
            AlgorithmConfig config
    ) {
        TravelMetricProvider metricProvider = config.travelMetricProvider();
        if (metricProvider != null) {
            int fromIndex = resolveNodeIndex(fromOrder, metricProvider);
            int toIndex = resolveNodeIndex(toOrder, metricProvider);
            if (fromIndex >= 0 && toIndex >= 0
                    && fromIndex < metricProvider.nodeCount()
                    && toIndex < metricProvider.nodeCount()) {
                return new LegMetric(
                        metricProvider.distanceKm()[fromIndex][toIndex],
                        metricProvider.travelMinutes()[fromIndex][toIndex]
                );
            }
        }

        double fromLatitude = fromOrder == null ? route.depotLatitude() : fromOrder.latitude();
        double fromLongitude = fromOrder == null ? route.depotLongitude() : fromOrder.longitude();
        double toLatitude = toOrder == null ? route.depotLatitude() : toOrder.latitude();
        double toLongitude = toOrder == null ? route.depotLongitude() : toOrder.longitude();

        double distanceValueKm = distanceKm(fromLatitude, fromLongitude, toLatitude, toLongitude);
        long travelMinuteValue = estimateTravelMinutes(distanceValueKm, config.averageSpeedKmph());
        return new LegMetric(distanceValueKm, travelMinuteValue);
    }

    private int resolveNodeIndex(PickupOrderNode order, TravelMetricProvider metricProvider) {
        if (order == null) {
            return 0;
        }
        if (order.orderId() == null) {
            return -1;
        }
        return metricProvider.orderNodeIndexByOrderId().getOrDefault(order.orderId(), -1);
    }

    private long convertDurationToMinutes(Long durationSeconds, double distanceKm, double averageSpeedKmph) {
        if (durationSeconds == null || durationSeconds < 0) {
            return estimateTravelMinutes(distanceKm, averageSpeedKmph);
        }

        if (durationSeconds == 0) {
            return distanceKm <= EPSILON ? 0 : 1;
        }

        return Math.max(1L, Math.round(durationSeconds / 60.0));
    }

    private EnumMap<PickupDestroyOperator, Double> initializeDestroyWeights() {
        EnumMap<PickupDestroyOperator, Double> weights = new EnumMap<>(PickupDestroyOperator.class);
        for (PickupDestroyOperator operator : PickupDestroyOperator.values()) {
            weights.put(operator, 1.0);
        }
        return weights;
    }

    private EnumMap<PickupDestroyOperator, Integer> initializeDestroyUsages() {
        EnumMap<PickupDestroyOperator, Integer> usages = new EnumMap<>(PickupDestroyOperator.class);
        for (PickupDestroyOperator operator : PickupDestroyOperator.values()) {
            usages.put(operator, 0);
        }
        return usages;
    }

    private EnumMap<PickupRepairOperator, Double> initializeRepairWeights() {
        EnumMap<PickupRepairOperator, Double> weights = new EnumMap<>(PickupRepairOperator.class);
        for (PickupRepairOperator operator : PickupRepairOperator.values()) {
            weights.put(operator, 1.0);
        }
        return weights;
    }

    private EnumMap<PickupRepairOperator, Integer> initializeRepairUsages() {
        EnumMap<PickupRepairOperator, Integer> usages = new EnumMap<>(PickupRepairOperator.class);
        for (PickupRepairOperator operator : PickupRepairOperator.values()) {
            usages.put(operator, 0);
        }
        return usages;
    }

    private PickupDestroyOperator selectDestroyOperator(EnumMap<PickupDestroyOperator, Double> weights, Random random) {
        return rouletteSelect(weights, random, PickupDestroyOperator.RANDOM);
    }

    private PickupRepairOperator selectRepairOperator(EnumMap<PickupRepairOperator, Double> weights, Random random) {
        return rouletteSelect(weights, random, PickupRepairOperator.GREEDY);
    }

    private <T extends Enum<T>> T rouletteSelect(EnumMap<T, Double> weights, Random random, T fallback) {
        double totalWeight = 0.0;
        for (double weight : weights.values()) {
            totalWeight += Math.max(weight, 0.0);
        }

        if (totalWeight <= EPSILON) {
            return fallback;
        }

        double randomValue = random.nextDouble() * totalWeight;
        double cumulative = 0.0;
        for (Map.Entry<T, Double> entry : weights.entrySet()) {
            cumulative += Math.max(entry.getValue(), 0.0);
            if (randomValue <= cumulative) {
                return entry.getKey();
            }
        }

        return fallback;
    }

    private void updateDestroyWeights(
            EnumMap<PickupDestroyOperator, Double> weights,
            EnumMap<PickupDestroyOperator, Double> scores,
            EnumMap<PickupDestroyOperator, Integer> usages
    ) {
        for (PickupDestroyOperator operator : PickupDestroyOperator.values()) {
            double averageScore = usages.get(operator) == 0
                    ? 0.0
                    : scores.get(operator) / usages.get(operator);
            double updatedWeight = (1 - OPERATOR_REACTION_FACTOR) * weights.get(operator)
                    + OPERATOR_REACTION_FACTOR * Math.max(OPERATOR_MIN_WEIGHT, averageScore);
            weights.put(operator, updatedWeight);
            scores.put(operator, 0.0);
            usages.put(operator, 0);
        }
    }

    private void updateRepairWeights(
            EnumMap<PickupRepairOperator, Double> weights,
            EnumMap<PickupRepairOperator, Double> scores,
            EnumMap<PickupRepairOperator, Integer> usages
    ) {
        for (PickupRepairOperator operator : PickupRepairOperator.values()) {
            double averageScore = usages.get(operator) == 0
                    ? 0.0
                    : scores.get(operator) / usages.get(operator);
            double updatedWeight = (1 - OPERATOR_REACTION_FACTOR) * weights.get(operator)
                    + OPERATOR_REACTION_FACTOR * Math.max(OPERATOR_MIN_WEIGHT, averageScore);
            weights.put(operator, updatedWeight);
            scores.put(operator, 0.0);
            usages.put(operator, 0);
        }
    }

    private PreparedOrderData prepareOrders(List<Order> candidateOrders) {
        List<PickupOrderNode> assignableOrders = new ArrayList<>();
        List<UnassignedOrderState> unassignedOrders = new ArrayList<>();

        for (Order order : candidateOrders) {
            Point senderLocation = order.getSenderLocation();
            if (senderLocation == null) {
                unassignedOrders.add(new UnassignedOrderState(
                        toOrderNodeWithoutLocation(order),
                        REASON_MISSING_SENDER_LOCATION,
                        false
                ));
                continue;
            }

            double latitude = senderLocation.getY();
            double longitude = senderLocation.getX();
            if (!isValidCoordinate(latitude, longitude)) {
                unassignedOrders.add(new UnassignedOrderState(
                        toOrderNodeWithoutLocation(order),
                        REASON_INVALID_SENDER_LOCATION,
                        false
                ));
                continue;
            }

            assignableOrders.add(new PickupOrderNode(
                    order.getId(),
                    order.getOrderCode(),
                    order.getCustomerOrderCode(),
                    order.getSenderName(),
                    order.getSenderPhone(),
                    latitude,
                    longitude,
                    safePositive(order.getTotalWeight()),
                    safePositive(order.getTotalVolume()),
                    order.getPickupTimeStart(),
                    order.getPickupTimeEnd()
            ));
        }

        assignableOrders.sort(Comparator
                .comparing(PickupOrderNode::pickupTimeEnd, Comparator.nullsLast(Comparator.naturalOrder()))
                .thenComparing(PickupOrderNode::orderId, Comparator.nullsLast(Comparator.naturalOrder()))
        );

        return new PreparedOrderData(assignableOrders, unassignedOrders);
    }

    private PickupOrderNode toOrderNodeWithoutLocation(Order order) {
        return new PickupOrderNode(
                order.getId(),
                order.getOrderCode(),
                order.getCustomerOrderCode(),
                order.getSenderName(),
                order.getSenderPhone(),
                null,
                null,
                safePositive(order.getTotalWeight()),
                safePositive(order.getTotalVolume()),
                order.getPickupTimeStart(),
                order.getPickupTimeEnd()
        );
    }

    private List<RouteState> initializeRoutes(
            List<CourierResource> couriers,
            List<Vehicle> activeVehicles,
            double depotLatitude,
            double depotLongitude
    ) {
        Map<Long, Vehicle> dedicatedVehicleByCourier = new HashMap<>();
        Set<Long> courierIds = new HashSet<>();
        for (CourierResource courier : couriers) {
            courierIds.add(courier.staffId());
        }

        List<Vehicle> sharedVehicles = new ArrayList<>();
        for (Vehicle vehicle : activeVehicles) {
            Long staffId = vehicle.getPostOfficeStaffId();
            if (staffId != null && courierIds.contains(staffId) && !dedicatedVehicleByCourier.containsKey(staffId)) {
                dedicatedVehicleByCourier.put(staffId, vehicle);
            } else {
                sharedVehicles.add(vehicle);
            }
        }

        List<RouteState> routes = new ArrayList<>();
        int sharedVehicleIndex = 0;

        for (CourierResource courier : couriers) {
            Vehicle selectedVehicle = dedicatedVehicleByCourier.get(courier.staffId());
            if (selectedVehicle == null && sharedVehicleIndex < sharedVehicles.size()) {
                selectedVehicle = sharedVehicles.get(sharedVehicleIndex);
                sharedVehicleIndex++;
            }

            Long vehicleId = selectedVehicle == null ? null : selectedVehicle.getId();
            String licensePlate = selectedVehicle == null ? null : selectedVehicle.getLicensePlate();
            double maxWeight = selectedVehicle == null
                    ? DEFAULT_MAX_WEIGHT_IF_MISSING
                    : resolvePositiveDouble(selectedVehicle.getMaxWeight(), DEFAULT_MAX_WEIGHT_IF_MISSING);
            double maxVolume = selectedVehicle == null
                    ? DEFAULT_MAX_VOLUME_IF_MISSING
                    : resolvePositiveDouble(selectedVehicle.getMaxVolume(), DEFAULT_MAX_VOLUME_IF_MISSING);

            routes.add(new RouteState(
                    courier.staffId(),
                    courier.code(),
                    courier.fullName(),
                    courier.maxStops(),
                    vehicleId,
                    licensePlate,
                    maxWeight,
                    maxVolume,
                    depotLatitude,
                    depotLongitude,
                    new ArrayList<>()
            ));
        }

        return routes;
    }

    private List<CourierResource> loadActiveCouriers(
            Long postOfficeId,
            Long tenantId,
            List<Long> requestCourierIds,
            LocalDate planningDate
    ) {
        List<PostOfficeStaffAssignment> assignments = postOfficeStaffAssignmentRepository
                .findActiveAssignmentsByPostOfficeIdAndTenantIdAndStaffRoleAndStaffStatus(
                        postOfficeId,
                        tenantId,
                        planningDate,
                        PostOfficeStaffRole.COURIER,
                        PostOfficeStaffStatus.ACTIVE
                );

        Set<Long> allowedCourierIds = null;
        if (requestCourierIds != null && !requestCourierIds.isEmpty()) {
            allowedCourierIds = new HashSet<>(requestCourierIds);
        }

        Map<Long, CourierResource> uniqueCouriers = new LinkedHashMap<>();
        for (PostOfficeStaffAssignment assignment : assignments) {
            PostOfficeStaff staff = assignment.getStaff();
            if (staff == null || staff.getId() == null) {
                continue;
            }

            if (allowedCourierIds != null && !allowedCourierIds.contains(staff.getId())) {
                continue;
            }

            if (uniqueCouriers.containsKey(staff.getId())) {
                continue;
            }

            Integer maxStops = resolveMaxStops(staff);
            uniqueCouriers.put(staff.getId(), new CourierResource(
                    staff.getId(),
                    staff.getCode(),
                    staff.getFullName(),
                    maxStops
            ));
        }

        return new ArrayList<>(uniqueCouriers.values());
    }

    private Integer resolveMaxStops(PostOfficeStaff staff) {
        Integer maxDailyStops = normalizePositiveInteger(staff.getMaxDailyStops());
        if (maxDailyStops != null) {
            return maxDailyStops;
        }
        return normalizePositiveInteger(staff.getMaxDailyParcels());
    }

    private AlgorithmConfig buildConfig(OptimizePickupPlanRequest request) {
        LocalDateTime planningStartTime = request.getPlanningStartTime() == null
                ? LocalDateTime.now()
                : request.getPlanningStartTime();

        LocalDateTime planningEndTime = request.getPlanningEndTime() == null
                ? planningStartTime.plusHours(8)
                : request.getPlanningEndTime();

        if (planningEndTime.isBefore(planningStartTime)) {
            throw new AppException(ErrorCode.INVALID_REQUEST);
        }

        int orderLimit = resolvePositiveInt(request.getOrderLimit(), DEFAULT_ORDER_LIMIT);
        double averageSpeedKmph = resolvePositiveDouble(request.getAverageSpeedKmph(), DEFAULT_AVERAGE_SPEED_KMPH);
        int serviceMinutesPerStop = resolvePositiveInt(request.getServiceMinutesPerStop(), DEFAULT_SERVICE_MINUTES_PER_STOP);
        int maxIterations = resolvePositiveInt(request.getMaxIterations(), DEFAULT_MAX_ITERATIONS);
        long maxRuntimeMillis = resolvePositiveLong(request.getMaxRuntimeMillis(), DEFAULT_MAX_RUNTIME_MILLIS);

        double destroyRate = clamp(resolvePositiveDouble(request.getDestroyRate(), DEFAULT_DESTROY_RATE), 0.01, 0.90);
        double initialTemperature = resolvePositiveDouble(request.getInitialTemperature(), DEFAULT_INITIAL_TEMPERATURE);
        double coolingRate = clamp(resolvePositiveDouble(request.getCoolingRate(), DEFAULT_COOLING_RATE), 0.80, 0.9999);

        boolean allowLateness = request.getAllowLateness() == null ? DEFAULT_ALLOW_LATENESS : request.getAllowLateness();
        boolean enforcePlanningEnd = request.getEnforcePlanningEnd() == null
                ? DEFAULT_ENFORCE_PLANNING_END
                : request.getEnforcePlanningEnd();
        boolean enforceCapacity = request.getEnforceCapacity() == null
                ? DEFAULT_ENFORCE_CAPACITY
                : request.getEnforceCapacity();

        double distanceWeight = resolveNonNegativeDouble(request.getDistanceWeight(), DEFAULT_DISTANCE_WEIGHT);
        double latenessWeight = resolveNonNegativeDouble(request.getLatenessWeight(), DEFAULT_LATENESS_WEIGHT);
        double unassignedPenalty = resolveNonNegativeDouble(request.getUnassignedPenalty(), DEFAULT_UNASSIGNED_PENALTY);
        double usedRoutePenalty = resolveNonNegativeDouble(request.getUsedRoutePenalty(), DEFAULT_USED_ROUTE_PENALTY);
        RoutingVehicle routingVehicle = RoutingVehicle.fromValue(request.getVehicle());
        int matrixBatchSize = resolvePositiveInt(distanceMatrixBatchSize, DEFAULT_DISTANCE_MATRIX_BATCH_SIZE);
        int matrixMaxNodes = resolvePositiveInt(distanceMatrixMaxNodes, DEFAULT_DISTANCE_MATRIX_MAX_NODES);

        return new AlgorithmConfig(
                planningStartTime,
                planningEndTime,
                planningStartTime.toLocalDate(),
                orderLimit,
                averageSpeedKmph,
                serviceMinutesPerStop,
                maxIterations,
                maxRuntimeMillis,
                destroyRate,
                initialTemperature,
                coolingRate,
                allowLateness,
                enforcePlanningEnd,
                enforceCapacity,
                distanceWeight,
                latenessWeight,
                unassignedPenalty,
                usedRoutePenalty,
                routingVehicle,
                matrixBatchSize,
                matrixMaxNodes,
                null
        );
    }

    private void validateManagerScope(Long postOfficeId, Long tenantId) {
        if (!isManagerScopedAccess()) {
            return;
        }

        Long currentUserId = authUtils.getCurrentUserId().orElseThrow(() -> new AppException(ErrorCode.UNAUTHORIZED));
        String managerCode = buildStaffCode(currentUserId, PostOfficeStaffRole.MANAGER);

        PostOfficeStaff managerStaff = postOfficeStaffRepository.findByCodeAndTenantId(managerCode, tenantId)
                .orElseThrow(() -> new AppException(ErrorCode.UNAUTHORIZED));

        boolean hasAssignment = postOfficeStaffAssignmentRepository.existsActiveAssignmentByStaffIdAndPostOfficeIdAndTenantId(
                managerStaff.getId(),
                postOfficeId,
                tenantId,
                LocalDate.now()
        );

        if (!hasAssignment) {
            throw new AppException(ErrorCode.UNAUTHORIZED);
        }
    }

    private List<OrderStatus> resolveCandidateStatuses(Collection<OrderStatus> requestedStatuses) {
        if (requestedStatuses == null || requestedStatuses.isEmpty()) {
            return DEFAULT_CANDIDATE_STATUSES;
        }

        List<OrderStatus> normalized = new ArrayList<>();
        for (OrderStatus status : requestedStatuses) {
            if (status != null && !normalized.contains(status)) {
                normalized.add(status);
            }
        }

        if (normalized.isEmpty()) {
            return DEFAULT_CANDIDATE_STATUSES;
        }

        return normalized;
    }

    private PickupOptimizationResponse toResponse(
            PostOffice postOffice,
            AlgorithmConfig config,
            SolutionState solution
    ) {
        sanitizeSolution(solution);
        SolutionEvaluation solutionEvaluation = evaluateSolution(solution, config);

        List<PickupOptimizationResponse.PickupRoutePlanResponse> routeResponses = new ArrayList<>();
        for (int index = 0; index < solution.routes().size(); index++) {
            RouteState route = solution.routes().get(index);
            RouteEvaluation evaluation = solutionEvaluation.routeEvaluations().get(index);

            List<PickupOptimizationResponse.PickupStopResponse> stopResponses = new ArrayList<>();
            for (StopEvaluationData stop : evaluation.stopDetails()) {
                PickupOrderNode order = stop.order();
                stopResponses.add(new PickupOptimizationResponse.PickupStopResponse(
                        stop.sequence(),
                        order.orderId(),
                        order.orderCode(),
                        order.customerOrderCode(),
                        order.senderName(),
                        order.senderPhone(),
                        order.latitude(),
                        order.longitude(),
                        order.pickupTimeStart(),
                        order.pickupTimeEnd(),
                        stop.arrivalTime(),
                        stop.startServiceTime(),
                        stop.departureTime(),
                        round3(stop.distanceFromPreviousKm()),
                        stop.travelMinutes(),
                        stop.latenessMinutes()
                ));
            }

            routeResponses.add(new PickupOptimizationResponse.PickupRoutePlanResponse(
                    route.courierStaffId(),
                    route.courierCode(),
                    route.courierName(),
                    route.vehicleId(),
                    route.vehicleLicensePlate(),
                    round3(route.maxWeight()),
                    round3(route.maxVolume()),
                    route.stops().size(),
                    round3(evaluation.totalWeight()),
                    round3(evaluation.totalVolume()),
                    round3(evaluation.totalDistanceKm()),
                    evaluation.totalTravelMinutes(),
                    evaluation.totalServiceMinutes(),
                    evaluation.totalLatenessMinutes(),
                    evaluation.routeStartTime(),
                    evaluation.routeEndTime(),
                    stopResponses
            ));
        }

        List<PickupOptimizationResponse.UnassignedPickupOrderResponse> unassignedResponses = new ArrayList<>();
        for (UnassignedOrderState unassignedOrder : solution.unassignedOrders()) {
            PickupOrderNode order = unassignedOrder.order();
            unassignedResponses.add(new PickupOptimizationResponse.UnassignedPickupOrderResponse(
                    order.orderId(),
                    order.orderCode(),
                    order.customerOrderCode(),
                    unassignedOrder.reason()
            ));
        }

        unassignedResponses.sort(Comparator
                .comparing(PickupOptimizationResponse.UnassignedPickupOrderResponse::orderId, Comparator.nullsLast(Comparator.naturalOrder()))
                .thenComparing(PickupOptimizationResponse.UnassignedPickupOrderResponse::customerOrderCode, Comparator.nullsLast(String::compareToIgnoreCase))
        );

        return new PickupOptimizationResponse(
                postOffice.getId(),
                postOffice.getCode(),
                postOffice.getName(),
                config.planningStartTime(),
                config.planningEndTime(),
                solutionEvaluation.assignedOrders() + solutionEvaluation.unassignedOrders(),
                solutionEvaluation.assignedOrders(),
                solutionEvaluation.unassignedOrders(),
                round3(solutionEvaluation.totalDistanceKm()),
                solutionEvaluation.totalTravelMinutes(),
                solutionEvaluation.totalServiceMinutes(),
                solutionEvaluation.totalLatenessMinutes(),
                round3(solutionEvaluation.objectiveScore()),
                solution.routes().size(),
                solutionEvaluation.usedRoutes(),
                routeResponses,
                unassignedResponses
        );
    }

    private void sanitizeSolution(SolutionState solution) {
        Set<Long> assignedOrderIds = new HashSet<>();
        for (RouteState route : solution.routes()) {
            List<PickupOrderNode> uniqueStops = new ArrayList<>();
            for (PickupOrderNode stop : route.stops()) {
                if (stop.orderId() == null || assignedOrderIds.add(stop.orderId())) {
                    uniqueStops.add(stop);
                }
            }
            route.stops().clear();
            route.stops().addAll(uniqueStops);
        }

        List<UnassignedOrderState> filteredUnassigned = new ArrayList<>();
        Set<Long> seenUnassignedIds = new HashSet<>();
        for (UnassignedOrderState unassignedOrder : solution.unassignedOrders()) {
            Long orderId = unassignedOrder.order().orderId();
            if (orderId != null && assignedOrderIds.contains(orderId)) {
                continue;
            }
            if (orderId != null && !seenUnassignedIds.add(orderId)) {
                continue;
            }
            filteredUnassigned.add(unassignedOrder);
        }

        solution.unassignedOrders().clear();
        solution.unassignedOrders().addAll(filteredUnassigned);
    }

    private Long getCurrentTenantIdOrThrow() {
        return authUtils.getCurrentTenantId().orElseThrow(() -> new AppException(ErrorCode.UNAUTHORIZED));
    }

    private boolean isManagerScopedAccess() {
        return isPostOfficerManager() && !isAdmin();
    }

    private boolean isAdmin() {
        return authUtils.hasAnyRole(ROLE_TMS_ADMIN);
    }

    private boolean isPostOfficerManager() {
        return authUtils.hasAnyRole(ROLE_TMS_POSTOFFICER_MANAGER);
    }

    private String buildStaffCode(Long userId, PostOfficeStaffRole role) {
        return "USR_" + userId + "_" + role.name();
    }

    private boolean isValidCoordinate(double latitude, double longitude) {
        return latitude >= -90.0 && latitude <= 90.0
                && longitude >= -180.0 && longitude <= 180.0;
    }

    private long estimateTravelMinutes(double distanceKm, double averageSpeedKmph) {
        if (distanceKm <= EPSILON) {
            return 0;
        }

        double hours = distanceKm / averageSpeedKmph;
        long minutes = Math.round(hours * 60.0);
        return Math.max(minutes, 1L);
    }

    private double distanceKm(double lat1, double lon1, double lat2, double lon2) {
        double lat1Rad = Math.toRadians(lat1);
        double lat2Rad = Math.toRadians(lat2);
        double deltaLat = Math.toRadians(lat2 - lat1);
        double deltaLon = Math.toRadians(lon2 - lon1);

        double a = Math.sin(deltaLat / 2.0) * Math.sin(deltaLat / 2.0)
                + Math.cos(lat1Rad) * Math.cos(lat2Rad)
                * Math.sin(deltaLon / 2.0) * Math.sin(deltaLon / 2.0);

        double c = 2.0 * Math.atan2(Math.sqrt(a), Math.sqrt(1.0 - a));
        return EARTH_RADIUS_KM * c;
    }

    private int resolvePositiveInt(Integer value, int defaultValue) {
        if (value == null || value <= 0) {
            return defaultValue;
        }
        return value;
    }

    private long resolvePositiveLong(Long value, long defaultValue) {
        if (value == null || value <= 0) {
            return defaultValue;
        }
        return value;
    }

    private double resolvePositiveDouble(Double value, double defaultValue) {
        if (value == null || value <= 0.0) {
            return defaultValue;
        }
        return value;
    }

    private double resolveNonNegativeDouble(Double value, double defaultValue) {
        if (value == null || value < 0.0) {
            return defaultValue;
        }
        return value;
    }

    private Integer normalizePositiveInteger(Integer value) {
        if (value == null || value <= 0) {
            return null;
        }
        return value;
    }

    private double safePositive(Double value) {
        if (value == null || value < 0.0) {
            return 0.0;
        }
        return value;
    }

    private double clamp(double value, double min, double max) {
        return Math.max(min, Math.min(max, value));
    }

    private double round3(double value) {
        return Math.round(value * 1000.0) / 1000.0;
    }

    private record ShiftPlanningWindow(
            LocalDate tripDate,
            LocalDateTime planningStartTime,
            LocalDateTime planningEndTime
    ) {
    }

    private record ExistingRouteData(
            Map<Long, List<PickupOrderNode>> routeNodesByTripId,
            List<UnassignedOrderState> invalidOrderStates
    ) {
    }

    private record AssignmentPersistResult(
            List<PickupAssignmentResponse.AssignedTripResponse> tripResponses,
            Set<Long> assignedOrderIds
    ) {
    }

}
