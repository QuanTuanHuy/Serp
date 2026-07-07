/*
Author: Nguyen The Anh
Description: Part of Serp Project
*/

package serp.project.first_mile.service.impl;

import lombok.RequiredArgsConstructor;
import org.locationtech.jts.geom.Point;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import serp.project.first_mile.caller.TmsOrderClient;
import serp.project.first_mile.caller.dto.tms_order.TmsOrderOperationView;
import serp.project.first_mile.caller.dto.tms_order.TmsOrderStatusTransitionRequest;
import serp.project.first_mile.domain.PostOffice;
import serp.project.first_mile.domain.PostOfficeStaff;
import serp.project.first_mile.domain.PostOfficeStaffAssignment;
import serp.project.first_mile.domain.Trip;
import serp.project.first_mile.domain.TripOrder;
import serp.project.first_mile.domain.Vehicle;
import serp.project.first_mile.dto.request.AutoAssignDeliveryPlanRequest;
import serp.project.first_mile.dto.request.ScanOutDeliveryOrderRequest;
import serp.project.first_mile.dto.response.DeliveryAssignmentResponse;
import serp.project.first_mile.dto.response.DeliveryScanOutResponse;
import serp.project.first_mile.enums.DeliveryOrderStatus;
import serp.project.first_mile.enums.OrderStatus;
import serp.project.first_mile.enums.PickupOptimizationGoal;
import serp.project.first_mile.enums.PickupShift;
import serp.project.first_mile.enums.PostOfficeStaffRole;
import serp.project.first_mile.enums.PostOfficeStaffStatus;
import serp.project.first_mile.enums.RoutingVehicle;
import serp.project.first_mile.enums.TripStatus;
import serp.project.first_mile.enums.TripType;
import serp.project.first_mile.enums.VehicleStatus;
import serp.project.first_mile.exception.AppException;
import serp.project.first_mile.exception.ErrorCode;
import serp.project.first_mile.kernel.utils.FirstMileAccessUtils;
import serp.project.first_mile.repository.PostOfficeRepository;
import serp.project.first_mile.repository.PostOfficeStaffAssignmentRepository;
import serp.project.first_mile.repository.PostOfficeStaffRepository;
import serp.project.first_mile.repository.DeliveryManifestOrderRepository;
import serp.project.first_mile.repository.TripOrderRepository;
import serp.project.first_mile.repository.TripRepository;
import serp.project.first_mile.repository.VehicleRepository;
import serp.project.first_mile.service.DeliveryDispatchService;
import serp.project.first_mile.service.TmsOrderTransitionPublisherService;
import serp.project.first_mile.service.dto.AlgorithmConfig;
import serp.project.first_mile.service.dto.CourierResource;
import serp.project.first_mile.service.dto.PickupOrderNode;
import serp.project.first_mile.service.dto.PreparedOrderData;
import serp.project.first_mile.service.dto.RouteEvaluation;
import serp.project.first_mile.service.dto.RouteState;
import serp.project.first_mile.service.dto.ShiftPlanningWindow;
import serp.project.first_mile.service.dto.SolutionEvaluation;
import serp.project.first_mile.service.dto.SolutionState;
import serp.project.first_mile.service.dto.StopEvaluationData;
import serp.project.first_mile.service.dto.TravelMetricProvider;
import serp.project.first_mile.service.dto.UnassignedOrderState;

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
import java.util.function.Function;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class DeliveryDispatchServiceImpl implements DeliveryDispatchService {

    private static final int DEFAULT_ORDER_LIMIT = 300;
    private static final double DEFAULT_AVERAGE_SPEED_KMPH = 25.0;
    private static final int DEFAULT_SERVICE_MINUTES_PER_STOP = 8;
    private static final boolean DEFAULT_ALLOW_LATENESS = false;
    private static final boolean DEFAULT_ENFORCE_PLANNING_END = true;
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
    private static final double GRAMS_PER_KILOGRAM = 1000.0;

    private static final String REASON_UNASSIGNED = "UNASSIGNED";
    private static final String REASON_MISSING_RECEIVER_LOCATION = "MISSING_RECEIVER_LOCATION";
    private static final String REASON_INVALID_RECEIVER_LOCATION = "INVALID_RECEIVER_LOCATION";
    private static final String REASON_ALREADY_ASSIGNED_TO_ACTIVE_TRIP = "ALREADY_ASSIGNED_TO_ACTIVE_DELIVERY_TRIP";
    private static final String REASON_ALREADY_ASSIGNED_TO_ACTIVE_MANIFEST =
            "ALREADY_ASSIGNED_TO_ACTIVE_DELIVERY_MANIFEST";
    private static final String REASON_ORDER_NOT_ASSIGNABLE = "ORDER_NOT_ASSIGNABLE";
    private static final String TRANSITION_SOURCE = "LAST_MILE_DELIVERY_DISPATCH";

    private static final LocalTime SHIFT_MORNING_START = LocalTime.of(7, 30);
    private static final LocalTime SHIFT_MORNING_END = LocalTime.of(12, 0);
    private static final LocalTime SHIFT_AFTERNOON_START = LocalTime.of(13, 30);
    private static final LocalTime SHIFT_AFTERNOON_END = LocalTime.of(18, 0);
    private static final LocalTime SHIFT_EVENING_START = LocalTime.of(18, 30);
    private static final LocalTime SHIFT_EVENING_END = LocalTime.of(22, 0);

    private static final List<TripStatus> ACTIVE_DELIVERY_TRIP_STATUSES = List.of(
            TripStatus.PLANNED,
            TripStatus.IN_PROGRESS
    );
    private static final List<TripStatus> REPLANNABLE_DELIVERY_TRIP_STATUSES = List.of(TripStatus.PLANNED);
    private static final List<TripStatus> VISIBLE_DELIVERY_TRIP_STATUSES = List.of(
            TripStatus.PLANNED,
            TripStatus.IN_PROGRESS,
            TripStatus.COMPLETED
    );
    private static final List<OrderStatus> DEFAULT_DELIVERY_CANDIDATE_STATUSES =
            List.of(OrderStatus.READY_FOR_DELIVERY);

    private final TmsOrderClient tmsOrderClient;
    private final PostOfficeRepository postOfficeRepository;
    private final PostOfficeStaffRepository postOfficeStaffRepository;
    private final PostOfficeStaffAssignmentRepository postOfficeStaffAssignmentRepository;
    private final VehicleRepository vehicleRepository;
    private final DeliveryManifestOrderRepository deliveryManifestOrderRepository;
    private final TripRepository tripRepository;
    private final TripOrderRepository tripOrderRepository;
    private final PickupOptimizationEngine pickupOptimizationEngine;
    private final FirstMileAccessUtils firstMileAccessUtils;
    private final TmsOrderTransitionPublisherService tmsOrderTransitionPublisherService;

    @Value("${distance-matrix.batch-size:20}")
    private Integer distanceMatrixBatchSize;

    @Value("${distance-matrix.max-nodes:120}")
    private Integer distanceMatrixMaxNodes;

    @Override
    @Transactional(rollbackFor = Exception.class)
    public DeliveryAssignmentResponse autoAssignDeliveryPlan(AutoAssignDeliveryPlanRequest request) {
        Long tenantId = firstMileAccessUtils.getCurrentTenantIdOrThrow();
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
        Point depotLocation = postOffice.getLocation();
        if (depotLocation == null) {
            throw invalidRequest("Selected post office has no geocoded location.");
        }

        AlgorithmConfig config = buildConfig(request, shiftPlanningWindow);
        List<CourierResource> couriers = loadActiveCouriers(
                postOffice.getId(),
                tenantId,
                request.getCourierIds(),
                shiftPlanningWindow.tripDate()
        );
        if (couriers.isEmpty()) {
            throw invalidRequest("No active courier assignment is available for the selected post office and trip date.");
        }

        List<Vehicle> activeVehicles = vehicleRepository.findByTenantIdAndPostOffice_IdAndStatusIn(
                tenantId,
                postOffice.getId(),
                List.of(VehicleStatus.ACTIVE)
        );
        List<RouteState> routes = initializeRoutes(couriers, activeVehicles, depotLocation.getY(), depotLocation.getX());
        if (routes.isEmpty()) {
            throw invalidRequest("No usable route can be initialized for delivery dispatch.");
        }

        Map<Long, Trip> existingTripByCourier = loadReplannableTripsByCourier(
                tenantId,
                postOffice.getId(),
                shiftPlanningWindow.tripDate(),
                request.getShift(),
                routes
        );
        ExistingDeliveryRouteData existingRouteData = loadExistingRouteData(existingTripByCourier, tenantId);
        applyExistingRouteData(routes, existingTripByCourier, existingRouteData);

        List<TmsOrderOperationView> candidateOrders = loadCandidateDeliveryOrders(postOffice, request, config, tenantId);
        validateCandidateOrders(candidateOrders, postOffice.getCode(), resolveCandidateStatuses(request.getCandidateStatuses()));

        PreparedOrderData preparedOrderData = prepareDeliveryOrders(candidateOrders);
        Set<Long> existingRouteOrderIds = extractOrderIds(existingRouteData.routeNodesByTripId().values());
        PreparedOrderData filteredPreparedOrderData =
                excludeOrdersAlreadyAssigned(preparedOrderData, tenantId, existingRouteOrderIds);

        List<PickupOrderNode> matrixNodes = new ArrayList<>();
        for (List<PickupOrderNode> routeNodes : existingRouteData.routeNodesByTripId().values()) {
            matrixNodes.addAll(routeNodes);
        }
        matrixNodes.addAll(filteredPreparedOrderData.assignableOrders());

        TravelMetricProvider travelMetricProvider = pickupOptimizationEngine.buildTravelMetricProvider(
                matrixNodes,
                depotLocation.getY(),
                depotLocation.getX(),
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
        pickupOptimizationEngine.applyGreedyRepair(solution, runtimeConfig);
        pickupOptimizationEngine.markNoFeasibleUnassigned(solution);
        pickupOptimizationEngine.sanitizeSolution(solution);

        SolutionEvaluation evaluation = pickupOptimizationEngine.evaluateSolution(solution, runtimeConfig);
        if (pickupOptimizationEngine.isInfeasible(evaluation)) {
            throw invalidRequest("Delivery dispatch produced an infeasible solution.");
        }

        DeliveryPersistResult persistResult = persistAssignments(
                postOffice,
                request.getShift(),
                shiftPlanningWindow.tripDate(),
                solution,
                evaluation,
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
    public DeliveryAssignmentResponse getDeliveryTrips(Long postOfficeId, PickupShift shift, LocalDate tripDate) {
        Long tenantId = firstMileAccessUtils.getCurrentTenantIdOrThrow();
        LocalDate effectiveTripDate = tripDate == null ? LocalDate.now() : tripDate;
        List<Trip> trips = tripRepository.findByTenantIdAndTripTypeAndTripDateAndStatusInOrderByPlannedStartTimeAscIdAsc(
                tenantId,
                TripType.DELIVERY,
                effectiveTripDate,
                VISIBLE_DELIVERY_TRIP_STATUSES
        );

        List<Trip> scopedTrips = trips.stream()
                .filter(trip -> shift == null || shift.equals(trip.getShift()))
                .filter(trip -> postOfficeId == null || Objects.equals(postOfficeId, trip.getPostOfficeId()))
                .filter(trip -> canViewTrip(tenantId, trip))
                .toList();

        PostOffice postOffice = resolveSinglePostOfficeForResponse(tenantId, postOfficeId, scopedTrips);
        List<DeliveryAssignmentResponse.DeliveryTripResponse> tripResponses =
                buildTripResponses(scopedTrips, tenantId);

        int assignedOrders = tripResponses.stream()
                .mapToInt(DeliveryAssignmentResponse.DeliveryTripResponse::totalStops)
                .sum();
        return new DeliveryAssignmentResponse(
                postOffice == null ? postOfficeId : postOffice.getId(),
                postOffice == null ? null : postOffice.getCode(),
                postOffice == null ? null : postOffice.getName(),
                shift,
                effectiveTripDate,
                assignedOrders,
                assignedOrders,
                0,
                0,
                tripResponses,
                List.of()
        );
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public DeliveryScanOutResponse scanOutDeliveryOrder(Long tripId, ScanOutDeliveryOrderRequest request) {
        Long tenantId = firstMileAccessUtils.getCurrentTenantIdOrThrow();
        Trip trip = tripRepository.findByIdAndTenantIdForUpdate(tripId, tenantId)
                .filter(candidate -> TripType.DELIVERY.equals(candidate.getTripType()))
                .orElseThrow(() -> new AppException(ErrorCode.INVALID_REQUEST, "Delivery trip not found."));
        ensureCanOperateTrip(tenantId, trip);
        if (!ACTIVE_DELIVERY_TRIP_STATUSES.contains(trip.getStatus())) {
            throw new AppException(ErrorCode.INVALID_REQUEST, "Delivery trip is not active.");
        }

        String orderCode = normalizeText(request == null ? null : request.getOrderCode());
        if (orderCode == null) {
            throw new AppException(ErrorCode.INVALID_REQUEST);
        }

        TmsOrderOperationView order = tmsOrderClient.lookupByCodes(tenantId, List.of(orderCode)).stream()
                .findFirst()
                .orElseThrow(() -> new AppException(ErrorCode.ORDER_NOT_FOUND));
        TripOrder tripOrder = tripOrderRepository.findByTenantIdAndTrip_IdAndOrderId(tenantId, trip.getId(), order.getId())
                .orElseThrow(() -> new AppException(
                        ErrorCode.INVALID_REQUEST,
                        "Order is not assigned to this delivery trip."
                ));

        if (!List.of(OrderStatus.READY_FOR_DELIVERY, OrderStatus.DELIVERY_FAILED, OrderStatus.OUT_FOR_DELIVERY)
                .contains(order.getStatus())) {
            throw new AppException(ErrorCode.ORDER_NOT_ASSIGNABLE);
        }

        LocalDateTime now = LocalDateTime.now();
        if (tripOrder.getScanOutTime() == null) {
            tripOrder.setScanOutTime(now);
            tripOrderRepository.save(tripOrder);
        }
        if (TripStatus.PLANNED.equals(trip.getStatus())) {
            trip.setStatus(TripStatus.IN_PROGRESS);
            tripRepository.save(trip);
        }

        DeliveryContext context = resolveDeliveryContext(trip, tenantId);
        enqueueTransitions(List.of(toTransitionItem(
                order,
                List.of(OrderStatus.READY_FOR_DELIVERY, OrderStatus.DELIVERY_FAILED, OrderStatus.OUT_FOR_DELIVERY),
                OrderStatus.OUT_FOR_DELIVERY,
                "Đơn hàng đã được quét xuất để giao.",
                trip,
                context,
                now
        )), tenantId);

        return new DeliveryScanOutResponse(
                trip.getId(),
                trip.getTripCode(),
                trip.getStatus(),
                tripOrder.getId(),
                order.getId(),
                order.getOrderCode(),
                order.getCustomerOrderCode(),
                OrderStatus.OUT_FOR_DELIVERY,
                tripOrder.getSequenceNo(),
                trip.getCourierStaffId(),
                context.courierCode(),
                context.courierName(),
                trip.getPostOfficeId(),
                context.postOfficeCode(),
                context.postOfficeName(),
                trip.getVehicleId(),
                context.vehicleLicensePlate(),
                tripOrder.getScanOutTime()
        );
    }

    private DeliveryPersistResult persistAssignments(
            PostOffice postOffice,
            PickupShift shift,
            LocalDate tripDate,
            SolutionState solution,
            SolutionEvaluation solutionEvaluation,
            Map<Long, Trip> existingTripByCourier,
            Long tenantId
    ) {
        Set<Long> assignedOrderIds = extractAssignedOrderIds(solution.routes());
        int createdTrips = 0;
        List<DeliveryAssignmentResponse.DeliveryTripResponse> tripResponses = new ArrayList<>();

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

            boolean created = false;
            if (trip == null) {
                trip = new Trip();
                trip.setTripCode(generateTripCode(tripDate, shift, route.courierCode()));
                created = true;
            } else if (trip.getTripCode() == null || trip.getTripCode().isBlank()) {
                trip.setTripCode(generateTripCode(tripDate, shift, route.courierCode()));
            }

            validateActiveTripConflict(route, tripDate, shift, trip.getId(), tenantId);

            trip.setTenantId(tenantId);
            trip.setTripType(TripType.DELIVERY);
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

            if (created) {
                createdTrips += 1;
            }
            tripResponses.add(toAssignedTripResponse(savedTrip, route, routeEvaluation));
        }

        return new DeliveryPersistResult(tripResponses, assignedOrderIds, createdTrips);
    }

    private List<DeliveryAssignmentResponse.DeliveryTripResponse> buildTripResponses(
            List<Trip> trips,
            Long tenantId
    ) {
        if (trips == null || trips.isEmpty()) {
            return List.of();
        }

        List<Long> tripIds = trips.stream().map(Trip::getId).filter(Objects::nonNull).toList();
        List<TripOrder> tripOrders = tripOrderRepository
                .findByTenantIdAndTripIdInOrderByTripIdAscSequenceNoAsc(tenantId, tripIds);
        Map<Long, List<TripOrder>> tripOrdersByTripId = tripOrders.stream()
                .filter(tripOrder -> tripOrder.getTrip() != null && tripOrder.getTrip().getId() != null)
                .collect(Collectors.groupingBy(tripOrder -> tripOrder.getTrip().getId(), LinkedHashMap::new, Collectors.toList()));

        List<Long> orderIds = tripOrders.stream()
                .map(TripOrder::getOrderId)
                .filter(Objects::nonNull)
                .distinct()
                .toList();
        Map<Long, TmsOrderOperationView> orderById = orderIds.isEmpty()
                ? Map.of()
                : tmsOrderClient.lookupByIds(tenantId, orderIds).stream()
                .collect(Collectors.toMap(TmsOrderOperationView::getId, Function.identity()));

        Map<Long, PostOfficeStaff> courierById = loadCourierById(tenantId, trips);
        Map<Long, String> vehicleLicensePlateById = loadVehicleLicensePlateById(tenantId, trips);
        List<DeliveryAssignmentResponse.DeliveryTripResponse> responses = new ArrayList<>();
        for (Trip trip : trips) {
            PostOfficeStaff courier = courierById.get(trip.getCourierStaffId());
            List<TripOrder> currentTripOrders = tripOrdersByTripId.getOrDefault(trip.getId(), List.of());
            List<DeliveryAssignmentResponse.DeliveryStopResponse> stops = new ArrayList<>();
            for (TripOrder tripOrder : currentTripOrders) {
                TmsOrderOperationView order = orderById.get(tripOrder.getOrderId());
                stops.add(toDeliveryStopResponse(tripOrder, order));
            }

            responses.add(new DeliveryAssignmentResponse.DeliveryTripResponse(
                    trip.getId(),
                    trip.getTripCode(),
                    trip.getCourierStaffId(),
                    courier == null ? null : courier.getCode(),
                    courier == null ? null : courier.getFullName(),
                    trip.getVehicleId(),
                    vehicleLicensePlateById.get(trip.getVehicleId()),
                    stops.size(),
                    round3(trip.getTotalDistanceKm()),
                    trip.getTotalTravelMinutes(),
                    null,
                    null,
                    trip.getPlannedStartTime(),
                    trip.getPlannedEndTime(),
                    stops
            ));
        }
        return responses;
    }

    private DeliveryAssignmentResponse buildAssignmentResponse(
            PostOffice postOffice,
            PickupShift shift,
            LocalDate tripDate,
            int totalRequestedOrders,
            Set<Long> scopedOrderIds,
            DeliveryPersistResult persistResult,
            List<UnassignedOrderState> unassignedOrderStates
    ) {
        List<DeliveryAssignmentResponse.UnassignedDeliveryOrderResponse> unassignedResponses =
                buildUnassignedResponses(unassignedOrderStates, scopedOrderIds);
        int assignedOrders = countMatchedOrderIds(persistResult.assignedOrderIds(), scopedOrderIds);
        return new DeliveryAssignmentResponse(
                postOffice.getId(),
                postOffice.getCode(),
                postOffice.getName(),
                shift,
                tripDate,
                totalRequestedOrders,
                assignedOrders,
                unassignedResponses.size(),
                persistResult.createdTrips(),
                persistResult.tripResponses(),
                unassignedResponses
        );
    }

    private DeliveryAssignmentResponse.DeliveryTripResponse toAssignedTripResponse(
            Trip trip,
            RouteState route,
            RouteEvaluation routeEvaluation
    ) {
        List<DeliveryAssignmentResponse.DeliveryStopResponse> stopResponses = new ArrayList<>();
        for (StopEvaluationData stop : routeEvaluation.stopDetails()) {
            PickupOrderNode order = stop.order();
            stopResponses.add(new DeliveryAssignmentResponse.DeliveryStopResponse(
                    stop.sequence(),
                    order.orderId(),
                    order.orderCode(),
                    order.customerOrderCode(),
                    order.senderName(),
                    order.senderPhone(),
                    order.latitude(),
                    order.longitude(),
                    stop.arrivalTime(),
                    stop.startServiceTime(),
                    stop.departureTime(),
                    round3(stop.distanceFromPreviousKm()),
                    stop.travelMinutes(),
                    stop.latenessMinutes(),
                    null
            ));
        }

        return new DeliveryAssignmentResponse.DeliveryTripResponse(
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

    private DeliveryAssignmentResponse.DeliveryStopResponse toDeliveryStopResponse(
            TripOrder tripOrder,
            TmsOrderOperationView order
    ) {
        return new DeliveryAssignmentResponse.DeliveryStopResponse(
                tripOrder.getSequenceNo(),
                tripOrder.getOrderId(),
                order == null ? null : order.getOrderCode(),
                order == null ? null : order.getCustomerOrderCode(),
                order == null ? null : order.getReceiverName(),
                order == null ? null : order.getReceiverPhone(),
                order == null ? null : order.getReceiverLatitude(),
                order == null ? null : order.getReceiverLongitude(),
                tripOrder.getPlannedArrivalTime(),
                tripOrder.getPlannedStartServiceTime(),
                tripOrder.getPlannedDepartureTime(),
                tripOrder.getDistanceFromPreviousKm(),
                tripOrder.getTravelMinutes(),
                tripOrder.getLatenessMinutes(),
                tripOrder.getScanOutTime()
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

    private List<TmsOrderOperationView> loadCandidateDeliveryOrders(
            PostOffice postOffice,
            AutoAssignDeliveryPlanRequest request,
            AlgorithmConfig config,
            Long tenantId
    ) {
        List<String> orderCodes = normalizeOrderCodes(request.getOrderCodes());
        if (!orderCodes.isEmpty()) {
            return tmsOrderClient.lookupByCodes(tenantId, orderCodes);
        }

        List<TmsOrderOperationView> orders = tmsOrderClient.lookupAtPostOffice(
                postOffice.getCode(),
                resolveCandidateStatuses(request.getCandidateStatuses()),
                tenantId
        );
        if (orders.size() <= config.orderLimit()) {
            return orders;
        }
        return orders.stream()
                .limit(config.orderLimit())
                .toList();
    }

    private PreparedOrderData prepareDeliveryOrders(List<TmsOrderOperationView> candidateOrders) {
        List<PickupOrderNode> assignableOrders = new ArrayList<>();
        List<UnassignedOrderState> unassignedOrders = new ArrayList<>();

        for (TmsOrderOperationView order : candidateOrders) {
            PickupOrderNode orderNode = toDeliveryOrderNode(order);
            if (orderNode == null) {
                unassignedOrders.add(new UnassignedOrderState(
                        toDeliveryOrderNodeWithoutLocation(order),
                        missingReceiverLocationReason(order),
                        false
                ));
                continue;
            }
            assignableOrders.add(orderNode);
        }

        assignableOrders.sort(Comparator
                .comparing(PickupOrderNode::orderId, Comparator.nullsLast(Comparator.naturalOrder()))
                .thenComparing(PickupOrderNode::customerOrderCode, Comparator.nullsLast(String::compareToIgnoreCase))
        );
        return new PreparedOrderData(assignableOrders, unassignedOrders);
    }

    private PreparedOrderData excludeOrdersAlreadyAssigned(
            PreparedOrderData preparedOrderData,
            Long tenantId,
            Set<Long> existingRouteOrderIds
    ) {
        List<PickupOrderNode> assignableOrders = new ArrayList<>();
        List<UnassignedOrderState> unassignedOrders = new ArrayList<>();
        for (UnassignedOrderState initialUnassignedOrder : preparedOrderData.initialUnassignedOrders()) {
            unassignedOrders.add(initialUnassignedOrder.copy());
        }

        for (PickupOrderNode order : preparedOrderData.assignableOrders()) {
            if (order.orderId() == null) {
                unassignedOrders.add(new UnassignedOrderState(order, REASON_ORDER_NOT_ASSIGNABLE, false));
                continue;
            }
            if (existingRouteOrderIds != null && existingRouteOrderIds.contains(order.orderId())) {
                continue;
            }
            if (isAssignedToActiveManifest(order, tenantId)) {
                unassignedOrders.add(new UnassignedOrderState(order, REASON_ALREADY_ASSIGNED_TO_ACTIVE_MANIFEST, false));
                continue;
            }
            boolean assigned = tripOrderRepository.existsByTenantIdAndOrderIdAndTripStatusIn(
                    tenantId,
                    order.orderId(),
                    TripType.DELIVERY,
                    ACTIVE_DELIVERY_TRIP_STATUSES,
                    null
            );
            if (assigned) {
                unassignedOrders.add(new UnassignedOrderState(order, REASON_ALREADY_ASSIGNED_TO_ACTIVE_TRIP, false));
                continue;
            }
            assignableOrders.add(order);
        }
        return new PreparedOrderData(assignableOrders, unassignedOrders);
    }

    private boolean isAssignedToActiveManifest(PickupOrderNode order, Long tenantId) {
        return order.orderCode() != null
                && !deliveryManifestOrderRepository.findByTenantIdAndOrderCodeAndStatusIn(
                        tenantId,
                        order.orderCode(),
                        List.of(DeliveryOrderStatus.PENDING, DeliveryOrderStatus.OUT_FOR_DELIVERY)
                ).isEmpty();
    }

    private ExistingDeliveryRouteData loadExistingRouteData(Map<Long, Trip> existingTripByCourier, Long tenantId) {
        Map<Long, List<PickupOrderNode>> routeNodesByTripId = new HashMap<>();
        List<UnassignedOrderState> invalidOrderStates = new ArrayList<>();
        if (existingTripByCourier == null || existingTripByCourier.isEmpty()) {
            return new ExistingDeliveryRouteData(routeNodesByTripId, invalidOrderStates);
        }

        Map<Long, List<TripOrder>> tripOrdersByTripId = new HashMap<>();
        LinkedHashSet<Long> orderIds = new LinkedHashSet<>();
        for (Trip trip : existingTripByCourier.values()) {
            if (trip == null || trip.getId() == null) {
                continue;
            }
            List<TripOrder> tripOrders = tripOrderRepository.findByTrip_IdOrderBySequenceNoAsc(trip.getId());
            tripOrdersByTripId.put(trip.getId(), tripOrders);
            for (TripOrder tripOrder : tripOrders) {
                if (tripOrder.getOrderId() != null) {
                    orderIds.add(tripOrder.getOrderId());
                }
            }
        }

        Map<Long, TmsOrderOperationView> orderById = orderIds.isEmpty()
                ? Map.of()
                : tmsOrderClient.lookupByIds(tenantId, orderIds).stream()
                .collect(Collectors.toMap(TmsOrderOperationView::getId, Function.identity()));

        for (Map.Entry<Long, List<TripOrder>> entry : tripOrdersByTripId.entrySet()) {
            List<PickupOrderNode> routeNodes = new ArrayList<>();
            for (TripOrder tripOrder : entry.getValue()) {
                TmsOrderOperationView order = orderById.get(tripOrder.getOrderId());
                if (order == null) {
                    continue;
                }
                PickupOrderNode orderNode = toDeliveryOrderNode(order);
                if (orderNode == null) {
                    invalidOrderStates.add(new UnassignedOrderState(
                            toDeliveryOrderNodeWithoutLocation(order),
                            missingReceiverLocationReason(order),
                            false
                    ));
                    continue;
                }
                routeNodes.add(orderNode);
            }
            routeNodesByTripId.put(entry.getKey(), routeNodes);
        }
        return new ExistingDeliveryRouteData(routeNodesByTripId, invalidOrderStates);
    }

    private void applyExistingRouteData(
            List<RouteState> routes,
            Map<Long, Trip> existingTripByCourier,
            ExistingDeliveryRouteData existingRouteData
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

    private PickupOrderNode toDeliveryOrderNode(TmsOrderOperationView order) {
        if (order == null || order.getReceiverLatitude() == null || order.getReceiverLongitude() == null) {
            return null;
        }
        double latitude = order.getReceiverLatitude();
        double longitude = order.getReceiverLongitude();
        if (!isValidCoordinate(latitude, longitude)) {
            return null;
        }

        return new PickupOrderNode(
                order.getId(),
                order.getOrderCode(),
                order.getCustomerOrderCode(),
                order.getReceiverName(),
                order.getReceiverPhone(),
                latitude,
                longitude,
                normalizeOrderWeightKg(order.getTotalWeight()),
                safePositive(order.getTotalVolume()),
                null,
                null
        );
    }

    private PickupOrderNode toDeliveryOrderNodeWithoutLocation(TmsOrderOperationView order) {
        return new PickupOrderNode(
                order == null ? null : order.getId(),
                order == null ? null : order.getOrderCode(),
                order == null ? null : order.getCustomerOrderCode(),
                order == null ? null : order.getReceiverName(),
                order == null ? null : order.getReceiverPhone(),
                null,
                null,
                order == null ? 0.0 : normalizeOrderWeightKg(order.getTotalWeight()),
                order == null ? 0.0 : safePositive(order.getTotalVolume()),
                null,
                null
        );
    }

    private String missingReceiverLocationReason(TmsOrderOperationView order) {
        if (order == null || order.getReceiverLatitude() == null || order.getReceiverLongitude() == null) {
            return REASON_MISSING_RECEIVER_LOCATION;
        }
        return REASON_INVALID_RECEIVER_LOCATION;
    }

    private void validateCandidateOrders(
            List<TmsOrderOperationView> orders,
            String postOfficeCode,
            List<OrderStatus> allowedStatuses
    ) {
        for (TmsOrderOperationView order : orders) {
            if (order == null || order.getId() == null) {
                throw new AppException(ErrorCode.ORDER_NOT_FOUND);
            }
            if (!allowedStatuses.contains(order.getStatus())) {
                throw new AppException(ErrorCode.ORDER_NOT_ASSIGNABLE);
            }
            if (postOfficeCode == null
                    || order.getDestinationPostOfficeCode() == null
                    || !postOfficeCode.equalsIgnoreCase(order.getDestinationPostOfficeCode())) {
                throw new AppException(ErrorCode.ORDER_NOT_ASSIGNABLE);
            }
        }
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

        List<Trip> trips = tripRepository
                .findByTenantIdAndTripTypeAndPostOfficeIdAndTripDateAndShiftAndStatusIn(
                        tenantId,
                        TripType.DELIVERY,
                        postOfficeId,
                        tripDate,
                        shift,
                        REPLANNABLE_DELIVERY_TRIP_STATUSES
                );

        Map<Long, Trip> tripByCourier = new HashMap<>();
        for (Trip trip : trips) {
            Long courierId = trip.getCourierStaffId();
            if (courierId == null || !courierIds.contains(courierId)) {
                continue;
            }
            tripByCourier.compute(courierId, (key, currentValue) -> {
                if (currentValue == null) {
                    return trip;
                }
                if (trip.getId() != null && (currentValue.getId() == null || trip.getId() > currentValue.getId())) {
                    return trip;
                }
                return currentValue;
            });
        }
        return tripByCourier;
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
                TripType.DELIVERY,
                tripDate,
                shift,
                route.courierStaffId(),
                ACTIVE_DELIVERY_TRIP_STATUSES,
                excludeTripId
        );
        if (courierConflict) {
            throw invalidRequest("Courier already has another active delivery trip in the same shift and trip date.");
        }

        if (route.vehicleId() == null) {
            throw invalidRequest("Route has no assigned vehicle.");
        }
        boolean vehicleConflict = tripRepository.existsActiveTripByVehicleAndShift(
                tenantId,
                TripType.DELIVERY,
                tripDate,
                shift,
                route.vehicleId(),
                ACTIVE_DELIVERY_TRIP_STATUSES,
                excludeTripId
        );
        if (vehicleConflict) {
            throw invalidRequest("Vehicle already has another active delivery trip in the same shift and trip date.");
        }
    }

    private ShiftPlanningWindow resolveShiftPlanningWindow(
            PickupShift shift,
            LocalDate tripDate,
            LocalDateTime planningStartTime,
            LocalDateTime planningEndTime
    ) {
        if (shift == null) {
            throw invalidRequest("shift is required.");
        }
        LocalDate effectiveTripDate = tripDate == null ? LocalDate.now() : tripDate;
        LocalDateTime defaultStart = LocalDateTime.of(effectiveTripDate, resolveShiftStartTime(shift));
        LocalDateTime defaultEnd = LocalDateTime.of(effectiveTripDate, resolveShiftEndTime(shift));
        LocalDateTime effectiveStart = planningStartTime == null ? defaultStart : planningStartTime;
        LocalDateTime effectiveEnd = planningEndTime == null ? defaultEnd : planningEndTime;

        if (effectiveEnd.isBefore(effectiveStart)) {
            throw invalidRequest("planning_end_time must be after or equal to planning_start_time.");
        }
        if (!effectiveStart.toLocalDate().equals(effectiveTripDate)
                || !effectiveEnd.toLocalDate().equals(effectiveTripDate)) {
            throw invalidRequest("planning_start_time and planning_end_time must be on the same date as trip_date.");
        }
        return new ShiftPlanningWindow(effectiveTripDate, effectiveStart, effectiveEnd);
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

    private AlgorithmConfig buildConfig(AutoAssignDeliveryPlanRequest request, ShiftPlanningWindow window) {
        GoalPreset goalPreset = resolveGoalPreset(request.getOptimizationGoal());
        int orderLimit = resolvePositiveInt(request.getOrderLimit(), DEFAULT_ORDER_LIMIT);
        double averageSpeedKmph = resolvePositiveDouble(request.getAverageSpeedKmph(), DEFAULT_AVERAGE_SPEED_KMPH);
        int serviceMinutesPerStop =
                resolvePositiveInt(request.getServiceMinutesPerStop(), DEFAULT_SERVICE_MINUTES_PER_STOP);
        boolean allowLateness = resolveBoolean(request.getAllowLateness(), goalPreset.allowLateness(), DEFAULT_ALLOW_LATENESS);
        boolean enforcePlanningEnd = resolveBoolean(
                request.getEnforcePlanningEnd(),
                goalPreset.enforcePlanningEnd(),
                DEFAULT_ENFORCE_PLANNING_END
        );
        boolean enforceCapacity = resolveBoolean(
                request.getEnforceCapacity(),
                goalPreset.enforceCapacity(),
                DEFAULT_ENFORCE_CAPACITY
        );
        double distanceWeight = resolveNonNegativeDouble(
                request.getDistanceWeight(),
                resolveNonNegativeDouble(goalPreset.distanceWeight(), DEFAULT_DISTANCE_WEIGHT)
        );
        double latenessWeight = resolveNonNegativeDouble(
                request.getLatenessWeight(),
                resolveNonNegativeDouble(goalPreset.latenessWeight(), DEFAULT_LATENESS_WEIGHT)
        );
        double unassignedPenalty = resolveNonNegativeDouble(
                request.getUnassignedPenalty(),
                resolveNonNegativeDouble(goalPreset.unassignedPenalty(), DEFAULT_UNASSIGNED_PENALTY)
        );
        double usedRoutePenalty = resolveNonNegativeDouble(
                request.getUsedRoutePenalty(),
                resolveNonNegativeDouble(goalPreset.usedRoutePenalty(), DEFAULT_USED_ROUTE_PENALTY)
        );
        int matrixBatchSize = resolvePositiveInt(distanceMatrixBatchSize, DEFAULT_DISTANCE_MATRIX_BATCH_SIZE);
        int matrixMaxNodes = resolvePositiveInt(distanceMatrixMaxNodes, DEFAULT_DISTANCE_MATRIX_MAX_NODES);

        return new AlgorithmConfig(
                window.planningStartTime(),
                window.planningEndTime(),
                window.tripDate(),
                orderLimit,
                averageSpeedKmph,
                serviceMinutesPerStop,
                allowLateness,
                enforcePlanningEnd,
                enforceCapacity,
                distanceWeight,
                latenessWeight,
                unassignedPenalty,
                usedRoutePenalty,
                RoutingVehicle.fromValue(request.getVehicle()),
                matrixBatchSize,
                matrixMaxNodes,
                null
        );
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
            throw invalidRequest("Selected post office is not active.");
        }
        LocalTime workingStart = postOffice.getWorkingStartTime();
        LocalTime workingEnd = postOffice.getWorkingEndTime();
        if (workingStart == null || workingEnd == null || !workingEnd.isAfter(workingStart)) {
            throw invalidRequest("Post office working hours are not configured correctly.");
        }
        if (!isWithinTimeRange(planningStartTime.toLocalTime(), workingStart, workingEnd)
                || !isWithinTimeRange(planningEndTime.toLocalTime(), workingStart, workingEnd)) {
            throw invalidRequest("Planning window is outside configured post office working hours.");
        }
    }

    private boolean isWithinTimeRange(LocalTime value, LocalTime startInclusive, LocalTime endInclusive) {
        return value != null
                && startInclusive != null
                && endInclusive != null
                && !value.isBefore(startInclusive)
                && !value.isAfter(endInclusive);
    }

    private List<RouteState> initializeRoutes(
            List<CourierResource> couriers,
            List<Vehicle> activeVehicles,
            double depotLatitude,
            double depotLongitude
    ) {
        Map<Long, Vehicle> dedicatedVehicleByCourier = new HashMap<>();
        Set<Long> courierIds = couriers.stream().map(CourierResource::staffId).collect(Collectors.toSet());

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
            Vehicle vehicle = dedicatedVehicleByCourier.get(courier.staffId());
            if (vehicle == null && sharedVehicleIndex < sharedVehicles.size()) {
                vehicle = sharedVehicles.get(sharedVehicleIndex++);
            }
            if (vehicle == null) {
                continue;
            }

            routes.add(new RouteState(
                    courier.staffId(),
                    courier.code(),
                    courier.fullName(),
                    courier.maxStops(),
                    vehicle.getId(),
                    vehicle.getLicensePlate(),
                    resolvePositiveDouble(vehicle.getMaxWeight(), DEFAULT_MAX_WEIGHT_IF_MISSING),
                    resolvePositiveDouble(vehicle.getMaxVolume(), DEFAULT_MAX_VOLUME_IF_MISSING),
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
        Set<Long> allowedCourierIds = requestCourierIds == null || requestCourierIds.isEmpty()
                ? null
                : new HashSet<>(requestCourierIds);
        Map<Long, CourierResource> uniqueCouriers = new LinkedHashMap<>();
        for (PostOfficeStaffAssignment assignment : assignments) {
            PostOfficeStaff staff = assignment.getStaff();
            if (staff == null || staff.getId() == null) {
                continue;
            }
            if (allowedCourierIds != null && !allowedCourierIds.contains(staff.getId())) {
                continue;
            }
            uniqueCouriers.putIfAbsent(staff.getId(), new CourierResource(
                    staff.getId(),
                    staff.getCode(),
                    staff.getFullName(),
                    resolveMaxStops(staff)
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

    private List<OrderStatus> resolveCandidateStatuses(Collection<OrderStatus> requestedStatuses) {
        if (requestedStatuses == null || requestedStatuses.isEmpty()) {
            return DEFAULT_DELIVERY_CANDIDATE_STATUSES;
        }

        List<OrderStatus> normalized = new ArrayList<>();
        for (OrderStatus status : requestedStatuses) {
            if (status != null && !normalized.contains(status)) {
                normalized.add(status);
            }
        }
        return normalized.isEmpty() ? DEFAULT_DELIVERY_CANDIDATE_STATUSES : normalized;
    }

    private void validateManagerScope(Long postOfficeId, Long tenantId) {
        if (!firstMileAccessUtils.isManagerScopedAccess()) {
            return;
        }
        firstMileAccessUtils.ensureCurrentManagerAssignedToPostOfficeOrThrow(postOfficeId, tenantId);
    }

    private boolean canViewTrip(Long tenantId, Trip trip) {
        if (firstMileAccessUtils.isAdmin()) {
            return true;
        }
        if (firstMileAccessUtils.isPostOfficerManager()) {
            Set<Long> managedPostOfficeIds = firstMileAccessUtils.getManagedPostOfficeIdsOrThrow(tenantId);
            return trip.getPostOfficeId() != null && managedPostOfficeIds.contains(trip.getPostOfficeId());
        }
        if (firstMileAccessUtils.isCourier()) {
            Long courierStaffId = firstMileAccessUtils.resolveCurrentStaffIdByRoleOrThrow(
                    tenantId,
                    PostOfficeStaffRole.COURIER
            );
            return Objects.equals(courierStaffId, trip.getCourierStaffId());
        }
        throw new AppException(ErrorCode.UNAUTHORIZED);
    }

    private void ensureCanOperateTrip(Long tenantId, Trip trip) {
        if (!canViewTrip(tenantId, trip)) {
            throw new AppException(ErrorCode.UNAUTHORIZED);
        }
    }

    private DeliveryContext resolveDeliveryContext(Trip trip, Long tenantId) {
        PostOffice postOffice = trip.getPostOfficeId() == null
                ? null
                : postOfficeRepository.findByIdAndTenantId(trip.getPostOfficeId(), tenantId).orElse(null);
        PostOfficeStaff courier = trip.getCourierStaffId() == null
                ? null
                : postOfficeStaffRepository.findByIdAndTenantId(trip.getCourierStaffId(), tenantId).orElse(null);
        Vehicle vehicle = trip.getVehicleId() == null
                ? null
                : vehicleRepository.findByIdAndTenantId(trip.getVehicleId(), tenantId).orElse(null);

        return new DeliveryContext(
                postOffice == null ? null : postOffice.getCode(),
                postOffice == null ? null : postOffice.getName(),
                courier == null ? null : courier.getCode(),
                courier == null ? null : courier.getFullName(),
                vehicle == null ? null : vehicle.getLicensePlate()
        );
    }

    private TmsOrderStatusTransitionRequest.Item toTransitionItem(
            TmsOrderOperationView order,
            List<OrderStatus> expectedStatuses,
            OrderStatus targetStatus,
            String description,
            Trip trip,
            DeliveryContext context,
            LocalDateTime eventTime
    ) {
        TmsOrderStatusTransitionRequest.Context transitionContext = TmsOrderStatusTransitionRequest.Context.builder()
                .eventTime(eventTime)
                .tripId(trip.getId())
                .tripCode(trip.getTripCode())
                .postOfficeId(trip.getPostOfficeId())
                .postOfficeCode(context.postOfficeCode())
                .postOfficeName(context.postOfficeName())
                .staffId(trip.getCourierStaffId())
                .staffCode(context.courierCode())
                .staffName(context.courierName())
                .vehicleId(trip.getVehicleId())
                .vehicleLicensePlate(context.vehicleLicensePlate())
                .latitude(order.getReceiverLatitude())
                .longitude(order.getReceiverLongitude())
                .locationLabel("Quét xuất giao hàng")
                .build();

        return TmsOrderStatusTransitionRequest.Item.builder()
                .orderId(order.getId())
                .orderCode(order.getOrderCode())
                .expectedStatuses(expectedStatuses)
                .targetStatus(targetStatus)
                .description(description)
                .context(transitionContext)
                .build();
    }

    private void enqueueTransitions(List<TmsOrderStatusTransitionRequest.Item> items, Long tenantId) {
        if (items == null || items.isEmpty()) {
            return;
        }
        tmsOrderTransitionPublisherService.publish(TmsOrderStatusTransitionRequest.builder()
                .source(TRANSITION_SOURCE)
                .idempotencyKey(TRANSITION_SOURCE + "-" + UUID.randomUUID())
                .items(items)
                .build(), tenantId);
    }

    private PostOffice resolveSinglePostOfficeForResponse(Long tenantId, Long postOfficeId, List<Trip> trips) {
        Long resolvedPostOfficeId = postOfficeId;
        if (resolvedPostOfficeId == null && trips != null && !trips.isEmpty()) {
            Set<Long> postOfficeIds = trips.stream()
                    .map(Trip::getPostOfficeId)
                    .filter(Objects::nonNull)
                    .collect(Collectors.toSet());
            if (postOfficeIds.size() == 1) {
                resolvedPostOfficeId = postOfficeIds.iterator().next();
            }
        }
        if (resolvedPostOfficeId == null) {
            return null;
        }
        return postOfficeRepository.findByIdAndTenantId(resolvedPostOfficeId, tenantId).orElse(null);
    }

    private Map<Long, PostOfficeStaff> loadCourierById(Long tenantId, List<Trip> trips) {
        List<Long> courierIds = trips.stream()
                .map(Trip::getCourierStaffId)
                .filter(Objects::nonNull)
                .distinct()
                .toList();
        if (courierIds.isEmpty()) {
            return Map.of();
        }
        return postOfficeStaffRepository.findByTenantIdAndIdIn(tenantId, courierIds).stream()
                .collect(Collectors.toMap(PostOfficeStaff::getId, Function.identity()));
    }

    private Map<Long, String> loadVehicleLicensePlateById(Long tenantId, List<Trip> trips) {
        List<Long> vehicleIds = trips.stream()
                .map(Trip::getVehicleId)
                .filter(Objects::nonNull)
                .distinct()
                .toList();
        if (vehicleIds.isEmpty()) {
            return Map.of();
        }
        return vehicleIds.stream()
                .map(vehicleId -> vehicleRepository.findByIdAndTenantId(vehicleId, tenantId).orElse(null))
                .filter(Objects::nonNull)
                .collect(Collectors.toMap(Vehicle::getId, Vehicle::getLicensePlate));
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

    private Set<Long> extractOrderIds(List<TmsOrderOperationView> orders) {
        Set<Long> orderIds = new HashSet<>();
        for (TmsOrderOperationView order : orders) {
            if (order != null && order.getId() != null) {
                orderIds.add(order.getId());
            }
        }
        return orderIds;
    }

    private Set<Long> extractOrderIds(Collection<List<PickupOrderNode>> routeNodes) {
        Set<Long> orderIds = new HashSet<>();
        if (routeNodes == null) {
            return orderIds;
        }
        for (List<PickupOrderNode> nodes : routeNodes) {
            if (nodes == null) {
                continue;
            }
            for (PickupOrderNode node : nodes) {
                if (node != null && node.orderId() != null) {
                    orderIds.add(node.orderId());
                }
            }
        }
        return orderIds;
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

    private List<DeliveryAssignmentResponse.UnassignedDeliveryOrderResponse> buildUnassignedResponses(
            List<UnassignedOrderState> unassignedOrderStates,
            Set<Long> scopedOrderIds
    ) {
        List<DeliveryAssignmentResponse.UnassignedDeliveryOrderResponse> responses = new ArrayList<>();
        for (UnassignedOrderState state : unassignedOrderStates) {
            PickupOrderNode order = state.order();
            if (order == null) {
                continue;
            }
            if (order.orderId() != null && scopedOrderIds != null && !scopedOrderIds.contains(order.orderId())) {
                continue;
            }
            responses.add(new DeliveryAssignmentResponse.UnassignedDeliveryOrderResponse(
                    order.orderId(),
                    order.orderCode(),
                    order.customerOrderCode(),
                    state.reason()
            ));
        }
        responses.sort(Comparator
                .comparing(DeliveryAssignmentResponse.UnassignedDeliveryOrderResponse::orderId,
                        Comparator.nullsLast(Comparator.naturalOrder()))
                .thenComparing(DeliveryAssignmentResponse.UnassignedDeliveryOrderResponse::customerOrderCode,
                        Comparator.nullsLast(String::compareToIgnoreCase))
        );
        return responses;
    }

    private List<String> normalizeOrderCodes(List<String> orderCodes) {
        if (orderCodes == null || orderCodes.isEmpty()) {
            return List.of();
        }
        return orderCodes.stream()
                .map(this::normalizeText)
                .filter(Objects::nonNull)
                .distinct()
                .toList();
    }

    private String normalizeText(String value) {
        if (value == null) {
            return null;
        }
        String trimmed = value.trim();
        return trimmed.isEmpty() ? null : trimmed;
    }

    private String generateTripCode(LocalDate tripDate, PickupShift shift, String courierCode) {
        String datePart = tripDate.format(java.time.format.DateTimeFormatter.BASIC_ISO_DATE);
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
        return "DLV-" + datePart + "-" + shift.name() + "-" + courierPart + "-" + randomPart;
    }

    private GoalPreset resolveGoalPreset(PickupOptimizationGoal optimizationGoal) {
        PickupOptimizationGoal effectiveGoal = optimizationGoal == null
                ? PickupOptimizationGoal.BALANCED
                : optimizationGoal;
        return switch (effectiveGoal) {
            case BALANCED -> new GoalPreset(null, null, null, DEFAULT_DISTANCE_WEIGHT, DEFAULT_LATENESS_WEIGHT,
                    DEFAULT_UNASSIGNED_PENALTY, DEFAULT_USED_ROUTE_PENALTY);
            case ON_TIME_PRIORITY -> new GoalPreset(false, true, true, 0.9, 2.2, 450.0, 3.5);
            case COST_EFFICIENCY -> new GoalPreset(false, true, true, 2.0, 0.2, 350.0, 5.0);
            case MAX_ASSIGNMENT -> new GoalPreset(true, false, true, 0.7, 0.1, 900.0, 1.2);
        };
    }

    private boolean resolveBoolean(Boolean explicitValue, Boolean presetValue, boolean defaultValue) {
        if (explicitValue != null) {
            return explicitValue;
        }
        if (presetValue != null) {
            return presetValue;
        }
        return defaultValue;
    }

    private boolean isValidCoordinate(double latitude, double longitude) {
        return latitude >= -90.0 && latitude <= 90.0
                && longitude >= -180.0 && longitude <= 180.0;
    }

    private int resolvePositiveInt(Integer value, int defaultValue) {
        return value == null || value <= 0 ? defaultValue : value;
    }

    private double resolvePositiveDouble(Double value, double defaultValue) {
        return value == null || value <= 0.0 ? defaultValue : value;
    }

    private double resolveNonNegativeDouble(Double value, double defaultValue) {
        return value == null || value < 0.0 ? defaultValue : value;
    }

    private Integer normalizePositiveInteger(Integer value) {
        return value == null || value <= 0 ? null : value;
    }

    private double safePositive(Double value) {
        return value == null || value < 0.0 ? 0.0 : value;
    }

    private double normalizeOrderWeightKg(Double weightGram) {
        return safePositive(weightGram) / GRAMS_PER_KILOGRAM;
    }

    private double round3(double value) {
        return Math.round(value * 1000.0) / 1000.0;
    }

    private AppException invalidRequest(String detail) {
        return new AppException(ErrorCode.INVALID_REQUEST, detail);
    }

    private record ExistingDeliveryRouteData(
            Map<Long, List<PickupOrderNode>> routeNodesByTripId,
            List<UnassignedOrderState> invalidOrderStates
    ) {
    }

    private record DeliveryPersistResult(
            List<DeliveryAssignmentResponse.DeliveryTripResponse> tripResponses,
            Set<Long> assignedOrderIds,
            int createdTrips
    ) {
    }

    private record DeliveryContext(
            String postOfficeCode,
            String postOfficeName,
            String courierCode,
            String courierName,
            String vehicleLicensePlate
    ) {
    }

    private record GoalPreset(
            Boolean allowLateness,
            Boolean enforcePlanningEnd,
            Boolean enforceCapacity,
            Double distanceWeight,
            Double latenessWeight,
            Double unassignedPenalty,
            Double usedRoutePenalty
    ) {
    }
}
