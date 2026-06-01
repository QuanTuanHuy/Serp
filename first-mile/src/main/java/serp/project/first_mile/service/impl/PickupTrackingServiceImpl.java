/*
Author: Nguyen The Anh
Description: Part of Serp Project
*/

package serp.project.first_mile.service.impl;

import lombok.RequiredArgsConstructor;
import org.locationtech.jts.geom.Point;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import serp.project.first_mile.domain.Order;
import serp.project.first_mile.domain.PickupCheckin;
import serp.project.first_mile.domain.PostOffice;
import serp.project.first_mile.domain.PostOfficeStaff;
import serp.project.first_mile.domain.Trip;
import serp.project.first_mile.domain.TripOrder;
import serp.project.first_mile.dto.request.ConfirmPostOfficeInboundRequest;
import serp.project.first_mile.dto.response.PickupCheckinDetailResponse;
import serp.project.first_mile.dto.response.PickupTripLifecycleResponse;
import serp.project.first_mile.dto.response.PickupTrackingOverviewResponse;
import serp.project.first_mile.enums.OrderStatus;
import serp.project.first_mile.enums.PostOfficeStaffRole;
import serp.project.first_mile.enums.TripStatus;
import serp.project.first_mile.exception.AppException;
import serp.project.first_mile.exception.ErrorCode;
import serp.project.first_mile.kafka.impl.order.SyncOrder;
import serp.project.first_mile.kernel.utils.FirstMileAccessUtils;
import serp.project.first_mile.repository.OrderRepository;
import serp.project.first_mile.repository.PickupCheckinRepository;
import serp.project.first_mile.repository.PostOfficeRepository;
import serp.project.first_mile.repository.PostOfficeStaffRepository;
import serp.project.first_mile.repository.TripOrderRepository;
import serp.project.first_mile.repository.TripRepository;
import serp.project.first_mile.service.OrderTimelineService;
import serp.project.first_mile.service.PickupTrackingService;
import serp.project.first_mile.service.dto.OrderTimelineContext;

