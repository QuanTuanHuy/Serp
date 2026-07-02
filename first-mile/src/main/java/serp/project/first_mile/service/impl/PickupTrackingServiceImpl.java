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
import org.springframework.web.multipart.MultipartFile;
import serp.project.first_mile.caller.TmsOrderClient;
import serp.project.first_mile.caller.dto.tms_order.TmsOrderOperationView;
import serp.project.first_mile.caller.dto.tms_order.TmsOrderStatusTransitionRequest;
import serp.project.first_mile.domain.Checkin;
import serp.project.first_mile.domain.PostOffice;
import serp.project.first_mile.domain.PostOfficeStaff;
import serp.project.first_mile.domain.Trip;
import serp.project.first_mile.domain.TripOrder;
import serp.project.first_mile.dto.request.FileUploadRequest;
import serp.project.first_mile.dto.request.ConfirmPostOfficeInboundRequest;
import serp.project.first_mile.dto.response.FileUploadResponse;
import serp.project.first_mile.dto.response.PickupCheckinResponse;
import serp.project.first_mile.dto.response.PickupCheckinDetailResponse;
import serp.project.first_mile.dto.response.PickupTripLifecycleResponse;
import serp.project.first_mile.dto.response.PickupTrackingOverviewResponse;
import serp.project.first_mile.enums.CheckinType;
import serp.project.first_mile.enums.OrderStatus;
import serp.project.first_mile.enums.PostOfficeStaffRole;
import serp.project.first_mile.enums.TripStatus;
import serp.project.first_mile.enums.TripType;
import serp.project.first_mile.exception.AppException;
import serp.project.first_mile.exception.ErrorCode;
import serp.project.first_mile.kernel.utils.FirstMileAccessUtils;
import serp.project.first_mile.kernel.utils.GeoPointUtils;
import serp.project.first_mile.kernel.utils.ImageContentTypeUtils;
import serp.project.first_mile.mapper.PickupTrackingMapper;
import serp.project.first_mile.mapper.PickupTrackingMapper.TripSummary;
import serp.project.first_mile.repository.CheckinRepository;
import serp.project.first_mile.repository.PostOfficeRepository;
import serp.project.first_mile.repository.PostOfficeStaffRepository;
import serp.project.first_mile.repository.TripOrderRepository;
import serp.project.first_mile.repository.TripRepository;
import serp.project.first_mile.service.FileStorageService;
import serp.project.first_mile.service.PickupTrackingService;
import serp.project.first_mile.service.TmsOrderTransitionPublisherService;
import serp.project.first_mile.service.dto.OrderTimelineContext;

