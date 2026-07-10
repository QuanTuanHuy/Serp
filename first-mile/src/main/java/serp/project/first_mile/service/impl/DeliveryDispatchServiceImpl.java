/*
Author: Nguyen The Anh
Description: Part of Serp Project
*/

package serp.project.first_mile.service.impl;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.locationtech.jts.geom.Coordinate;
import org.locationtech.jts.geom.GeometryFactory;
import org.locationtech.jts.geom.Point;
import org.locationtech.jts.geom.PrecisionModel;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.StringUtils;
import org.springframework.web.multipart.MultipartFile;
import serp.project.first_mile.caller.PaymentServiceCaller;
import serp.project.first_mile.caller.dto.payment.PaymentCreateOrderRequest;
import serp.project.first_mile.caller.dto.payment.PaymentCreateOrderResponse;
import serp.project.first_mile.caller.dto.payment.PaymentQueryOrderResponse;
import serp.project.first_mile.caller.TmsOrderClient;
import serp.project.first_mile.caller.dto.tms_order.TmsOrderOperationView;
import serp.project.first_mile.caller.dto.tms_order.TmsOrderStatusTransitionRequest;
import serp.project.first_mile.domain.Checkin;
import serp.project.first_mile.domain.DeliveryManifest;
import serp.project.first_mile.domain.DeliveryManifestOrder;
import serp.project.first_mile.domain.PostOffice;
import serp.project.first_mile.domain.PostOfficeStaff;
import serp.project.first_mile.domain.PostOfficeStaffAssignment;
import serp.project.first_mile.domain.Trip;
import serp.project.first_mile.domain.TripOrder;
import serp.project.first_mile.domain.Vehicle;
import serp.project.first_mile.dto.request.AutoAssignDeliveryPlanRequest;
import serp.project.first_mile.dto.request.ConfirmDeliveryFailureRequest;
import serp.project.first_mile.dto.request.ConfirmDeliveryPaymentRequest;
import serp.project.first_mile.dto.request.ConfirmDeliveryRequest;
import serp.project.first_mile.dto.request.FileUploadRequest;
import serp.project.first_mile.dto.request.ManualAssignDeliveryOrdersRequest;
import serp.project.first_mile.dto.request.ReturnToSenderRequest;
import serp.project.first_mile.dto.request.ScanOutDeliveryOrderRequest;
import serp.project.first_mile.dto.response.DeliveryAssignmentResponse;
import serp.project.first_mile.dto.response.DeliveryManifestOrderResponse;
import serp.project.first_mile.dto.response.DeliveryManifestResponse;
import serp.project.first_mile.dto.response.DeliveryPaymentConfirmResponse;
import serp.project.first_mile.dto.response.DeliveryPaymentInitResponse;
import serp.project.first_mile.dto.response.DeliveryScanOutResponse;
import serp.project.first_mile.dto.response.FileUploadResponse;
import serp.project.first_mile.dto.response.PickupOptimizationResponse;
import serp.project.first_mile.enums.CheckinType;
import serp.project.first_mile.enums.DeliveryManifestStatus;
import serp.project.first_mile.enums.DeliveryOrderStatus;
import serp.project.first_mile.enums.OrderStatus;
import serp.project.first_mile.enums.PaymentStatus;
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
import serp.project.first_mile.kafka.StaffNotificationEventPublisher;
import serp.project.first_mile.kernel.utils.FirstMileAccessUtils;
import serp.project.first_mile.kernel.utils.ImageContentTypeUtils;
import serp.project.first_mile.repository.CheckinRepository;
import serp.project.first_mile.repository.DeliveryManifestRepository;
import serp.project.first_mile.repository.PostOfficeRepository;
import serp.project.first_mile.repository.PostOfficeStaffAssignmentRepository;
import serp.project.first_mile.repository.PostOfficeStaffRepository;
import serp.project.first_mile.repository.DeliveryManifestOrderRepository;
import serp.project.first_mile.repository.TripOrderRepository;
import serp.project.first_mile.repository.TripRepository;
import serp.project.first_mile.repository.VehicleRepository;
import serp.project.first_mile.service.DeliveryDispatchService;
import serp.project.first_mile.service.FileStorageService;
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

import java.io.IOException;
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
@Slf4j
@Transactional(readOnly = true)
public class DeliveryDispatchServiceImpl implements DeliveryDispatchService {

    private static final GeometryFactory GEOMETRY_FACTORY = new GeometryFactory(new PrecisionModel(), 4326);
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
    private static final String RECEIVER = "RECEIVER";
    private static final String STORAGE_SERVICE_NAME = "first-mile";
    private static final String DELIVERY_CHECKIN_IMAGE_FOLDER = "orders/delivery-checkin";
    private static final String PAYMENT_SOURCE_SERVICE = "first-mile";
    private static final String PAYMENT_SOURCE = "last-mile-delivery-trip";
    private static final long MIN_PAYMENT_SERVICE_AMOUNT = 1_000L;
    private static final double EARTH_RADIUS_METERS = 6_371_000D;

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
    private final PaymentServiceCaller paymentServiceCaller;
    private final PostOfficeRepository postOfficeRepository;
    private final PostOfficeStaffRepository postOfficeStaffRepository;
    private final PostOfficeStaffAssignmentRepository postOfficeStaffAssignmentRepository;
    private final VehicleRepository vehicleRepository;
    private final DeliveryManifestOrderRepository deliveryManifestOrderRepository;
    private final DeliveryManifestRepository deliveryManifestRepository;
    private final CheckinRepository checkinRepository;
    private final TripRepository tripRepository;
    private final TripOrderRepository tripOrderRepository;
    private final PickupOptimizationEngine pickupOptimizationEngine;
    private final FirstMileAccessUtils firstMileAccessUtils;
    private final TmsOrderTransitionPublisherService tmsOrderTransitionPublisherService;
    private final FileStorageService fileStorageService;
    private final StaffNotificationEventPublisher staffNotificationEventPublisher;

    @Value("${distance-matrix.batch-size:20}")
    private Integer distanceMatrixBatchSize;

    @Value("${distance-matrix.max-nodes:120}")
    private Integer distanceMatrixMaxNodes;

    @Value("${app.delivery.max-attempts:3}")
    private int maxDeliveryAttempts;

    @Value("${payment.service.redirect-url:http://localhost:3000/payment/result}")
    private String paymentRedirectUrl;