import java.time.LocalDate;
import java.util.ArrayList;
import java.util.Locale;
import java.util.Collection;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Set;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class PickupTrackingServiceImpl implements PickupTrackingService {

    private static final String ACTOR_SCOPE_ADMIN_ALL = "ADMIN_ALL";
    private static final String ACTOR_SCOPE_MANAGER_SCOPED = "MANAGER_SCOPED";
    private static final String ACTOR_SCOPE_COURIER_SELF = "COURIER_SELF";

    private static final List<TripStatus> TRACKABLE_TRIP_STATUSES = List.of(
            TripStatus.PLANNED,
            TripStatus.IN_PROGRESS,
            TripStatus.COMPLETED
    );
    private static final List<OrderStatus> RETURNABLE_ORDER_STATUSES = List.of(
            OrderStatus.PICKING_UP,
            OrderStatus.PICKED_UP
    );
    private static final List<OrderStatus> AUTO_PICKUP_FAILED_CANDIDATE_STATUSES = List.of(
            OrderStatus.ASSIGNED_TO_PICKUP,
            OrderStatus.PICKING_UP
    );

    private final FirstMileAccessUtils firstMileAccessUtils;
    private final TripRepository tripRepository;
    private final TripOrderRepository tripOrderRepository;
    private final OrderRepository orderRepository;
    private final PickupCheckinRepository pickupCheckinRepository;
    private final PostOfficeRepository postOfficeRepository;
    private final PostOfficeStaffRepository postOfficeStaffRepository;
    private final OrderTimelineService orderTimelineService;
    private final SyncOrder syncOrder;

    @Override
    public PickupTrackingOverviewResponse getPickupTrackingOverview(
            LocalDate tripDate,
            Long postOfficeId,
            Long courierStaffId
    ) {
        Long tenantId = firstMileAccessUtils.getCurrentTenantIdOrThrow();
        LocalDate effectiveTripDate = tripDate == null ? LocalDate.now() : tripDate;

        ScopeContext scopeContext = resolveScopeContext(
                tenantId,
                normalizePositiveId(postOfficeId),
                normalizePositiveId(courierStaffId)
        );

        if (scopeContext.visiblePostOfficeIds() != null
                && scopeContext.visiblePostOfficeIds().isEmpty()) {
            return buildEmptyOverview(effectiveTripDate, scopeContext);
        }

        List<Trip> allTrips = tripRepository
                .findByTenantIdAndTripDateAndStatusInOrderByPlannedStartTimeAscIdAsc(
                        tenantId,
                        effectiveTripDate,
                        TRACKABLE_TRIP_STATUSES
                );

        List<Trip> scopedTrips = allTrips.stream()
                .filter(trip -> isTripInScope(trip, scopeContext))
                .toList();

        if (scopedTrips.isEmpty()) {
            return buildEmptyOverview(effectiveTripDate, scopeContext);
        }

        Map<Long, Trip> tripById = scopedTrips.stream()
                .collect(Collectors.toMap(Trip::getId, trip -> trip));

        List<Long> tripIds = scopedTrips.stream()
                .map(Trip::getId)
                .toList();

        List<TripOrder> tripOrders = tripOrderRepository
                .findByTenantIdAndTripIdInOrderByTripIdAscSequenceNoAsc(tenantId, tripIds);

        List<Long> orderIds = tripOrders.stream()
                .map(TripOrder::getOrderId)
                .filter(Objects::nonNull)
                .distinct()
                .toList();

        Map<Long, Order> orderById = orderIds.isEmpty()
                ? Map.of()
                : orderRepository.findByIdInAndTenantId(orderIds, tenantId).stream()
                        .collect(Collectors.toMap(Order::getId, order -> order));

        List<Long> tripOrderIds = tripOrders.stream()
                .map(TripOrder::getId)
                .filter(Objects::nonNull)
                .toList();

        Map<Long, PickupCheckin> pickupCheckinByTripOrderId = tripOrderIds.isEmpty()
                ? Map.of()
                : pickupCheckinRepository.findByTenantIdAndTripOrderIdIn(tenantId, tripOrderIds).stream()
                        .collect(Collectors.toMap(PickupCheckin::getTripOrderId, pickupCheckin -> pickupCheckin));

        Map<Long, PostOffice> postOfficeById = loadPostOfficeById(tenantId, scopedTrips);
        Map<Long, PostOfficeStaff> courierById = loadCourierById(tenantId, scopedTrips);

        Map<Long, TripSummaryAccumulator> tripSummaryByTripId = new HashMap<>();
        for (Trip trip : scopedTrips) {
            tripSummaryByTripId.put(trip.getId(), new TripSummaryAccumulator());
        }

        int totalOrders = 0;
        int checkedInOrders = 0;
        int pendingCheckinOrders = 0;
        int pickingUpOrders = 0;
        int pickedUpOrders = 0;
        int pickupFailedOrders = 0;
        int pendingPostOfficeInboundOrders = 0;

        List<PickupTrackingOverviewResponse.PickupTrackingOrderResponse> orderResponses = new ArrayList<>();

        for (TripOrder tripOrder : tripOrders) {
            if (tripOrder == null || tripOrder.getTrip() == null || tripOrder.getTrip().getId() == null) {
                continue;
            }

            Trip trip = tripById.get(tripOrder.getTrip().getId());
            if (trip == null) {
                continue;
            }

            Order order = orderById.get(tripOrder.getOrderId());
            if (order == null || order.getId() == null) {
                continue;
            }

            PickupCheckin pickupCheckin = pickupCheckinByTripOrderId.get(tripOrder.getId());
            boolean checkedIn = pickupCheckin != null;

            totalOrders += 1;
            if (checkedIn) {
                checkedInOrders += 1;
            } else {
                pendingCheckinOrders += 1;
            }

            if (OrderStatus.PICKING_UP.equals(order.getStatus())) {
                pickingUpOrders += 1;
            }
            if (OrderStatus.PICKED_UP.equals(order.getStatus())) {
                pickedUpOrders += 1;
            }
            if (OrderStatus.PICKUP_FAILED.equals(order.getStatus())) {
                pickupFailedOrders += 1;
            }
            if (OrderStatus.PENDING_ORIGIN_POST_OFFICE_INBOUND.equals(order.getStatus())) {
                pendingPostOfficeInboundOrders += 1;
            }

            TripSummaryAccumulator tripSummary = tripSummaryByTripId.computeIfAbsent(
                    trip.getId(),
                    ignored -> new TripSummaryAccumulator()
            );
            tripSummary.totalOrders += 1;
            if (checkedIn) {
                tripSummary.checkedInOrders += 1;
            } else {
                tripSummary.pendingCheckinOrders += 1;
            }
            if (order.getStatus() != null && RETURNABLE_ORDER_STATUSES.contains(order.getStatus())) {
                tripSummary.returnableToPostOfficeOrders += 1;
            }
            if (OrderStatus.PENDING_ORIGIN_POST_OFFICE_INBOUND.equals(order.getStatus())) {
                tripSummary.pendingPostOfficeInboundOrders += 1;
            }

            PostOffice postOffice = postOfficeById.get(trip.getPostOfficeId());
            PostOfficeStaff courier = courierById.get(trip.getCourierStaffId());

            Point senderLocation = order.getSenderLocation();
            Point checkinLocation = pickupCheckin == null ? null : pickupCheckin.getCheckinLocation();

            orderResponses.add(new PickupTrackingOverviewResponse.PickupTrackingOrderResponse(
                    tripOrder.getId(),
                    trip.getId(),
                    trip.getTripCode(),
                    trip.getStatus(),
                    tripOrder.getSequenceNo(),
                    order.getId(),
                    order.getOrderCode(),
                    order.getCustomerOrderCode(),
                    order.getStatus(),
                    order.getSenderName(),
                    order.getSenderPhone(),
                    order.getSenderAddressDetail(),
                    getLatitude(senderLocation),
                    getLongitude(senderLocation),
                    order.getPickupTimeStart(),
                    order.getPickupTimeEnd(),
                    tripOrder.getPlannedArrivalTime(),
                    tripOrder.getPlannedStartServiceTime(),
                    tripOrder.getPlannedDepartureTime(),
                    trip.getCourierStaffId(),
                    courier == null ? null : courier.getCode(),
                    courier == null ? null : courier.getFullName(),
                    trip.getPostOfficeId(),
                    postOffice == null ? null : postOffice.getCode(),
                    postOffice == null ? null : postOffice.getName(),
                    checkedIn,
                    pickupCheckin == null ? null : pickupCheckin.getId(),
                    pickupCheckin == null ? null : pickupCheckin.getCheckinTime(),
                    getLatitude(checkinLocation),
                    getLongitude(checkinLocation),
                    pickupCheckin == null ? null : pickupCheckin.getPhotoUrl(),
                    pickupCheckin == null ? null : pickupCheckin.getDistanceM(),
                    pickupCheckin == null ? null : pickupCheckin.getAllowedRadiusM()
            ));
        }

        List<PickupTrackingOverviewResponse.PickupTrackingTripResponse> tripResponses = new ArrayList<>();
        for (Trip trip : scopedTrips) {
            TripSummaryAccumulator tripSummary = tripSummaryByTripId.getOrDefault(
                    trip.getId(),
                    new TripSummaryAccumulator()
            );

            PostOffice postOffice = postOfficeById.get(trip.getPostOfficeId());
            PostOfficeStaff courier = courierById.get(trip.getCourierStaffId());

            tripResponses.add(new PickupTrackingOverviewResponse.PickupTrackingTripResponse(
                    trip.getId(),
                    trip.getTripCode(),
                    trip.getStatus(),
                    trip.getShift(),
                    trip.getPostOfficeId(),
                    postOffice == null ? null : postOffice.getCode(),
                    postOffice == null ? null : postOffice.getName(),
                    trip.getCourierStaffId(),
                    courier == null ? null : courier.getCode(),
                    courier == null ? null : courier.getFullName(),
                    trip.getPlannedStartTime(),
                    trip.getPlannedEndTime(),
                    tripSummary.totalOrders,
                    tripSummary.checkedInOrders,
                    tripSummary.pendingCheckinOrders,
                    tripSummary.returnableToPostOfficeOrders,
                    tripSummary.pendingPostOfficeInboundOrders
            ));
        }

        return new PickupTrackingOverviewResponse(
                effectiveTripDate,
                scopeContext.actorScope(),
                scopeContext.selectedPostOfficeId(),
                scopeContext.selectedCourierStaffId(),
                scopedTrips.size(),
                totalOrders,
                checkedInOrders,
                pendingCheckinOrders,
                pickingUpOrders,
                pickedUpOrders,
                pickupFailedOrders,
                pendingPostOfficeInboundOrders,
                tripResponses,
                orderResponses
        );
    }

    @Override
    public PickupCheckinDetailResponse getPickupCheckinDetail(Long orderId, Long tenantId) {
        if (orderId == null || orderId <= 0) {
            throw new AppException(ErrorCode.INVALID_REQUEST, "orderId must be greater than 0.");
        }

        PickupCheckin pickupCheckin = pickupCheckinRepository
                .findByTenantIdAndOrderId(tenantId, orderId)
                .orElseThrow(() -> new AppException(
                        ErrorCode.ORDER_NOT_FOUND,
                        "Pickup check-in not found for this order."
                ));

        Order order = orderRepository.findByIdAndTenantId(orderId, tenantId)
                .orElseThrow(() -> new AppException(ErrorCode.ORDER_NOT_FOUND));

        Trip trip = tripRepository.findByIdAndTenantId(pickupCheckin.getTripId(), tenantId)
                .orElseThrow(() -> new AppException(ErrorCode.INVALID_REQUEST, "Pickup trip is invalid."));

        ensureCanViewPickupCheckin(tenantId, trip, pickupCheckin);

        PostOffice postOffice = trip.getPostOfficeId() == null
                ? null
                : postOfficeRepository.findByIdAndTenantId(trip.getPostOfficeId(), tenantId).orElse(null);

        PostOfficeStaff courier = pickupCheckin.getCourierStaffId() == null
                ? null
                : postOfficeStaffRepository
                        .findByIdAndTenantId(pickupCheckin.getCourierStaffId(), tenantId)
                        .orElse(null);

        Point senderLocation = order.getSenderLocation();
        Point checkinLocation = pickupCheckin.getCheckinLocation();

        return new PickupCheckinDetailResponse(
                pickupCheckin.getId(),
                order.getId(),
                order.getOrderCode(),
                order.getCustomerOrderCode(),
                order.getStatus(),
                trip.getId(),
                trip.getTripCode(),
                trip.getStatus(),
                pickupCheckin.getCourierStaffId(),
                courier == null ? null : courier.getCode(),
                courier == null ? null : courier.getFullName(),
                trip.getPostOfficeId(),
                postOffice == null ? null : postOffice.getCode(),
                postOffice == null ? null : postOffice.getName(),
                order.getSenderName(),
                order.getSenderPhone(),
                order.getSenderAddressDetail(),
                getLatitude(senderLocation),
                getLongitude(senderLocation),
                pickupCheckin.getCheckinTime(),
                getLatitude(checkinLocation),
                getLongitude(checkinLocation),
                pickupCheckin.getPhotoUrl(),
                pickupCheckin.getDistanceM(),
                pickupCheckin.getAllowedRadiusM()
        );
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public PickupTripLifecycleResponse completeTrip(Long tripId, Long tenantId) {
        Trip trip = resolveTripForLifecycle(tripId, tenantId);
        ensureCanManageTrip(tenantId, trip);

        if (TripStatus.CANCELLED.equals(trip.getStatus())) {
            throw new AppException(ErrorCode.INVALID_REQUEST, "Cancelled trip cannot be completed.");
        }

        markUncheckedOrdersAsPickupFailed(trip, tenantId);

        if (!TripStatus.COMPLETED.equals(trip.getStatus())) {
            trip.setStatus(TripStatus.COMPLETED);
            tripRepository.save(trip);
        }

        return buildTripLifecycleResponse(trip, tenantId);
    }

    private void markUncheckedOrdersAsPickupFailed(Trip trip, Long tenantId) {
        List<TripOrder> tripOrders = tripOrderRepository.findByTenantIdAndTrip_IdOrderBySequenceNoAsc(tenantId, trip.getId());
        if (tripOrders.isEmpty()) {
            return;
        }

        List<Long> tripOrderIds = tripOrders.stream()
                .map(TripOrder::getId)
                .filter(Objects::nonNull)
                .toList();

        Set<Long> checkedInTripOrderIds = tripOrderIds.isEmpty()
                ? Set.of()
                : pickupCheckinRepository.findByTenantIdAndTripOrderIdIn(tenantId, tripOrderIds).stream()
                .map(PickupCheckin::getTripOrderId)
                .filter(Objects::nonNull)
                .collect(Collectors.toSet());

        List<Long> uncheckedOrderIds = tripOrders.stream()
                .filter(tripOrder -> tripOrder.getId() != null && !checkedInTripOrderIds.contains(tripOrder.getId()))
                .map(TripOrder::getOrderId)
                .filter(Objects::nonNull)
                .distinct()
                .toList();

        if (uncheckedOrderIds.isEmpty()) {
            return;
        }

        Map<Long, Order> orderById = orderRepository.findByTenantIdAndIdInWithLock(tenantId, uncheckedOrderIds).stream()
                .collect(Collectors.toMap(Order::getId, order -> order));

        PostOffice postOffice = trip.getPostOfficeId() == null
                ? null
                : postOfficeRepository.findByIdAndTenantId(trip.getPostOfficeId(), tenantId).orElse(null);
        PostOfficeStaff courier = trip.getCourierStaffId() == null
                ? null
                : postOfficeStaffRepository.findByIdAndTenantId(trip.getCourierStaffId(), tenantId).orElse(null);

        List<Order> changedOrders = new ArrayList<>();
        for (Long uncheckedOrderId : uncheckedOrderIds) {
            Order order = orderById.get(uncheckedOrderId);
            if (order == null || order.getStatus() == null) {
                continue;
            }
            if (!AUTO_PICKUP_FAILED_CANDIDATE_STATUSES.contains(order.getStatus())) {
                continue;
            }

            order.setStatus(OrderStatus.PICKUP_FAILED);
            changedOrders.add(order);
        }

        if (changedOrders.isEmpty()) {
            return;
        }

        orderRepository.saveAll(changedOrders);

        for (Order changedOrder : changedOrders) {
            orderTimelineService.recordStatusEvent(
                    changedOrder,
                    OrderStatus.PICKUP_FAILED,
                    "Pickup failed because trip was completed before this order was checked in.",
                    new OrderTimelineContext(
                            null,
                            trip.getId(),
                            trip.getTripCode(),
                            trip.getPostOfficeId(),
                            postOffice == null ? null : postOffice.getCode(),
                            postOffice == null ? null : postOffice.getName(),
                            trip.getCourierStaffId(),
                            courier == null ? null : courier.getCode(),
                            courier == null ? null : courier.getFullName(),
                            trip.getVehicleId(),
                            null,
                            null,
                            null,
                            "Trip completion without check-in"
                    )
            );
            syncOrder.sendOrderEvent(changedOrder);
        }
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public PickupTripLifecycleResponse returnTripToPostOffice(Long tripId, Long tenantId) {
        Trip trip = resolveTripForLifecycle(tripId, tenantId);
        ensureCanManageTrip(tenantId, trip);

        if (!TripStatus.COMPLETED.equals(trip.getStatus())) {
            throw new AppException(
                    ErrorCode.INVALID_REQUEST,
                    "Trip must be completed before returning shipment to post office."
            );
        }

        List<TripOrder> tripOrders = tripOrderRepository.findByTenantIdAndTrip_IdOrderBySequenceNoAsc(tenantId, trip.getId());
        if (tripOrders.isEmpty()) {
            return buildTripLifecycleResponse(trip, tenantId);
        }

        List<Long> orderIds = tripOrders.stream()
                .map(TripOrder::getOrderId)
                .filter(Objects::nonNull)
                .distinct()
                .toList();
        if (orderIds.isEmpty()) {
            return buildTripLifecycleResponse(trip, tenantId);
        }

        Map<Long, Order> orderById = orderRepository.findByTenantIdAndIdInWithLock(tenantId, orderIds).stream()
                .collect(Collectors.toMap(Order::getId, order -> order));

        PostOffice postOffice = trip.getPostOfficeId() == null
                ? null
                : postOfficeRepository.findByIdAndTenantId(trip.getPostOfficeId(), tenantId).orElse(null);
        PostOfficeStaff courier = trip.getCourierStaffId() == null
                ? null
                : postOfficeStaffRepository.findByIdAndTenantId(trip.getCourierStaffId(), tenantId).orElse(null);

        List<Order> changedOrders = new ArrayList<>();
        for (TripOrder tripOrder : tripOrders) {
            Order order = orderById.get(tripOrder.getOrderId());
            if (order == null || order.getStatus() == null || !RETURNABLE_ORDER_STATUSES.contains(order.getStatus())) {
                continue;
            }

            order.setStatus(OrderStatus.PENDING_ORIGIN_POST_OFFICE_INBOUND);
            if (order.getOriginPostOfficeCode() == null && postOffice != null) {
                order.setOriginPostOfficeCode(postOffice.getCode());
            }
            changedOrders.add(order);
        }

        if (changedOrders.isEmpty()) {
            throw new AppException(ErrorCode.INVALID_REQUEST, "No pickup orders are ready to return to post office.");
        }

        orderRepository.saveAll(changedOrders);

        for (Order changedOrder : changedOrders) {
            orderTimelineService.recordStatusEvent(
                    changedOrder,
                    OrderStatus.PENDING_ORIGIN_POST_OFFICE_INBOUND,
                    "Courier returned shipment to origin post office. Awaiting post office inbound scan.",
                    new OrderTimelineContext(
                            null,
                            trip.getId(),
                            trip.getTripCode(),
                            trip.getPostOfficeId(),
                            postOffice == null ? null : postOffice.getCode(),
                            postOffice == null ? null : postOffice.getName(),
                            trip.getCourierStaffId(),
                            courier == null ? null : courier.getCode(),
                            courier == null ? null : courier.getFullName(),
                            trip.getVehicleId(),
                            null,
                            null,
                            null,
                            "Origin post office"
                    )
            );
            syncOrder.sendOrderEvent(changedOrder);
        }

        return buildTripLifecycleResponse(trip, tenantId);
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public PickupTripLifecycleResponse confirmPostOfficeInbound(
            Long tripId,
            ConfirmPostOfficeInboundRequest request,
            Long tenantId
    ) {
        Trip trip = resolveTripForLifecycle(tripId, tenantId);
        ensureCanConfirmPostOfficeInbound(tenantId, trip);

        if (!TripStatus.COMPLETED.equals(trip.getStatus())) {
            throw new AppException(
                    ErrorCode.INVALID_REQUEST,
                    "Trip must be completed before confirming post office inbound."
            );
        }

        List<String> requestedOrderCodes = normalizeOrderCodes(request == null ? null : request.getOrderCodes());
        if (requestedOrderCodes.isEmpty()) {
            throw new AppException(ErrorCode.INVALID_REQUEST, "At least one order code is required.");
        }

        List<TripOrder> tripOrders = tripOrderRepository.findByTenantIdAndTrip_IdOrderBySequenceNoAsc(tenantId, trip.getId());
        if (tripOrders.isEmpty()) {
            throw new AppException(ErrorCode.INVALID_REQUEST, "Pickup trip has no orders.");
        }

        List<Long> orderIds = tripOrders.stream()
                .map(TripOrder::getOrderId)
                .filter(Objects::nonNull)
                .distinct()
                .toList();
        Map<Long, Order> orderById = orderRepository.findByTenantIdAndIdInWithLock(tenantId, orderIds).stream()
                .collect(Collectors.toMap(Order::getId, order -> order));

        PostOffice postOffice = trip.getPostOfficeId() == null
                ? null
                : postOfficeRepository.findByIdAndTenantId(trip.getPostOfficeId(), tenantId).orElse(null);
        PostOfficeStaff manager = resolveCurrentManagerStaff(tenantId);

        List<Order> changedOrders = new ArrayList<>();
        for (String orderCode : requestedOrderCodes) {
            Order matchedOrder = orderById.values().stream()
                    .filter(order -> order.getOrderCode() != null
                            && order.getOrderCode().equalsIgnoreCase(orderCode))
                    .findFirst()
                    .orElse(null);
            if (matchedOrder == null) {
                throw new AppException(
                        ErrorCode.INVALID_REQUEST,
                        "Order " + orderCode + " is not assigned to this pickup trip."
                );
            }
            if (!OrderStatus.PENDING_ORIGIN_POST_OFFICE_INBOUND.equals(matchedOrder.getStatus())) {
                throw new AppException(
                        ErrorCode.INVALID_REQUEST,
                        "Order " + orderCode + " is not pending post office inbound confirmation."
                );
            }
            if (changedOrders.stream().anyMatch(order -> Objects.equals(order.getId(), matchedOrder.getId()))) {
                continue;
            }

            matchedOrder.setStatus(OrderStatus.AT_ORIGIN_POST_OFFICE);
            if (matchedOrder.getOriginPostOfficeCode() == null && postOffice != null) {
                matchedOrder.setOriginPostOfficeCode(postOffice.getCode());
            }
            changedOrders.add(matchedOrder);
        }

        if (changedOrders.isEmpty()) {
            throw new AppException(ErrorCode.INVALID_REQUEST, "No orders are ready for post office inbound confirmation.");
        }

        orderRepository.saveAll(changedOrders);

        for (Order changedOrder : changedOrders) {
            orderTimelineService.recordStatusEvent(
                    changedOrder,
                    OrderStatus.AT_ORIGIN_POST_OFFICE,
                    "Post office confirmed inbound receiving.",
                    new OrderTimelineContext(
                            null,
                            trip.getId(),
                            trip.getTripCode(),
                            trip.getPostOfficeId(),
                            postOffice == null ? null : postOffice.getCode(),
                            postOffice == null ? null : postOffice.getName(),
                            manager == null ? null : manager.getId(),
                            manager == null ? null : manager.getCode(),
                            manager == null ? null : manager.getFullName(),
                            trip.getVehicleId(),
                            null,
                            null,
                            null,
                            "Origin post office inbound"
                    )
            );
            syncOrder.sendOrderEvent(changedOrder);
        }

        return buildTripLifecycleResponse(trip, tenantId);
    }

    private void ensureCanViewPickupCheckin(Long tenantId, Trip trip, PickupCheckin pickupCheckin) {
        if (firstMileAccessUtils.isAdmin()) {
            return;
        }

        if (firstMileAccessUtils.isPostOfficerManager()) {
            Set<Long> managedPostOfficeIds = firstMileAccessUtils.getManagedPostOfficeIdsOrThrow(tenantId);
            if (trip.getPostOfficeId() == null || !managedPostOfficeIds.contains(trip.getPostOfficeId())) {
                throw new AppException(ErrorCode.UNAUTHORIZED);
            }
            return;
        }

        if (firstMileAccessUtils.isCourier()) {
            Long currentCourierStaffId = firstMileAccessUtils.resolveCurrentStaffIdByRoleOrThrow(
                    tenantId,
                    PostOfficeStaffRole.COURIER
            );
            if (!Objects.equals(currentCourierStaffId, pickupCheckin.getCourierStaffId())) {
                throw new AppException(ErrorCode.UNAUTHORIZED);
            }
            return;
        }

        throw new AppException(ErrorCode.UNAUTHORIZED);
    }

    private void ensureCanConfirmPostOfficeInbound(Long tenantId, Trip trip) {
        if (firstMileAccessUtils.isAdmin()) {
            return;
        }

        if (firstMileAccessUtils.isPostOfficerManager()) {
            Set<Long> managedPostOfficeIds = firstMileAccessUtils.getManagedPostOfficeIdsOrThrow(tenantId);
            if (trip.getPostOfficeId() == null || !managedPostOfficeIds.contains(trip.getPostOfficeId())) {
                throw new AppException(ErrorCode.UNAUTHORIZED);
            }
            return;
        }

        throw new AppException(ErrorCode.UNAUTHORIZED);
    }

    private PostOfficeStaff resolveCurrentManagerStaff(Long tenantId) {
        if (!firstMileAccessUtils.isPostOfficerManager()) {
            return null;
        }

        try {
            Long managerStaffId = firstMileAccessUtils.resolveCurrentStaffIdByRoleOrThrow(
                    tenantId,
                    PostOfficeStaffRole.MANAGER
            );
            return postOfficeStaffRepository.findByIdAndTenantId(managerStaffId, tenantId).orElse(null);
        } catch (AppException exception) {
            return null;
        }
    }

    private List<String> normalizeOrderCodes(List<String> orderCodes) {
        if (orderCodes == null || orderCodes.isEmpty()) {
            return List.of();
        }

        return orderCodes.stream()
                .filter(Objects::nonNull)
                .map(code -> code.trim())
                .filter(code -> !code.isEmpty())
                .map(code -> code.toUpperCase(Locale.ROOT))
                .distinct()
                .toList();
    }

    private Trip resolveTripForLifecycle(Long tripId, Long tenantId) {
        if (tripId == null || tripId <= 0) {
            throw new AppException(ErrorCode.INVALID_REQUEST, "tripId must be greater than 0.");
        }
        return tripRepository.findByIdAndTenantIdForUpdate(tripId, tenantId)
                .orElseThrow(() -> new AppException(ErrorCode.INVALID_REQUEST, "Pickup trip not found."));
    }

    private void ensureCanManageTrip(Long tenantId, Trip trip) {
        if (firstMileAccessUtils.isAdmin()) {
            return;
        }

        if (firstMileAccessUtils.isPostOfficerManager()) {
            Set<Long> managedPostOfficeIds = firstMileAccessUtils.getManagedPostOfficeIdsOrThrow(tenantId);
            if (trip.getPostOfficeId() == null || !managedPostOfficeIds.contains(trip.getPostOfficeId())) {
                throw new AppException(ErrorCode.UNAUTHORIZED);
            }
            return;
        }

        if (firstMileAccessUtils.isCourier()) {
            Long currentCourierStaffId = firstMileAccessUtils.resolveCurrentStaffIdByRoleOrThrow(
                    tenantId,
                    PostOfficeStaffRole.COURIER
            );
            if (!Objects.equals(currentCourierStaffId, trip.getCourierStaffId())) {
                throw new AppException(ErrorCode.UNAUTHORIZED);
            }
            return;
        }

        throw new AppException(ErrorCode.UNAUTHORIZED);
    }

    private PickupTripLifecycleResponse buildTripLifecycleResponse(Trip trip, Long tenantId) {
        List<TripOrder> tripOrders = tripOrderRepository.findByTenantIdAndTrip_IdOrderBySequenceNoAsc(tenantId, trip.getId());
        int totalOrders = tripOrders.size();
        int checkedInOrders = (int) pickupCheckinRepository.countByTenantIdAndTripId(tenantId, trip.getId());

        List<Long> orderIds = tripOrders.stream()
                .map(TripOrder::getOrderId)
                .filter(Objects::nonNull)
                .distinct()
                .toList();
        List<Order> tripLinkedOrders = orderIds.isEmpty()
                ? List.of()
                : orderRepository.findByIdInAndTenantId(orderIds, tenantId);
        int pendingPostOfficeInboundOrders = (int) tripLinkedOrders.stream()
                .filter(order -> OrderStatus.PENDING_ORIGIN_POST_OFFICE_INBOUND.equals(order.getStatus()))
                .count();
        int receivedAtPostOfficeOrders = (int) tripLinkedOrders.stream()
                .filter(order -> OrderStatus.AT_ORIGIN_POST_OFFICE.equals(order.getStatus()))
                .count();
        int returnedToPostOfficeOrders = pendingPostOfficeInboundOrders + receivedAtPostOfficeOrders;

        int pendingCheckinOrders = Math.max(0, totalOrders - checkedInOrders);
        return new PickupTripLifecycleResponse(
                trip.getId(),
                trip.getTripCode(),
                trip.getStatus(),
                totalOrders,
                checkedInOrders,
                pendingCheckinOrders,
                returnedToPostOfficeOrders,
                pendingPostOfficeInboundOrders,
                receivedAtPostOfficeOrders,
                totalOrders > 0 && checkedInOrders >= totalOrders
        );
    }

    private Map<Long, PostOffice> loadPostOfficeById(Long tenantId, List<Trip> trips) {
        List<Long> postOfficeIds = trips.stream()
                .map(Trip::getPostOfficeId)
                .filter(Objects::nonNull)
                .distinct()
                .toList();

        if (postOfficeIds.isEmpty()) {
            return Map.of();
        }

        return postOfficeRepository.findAllByTenantIdAndIdIn(tenantId, postOfficeIds).stream()
                .collect(Collectors.toMap(PostOffice::getId, postOffice -> postOffice));
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
                .collect(Collectors.toMap(PostOfficeStaff::getId, courier -> courier));
    }

    private ScopeContext resolveScopeContext(Long tenantId, Long postOfficeId, Long courierStaffId) {
        if (firstMileAccessUtils.isAdmin()) {
            return new ScopeContext(
                    ACTOR_SCOPE_ADMIN_ALL,
                    postOfficeId == null ? null : Set.of(postOfficeId),
                    postOfficeId,
                    courierStaffId,
                    courierStaffId
            );
        }

        if (firstMileAccessUtils.isPostOfficerManager()) {
            Set<Long> managedPostOfficeIds = firstMileAccessUtils.getManagedPostOfficeIdsOrThrow(tenantId);

            if (postOfficeId != null && !managedPostOfficeIds.contains(postOfficeId)) {
                throw new AppException(ErrorCode.UNAUTHORIZED);
            }

            Set<Long> visiblePostOfficeIds = postOfficeId == null
                    ? managedPostOfficeIds
                    : Set.of(postOfficeId);

            return new ScopeContext(
                    ACTOR_SCOPE_MANAGER_SCOPED,
                    visiblePostOfficeIds,
                    postOfficeId,
                    courierStaffId,
                    courierStaffId
            );
        }

        if (firstMileAccessUtils.isCourier()) {
            Long currentCourierStaffId = firstMileAccessUtils.resolveCurrentStaffIdByRoleOrThrow(
                    tenantId,
                    PostOfficeStaffRole.COURIER
            );

            if (courierStaffId != null && !courierStaffId.equals(currentCourierStaffId)) {
                throw new AppException(ErrorCode.UNAUTHORIZED);
            }

            return new ScopeContext(
                    ACTOR_SCOPE_COURIER_SELF,
                    postOfficeId == null ? null : Set.of(postOfficeId),
                    postOfficeId,
                    currentCourierStaffId,
                    currentCourierStaffId
            );
        }

        throw new AppException(ErrorCode.UNAUTHORIZED);
    }

    private boolean isTripInScope(Trip trip, ScopeContext scopeContext) {
        if (trip == null || trip.getId() == null) {
            return false;
        }

        if (scopeContext.visiblePostOfficeIds() != null
                && !scopeContext.visiblePostOfficeIds().contains(trip.getPostOfficeId())) {
            return false;
        }

        return scopeContext.effectiveCourierStaffId() == null
                || Objects.equals(scopeContext.effectiveCourierStaffId(), trip.getCourierStaffId());
    }

    private PickupTrackingOverviewResponse buildEmptyOverview(LocalDate tripDate, ScopeContext scopeContext) {
        return new PickupTrackingOverviewResponse(
                tripDate,
                scopeContext.actorScope(),
                scopeContext.selectedPostOfficeId(),
                scopeContext.selectedCourierStaffId(),
                0,
                0,
                0,
                0,
                0,
                0,
                0,
                0,
                List.of(),
                List.of()
        );
    }

    private Long normalizePositiveId(Long value) {
        if (value == null) {
            return null;
        }

        if (value <= 0) {
            throw new AppException(ErrorCode.INVALID_REQUEST);
        }

        return value;
    }

    private Double getLatitude(Point point) {
        if (point == null) {
            return null;
        }
        return point.getY();
    }

    private Double getLongitude(Point point) {
        if (point == null) {
            return null;
        }
        return point.getX();
    }

    private record ScopeContext(
            String actorScope,
            Set<Long> visiblePostOfficeIds,
            Long selectedPostOfficeId,
            Long effectiveCourierStaffId,
            Long selectedCourierStaffId
    ) {
    }

    private static class TripSummaryAccumulator {
        private int totalOrders;
        private int checkedInOrders;
        private int pendingCheckinOrders;
        private int returnableToPostOfficeOrders;
        private int pendingPostOfficeInboundOrders;
    }
}