import java.io.IOException;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.Locale;
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

    private static final double DEFAULT_PICKUP_CHECKIN_RADIUS_METERS = 100.0;
    private static final String STORAGE_SERVICE_NAME = "first-mile";
    private static final String PICKUP_CHECKIN_IMAGE_FOLDER = "orders/pickup-checkin";

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
    private final PickupTrackingAccessPolicy pickupTrackingAccessPolicy;
    private final TmsOrderClient tmsOrderClient;
    private final TripRepository tripRepository;
    private final TripOrderRepository tripOrderRepository;
    private final CheckinRepository checkinRepository;
    private final PostOfficeRepository postOfficeRepository;
    private final PostOfficeStaffRepository postOfficeStaffRepository;
    private final FileStorageService fileStorageService;
    private final TmsOrderTransitionPublisherService tmsOrderTransitionPublisherService;

    @Value("${pickup.checkin.radius-meters:100}")
    private Double pickupCheckinRadiusMeters;

    @Override
    public PickupTrackingOverviewResponse getPickupTrackingOverview(
            LocalDate tripDate,
            Long postOfficeId,
            Long courierStaffId
    ) {
        Long tenantId = firstMileAccessUtils.getCurrentTenantIdOrThrow();
        LocalDate effectiveTripDate = tripDate == null ? LocalDate.now() : tripDate;

        PickupTrackingAccessPolicy.ScopeContext scopeContext = pickupTrackingAccessPolicy.resolveScopeContext(
                tenantId,
                pickupTrackingAccessPolicy.normalizePositiveId(postOfficeId),
                pickupTrackingAccessPolicy.normalizePositiveId(courierStaffId)
        );

        if (scopeContext.visiblePostOfficeIds() != null
                && scopeContext.visiblePostOfficeIds().isEmpty()) {
            return buildEmptyOverview(effectiveTripDate, scopeContext);
        }

        List<Trip> allTrips = tripRepository
                .findByTenantIdAndTripTypeAndTripDateAndStatusInOrderByPlannedStartTimeAscIdAsc(
                        tenantId,
                        TripType.PICKUP,
                        effectiveTripDate,
                        TRACKABLE_TRIP_STATUSES
                );

        List<Trip> scopedTrips = allTrips.stream()
                .filter(trip -> pickupTrackingAccessPolicy.isTripInScope(trip, scopeContext))
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

        Map<Long, TmsOrderOperationView> orderById = orderIds.isEmpty()
                ? Map.of()
                : tmsOrderClient.lookupByIds(orderIds).stream()
                        .collect(Collectors.toMap(TmsOrderOperationView::getId, order -> order));

        List<Long> tripOrderIds = tripOrders.stream()
                .map(TripOrder::getId)
                .filter(Objects::nonNull)
                .toList();

        Map<Long, Checkin> pickupCheckinByTripOrderId = tripOrderIds.isEmpty()
                ? Map.of()
                : checkinRepository.findByTenantIdAndCheckinTypeAndTripOrderIdIn(
                                tenantId,
                                CheckinType.PICKUP,
                                tripOrderIds
                        )
                        .stream()
                        .collect(Collectors.toMap(Checkin::getTripOrderId, pickupCheckin -> pickupCheckin));

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

            TmsOrderOperationView order = orderById.get(tripOrder.getOrderId());
            if (order == null || order.getId() == null) {
                continue;
            }

            Checkin pickupCheckin = pickupCheckinByTripOrderId.get(tripOrder.getId());
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

            orderResponses.add(PickupTrackingMapper.toTrackingOrderResponse(
                    tripOrder,
                    trip,
                    order,
                    pickupCheckin,
                    postOffice,
                    courier
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

            tripResponses.add(PickupTrackingMapper.toTrackingTripResponse(
                    trip,
                    postOffice,
                    courier,
                    new TripSummary(
                            tripSummary.totalOrders,
                            tripSummary.checkedInOrders,
                            tripSummary.pendingCheckinOrders,
                            tripSummary.returnableToPostOfficeOrders,
                            tripSummary.pendingPostOfficeInboundOrders
                    )
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
    @Transactional(rollbackFor = Exception.class)
    public PickupCheckinResponse checkInPickupOrder(
            Long orderId,
            Double checkinLatitude,
            Double checkinLongitude,
            MultipartFile photo,
            Long tenantId
    ) {
        validatePickupCheckinRequest(orderId, checkinLatitude, checkinLongitude, photo);

        Long courierStaffId = firstMileAccessUtils.resolveCurrentStaffIdByRoleOrThrow(
                tenantId,
                PostOfficeStaffRole.COURIER
        );

        TmsOrderOperationView order = loadOrderOrThrow(orderId);
        if (order.getStatus() == null || !List.of(OrderStatus.ASSIGNED_TO_PICKUP, OrderStatus.PICKING_UP)
                .contains(order.getStatus())) {
            throw new AppException(
                    ErrorCode.ORDER_NOT_ASSIGNABLE,
                    String.format(Locale.ROOT, "Order status '%s' does not allow pickup check-in.", order.getStatus())
            );
        }

        var tripOrder = tripOrderRepository
                .findFirstByTenantIdAndOrderIdAndTrip_TripTypeAndTrip_CourierStaffIdAndTrip_StatusInOrderByTrip_IdDesc(
                        tenantId,
                        orderId,
                        TripType.PICKUP,
                        courierStaffId,
                        List.of(TripStatus.PLANNED, TripStatus.IN_PROGRESS)
                )
                .orElseThrow(() -> new AppException(ErrorCode.UNAUTHORIZED));

        if (tripOrder.getId() == null || tripOrder.getTrip() == null || tripOrder.getTrip().getId() == null) {
            throw new AppException(
                    ErrorCode.INVALID_REQUEST,
                    "Pickup trip assignment is invalid. Please contact support."
            );
        }

        Point pickupLocation = order.getSenderLocation();
        if (pickupLocation == null || !GeoPointUtils.isValidCoordinate(pickupLocation.getY(), pickupLocation.getX())) {
            throw new AppException(ErrorCode.ORDER_NOT_ASSIGNABLE, "Order pickup location is missing or invalid.");
        }

        double allowedRadiusMeters = resolvePickupCheckinRadiusMeters();
        double distanceMeters = GeoPointUtils.distanceMeters(
                checkinLatitude,
                checkinLongitude,
                pickupLocation.getY(),
                pickupLocation.getX()
        );

        if (distanceMeters > allowedRadiusMeters) {
            throw new AppException(
                    ErrorCode.INVALID_REQUEST,
                    String.format(
                            Locale.ROOT,
                            "Check-in location is outside allowed radius %.2f m (actual %.2f m).",
                            allowedRadiusMeters,
                            distanceMeters
                    )
            );
        }

        String contentType = ImageContentTypeUtils.normalizeImageContentType(photo.getContentType());
        FileUploadResponse uploadResponse = uploadPickupCheckinPhoto(photo, contentType, tenantId);

        Checkin pickupCheckin = checkinRepository
                .findByTenantIdAndCheckinTypeAndTripOrderId(tenantId, CheckinType.PICKUP, tripOrder.getId())
                .orElseGet(Checkin::new);

        pickupCheckin.setTenantId(tenantId);
        pickupCheckin.setCheckinType(CheckinType.PICKUP);
        pickupCheckin.setTripOrderId(tripOrder.getId());
        pickupCheckin.setOrderId(order.getId());
        pickupCheckin.setOrderCode(order.getOrderCode());
        pickupCheckin.setTripId(tripOrder.getTrip().getId());
        pickupCheckin.setCourierStaffId(courierStaffId);
        pickupCheckin.setCheckinTime(LocalDateTime.now());
        pickupCheckin.setCheckinLocation(GeoPointUtils.toPoint(checkinLatitude, checkinLongitude));
        pickupCheckin.setDistanceM(GeoPointUtils.round3(distanceMeters));
        pickupCheckin.setAllowedRadiusM(GeoPointUtils.round3(allowedRadiusMeters));
        pickupCheckin.setPhotoUrl(uploadResponse.getUrl());

        Checkin savedCheckin = checkinRepository.save(pickupCheckin);

        if (TripStatus.PLANNED.equals(tripOrder.getTrip().getStatus())) {
            tripOrder.getTrip().setStatus(TripStatus.IN_PROGRESS);
        }

        enqueueTransitions(List.of(PickupOrderTransitionFactory.item(
                order,
                List.of(OrderStatus.ASSIGNED_TO_PICKUP, OrderStatus.PICKING_UP),
                OrderStatus.PICKING_UP,
                "Courier checked in and started pickup.",
                new OrderTimelineContext(
                        savedCheckin.getCheckinTime(),
                        tripOrder.getTrip().getId(),
                        tripOrder.getTrip().getTripCode(),
                        null,
                        order.getOriginPostOfficeCode(),
                        null,
                        courierStaffId,
                        null,
                        null,
                        tripOrder.getTrip().getVehicleId(),
                        null,
                        checkinLatitude,
                        checkinLongitude,
                        "Pickup check-in location"
                )
        )), tenantId);

        tryAutoCompleteTripAfterCheckin(tripOrder.getTrip(), tenantId);

        return PickupTrackingMapper.toPickupCheckinResponse(order, tripOrder.getTrip(), savedCheckin, pickupLocation);
    }

    @Override
    public PickupCheckinDetailResponse getPickupCheckinDetail(Long orderId, Long tenantId) {
        if (orderId == null || orderId <= 0) {
            throw new AppException(ErrorCode.INVALID_REQUEST, "orderId must be greater than 0.");
        }

        Checkin pickupCheckin = checkinRepository
                .findByTenantIdAndCheckinTypeAndOrderId(tenantId, CheckinType.PICKUP, orderId)
                .orElseThrow(() -> new AppException(
                        ErrorCode.ORDER_NOT_FOUND,
                        "Pickup check-in not found for this order."
                ));

        TmsOrderOperationView order = loadOrderOrThrow(orderId);

        Trip trip = tripRepository.findByIdAndTenantId(pickupCheckin.getTripId(), tenantId)
                .orElseThrow(() -> new AppException(ErrorCode.INVALID_REQUEST, "Pickup trip is invalid."));
        if (!TripType.PICKUP.equals(trip.getTripType())) {
            throw new AppException(ErrorCode.INVALID_REQUEST, "Pickup trip is invalid.");
        }

        pickupTrackingAccessPolicy.ensureCanViewPickupCheckin(tenantId, trip, pickupCheckin);

        PostOffice postOffice = trip.getPostOfficeId() == null
                ? null
                : postOfficeRepository.findByIdAndTenantId(trip.getPostOfficeId(), tenantId).orElse(null);

        PostOfficeStaff courier = pickupCheckin.getCourierStaffId() == null
                ? null
                : postOfficeStaffRepository
                        .findByIdAndTenantId(pickupCheckin.getCourierStaffId(), tenantId)
                        .orElse(null);

        return PickupTrackingMapper.toPickupCheckinDetailResponse(
                pickupCheckin,
                order,
                trip,
                postOffice,
                courier
        );
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public PickupTripLifecycleResponse completeTrip(Long tripId, Long tenantId) {
        Trip trip = resolveTripForLifecycle(tripId, tenantId);
        pickupTrackingAccessPolicy.ensureCanManageTrip(tenantId, trip);

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
                : checkinRepository.findByTenantIdAndCheckinTypeAndTripOrderIdIn(
                                tenantId,
                                CheckinType.PICKUP,
                                tripOrderIds
                        )
                .stream()
                .map(Checkin::getTripOrderId)
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

        Map<Long, TmsOrderOperationView> orderById = tmsOrderClient.lookupByIds(uncheckedOrderIds).stream()
                .collect(Collectors.toMap(TmsOrderOperationView::getId, order -> order));

        PostOffice postOffice = trip.getPostOfficeId() == null
                ? null
                : postOfficeRepository.findByIdAndTenantId(trip.getPostOfficeId(), tenantId).orElse(null);
        PostOfficeStaff courier = trip.getCourierStaffId() == null
                ? null
                : postOfficeStaffRepository.findByIdAndTenantId(trip.getCourierStaffId(), tenantId).orElse(null);

        List<TmsOrderStatusTransitionRequest.Item> transitionItems = new ArrayList<>();
        for (Long uncheckedOrderId : uncheckedOrderIds) {
            TmsOrderOperationView order = orderById.get(uncheckedOrderId);
            if (order == null || order.getStatus() == null) {
                continue;
            }
            if (!AUTO_PICKUP_FAILED_CANDIDATE_STATUSES.contains(order.getStatus())) {
                continue;
            }

            transitionItems.add(PickupOrderTransitionFactory.item(
                    order,
                    AUTO_PICKUP_FAILED_CANDIDATE_STATUSES,
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
            ));
        }
        enqueueTransitions(transitionItems, tenantId);
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public PickupTripLifecycleResponse returnTripToPostOffice(Long tripId, Long tenantId) {
        Trip trip = resolveTripForLifecycle(tripId, tenantId);
        pickupTrackingAccessPolicy.ensureCanManageTrip(tenantId, trip);

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

        Map<Long, TmsOrderOperationView> orderById = tmsOrderClient.lookupByIds(orderIds).stream()
                .collect(Collectors.toMap(TmsOrderOperationView::getId, order -> order));

        PostOffice postOffice = trip.getPostOfficeId() == null
                ? null
                : postOfficeRepository.findByIdAndTenantId(trip.getPostOfficeId(), tenantId).orElse(null);
        PostOfficeStaff courier = trip.getCourierStaffId() == null
                ? null
                : postOfficeStaffRepository.findByIdAndTenantId(trip.getCourierStaffId(), tenantId).orElse(null);

        List<TmsOrderStatusTransitionRequest.Item> transitionItems = new ArrayList<>();
        for (TripOrder tripOrder : tripOrders) {
            TmsOrderOperationView order = orderById.get(tripOrder.getOrderId());
            if (order == null || order.getStatus() == null || !RETURNABLE_ORDER_STATUSES.contains(order.getStatus())) {
                continue;
            }

            transitionItems.add(PickupOrderTransitionFactory.item(
                    order,
                    RETURNABLE_ORDER_STATUSES,
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
            ));
        }

        if (transitionItems.isEmpty()) {
            throw new AppException(ErrorCode.INVALID_REQUEST, "No pickup orders are ready to return to post office.");
        }
        enqueueTransitions(transitionItems, tenantId);

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
        pickupTrackingAccessPolicy.ensureCanConfirmPostOfficeInbound(tenantId, trip);

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
        Map<Long, TmsOrderOperationView> orderById = tmsOrderClient.lookupByIds(orderIds).stream()
                .collect(Collectors.toMap(TmsOrderOperationView::getId, order -> order));

        PostOffice postOffice = trip.getPostOfficeId() == null
                ? null
                : postOfficeRepository.findByIdAndTenantId(trip.getPostOfficeId(), tenantId).orElse(null);
        PostOfficeStaff manager = resolveCurrentManagerStaff(tenantId);

        List<TmsOrderStatusTransitionRequest.Item> transitionItems = new ArrayList<>();
        for (String orderCode : requestedOrderCodes) {
            TmsOrderOperationView matchedOrder = orderById.values().stream()
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
            if (transitionItems.stream().anyMatch(item -> Objects.equals(item.getOrderId(), matchedOrder.getId()))) {
                continue;
            }

            transitionItems.add(PickupOrderTransitionFactory.item(
                    matchedOrder,
                    List.of(OrderStatus.PENDING_ORIGIN_POST_OFFICE_INBOUND),
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
            ));
        }

        if (transitionItems.isEmpty()) {
            throw new AppException(ErrorCode.INVALID_REQUEST, "No orders are ready for post office inbound confirmation.");
        }
        enqueueTransitions(transitionItems, tenantId);

        return buildTripLifecycleResponse(trip, tenantId);
    }

    private TmsOrderOperationView loadOrderOrThrow(Long orderId) {
        return tmsOrderClient.lookupByIds(List.of(orderId)).stream()
                .findFirst()
                .orElseThrow(() -> new AppException(ErrorCode.ORDER_NOT_FOUND));
    }

    private void enqueueTransitions(List<TmsOrderStatusTransitionRequest.Item> items, Long tenantId) {
        if (items == null || items.isEmpty()) {
            return;
        }

        tmsOrderTransitionPublisherService.publish(PickupOrderTransitionFactory.request(items), tenantId);
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
                .filter(trip -> TripType.PICKUP.equals(trip.getTripType()))
                .orElseThrow(() -> new AppException(ErrorCode.INVALID_REQUEST, "Pickup trip not found."));
    }

    private PickupTripLifecycleResponse buildTripLifecycleResponse(Trip trip, Long tenantId) {
        List<TripOrder> tripOrders = tripOrderRepository.findByTenantIdAndTrip_IdOrderBySequenceNoAsc(tenantId, trip.getId());
        int totalOrders = tripOrders.size();
        int checkedInOrders = (int) checkinRepository.countByTenantIdAndCheckinTypeAndTripId(
                tenantId,
                CheckinType.PICKUP,
                trip.getId()
        );

        List<Long> orderIds = tripOrders.stream()
                .map(TripOrder::getOrderId)
                .filter(Objects::nonNull)
                .distinct()
                .toList();
        List<TmsOrderOperationView> tripLinkedOrders = orderIds.isEmpty()
                ? List.of()
                : tmsOrderClient.lookupByIds(orderIds);
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

    private PickupTrackingOverviewResponse buildEmptyOverview(
            LocalDate tripDate,
            PickupTrackingAccessPolicy.ScopeContext scopeContext
    ) {
        return PickupTrackingMapper.emptyOverview(
                tripDate,
                scopeContext.actorScope(),
                scopeContext.selectedPostOfficeId(),
                scopeContext.selectedCourierStaffId()
        );
    }

    private void validatePickupCheckinRequest(
            Long orderId,
            Double checkinLatitude,
            Double checkinLongitude,
            MultipartFile photo
    ) {
        if (orderId == null || orderId <= 0) {
            throw new AppException(ErrorCode.INVALID_REQUEST, "orderId must be greater than 0.");
        }
        if (checkinLatitude == null) {
            throw new AppException(ErrorCode.INVALID_REQUEST, "latitude is required.");
        }
        if (checkinLongitude == null) {
            throw new AppException(ErrorCode.INVALID_REQUEST, "longitude is required.");
        }
        if (!GeoPointUtils.isValidCoordinate(checkinLatitude, checkinLongitude)) {
            throw new AppException(
                    ErrorCode.INVALID_REQUEST,
                    String.format(
                            Locale.ROOT,
                            "Invalid check-in coordinates (latitude=%s, longitude=%s). latitude must be between -90 and 90 and longitude between -180 and 180.",
                            checkinLatitude,
                            checkinLongitude
                    )
            );
        }
        if (photo == null || photo.isEmpty()) {
            throw new AppException(ErrorCode.FILE_UPLOAD_EMPTY);
        }
    }

    private void tryAutoCompleteTripAfterCheckin(Trip trip, Long tenantId) {
        if (trip == null || trip.getId() == null) {
            return;
        }
        if (!TripStatus.PLANNED.equals(trip.getStatus()) && !TripStatus.IN_PROGRESS.equals(trip.getStatus())) {
            return;
        }

        long totalOrders = tripOrderRepository.countByTenantIdAndTrip_Id(tenantId, trip.getId());
        if (totalOrders <= 0) {
            return;
        }

        long checkedInOrders = checkinRepository.countByTenantIdAndCheckinTypeAndTripId(
                tenantId,
                CheckinType.PICKUP,
                trip.getId()
        );
        if (checkedInOrders >= totalOrders) {
            trip.setStatus(TripStatus.COMPLETED);
        }
    }

    private FileUploadResponse uploadPickupCheckinPhoto(MultipartFile photo, String contentType, Long tenantId) {
        try {
            return fileStorageService.upload(FileUploadRequest.builder()
                    .content(photo.getBytes())
                    .originalFileName(photo.getOriginalFilename())
                    .contentType(contentType)
                    .serviceName(STORAGE_SERVICE_NAME)
                    .folder(PICKUP_CHECKIN_IMAGE_FOLDER)
                    .tenantId(tenantId)
                    .uploaderId(firstMileAccessUtils.getCurrentUserIdOrNull())
                    .publicFile(true)
                    .build());
        } catch (IOException exception) {
            throw new AppException(ErrorCode.FILE_UPLOAD_FAILED);
        }
    }

    private double resolvePickupCheckinRadiusMeters() {
        Double configuredRadius = pickupCheckinRadiusMeters;
        if (configuredRadius == null
                || Double.isNaN(configuredRadius)
                || Double.isInfinite(configuredRadius)
                || configuredRadius <= 0D) {
            return DEFAULT_PICKUP_CHECKIN_RADIUS_METERS;
        }
        return configuredRadius;
    }

    private static class TripSummaryAccumulator {
        private int totalOrders;
        private int checkedInOrders;
        private int pendingCheckinOrders;
        private int returnableToPostOfficeOrders;
        private int pendingPostOfficeInboundOrders;
    }
}