    @Override
    public PickupOptimizationResponse optimizeDeliveryPlan(AutoAssignDeliveryPlanRequest request) {
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

        return buildOptimizationResponse(postOffice, runtimeConfig, solution, evaluation);
    }

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
    @Transactional(rollbackFor = Exception.class)
    public DeliveryAssignmentResponse manualAssignDeliveryOrders(ManualAssignDeliveryOrdersRequest request) {
        Long tenantId = firstMileAccessUtils.getCurrentTenantIdOrThrow();
        ShiftPlanningWindow shiftPlanningWindow = resolveShiftPlanningWindow(
                request.getShift(),
                request.getTripDate(),
                request.getPlanningStartTime(),
                request.getPlanningEndTime()
        );

        if (Boolean.TRUE.equals(request.getForceAssign())) {
            return forceManualAssignDeliveryOrders(request, tenantId, shiftPlanningWindow);
        }

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

        PostOfficeStaff courier = postOfficeStaffRepository.findByIdAndTenantId(request.getCourierStaffId(), tenantId)
                .orElseThrow(() -> new AppException(ErrorCode.POST_OFFICE_STAFF_NOT_FOUND));
        validateCourierAssignableForManual(
                courier,
                postOffice.getId(),
                tenantId,
                shiftPlanningWindow.tripDate()
        );

        AlgorithmConfig config = buildConfig(request, shiftPlanningWindow);
        List<Vehicle> activeVehicles = vehicleRepository.findByTenantIdAndPostOffice_IdAndStatusIn(
                tenantId,
                postOffice.getId(),
                List.of(VehicleStatus.ACTIVE)
        );
        List<RouteState> routes = initializeRoutes(
                List.of(new CourierResource(
                        courier.getId(),
                        courier.getCode(),
                        courier.getFullName(),
                        resolveMaxStops(courier)
                )),
                activeVehicles,
                depotLocation.getY(),
                depotLocation.getX()
        );
        if (routes.isEmpty()) {
            throw invalidRequest("No usable route can be initialized for manual delivery assign.");
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

        List<TripOrder> existingTripOrders = existingTrip == null || existingTrip.getId() == null
                ? List.of()
                : tripOrderRepository.findByTrip_IdOrderBySequenceNoAsc(existingTrip.getId());
        Set<Long> existingTripOrderIds = existingTripOrders.stream()
                .map(TripOrder::getOrderId)
                .filter(Objects::nonNull)
                .collect(Collectors.toCollection(LinkedHashSet::new));

        LinkedHashSet<Long> allOrderIds = new LinkedHashSet<>(existingTripOrderIds);
        allOrderIds.addAll(requestedOrderIds);
        Map<Long, TmsOrderOperationView> orderById = loadOrdersByIdMapOrThrow(allOrderIds, tenantId);

        Long excludeTripId = existingTrip == null ? null : existingTrip.getId();
        for (Long orderId : requestedOrderIds) {
            TmsOrderOperationView order = orderById.get(orderId);
            if (order == null) {
                throw new AppException(ErrorCode.ORDER_NOT_FOUND);
            }

            boolean alreadyInCurrentTrip = existingTripOrderIds.contains(orderId);
            validateManualDeliveryOrder(order, postOffice.getCode(), alreadyInCurrentTrip);

            if (!alreadyInCurrentTrip && isAssignedToActiveManifest(toDeliveryOrderNode(order), tenantId)) {
                throw invalidRequest("Order is already assigned to an active delivery manifest.");
            }

            boolean assignedInAnotherTrip = tripOrderRepository.existsByTenantIdAndOrderIdAndTripStatusIn(
                    tenantId,
                    orderId,
                    TripType.DELIVERY,
                    ACTIVE_DELIVERY_TRIP_STATUSES,
                    excludeTripId
            );
            if (assignedInAnotherTrip) {
                throw invalidRequest("Order is already assigned to another active delivery trip.");
            }
        }

        ExistingDeliveryRouteData existingRouteData = loadExistingRouteData(existingTripByCourier, tenantId);
        applyExistingRouteData(routes, existingTripByCourier, existingRouteData);

        List<PickupOrderNode> manualCandidateNodes = new ArrayList<>();
        for (Long orderId : requestedOrderIds) {
            if (existingTripOrderIds.contains(orderId)) {
                continue;
            }
            PickupOrderNode node = toDeliveryOrderNode(orderById.get(orderId));
            if (node == null) {
                throw new AppException(ErrorCode.ORDER_NOT_ASSIGNABLE);
            }
            manualCandidateNodes.add(node);
        }
        manualCandidateNodes.sort(Comparator
                .comparing(PickupOrderNode::orderId, Comparator.nullsLast(Comparator.naturalOrder()))
                .thenComparing(PickupOrderNode::customerOrderCode, Comparator.nullsLast(String::compareToIgnoreCase))
        );

        List<PickupOrderNode> matrixNodes = new ArrayList<>();
        for (List<PickupOrderNode> routeNodes : existingRouteData.routeNodesByTripId().values()) {
            matrixNodes.addAll(routeNodes);
        }
        matrixNodes.addAll(manualCandidateNodes);

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
        for (PickupOrderNode manualCandidateNode : manualCandidateNodes) {
            unassignedOrders.add(new UnassignedOrderState(manualCandidateNode, REASON_UNASSIGNED, true));
        }

        SolutionState solution = new SolutionState(routes, unassignedOrders);
        pickupOptimizationEngine.applyGreedyRepair(solution, runtimeConfig);
        pickupOptimizationEngine.markNoFeasibleUnassigned(solution);
        pickupOptimizationEngine.sanitizeSolution(solution);

        SolutionEvaluation solutionEvaluation = pickupOptimizationEngine.evaluateSolution(solution, runtimeConfig);
        if (pickupOptimizationEngine.isInfeasible(solutionEvaluation)) {
            throw new AppException(ErrorCode.ORDER_NOT_ASSIGNABLE);
        }

        DeliveryPersistResult persistResult = persistAssignments(
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

    @Override
    @Transactional(rollbackFor = Exception.class)
    public DeliveryPaymentInitResponse initiateTripDeliveryPayment(Long tripId, String orderCode, Long tenantId) {
        DeliveryTripOrderContext context = resolveTripOrderContextForUpdate(tripId, orderCode, tenantId);
        ensureTripOrderOutForDelivery(context.tripOrder());

        long requiredAmount = requiredDeliveryPaymentAmount(context.order());
        if (requiredAmount <= 0L) {
            throw new AppException(ErrorCode.INVALID_REQUEST, "This delivery does not require customer payment.");
        }
        if (requiredAmount < MIN_PAYMENT_SERVICE_AMOUNT) {
            throw new AppException(
                    ErrorCode.INVALID_REQUEST,
                    "Customer payment amount must be at least 1,000 VND for payment service."
            );
        }

        TripOrder tripOrder = context.tripOrder();
        if (PaymentStatus.PAID.equals(tripOrder.getDeliveryPaymentStatus())) {
            return new DeliveryPaymentInitResponse(
                    context.trip().getId(),
                    context.order().getOrderCode(),
                    requiredAmount,
                    tripOrder.getDeliveryPaymentStatus(),
                    tripOrder.getDeliveryPaymentAppTransId(),
                    null,
                    "SUCCESS",
                    "Customer payment is already confirmed."
            );
        }

        PaymentCreateOrderResponse paymentResponse = paymentServiceCaller.createOrder(
                buildTripDeliveryPaymentRequest(context, requiredAmount, tenantId)
        );
        if (paymentResponse.getStatus() == null || !"SUCCESS".equalsIgnoreCase(paymentResponse.getStatus())) {
            throw new AppException(
                    ErrorCode.INVALID_REQUEST,
                    paymentResponse.getMessage() == null
                            ? "Cannot create payment order for delivery customer payment."
                            : paymentResponse.getMessage()
            );
        }

        tripOrder.setDeliveryPaymentStatus(PaymentStatus.UNPAID);
        tripOrder.setDeliveryPaymentAmount(requiredAmount);
        tripOrder.setDeliveryPaymentAppTransId(paymentResponse.getAppTransId());
        tripOrder.setDeliveryPaymentConfirmedAt(null);
        tripOrderRepository.save(tripOrder);

        return new DeliveryPaymentInitResponse(
                context.trip().getId(),
                context.order().getOrderCode(),
                requiredAmount,
                tripOrder.getDeliveryPaymentStatus(),
                paymentResponse.getAppTransId(),
                paymentResponse.getOrderUrl(),
                paymentResponse.getStatus(),
                paymentResponse.getMessage()
        );
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public DeliveryPaymentConfirmResponse confirmTripDeliveryPayment(
            Long tripId,
            String orderCode,
            ConfirmDeliveryPaymentRequest request,
            Long tenantId
    ) {
        if (request == null || !StringUtils.hasText(request.getAppTransId())) {
            throw new AppException(ErrorCode.INVALID_REQUEST, "appTransId is required.");
        }

        DeliveryTripOrderContext context = resolveTripOrderContextForUpdate(tripId, orderCode, tenantId);
        ensureTripOrderOutForDelivery(context.tripOrder());

        TripOrder tripOrder = context.tripOrder();
        long requiredAmount = requiredDeliveryPaymentAmount(context.order());
        if (requiredAmount <= 0L) {
            throw new AppException(ErrorCode.INVALID_REQUEST, "This delivery does not require customer payment.");
        }

        String expectedAppTransId = normalizeText(tripOrder.getDeliveryPaymentAppTransId());
        String requestedAppTransId = request.getAppTransId().trim();
        if (expectedAppTransId == null) {
            throw new AppException(ErrorCode.INVALID_REQUEST, "Customer payment must be initiated before confirmation.");
        }
        if (!expectedAppTransId.equals(requestedAppTransId)) {
            throw new AppException(ErrorCode.INVALID_REQUEST, "Payment transaction does not match this delivery order.");
        }

        if (!PaymentStatus.PAID.equals(tripOrder.getDeliveryPaymentStatus())) {
            PaymentQueryOrderResponse queryResponse = paymentServiceCaller.queryOrderStatus(expectedAppTransId);
            String gatewayStatus = queryResponse.getStatus() == null ? "UNKNOWN" : queryResponse.getStatus();
            if (!"SUCCESS".equalsIgnoreCase(gatewayStatus)) {
                throw new AppException(
                        ErrorCode.INVALID_REQUEST,
                        "Payment status is not successful yet. Current status: " + gatewayStatus
                );
            }
            if (queryResponse.getAmount() != null && queryResponse.getAmount() < requiredAmount) {
                throw new AppException(
                        ErrorCode.INVALID_REQUEST,
                        "Paid amount is lower than required customer payment."
                );
            }
            tripOrder.setDeliveryPaymentStatus(PaymentStatus.PAID);
            tripOrder.setDeliveryPaymentAmount(requiredAmount);
            tripOrder.setDeliveryPaymentConfirmedAt(LocalDateTime.now());
            tripOrderRepository.save(tripOrder);
            markReceiverShippingFeePaidIfNeeded(context.order(), tenantId);

            return new DeliveryPaymentConfirmResponse(
                    context.trip().getId(),
                    context.order().getOrderCode(),
                    requiredAmount,
                    PaymentStatus.PAID,
                    expectedAppTransId,
                    gatewayStatus,
                    queryResponse.getMessage()
            );
        }

        ensureConfirmedPaymentAmountCoversRequiredAmount(tripOrder, requiredAmount);
        return new DeliveryPaymentConfirmResponse(
                context.trip().getId(),
                context.order().getOrderCode(),
                tripOrder.getDeliveryPaymentAmount(),
                tripOrder.getDeliveryPaymentStatus(),
                expectedAppTransId,
                "SUCCESS",
                "Customer payment is already confirmed."
        );
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public DeliveryAssignmentResponse confirmTripDelivered(
            Long tripId,
            String orderCode,
            ConfirmDeliveryRequest request,
            MultipartFile photo,
            Long tenantId
    ) {
        DeliveryTripOrderContext context = resolveTripOrderContextForUpdate(tripId, orderCode, tenantId);
        LocalDateTime deliveredAt = request.getDeliveredAt() != null ? request.getDeliveredAt() : LocalDateTime.now();
        ensureTripOrderReadyForDeliveryAction(context.tripOrder(), context.order(), deliveredAt);
        validateDeliveryCheckinRequest(request, photo);
        validateDeliveryPayment(context.tripOrder(), context.order());

        TripOrder tripOrder = context.tripOrder();
        long codCollected = safeAmount(context.order().getCodAmount());
        long shippingFeeCollected = requiredReceiverShippingFee(context.order());
        double distanceMeters = calculateDeliveryDistanceMeters(
                request.getLatitude(),
                request.getLongitude(),
                context.order().getReceiverLatitude(),
                context.order().getReceiverLongitude()
        );
        String contentType = ImageContentTypeUtils.normalizeImageContentType(photo.getContentType());
        FileUploadResponse uploadResponse = uploadDeliveryCheckinPhoto(photo, contentType, tenantId);

        Checkin deliveryCheckin = checkinRepository
                .findByTenantIdAndCheckinTypeAndTripOrderId(tenantId, CheckinType.DELIVERY, tripOrder.getId())
                .orElseGet(Checkin::new);
        deliveryCheckin.setTenantId(tenantId);
        deliveryCheckin.setCheckinType(CheckinType.DELIVERY);
        deliveryCheckin.setTripOrderId(tripOrder.getId());
        deliveryCheckin.setOrderId(context.order().getId());
        deliveryCheckin.setOrderCode(context.order().getOrderCode());
        deliveryCheckin.setTripId(context.trip().getId());
        deliveryCheckin.setDeliveryManifestId(null);
        deliveryCheckin.setDeliveryManifestOrderId(null);
        deliveryCheckin.setCourierStaffId(context.trip().getCourierStaffId());
        deliveryCheckin.setCheckinTime(deliveredAt);
        deliveryCheckin.setCheckinLocation(toPoint(request.getLatitude(), request.getLongitude()));
        deliveryCheckin.setDistanceM(round3(distanceMeters));
        deliveryCheckin.setAllowedRadiusM(null);
        deliveryCheckin.setPhotoUrl(uploadResponse.getUrl());
        checkinRepository.save(deliveryCheckin);

        tripOrder.setDeliveryStatus(DeliveryOrderStatus.DELIVERED);
        tripOrder.setCodCollected(codCollected);
        tripOrder.setShippingFeeCollected(shippingFeeCollected);
        tripOrder.setDeliveredAt(deliveredAt);
        tripOrder.setDeliveryNote(request.getNote());
        tripOrderRepository.save(tripOrder);

        enqueueTransitions(List.of(toTransitionItem(
                context.order(),
                List.of(OrderStatus.READY_FOR_DELIVERY, OrderStatus.DELIVERY_FAILED, OrderStatus.OUT_FOR_DELIVERY),
                OrderStatus.DELIVERED,
                "Giao hàng thành công.",
                context.trip(),
                context.deliveryContext(),
                deliveredAt
        )), tenantId);
        markReceiverShippingFeePaidIfNeeded(context.order(), tenantId);

        return getDeliveryTrips(context.trip().getPostOfficeId(), context.trip().getShift(), context.trip().getTripDate());
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public DeliveryAssignmentResponse confirmTripDeliveryFailed(
            Long tripId,
            String orderCode,
            ConfirmDeliveryFailureRequest request,
            Long tenantId
    ) {
        DeliveryTripOrderContext context = resolveTripOrderContextForUpdate(tripId, orderCode, tenantId);
        LocalDateTime failedAt = LocalDateTime.now();
        ensureTripOrderReadyForDeliveryAction(context.tripOrder(), context.order(), failedAt);

        TripOrder tripOrder = context.tripOrder();
        tripOrder.setDeliveryStatus(DeliveryOrderStatus.FAILED);
        tripOrder.setFailureReason(request == null ? null : request.getFailureReason());
        tripOrder.setDeliveryNote(request == null ? null : request.getNote());
        tripOrder.setDeliveryAttemptCount(safeInteger(tripOrder.getDeliveryAttemptCount()) + 1);
        tripOrderRepository.save(tripOrder);

        enqueueTransitions(List.of(toTransitionItem(
                context.order(),
                List.of(OrderStatus.OUT_FOR_DELIVERY),
                OrderStatus.DELIVERY_FAILED,
                "Giao hàng thất bại: " + (tripOrder.getFailureReason() == null ? "UNKNOWN" : tripOrder.getFailureReason()),
                context.trip(),
                context.deliveryContext(),
                failedAt
        )), tenantId);

        if (tripOrder.getDeliveryAttemptCount() >= maxDeliveryAttempts) {
            returnTripOrderToSender(tripId, orderCode, ReturnToSenderRequest.builder()
                    .note("Đã vượt quá số lần giao tối đa (" + maxDeliveryAttempts + ")")
                    .build(), tenantId);
        }

        return getDeliveryTrips(context.trip().getPostOfficeId(), context.trip().getShift(), context.trip().getTripDate());
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public DeliveryAssignmentResponse returnTripOrderToSender(
            Long tripId,
            String orderCode,
            ReturnToSenderRequest request,
            Long tenantId
    ) {
        DeliveryTripOrderContext context = resolveTripOrderContextForUpdate(tripId, orderCode, tenantId);
        TripOrder tripOrder = context.tripOrder();
        if (!DeliveryOrderStatus.FAILED.equals(resolveDeliveryStatus(tripOrder, context.order()))) {
            throw new AppException(ErrorCode.DELIVERY_ORDER_INVALID_STATUS, "Order must be FAILED to return to sender.");
        }

        LocalDateTime now = LocalDateTime.now();
        tripOrder.setDeliveryStatus(DeliveryOrderStatus.RETURNED);
        tripOrder.setReturnedAt(now);
        tripOrder.setDeliveryNote(request == null ? null : request.getNote());
        tripOrderRepository.save(tripOrder);

        enqueueTransitions(List.of(toTransitionItem(
                context.order(),
                List.of(OrderStatus.DELIVERY_FAILED),
                OrderStatus.RETURNED_TO_SENDER,
                "Đã hoàn trả cho người gửi: " + (tripOrder.getDeliveryNote() == null ? "" : tripOrder.getDeliveryNote()),
                context.trip(),
                context.deliveryContext(),
                now
        )), tenantId);

        return getDeliveryTrips(context.trip().getPostOfficeId(), context.trip().getShift(), context.trip().getTripDate());
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public DeliveryManifestResponse completeDeliveryTrip(Long tripId, Long tenantId) {
        Trip trip = tripRepository.findByIdAndTenantIdForUpdate(tripId, tenantId)
                .filter(candidate -> TripType.DELIVERY.equals(candidate.getTripType()))
                .orElseThrow(() -> new AppException(ErrorCode.INVALID_REQUEST, "Delivery trip not found."));
        ensureCanOperateTrip(tenantId, trip);
        List<TripOrder> tripOrders = tripOrderRepository.findByTenantIdAndTrip_IdOrderBySequenceNoAsc(tenantId, tripId);
        if (tripOrders.isEmpty()) {
            throw new AppException(ErrorCode.INVALID_REQUEST, "Delivery trip has no orders.");
        }

        Map<Long, TmsOrderOperationView> orderById = tmsOrderClient.lookupByIds(
                        tenantId,
                        tripOrders.stream().map(TripOrder::getOrderId).filter(Objects::nonNull).toList()
                )
                .stream()
                .collect(Collectors.toMap(TmsOrderOperationView::getId, Function.identity()));
        boolean hasOpenOrders = tripOrders.stream()
                .map(tripOrder -> resolveDeliveryStatus(tripOrder, orderById.get(tripOrder.getOrderId())))
                .anyMatch(status -> status == DeliveryOrderStatus.PENDING || status == DeliveryOrderStatus.OUT_FOR_DELIVERY);
        if (hasOpenOrders) {
            throw new AppException(ErrorCode.INVALID_REQUEST, "All delivery orders must be delivered, failed, or returned before completing the trip.");
        }

        DeliveryContext context = resolveDeliveryContext(trip, tenantId);
        DeliveryManifest manifest = buildManifestFromTrip(trip, tripOrders, orderById, context, tenantId);
        DeliveryManifest savedManifest = deliveryManifestRepository.save(manifest);
        linkDeliveryCheckinsToManifest(savedManifest, tripOrders, tenantId);

        trip.setStatus(TripStatus.COMPLETED);
        tripRepository.save(trip);

        return toManifestResponse(savedManifest);
    }

    private DeliveryAssignmentResponse forceManualAssignDeliveryOrders(
            ManualAssignDeliveryOrdersRequest request,
            Long tenantId,
            ShiftPlanningWindow shiftPlanningWindow
    ) {
        PostOffice postOffice = lockPostOfficeAndValidateScope(request.getPostOfficeId(), tenantId);
        PostOfficeStaff courier = postOfficeStaffRepository.findByIdAndTenantId(request.getCourierStaffId(), tenantId)
                .orElseThrow(() -> new AppException(ErrorCode.POST_OFFICE_STAFF_NOT_FOUND));

        Set<Long> requestedOrderIds = normalizeDistinctOrderIds(request.getOrderIds());
        loadOrdersByIdMapOrThrow(requestedOrderIds, tenantId);

        Trip trip = tripRepository.findFirstByTenantIdAndTripTypeAndPostOfficeIdAndCourierStaffIdAndTripDateAndShiftAndStatusIn(
                tenantId,
                TripType.DELIVERY,
                postOffice.getId(),
                courier.getId(),
                shiftPlanningWindow.tripDate(),
                request.getShift(),
                REPLANNABLE_DELIVERY_TRIP_STATUSES
        ).orElse(null);
        boolean createdTrip = trip == null;
        if (trip == null) {
            trip = new Trip();
        }
        if (trip.getTripCode() == null || trip.getTripCode().isBlank()) {
            trip.setTripCode(generateTripCode(shiftPlanningWindow.tripDate(), request.getShift(), courier.getCode()));
        }

        List<Vehicle> activeVehicles = vehicleRepository.findByTenantIdAndPostOffice_IdAndStatusIn(
                tenantId,
                postOffice.getId(),
                List.of(VehicleStatus.ACTIVE)
        );
        if (trip.getVehicleId() == null) {
            trip.setVehicleId(resolveForceVehicleId(activeVehicles, courier.getId()));
        }
        String vehicleLicensePlate = resolveVehicleLicensePlate(activeVehicles, trip.getVehicleId());

        LinkedHashSet<Long> finalTripOrderIds = new LinkedHashSet<>();
        if (trip.getId() != null) {
            List<TripOrder> existingTripOrders = tripOrderRepository.findByTrip_IdOrderBySequenceNoAsc(trip.getId());
            for (TripOrder existingTripOrder : existingTripOrders) {
                if (existingTripOrder.getOrderId() != null) {
                    finalTripOrderIds.add(existingTripOrder.getOrderId());
                }
            }
        }
        finalTripOrderIds.addAll(requestedOrderIds);

        trip.setTenantId(tenantId);
        trip.setTripType(TripType.DELIVERY);
        trip.setPostOfficeId(postOffice.getId());
        trip.setCourierStaffId(courier.getId());
        trip.setShift(request.getShift());
        trip.setTripDate(shiftPlanningWindow.tripDate());
        trip.setPlannedStartTime(shiftPlanningWindow.planningStartTime());
        trip.setPlannedEndTime(shiftPlanningWindow.planningEndTime());
        trip.setStatus(TripStatus.PLANNED);
        trip.setTotalOrders(finalTripOrderIds.size());
        trip.setTotalDistanceKm(0.0);
        trip.setTotalTravelMinutes(0L);
        Trip savedTrip = tripRepository.save(trip);

        tripOrderRepository.deleteByTenantIdAndOrderIdInAndTripStatusInAndTripIdNot(
                tenantId,
                requestedOrderIds,
                TripType.DELIVERY,
                ACTIVE_DELIVERY_TRIP_STATUSES,
                savedTrip.getId()
        );

        tripOrderRepository.deleteByTrip_Id(savedTrip.getId());
        List<TripOrder> forceTripOrders = new ArrayList<>();
        int sequence = 1;
        for (Long orderId : finalTripOrderIds) {
            TripOrder tripOrder = new TripOrder();
            tripOrder.setTenantId(tenantId);
            tripOrder.setTrip(savedTrip);
            tripOrder.setOrderId(orderId);
            tripOrder.setSequenceNo(sequence++);
            forceTripOrders.add(tripOrder);
        }
        if (!forceTripOrders.isEmpty()) {
            tripOrderRepository.saveAll(forceTripOrders);
        }

        Map<Long, TmsOrderOperationView> finalOrderById = loadOrdersByIdMapOrThrow(finalTripOrderIds, tenantId);
        DeliveryContext deliveryContext = new DeliveryContext(
                postOffice.getCode(),
                postOffice.getName(),
                courier.getCode(),
                courier.getFullName(),
                vehicleLicensePlate
        );
        enqueueTransitions(
                finalOrderById.values().stream()
                        .filter(Objects::nonNull)
                        .filter(order -> isDeliveryAssignmentTransitionStatus(order.getStatus()))
                        .map(this::toDeliveryOrderNode)
                        .filter(Objects::nonNull)
                        .map(order -> toDeliveryAssignmentTransitionItem(order, savedTrip, deliveryContext, LocalDateTime.now()))
                        .toList(),
                tenantId
        );
        staffNotificationEventPublisher.publishDeliveryTripAssigned(courier, savedTrip, tenantId);
        List<DeliveryAssignmentResponse.DeliveryStopResponse> stopResponses = new ArrayList<>();
        int stopSequence = 1;
        for (Long orderId : finalTripOrderIds) {
            TmsOrderOperationView order = finalOrderById.get(orderId);
            stopResponses.add(new DeliveryAssignmentResponse.DeliveryStopResponse(
                    stopSequence++,
                    orderId,
                    order == null ? null : order.getOrderCode(),
                    order == null ? null : order.getCustomerOrderCode(),
                    order == null ? null : order.getReceiverName(),
                    order == null ? null : order.getReceiverPhone(),
                    order == null ? null : order.getReceiverLatitude(),
                    order == null ? null : order.getReceiverLongitude(),
                    null,
                    null,
                    null,
                    null,
                    null,
                    null,
                    null,
                    order == null ? null : order.getStatus(),
                    null,
                    order == null ? null : order.getReceiverAddressDetail(),
                    order == null ? null : order.getReceiverWardCode(),
                    order == null ? null : order.getReceiverProvinceCode(),
                    order == null ? 0L : safeAmount(order.getCodAmount()),
                    order == null ? 0L : safeAmount(order.getTotalShippingFee()),
                    order == null ? null : order.getFeePayer(),
                    PaymentStatus.UNPAID,
                    0L,
                    null,
                    null,
                    0,
                    0L,
                    0L,
                    null,
                    null,
                    null,
                    null,
                    null,
                    null,
                    null,
                    null,
                    null,
                    null
            ));
        }

        DeliveryAssignmentResponse.DeliveryTripResponse assignedTripResponse =
                new DeliveryAssignmentResponse.DeliveryTripResponse(
                        savedTrip.getId(),
                        savedTrip.getTripCode(),
                        courier.getId(),
                        courier.getCode(),
                        courier.getFullName(),
                        savedTrip.getVehicleId(),
                        vehicleLicensePlate,
                        stopResponses.size(),
                        round3(savedTrip.getTotalDistanceKm()),
                        savedTrip.getTotalTravelMinutes(),
                        0L,
                        0L,
                        savedTrip.getPlannedStartTime(),
                        savedTrip.getPlannedEndTime(),
                        stopResponses
                );

        return new DeliveryAssignmentResponse(
                postOffice.getId(),
                postOffice.getCode(),
                postOffice.getName(),
                request.getShift(),
                shiftPlanningWindow.tripDate(),
                requestedOrderIds.size(),
                requestedOrderIds.size(),
                0,
                createdTrip ? 1 : 0,
                List.of(assignedTripResponse),
                List.of()
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
        List<TmsOrderStatusTransitionRequest.Item> assignmentTransitionItems = new ArrayList<>();

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
            DeliveryContext deliveryContext = new DeliveryContext(
                    postOffice.getCode(),
                    postOffice.getName(),
                    route.courierCode(),
                    route.courierName(),
                    route.vehicleLicensePlate()
            );
            LocalDateTime assignmentEventTime = LocalDateTime.now();
            routeEvaluation.stopDetails().stream()
                    .map(StopEvaluationData::order)
                    .filter(Objects::nonNull)
                    .map(order -> toDeliveryAssignmentTransitionItem(
                            order,
                            savedTrip,
                            deliveryContext,
                            assignmentEventTime
                    ))
                    .forEach(assignmentTransitionItems::add);

            if (created) {
                createdTrips += 1;
            }
            publishDeliveryAssignmentNotification(route.courierStaffId(), savedTrip, tenantId);
            tripResponses.add(toAssignedTripResponse(savedTrip, route, routeEvaluation));
        }

        enqueueTransitions(assignmentTransitionItems, tenantId);
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
        List<Long> tripOrderIds = tripOrders.stream()
                .map(TripOrder::getId)
                .filter(Objects::nonNull)
                .toList();
        Map<Long, Checkin> deliveryCheckinByTripOrderId = tripOrderIds.isEmpty()
                ? Map.of()
                : checkinRepository.findByTenantIdAndCheckinTypeAndTripOrderIdIn(
                                tenantId,
                                CheckinType.DELIVERY,
                                tripOrderIds
                        )
                        .stream()
                        .filter(checkin -> checkin.getTripOrderId() != null)
                        .collect(Collectors.toMap(Checkin::getTripOrderId, Function.identity(), (left, right) -> left));
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
                Checkin deliveryCheckin = deliveryCheckinByTripOrderId.get(tripOrder.getId());
                stops.add(toDeliveryStopResponse(tripOrder, order, deliveryCheckin));
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

    private PickupOptimizationResponse buildOptimizationResponse(
            PostOffice postOffice,
            AlgorithmConfig config,
            SolutionState solution,
            SolutionEvaluation solutionEvaluation
    ) {
        List<PickupOptimizationResponse.PickupRoutePlanResponse> routeResponses = new ArrayList<>();
        for (int index = 0; index < solution.routes().size(); index++) {
            RouteState route = solution.routes().get(index);
            RouteEvaluation routeEvaluation = solutionEvaluation.routeEvaluations().get(index);

            List<PickupOptimizationResponse.PickupStopResponse> stopResponses = new ArrayList<>();
            for (StopEvaluationData stop : routeEvaluation.stopDetails()) {
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
                        null,
                        null,
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
                    round3(routeEvaluation.totalWeight()),
                    round3(routeEvaluation.totalVolume()),
                    round3(routeEvaluation.totalDistanceKm()),
                    routeEvaluation.totalTravelMinutes(),
                    routeEvaluation.totalServiceMinutes(),
                    routeEvaluation.totalLatenessMinutes(),
                    routeEvaluation.routeStartTime(),
                    routeEvaluation.routeEndTime(),
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
                .comparing(PickupOptimizationResponse.UnassignedPickupOrderResponse::orderId,
                        Comparator.nullsLast(Comparator.naturalOrder()))
                .thenComparing(PickupOptimizationResponse.UnassignedPickupOrderResponse::customerOrderCode,
                        Comparator.nullsLast(String::compareToIgnoreCase))
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
                    null,
                    null,
                    null,
                    null,
                    null,
                    null,
                    null,
                    null,
                    null,
                    PaymentStatus.UNPAID,
                    0L,
                    null,
                    null,
                    0,
                    0L,
                    0L,
                    null,
                    null,
                    null,
                    null,
                    null,
                    null,
                    null,
                    null,
                    null,
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
            TmsOrderOperationView order,
            Checkin deliveryCheckin
    ) {
        Point checkinLocation = deliveryCheckin == null ? null : deliveryCheckin.getCheckinLocation();
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
                tripOrder.getScanOutTime(),
                order == null ? null : order.getStatus(),
                resolveDeliveryStatus(tripOrder, order),
                order == null ? null : order.getReceiverAddressDetail(),
                order == null ? null : order.getReceiverWardCode(),
                order == null ? null : order.getReceiverProvinceCode(),
                order == null ? null : safeAmount(order.getCodAmount()),
                order == null ? null : safeAmount(order.getTotalShippingFee()),
                order == null ? null : order.getFeePayer(),
                tripOrder.getDeliveryPaymentStatus(),
                tripOrder.getDeliveryPaymentAmount(),
                tripOrder.getDeliveryPaymentAppTransId(),
                tripOrder.getDeliveryPaymentConfirmedAt(),
                tripOrder.getDeliveryAttemptCount(),
                tripOrder.getCodCollected(),
                tripOrder.getShippingFeeCollected(),
                tripOrder.getFailureReason(),
                tripOrder.getDeliveryNote(),
                tripOrder.getDeliveredAt(),
                tripOrder.getReturnedAt(),
                deliveryCheckin == null ? null : deliveryCheckin.getId(),
                deliveryCheckin == null ? null : deliveryCheckin.getCheckinTime(),
                checkinLocation == null ? null : round3(checkinLocation.getY()),
                checkinLocation == null ? null : round3(checkinLocation.getX()),
                deliveryCheckin == null ? null : deliveryCheckin.getDistanceM(),
                deliveryCheckin == null ? null : deliveryCheckin.getPhotoUrl()
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

    private AlgorithmConfig buildConfig(ManualAssignDeliveryOrdersRequest request, ShiftPlanningWindow window) {
        GoalPreset goalPreset = resolveGoalPreset(request.getOptimizationGoal());
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
        int matrixBatchSize = resolvePositiveInt(distanceMatrixBatchSize, DEFAULT_DISTANCE_MATRIX_BATCH_SIZE);
        int matrixMaxNodes = resolvePositiveInt(distanceMatrixMaxNodes, DEFAULT_DISTANCE_MATRIX_MAX_NODES);

        return new AlgorithmConfig(
                window.planningStartTime(),
                window.planningEndTime(),
                window.tripDate(),
                request.getOrderIds() == null ? DEFAULT_ORDER_LIMIT : Math.max(request.getOrderIds().size(), 1),
                averageSpeedKmph,
                serviceMinutesPerStop,
                allowLateness,
                enforcePlanningEnd,
                enforceCapacity,
                resolveNonNegativeDouble(goalPreset.distanceWeight(), DEFAULT_DISTANCE_WEIGHT),
                resolveNonNegativeDouble(goalPreset.latenessWeight(), DEFAULT_LATENESS_WEIGHT),
                resolveNonNegativeDouble(goalPreset.unassignedPenalty(), DEFAULT_UNASSIGNED_PENALTY),
                resolveNonNegativeDouble(goalPreset.usedRoutePenalty(), DEFAULT_USED_ROUTE_PENALTY),
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

    private void validateManualDeliveryOrder(
            TmsOrderOperationView order,
            String postOfficeCode,
            boolean alreadyInCurrentTrip
    ) {
        OrderStatus status = order.getStatus();
        if (alreadyInCurrentTrip) {
            boolean validExistingStatus = OrderStatus.READY_FOR_DELIVERY.equals(status)
                    || OrderStatus.DELIVERY_FAILED.equals(status)
                    || OrderStatus.OUT_FOR_DELIVERY.equals(status);
            if (!validExistingStatus) {
                throw new AppException(ErrorCode.ORDER_NOT_ASSIGNABLE);
            }
        } else if (!List.of(OrderStatus.READY_FOR_DELIVERY, OrderStatus.DELIVERY_FAILED).contains(status)) {
            throw new AppException(ErrorCode.ORDER_NOT_ASSIGNABLE);
        }

        String destinationPostOfficeCode = order.getDestinationPostOfficeCode();
        if (postOfficeCode == null
                || destinationPostOfficeCode == null
                || !postOfficeCode.equalsIgnoreCase(destinationPostOfficeCode)) {
            throw new AppException(ErrorCode.ORDER_NOT_ASSIGNABLE);
        }

        if (toDeliveryOrderNode(order) == null) {
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

        List<PostOfficeStaffAssignment> assignments = postOfficeStaffAssignmentRepository
                .findActiveAssignmentsByStaffIdAndPostOfficeIdAndTenantId(
                        courier.getId(),
                        postOfficeId,
                        tenantId,
                        planningDate
                );
        if (assignments.isEmpty()) {
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

    private Map<Long, TmsOrderOperationView> loadOrdersByIdMapOrThrow(Collection<Long> orderIds, Long tenantId) {
        Map<Long, TmsOrderOperationView> orderById = new HashMap<>();
        List<TmsOrderOperationView> orders = tmsOrderClient.lookupByIds(tenantId, orderIds);
        for (TmsOrderOperationView order : orders) {
            if (order != null && order.getId() != null) {
                orderById.put(order.getId(), order);
            }
        }

        for (Long orderId : orderIds) {
            if (orderId != null && !orderById.containsKey(orderId)) {
                throw new AppException(ErrorCode.ORDER_NOT_FOUND);
            }
        }

        return orderById;
    }

    private Long resolveForceVehicleId(List<Vehicle> activeVehicles, Long courierStaffId) {
        if (activeVehicles == null || activeVehicles.isEmpty()) {
            return null;
        }

        for (Vehicle vehicle : activeVehicles) {
            if (vehicle == null || vehicle.getId() == null) {
                continue;
            }
            if (courierStaffId != null && Objects.equals(vehicle.getPostOfficeStaffId(), courierStaffId)) {
                return vehicle.getId();
            }
        }

        for (Vehicle vehicle : activeVehicles) {
            if (vehicle != null && vehicle.getId() != null) {
                return vehicle.getId();
            }
        }

        return null;
    }

    private String resolveVehicleLicensePlate(List<Vehicle> activeVehicles, Long vehicleId) {
        if (vehicleId == null || activeVehicles == null || activeVehicles.isEmpty()) {
            return null;
        }

        for (Vehicle vehicle : activeVehicles) {
            if (vehicle == null || vehicle.getId() == null) {
                continue;
            }
            if (Objects.equals(vehicle.getId(), vehicleId)) {
                return vehicle.getLicensePlate();
            }
        }

        return null;
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

    private TmsOrderStatusTransitionRequest.Item toDeliveryAssignmentTransitionItem(
            PickupOrderNode order,
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
                .latitude(order.latitude())
                .longitude(order.longitude())
                .locationLabel("Điều phối giao hàng")
                .build();

        return TmsOrderStatusTransitionRequest.Item.builder()
                .orderId(order.orderId())
                .orderCode(order.orderCode())
                .expectedStatuses(List.of(
                        OrderStatus.READY_FOR_DELIVERY,
                        OrderStatus.DELIVERY_FAILED,
                        OrderStatus.OUT_FOR_DELIVERY
                ))
                .targetStatus(OrderStatus.OUT_FOR_DELIVERY)
                .description("Đơn hàng đã được điều phối cho bưu tá giao hàng.")
                .recordTimelineWhenUnchanged(true)
                .context(transitionContext)
                .build();
    }

    private boolean isDeliveryAssignmentTransitionStatus(OrderStatus status) {
        return status == OrderStatus.READY_FOR_DELIVERY
                || status == OrderStatus.DELIVERY_FAILED
                || status == OrderStatus.OUT_FOR_DELIVERY;
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

    private void publishDeliveryAssignmentNotification(Long courierStaffId, Trip trip, Long tenantId) {
        if (courierStaffId == null || trip == null) {
            return;
        }
        postOfficeStaffRepository.findByIdAndTenantId(courierStaffId, tenantId)
                .ifPresent(courier -> staffNotificationEventPublisher.publishDeliveryTripAssigned(courier, trip, tenantId));
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

    private DeliveryTripOrderContext resolveTripOrderContextForUpdate(Long tripId, String orderCode, Long tenantId) {
        Trip trip = tripRepository.findByIdAndTenantIdForUpdate(tripId, tenantId)
                .filter(candidate -> TripType.DELIVERY.equals(candidate.getTripType()))
                .orElseThrow(() -> new AppException(ErrorCode.INVALID_REQUEST, "Delivery trip not found."));
        ensureCanOperateTrip(tenantId, trip);
        if (!ACTIVE_DELIVERY_TRIP_STATUSES.contains(trip.getStatus())) {
            throw new AppException(ErrorCode.INVALID_REQUEST, "Delivery trip is not active.");
        }

        String normalizedOrderCode = normalizeText(orderCode);
        if (normalizedOrderCode == null) {
            throw new AppException(ErrorCode.INVALID_REQUEST, "orderCode is required.");
        }

        TmsOrderOperationView order = tmsOrderClient.lookupByCodes(tenantId, List.of(normalizedOrderCode)).stream()
                .findFirst()
                .orElseThrow(() -> new AppException(ErrorCode.ORDER_NOT_FOUND));
        TripOrder tripOrder = tripOrderRepository
                .findByTenantIdAndTrip_IdAndOrderIdAndTrip_TripType(tenantId, trip.getId(), order.getId(), TripType.DELIVERY)
                .orElseThrow(() -> new AppException(
                        ErrorCode.INVALID_REQUEST,
                        "Order is not assigned to this delivery trip."
                ));
        return new DeliveryTripOrderContext(trip, tripOrder, order, resolveDeliveryContext(trip, tenantId));
    }

    private void ensureTripOrderOutForDelivery(TripOrder tripOrder) {
        if (tripOrder.getScanOutTime() == null) {
            throw new AppException(ErrorCode.DELIVERY_ORDER_INVALID_STATUS, "Order must be scanned out before delivery action.");
        }
        DeliveryOrderStatus status = tripOrder.getDeliveryStatus();
        if (status != null && status != DeliveryOrderStatus.OUT_FOR_DELIVERY && status != DeliveryOrderStatus.PENDING) {
            throw new AppException(ErrorCode.DELIVERY_ORDER_INVALID_STATUS, "Order is already processed.");
        }
    }

    private void ensureTripOrderReadyForDeliveryAction(
            TripOrder tripOrder,
            TmsOrderOperationView order,
            LocalDateTime actionTime
    ) {
        if (tripOrder.getScanOutTime() == null) {
            if (!List.of(OrderStatus.READY_FOR_DELIVERY, OrderStatus.DELIVERY_FAILED, OrderStatus.OUT_FOR_DELIVERY)
                    .contains(order.getStatus())) {
                throw new AppException(ErrorCode.ORDER_NOT_ASSIGNABLE);
            }
            tripOrder.setScanOutTime(actionTime);
        }

        DeliveryOrderStatus status = tripOrder.getDeliveryStatus();
        if (status != null && status != DeliveryOrderStatus.OUT_FOR_DELIVERY && status != DeliveryOrderStatus.PENDING) {
            throw new AppException(ErrorCode.DELIVERY_ORDER_INVALID_STATUS, "Order is already processed.");
        }
        if (status == null || status == DeliveryOrderStatus.PENDING) {
            tripOrder.setDeliveryStatus(DeliveryOrderStatus.OUT_FOR_DELIVERY);
        }
    }

    private DeliveryOrderStatus resolveDeliveryStatus(TripOrder tripOrder, TmsOrderOperationView order) {
        if (tripOrder.getDeliveryStatus() != null) {
            return tripOrder.getDeliveryStatus();
        }
        if (tripOrder.getScanOutTime() != null) {
            return DeliveryOrderStatus.OUT_FOR_DELIVERY;
        }
        if (order != null && OrderStatus.DELIVERED.equals(order.getStatus())) {
            return DeliveryOrderStatus.DELIVERED;
        }
        if (order != null && OrderStatus.DELIVERY_FAILED.equals(order.getStatus())) {
            return DeliveryOrderStatus.FAILED;
        }
        if (order != null && OrderStatus.RETURNED_TO_SENDER.equals(order.getStatus())) {
            return DeliveryOrderStatus.RETURNED;
        }
        return DeliveryOrderStatus.PENDING;
    }

    private void validateDeliveryCheckinRequest(ConfirmDeliveryRequest request, MultipartFile photo) {
        if (request == null) {
            throw new AppException(ErrorCode.INVALID_REQUEST, "Delivery check-in request is required.");
        }
        if (request.getLatitude() == null || request.getLongitude() == null) {
            throw new AppException(ErrorCode.INVALID_REQUEST, "latitude and longitude are required.");
        }
        if (!isValidCoordinate(request.getLatitude(), request.getLongitude())) {
            throw new AppException(ErrorCode.INVALID_REQUEST, "Invalid delivery check-in coordinates.");
        }
        if (photo == null || photo.isEmpty()) {
            throw new AppException(ErrorCode.FILE_UPLOAD_EMPTY);
        }
    }

    private void validateDeliveryPayment(TripOrder tripOrder, TmsOrderOperationView order) {
        long requiredPayment = requiredDeliveryPaymentAmount(order);
        if (requiredPayment > 0 && !PaymentStatus.PAID.equals(tripOrder.getDeliveryPaymentStatus())) {
            throw new AppException(
                    ErrorCode.INVALID_REQUEST,
                    "Customer payment must be confirmed through payment service before delivery check-in."
            );
        }
        ensureConfirmedPaymentAmountCoversRequiredAmount(tripOrder, requiredPayment);
    }

    private long requiredDeliveryPaymentAmount(TmsOrderOperationView order) {
        return safeAmount(order == null ? null : order.getCodAmount()) + requiredReceiverShippingFee(order);
    }

    private long requiredReceiverShippingFee(TmsOrderOperationView order) {
        if (order == null || !RECEIVER.equalsIgnoreCase(order.getFeePayer())) {
            return 0L;
        }
        return safeAmount(order.getTotalShippingFee());
    }

    private long safeAmount(Long amount) {
        return amount != null ? amount : 0L;
    }

    private int safeInteger(Integer value) {
        return value != null ? value : 0;
    }

    private void ensureConfirmedPaymentAmountCoversRequiredAmount(TripOrder tripOrder, long requiredPayment) {
        if (requiredPayment > 0 && safeAmount(tripOrder.getDeliveryPaymentAmount()) < requiredPayment) {
            throw new AppException(ErrorCode.INVALID_REQUEST, "Confirmed customer payment amount is insufficient.");
        }
    }

    private PaymentCreateOrderRequest buildTripDeliveryPaymentRequest(
            DeliveryTripOrderContext context,
            long requiredAmount,
            Long tenantId
    ) {
        Long actorId = firstMileAccessUtils.getCurrentUserIdOrNull();
        String orderCode = context.order().getOrderCode();
        return PaymentCreateOrderRequest.builder()
                .appUser(orderCode)
                .amount(requiredAmount)
                .description("Thanh toán của khách hàng cho đơn giao hàng " + orderCode)
                .embedData(PaymentCreateOrderRequest.EmbedData.builder()
                        .redirectUrl(paymentRedirectUrl
                                + "?source=first-mile&tripId=" + context.trip().getId()
                                + "&orderCode=" + orderCode)
                        .merchantInfo(buildTripDeliveryPaymentMerchantInfo(context, requiredAmount, tenantId, actorId))
                        .build())
                .title("Delivery payment - " + orderCode)
                .tenantId(tenantId)
                .actorId(actorId)
                .userId(actorId)
                .items(List.of(PaymentCreateOrderRequest.Item.builder()
                        .itemId("delivery-trip-payment-" + orderCode)
                        .itemName("Thanh toán của khách hàng cho đơn giao hàng " + orderCode)
                        .itemPrice(requiredAmount)
                        .itemQuantity(1)
                        .build()))
                .build();
    }

    private String buildTripDeliveryPaymentMerchantInfo(
            DeliveryTripOrderContext context,
            long requiredAmount,
            Long tenantId,
            Long actorId
    ) {
        return """
                {"sourceService":"%s","source":"%s","tenantId":%d,"actorId":%s,"userId":%s,"tripId":%d,"tripOrderId":%d,"orderCode":"%s","amount":%d,"codAmount":%d,"shippingFee":%d}
                """.formatted(
                PAYMENT_SOURCE_SERVICE,
                PAYMENT_SOURCE,
                tenantId,
                actorId == null ? "null" : actorId.toString(),
                actorId == null ? "null" : actorId.toString(),
                context.trip().getId(),
                context.tripOrder().getId(),
                context.order().getOrderCode(),
                requiredAmount,
                safeAmount(context.order().getCodAmount()),
                requiredReceiverShippingFee(context.order())
        ).trim();
    }

    private void markReceiverShippingFeePaidIfNeeded(TmsOrderOperationView order, Long tenantId) {
        if (requiredReceiverShippingFee(order) <= 0L) {
            return;
        }
        try {
            tmsOrderClient.updatePaymentStatus(order.getOrderCode(), tenantId, "PAID");
        } catch (Exception e) {
            log.warn("Failed to update payment status for order {}: {}", order.getOrderCode(), e.getMessage());
        }
    }

    private FileUploadResponse uploadDeliveryCheckinPhoto(MultipartFile photo, String contentType, Long tenantId) {
        try {
            return fileStorageService.upload(FileUploadRequest.builder()
                    .content(photo.getBytes())
                    .originalFileName(photo.getOriginalFilename())
                    .contentType(contentType)
                    .serviceName(STORAGE_SERVICE_NAME)
                    .folder(DELIVERY_CHECKIN_IMAGE_FOLDER)
                    .tenantId(tenantId)
                    .uploaderId(firstMileAccessUtils.getCurrentUserIdOrNull())
                    .publicFile(true)
                    .build());
        } catch (IOException exception) {
            throw new AppException(ErrorCode.FILE_UPLOAD_FAILED);
        }
    }

    private double calculateDeliveryDistanceMeters(
            double checkinLatitude,
            double checkinLongitude,
            Double receiverLatitude,
            Double receiverLongitude
    ) {
        if (receiverLatitude == null
                || receiverLongitude == null
                || !isValidCoordinate(receiverLatitude, receiverLongitude)) {
            return 0D;
        }

        double latitudeDeltaRadians = Math.toRadians(receiverLatitude - checkinLatitude);
        double longitudeDeltaRadians = Math.toRadians(receiverLongitude - checkinLongitude);
        double checkinLatitudeRadians = Math.toRadians(checkinLatitude);
        double receiverLatitudeRadians = Math.toRadians(receiverLatitude);
        double haversineComponent = Math.sin(latitudeDeltaRadians / 2) * Math.sin(latitudeDeltaRadians / 2)
                + Math.cos(checkinLatitudeRadians) * Math.cos(receiverLatitudeRadians)
                * Math.sin(longitudeDeltaRadians / 2) * Math.sin(longitudeDeltaRadians / 2);
        double normalizedHaversineComponent = Math.max(0D, Math.min(1D, haversineComponent));
        double centralAngle = 2 * Math.atan2(
                Math.sqrt(normalizedHaversineComponent),
                Math.sqrt(1D - normalizedHaversineComponent)
        );
        return EARTH_RADIUS_METERS * centralAngle;
    }

    private Point toPoint(double latitude, double longitude) {
        Point point = GEOMETRY_FACTORY.createPoint(new Coordinate(longitude, latitude));
        point.setSRID(4326);
        return point;
    }

    private DeliveryManifest buildManifestFromTrip(
            Trip trip,
            List<TripOrder> tripOrders,
            Map<Long, TmsOrderOperationView> orderById,
            DeliveryContext context,
            Long tenantId
    ) {
        DeliveryManifest manifest = DeliveryManifest.builder()
                .tenantId(tenantId)
                .manifestCode(generateManifestCode(tenantId))
                .postOfficeCode(context.postOfficeCode())
                .courierId(trip.getCourierStaffId())
                .courierName(context.courierName())
                .vehicleId(trip.getVehicleId() == null ? null : trip.getVehicleId().toString())
                .status(DeliveryManifestStatus.COMPLETED)
                .plannedDate(trip.getTripDate())
                .plannedDepartureAt(trip.getPlannedStartTime())
                .actualDepartureAt(resolveActualDepartureAt(tripOrders))
                .actualReturnAt(LocalDateTime.now())
                .totalOrders(tripOrders.size())
                .deliveredCount(0)
                .failedCount(0)
                .totalCodAmount(0L)
                .collectedCodAmount(0L)
                .totalShippingFee(0L)
                .collectedShippingFee(0L)
                .note("Tạo tự động khi kết thúc chuyến " + trip.getTripCode())
                .build();

        long totalCod = 0L;
        long collectedCod = 0L;
        long totalShippingFee = 0L;
        long collectedShippingFee = 0L;
        int deliveredCount = 0;
        int failedCount = 0;

        for (TripOrder tripOrder : tripOrders) {
            TmsOrderOperationView order = orderById.get(tripOrder.getOrderId());
            DeliveryManifestOrder manifestOrder = buildManifestOrderFromTripOrder(tripOrder, order, manifest, tenantId);
            manifest.addOrder(manifestOrder);

            totalCod += safeAmount(order == null ? null : order.getCodAmount());
            totalShippingFee += requiredReceiverShippingFee(order);
            collectedCod += safeAmount(tripOrder.getCodCollected());
            collectedShippingFee += safeAmount(tripOrder.getShippingFeeCollected());
            DeliveryOrderStatus status = manifestOrder.getStatus();
            if (status == DeliveryOrderStatus.DELIVERED) {
                deliveredCount += 1;
            } else if (status == DeliveryOrderStatus.FAILED || status == DeliveryOrderStatus.RETURNED) {
                failedCount += 1;
            }
        }

        manifest.setTotalCodAmount(totalCod);
        manifest.setCollectedCodAmount(collectedCod);
        manifest.setTotalShippingFee(totalShippingFee);
        manifest.setCollectedShippingFee(collectedShippingFee);
        manifest.setDeliveredCount(deliveredCount);
        manifest.setFailedCount(failedCount);
        return manifest;
    }

    private DeliveryManifestOrder buildManifestOrderFromTripOrder(
            TripOrder tripOrder,
            TmsOrderOperationView order,
            DeliveryManifest manifest,
            Long tenantId
    ) {
        DeliveryOrderStatus status = resolveDeliveryStatus(tripOrder, order);
        return DeliveryManifestOrder.builder()
                .tenantId(tenantId)
                .manifest(manifest)
                .orderId(tripOrder.getOrderId())
                .orderCode(order == null ? String.valueOf(tripOrder.getOrderId()) : order.getOrderCode())
                .sequence(tripOrder.getSequenceNo())
                .deliveryAttemptCount(safeInteger(tripOrder.getDeliveryAttemptCount()))
                .status(status)
                .receiverName(order == null ? null : order.getReceiverName())
                .receiverPhone(order == null ? null : order.getReceiverPhone())
                .receiverAddressDetail(order == null ? null : order.getReceiverAddressDetail())
                .receiverWardCode(order == null ? null : order.getReceiverWardCode())
                .receiverProvinceCode(order == null ? null : order.getReceiverProvinceCode())
                .receiverLat(order == null ? null : order.getReceiverLatitude())
                .receiverLng(order == null ? null : order.getReceiverLongitude())
                .codAmount(safeAmount(order == null ? null : order.getCodAmount()))
                .codCollected(safeAmount(tripOrder.getCodCollected()))
                .shippingFee(requiredReceiverShippingFee(order))
                .shippingFeeCollected(safeAmount(tripOrder.getShippingFeeCollected()))
                .feePayer(order == null ? null : order.getFeePayer())
                .deliveryPaymentStatus(tripOrder.getDeliveryPaymentStatus())
                .deliveryPaymentAmount(tripOrder.getDeliveryPaymentAmount())
                .deliveryPaymentAppTransId(tripOrder.getDeliveryPaymentAppTransId())
                .deliveryPaymentConfirmedAt(tripOrder.getDeliveryPaymentConfirmedAt())
                .failureReason(tripOrder.getFailureReason())
                .deliveredAt(tripOrder.getDeliveredAt())
                .note(tripOrder.getDeliveryNote())
                .build();
    }

    private void linkDeliveryCheckinsToManifest(DeliveryManifest manifest, List<TripOrder> tripOrders, Long tenantId) {
        if (manifest.getOrders() == null || manifest.getOrders().isEmpty()) {
            return;
        }
        Map<Long, Long> tripOrderIdByOrderId = tripOrders.stream()
                .filter(tripOrder -> tripOrder.getOrderId() != null && tripOrder.getId() != null)
                .collect(Collectors.toMap(TripOrder::getOrderId, TripOrder::getId, (left, right) -> left));
        for (DeliveryManifestOrder manifestOrder : manifest.getOrders()) {
            Long tripOrderId = tripOrderIdByOrderId.get(manifestOrder.getOrderId());
            if (tripOrderId == null) {
                continue;
            }
            checkinRepository
                    .findByTenantIdAndCheckinTypeAndTripOrderId(tenantId, CheckinType.DELIVERY, tripOrderId)
                    .ifPresent(checkin -> {
                        checkin.setDeliveryManifestId(manifest.getId());
                        checkin.setDeliveryManifestOrderId(manifestOrder.getId());
                        checkinRepository.save(checkin);
                    });
        }
    }

    private LocalDateTime resolveActualDepartureAt(List<TripOrder> tripOrders) {
        return tripOrders.stream()
                .map(TripOrder::getScanOutTime)
                .filter(Objects::nonNull)
                .min(LocalDateTime::compareTo)
                .orElse(null);
    }

    private String generateManifestCode(Long tenantId) {
        String dateStr = LocalDate.now().toString().replace("-", "");
        String prefix = "DM-" + dateStr + "-" + tenantId + "-";
        return deliveryManifestRepository.findTopByTenantIdAndManifestCodeStartingWithOrderByManifestCodeDesc(tenantId, prefix)
                .map(manifest -> {
                    String code = manifest.getManifestCode();
                    String seqStr = code.substring(prefix.length());
                    int seq = Integer.parseInt(seqStr) + 1;
                    return String.format("%s%05d", prefix, seq);
                })
                .orElse(prefix + "00001");
    }

    private DeliveryManifestResponse toManifestResponse(DeliveryManifest manifest) {
        List<DeliveryManifestOrderResponse> orderResponses = manifest.getOrders() == null
                ? List.of()
                : manifest.getOrders().stream().map(this::toManifestOrderResponse).toList();

        return DeliveryManifestResponse.builder()
                .id(manifest.getId())
                .manifestCode(manifest.getManifestCode())
                .postOfficeCode(manifest.getPostOfficeCode())
                .courierId(manifest.getCourierId())
                .courierName(manifest.getCourierName())
                .vehicleId(manifest.getVehicleId())
                .status(manifest.getStatus())
                .plannedDate(manifest.getPlannedDate())
                .plannedDepartureAt(manifest.getPlannedDepartureAt())
                .actualDepartureAt(manifest.getActualDepartureAt())
                .actualReturnAt(manifest.getActualReturnAt())
                .totalOrders(manifest.getTotalOrders())
                .deliveredCount(manifest.getDeliveredCount())
                .failedCount(manifest.getFailedCount())
                .totalCodAmount(manifest.getTotalCodAmount())
                .collectedCodAmount(manifest.getCollectedCodAmount())
                .totalShippingFee(manifest.getTotalShippingFee())
                .collectedShippingFee(manifest.getCollectedShippingFee())
                .note(manifest.getNote())
                .createdAt(manifest.getCreatedAt())
                .orders(orderResponses)
                .build();
    }

    private DeliveryManifestOrderResponse toManifestOrderResponse(DeliveryManifestOrder order) {
        Checkin deliveryCheckin = order.getOrderId() == null
                ? null
                : checkinRepository.findByTenantIdAndCheckinTypeAndDeliveryManifestOrderId(
                        order.getTenantId(),
                        CheckinType.DELIVERY,
                        order.getId()
                ).orElse(null);
        Point checkinLocation = deliveryCheckin == null ? null : deliveryCheckin.getCheckinLocation();

        return DeliveryManifestOrderResponse.builder()
                .id(order.getId())
                .orderId(order.getOrderId())
                .orderCode(order.getOrderCode())
                .sequence(order.getSequence())
                .deliveryAttemptCount(order.getDeliveryAttemptCount())
                .status(order.getStatus())
                .receiverName(order.getReceiverName())
                .receiverPhone(order.getReceiverPhone())
                .receiverAddressDetail(order.getReceiverAddressDetail())
                .receiverWardCode(order.getReceiverWardCode())
                .receiverProvinceCode(order.getReceiverProvinceCode())
                .receiverLat(order.getReceiverLat())
                .receiverLng(order.getReceiverLng())
                .codAmount(order.getCodAmount())
                .codCollected(order.getCodCollected())
                .shippingFee(order.getShippingFee())
                .shippingFeeCollected(order.getShippingFeeCollected())
                .feePayer(order.getFeePayer())
                .deliveryPaymentStatus(order.getDeliveryPaymentStatus())
                .deliveryPaymentAmount(order.getDeliveryPaymentAmount())
                .deliveryPaymentAppTransId(order.getDeliveryPaymentAppTransId())
                .deliveryPaymentConfirmedAt(order.getDeliveryPaymentConfirmedAt())
                .proofPhotoUrl(deliveryCheckin == null ? order.getProofPhotoUrl() : deliveryCheckin.getPhotoUrl())
                .failureReason(order.getFailureReason())
                .deliveredAt(order.getDeliveredAt())
                .deliveryCheckinLat(checkinLocation == null ? null : round3(checkinLocation.getY()))
                .deliveryCheckinLng(checkinLocation == null ? null : round3(checkinLocation.getX()))
                .deliveryCheckinDistanceM(deliveryCheckin == null ? null : deliveryCheckin.getDistanceM())
                .note(order.getNote())
                .build();
    }

    private AppException invalidRequest(String detail) {
        return new AppException(ErrorCode.INVALID_REQUEST, detail);
    }

    private record DeliveryTripOrderContext(
            Trip trip,
            TripOrder tripOrder,
            TmsOrderOperationView order,
            DeliveryContext deliveryContext
    ) {
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
