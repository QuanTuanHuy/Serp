/*
Author: Nguyen The Anh
Description: Part of Serp Project
*/

package serp.project.first_mile.service.impl;

import lombok.RequiredArgsConstructor;
import org.locationtech.jts.geom.Point;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.data.domain.PageRequest;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
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
import serp.project.first_mile.enums.PickupShift;
import serp.project.first_mile.enums.RoutingVehicle;
import serp.project.first_mile.enums.TripStatus;
import serp.project.first_mile.enums.VehicleStatus;
import serp.project.first_mile.exception.AppException;
import serp.project.first_mile.exception.ErrorCode;
import serp.project.first_mile.kernel.utils.FirstMileAccessUtils;
import serp.project.first_mile.repository.OrderRepository;
import serp.project.first_mile.repository.PostOfficeRepository;
import serp.project.first_mile.repository.PostOfficeStaffAssignmentRepository;
import serp.project.first_mile.repository.PostOfficeStaffRepository;
import serp.project.first_mile.repository.TripOrderRepository;
import serp.project.first_mile.repository.TripRepository;
import serp.project.first_mile.repository.VehicleRepository;
import serp.project.first_mile.service.PickupOptimizationService;
import serp.project.first_mile.service.dto.*;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Comparator;
import java.util.HashMap;
import java.util.HashSet;
import java.util.LinkedHashMap;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.Objects;
import java.util.Set;
import java.util.UUID;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class PickupOptimizationServiceImpl implements PickupOptimizationService {

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
    private static final boolean DEFAULT_AUTO_ALLOW_LATENESS = false;
    private static final boolean DEFAULT_AUTO_ENFORCE_PLANNING_END = true;
    private static final boolean DEFAULT_ENFORCE_CAPACITY = true;

    private static final double DEFAULT_DISTANCE_WEIGHT = 1.0;
    private static final double DEFAULT_LATENESS_WEIGHT = 0.5;
    private static final double DEFAULT_UNASSIGNED_PENALTY = 500.0;
    private static final double DEFAULT_USED_ROUTE_PENALTY = 3.0;
    private static final int DEFAULT_DISTANCE_MATRIX_BATCH_SIZE = 20;
    private static final int DEFAULT_DISTANCE_MATRIX_MAX_NODES = 120;
    private static final int DEFAULT_TRIP_CODE_RANDOM_LENGTH = 8;

    private static final double DEFAULT_MAX_WEIGHT_IF_MISSING = 100000.0;
    private static final double DEFAULT_MAX_VOLUME_IF_MISSING = 1000.0;

    private static final String REASON_UNASSIGNED = "UNASSIGNED";
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
    private final PickupOptimizationEngine pickupOptimizationEngine;
    private final FirstMileAccessUtils firstMileAccessUtils;

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
            throw invalidRequest("Selected post office has no geocoded location (latitude/longitude). Please geocode the post office before planning.");
        }

        validateManagerScope(postOffice.getId(), tenantId);

        AlgorithmConfig config = buildConfig(request);
        validatePostOfficeOperational(postOffice, config.planningStartTime(), config.planningEndTime());
        double depotLatitude = location.getY();
        double depotLongitude = location.getX();

        List<CourierResource> couriers = loadActiveCouriers(
                postOffice.getId(),
                tenantId,
                request.getCourierIds(),
                config.planningDate(),
                config.planningStartTime().toLocalTime(),
                config.planningEndTime().toLocalTime()
        );
        if (couriers.isEmpty()) {
            throw invalidRequest("No active courier assignment is available for the selected post office and planning window.");
        }

        List<Vehicle> activeVehicles = vehicleRepository.findByTenantIdAndPostOffice_IdAndStatusIn(
                tenantId,
                postOffice.getId(),
                List.of(VehicleStatus.ACTIVE)
        );

        List<RouteState> initialRoutes = initializeRoutes(couriers, activeVehicles, depotLatitude, depotLongitude);
        if (initialRoutes.isEmpty()) {
            throw invalidRequest("No usable route can be initialized. Ensure active vehicles exist and can be mapped to selected couriers.");
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

        PostOffice postOffice = lockPostOfficeAndValidateScope(request.getPostOfficeId(), tenantId);
        validatePostOfficeOperational(
                postOffice,
                shiftPlanningWindow.planningStartTime(),
                shiftPlanningWindow.planningEndTime()
        );
        Point location = postOffice.getLocation();
        if (location == null) {
            throw invalidRequest("Selected post office has no geocoded location (latitude/longitude). Please geocode the post office before auto assign.");
        }

        AlgorithmConfig config = buildConfig(request, shiftPlanningWindow);
        double depotLatitude = location.getY();
        double depotLongitude = location.getX();

        List<CourierResource> couriers = loadActiveCouriers(
                postOffice.getId(),
                tenantId,
                request.getCourierIds(),
                shiftPlanningWindow.tripDate(),
                shiftPlanningWindow.planningStartTime().toLocalTime(),
                shiftPlanningWindow.planningEndTime().toLocalTime()
        );
        if (couriers.isEmpty()) {
            throw invalidRequest("No active courier assignment is available for the selected post office and shift window.");
        }

        List<Vehicle> activeVehicles = vehicleRepository.findByTenantIdAndPostOffice_IdAndStatusIn(
                tenantId,
                postOffice.getId(),
                List.of(VehicleStatus.ACTIVE)
        );

        List<RouteState> routes = initializeRoutes(couriers, activeVehicles, depotLatitude, depotLongitude);
        if (routes.isEmpty()) {
            throw invalidRequest("No usable route can be initialized for auto assign. Ensure active vehicles exist and can be mapped to selected couriers.");
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
        lockOrdersForAssignment(candidateOrders, tenantId);

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
        if (pickupOptimizationEngine.isInfeasible(solutionEvaluation)) {
            throw invalidRequest("Auto assign produced an infeasible solution. Please review planning window, constraints, and selected resources.");
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

        PostOffice postOffice = lockPostOfficeAndValidateScope(request.getPostOfficeId(), tenantId);
        validatePostOfficeOperational(
                postOffice,
                shiftPlanningWindow.planningStartTime(),
                shiftPlanningWindow.planningEndTime()
        );
        Point location = postOffice.getLocation();
        if (location == null) {
            throw invalidRequest("Selected post office has no geocoded location (latitude/longitude). Please geocode the post office before manual assign.");
        }

        PostOfficeStaff courier = postOfficeStaffRepository.findByIdAndTenantId(request.getCourierStaffId(), tenantId)
                .orElseThrow(() -> new AppException(ErrorCode.POST_OFFICE_STAFF_NOT_FOUND));
        validateCourierAssignableForManual(
            courier,
            postOffice.getId(),
            tenantId,
            shiftPlanningWindow.tripDate(),
            shiftPlanningWindow.planningStartTime().toLocalTime(),
            shiftPlanningWindow.planningEndTime().toLocalTime()
        );

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
            throw invalidRequest("No usable route can be initialized for manual assign. Ensure the selected courier has an available active vehicle.");
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
        lockOrderIdsForAssignment(requestedOrderIds, tenantId);

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
        if (pickupOptimizationEngine.isInfeasible(solutionEvaluation)) {
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

            validateActiveTripConflict(route, tripDate, shift, trip.getId(), tenantId);

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

    private void lockOrdersForAssignment(List<Order> orders, Long tenantId) {
        if (orders == null || orders.isEmpty()) {
            return;
        }

        Set<Long> orderIds = new LinkedHashSet<>();
        for (Order order : orders) {
            if (order != null && order.getId() != null) {
                orderIds.add(order.getId());
            }
        }

        lockOrderIdsForAssignment(orderIds, tenantId);
    }

    private void lockOrderIdsForAssignment(Set<Long> orderIds, Long tenantId) {
        if (orderIds == null || orderIds.isEmpty()) {
            return;
        }
        orderRepository.findByTenantIdAndIdInWithLock(tenantId, orderIds);
    }

    private void validateActiveTripConflict(
            RouteState route,
            LocalDate tripDate,
            PickupShift shift,
            Long excludeTripId,
            Long tenantId
    ) {
        boolean courierConflict = tripRepository.existsActiveTripByCourierAndShift(
                tenantId,
                tripDate,
                shift,
                route.courierStaffId(),
                ACTIVE_ASSIGNMENT_TRIP_STATUSES,
                excludeTripId
        );
        if (courierConflict) {
            throw invalidRequest("Courier already has another active trip in the same shift and trip date.");
        }

        if (route.vehicleId() == null) {
            throw invalidRequest("Route has no assigned vehicle. A valid active vehicle is required for assignment.");
        }

        boolean vehicleConflict = tripRepository.existsActiveTripByVehicleAndShift(
                tenantId,
                tripDate,
                shift,
                route.vehicleId(),
                ACTIVE_ASSIGNMENT_TRIP_STATUSES,
                excludeTripId
        );
        if (vehicleConflict) {
            throw invalidRequest("Vehicle already has another active trip in the same shift and trip date.");
        }
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
        if (postOfficeCode == null
                || originPostOfficeCode == null
                || !postOfficeCode.equalsIgnoreCase(originPostOfficeCode)) {
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
            LocalDate planningDate,
            LocalTime planningStartTime,
            LocalTime planningEndTime
    ) {
        if (!PostOfficeStaffRole.COURIER.equals(courier.getRole())
                || !PostOfficeStaffStatus.ACTIVE.equals(courier.getStatus())) {
            throw new AppException(ErrorCode.COURIER_NOT_ASSIGNED_TO_POST_OFFICE);
        }

        List<PostOfficeStaffAssignment> assignments = postOfficeStaffAssignmentRepository
                .findActiveAssignmentsByStaffIdAndPostOfficeIdAndTenantId(
                courier.getId(),
                postOfficeId,
                tenantId,
                planningDate
            );
        boolean assigned = assignments.stream()
                .anyMatch(assignment -> isAssignmentUsableForWindow(assignment, planningStartTime, planningEndTime));
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
            throw invalidRequest("order_ids must contain at least one positive order id.");
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
            throw invalidRequest("planning_end_time must be after or equal to planning_start_time.");
        }

        if (!effectivePlanningStartTime.toLocalDate().equals(effectiveTripDate)
                || !effectivePlanningEndTime.toLocalDate().equals(effectiveTripDate)) {
            throw invalidRequest("planning_start_time and planning_end_time must be on the same date as trip_date.");
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

        boolean allowLateness = request.getAllowLateness() == null
                ? DEFAULT_AUTO_ALLOW_LATENESS
                : request.getAllowLateness();
        boolean enforcePlanningEnd = request.getEnforcePlanningEnd() == null
                ? DEFAULT_AUTO_ENFORCE_PLANNING_END
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

    private PostOffice lockPostOfficeAndValidateScope(Long postOfficeId, Long tenantId) {
        PostOffice postOffice = postOfficeRepository.findByIdAndTenantIdForUpdate(postOfficeId, tenantId)
                .orElseThrow(() -> new AppException(ErrorCode.POST_OFFICE_NOT_FOUND));
        validateManagerScope(postOffice.getId(), tenantId);
        return postOffice;
    }

    private void validatePostOfficeOperational(
            PostOffice postOffice,
            LocalDateTime planningStartTime,
            LocalDateTime planningEndTime
    ) {
        if (postOffice == null || !postOffice.isActive()) {
            throw invalidRequest("Selected post office is not active for dispatch planning.");
        }

        LocalTime workingStartTime = postOffice.getWorkingStartTime();
        LocalTime workingEndTime = postOffice.getWorkingEndTime();
        if (workingStartTime == null || workingEndTime == null || !workingEndTime.isAfter(workingStartTime)) {
            throw invalidRequest("Post office working hours are not configured correctly.");
        }

        LocalTime planningStartLocalTime = planningStartTime.toLocalTime();
        LocalTime planningEndLocalTime = planningEndTime.toLocalTime();
        if (!isWithinTimeRange(planningStartLocalTime, workingStartTime, workingEndTime)
                || !isWithinTimeRange(planningEndLocalTime, workingStartTime, workingEndTime)) {
            throw invalidRequest("Planning window is outside configured post office working hours.");
        }
    }

    private boolean isWithinTimeRange(LocalTime value, LocalTime startInclusive, LocalTime endInclusive) {
        if (value == null || startInclusive == null || endInclusive == null) {
            return false;
        }
        return !value.isBefore(startInclusive) && !value.isAfter(endInclusive);
    }

    private boolean isAssignmentUsableForWindow(
            PostOfficeStaffAssignment assignment,
            LocalTime planningStartTime,
            LocalTime planningEndTime
    ) {
        if (assignment == null) {
            return false;
        }

        LocalTime shiftStartTime = assignment.getShiftStartTime();
        LocalTime shiftEndTime = assignment.getShiftEndTime();
        if (shiftStartTime == null && shiftEndTime == null) {
            return true;
        }
        if (shiftStartTime == null || shiftEndTime == null || !shiftEndTime.isAfter(shiftStartTime)) {
            return false;
        }

        return !planningStartTime.isBefore(shiftStartTime) && !planningEndTime.isAfter(shiftEndTime);
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
        return pickupOptimizationEngine.buildInitialSolution(initialRoutes, preparedOrderData, config);
    }

    private SolutionState runAlns(SolutionState initialSolution, AlgorithmConfig config) {
        return pickupOptimizationEngine.runAlns(initialSolution, config);
    }

    private void applyGreedyRepair(SolutionState solution, AlgorithmConfig config) {
        pickupOptimizationEngine.applyGreedyRepair(solution, config);
    }

    private void markNoFeasibleUnassigned(SolutionState solution) {
        pickupOptimizationEngine.markNoFeasibleUnassigned(solution);
    }

    private SolutionEvaluation evaluateSolution(SolutionState solution, AlgorithmConfig config) {
        return pickupOptimizationEngine.evaluateSolution(solution, config);
    }

    private PreparedOrderData prepareOrders(List<Order> candidateOrders) {
        return pickupOptimizationEngine.prepareOrders(candidateOrders);
    }

    private PickupOrderNode toOrderNodeWithoutLocation(Order order) {
        return pickupOptimizationEngine.toOrderNodeWithoutLocation(order);
    }

    private TravelMetricProvider buildTravelMetricProvider(
            List<PickupOrderNode> assignableOrders,
            double depotLatitude,
            double depotLongitude,
            AlgorithmConfig config
    ) {
        return pickupOptimizationEngine.buildTravelMetricProvider(assignableOrders, depotLatitude, depotLongitude, config);
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

            if (selectedVehicle == null) {
                continue;
            }

            Long vehicleId = selectedVehicle.getId();
            String licensePlate = selectedVehicle.getLicensePlate();
            double maxWeight = resolvePositiveDouble(selectedVehicle.getMaxWeight(), DEFAULT_MAX_WEIGHT_IF_MISSING);
            double maxVolume = resolvePositiveDouble(selectedVehicle.getMaxVolume(), DEFAULT_MAX_VOLUME_IF_MISSING);

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
            LocalDate planningDate,
            LocalTime planningStartTime,
            LocalTime planningEndTime
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

            if (!isAssignmentUsableForWindow(assignment, planningStartTime, planningEndTime)) {
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
            throw invalidRequest("planning_end_time must be after or equal to planning_start_time.");
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

        firstMileAccessUtils.ensureCurrentManagerAssignedToPostOfficeOrThrow(postOfficeId, tenantId);
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
        pickupOptimizationEngine.sanitizeSolution(solution);
    }

    private Long getCurrentTenantIdOrThrow() {
        return firstMileAccessUtils.getCurrentTenantIdOrThrow();
    }

    private boolean isManagerScopedAccess() {
        return firstMileAccessUtils.isManagerScopedAccess();
    }

    private AppException invalidRequest(String detail) {
        return new AppException(ErrorCode.INVALID_REQUEST, detail);
    }

    private boolean isValidCoordinate(double latitude, double longitude) {
        return latitude >= -90.0 && latitude <= 90.0
                && longitude >= -180.0 && longitude <= 180.0;
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

}
