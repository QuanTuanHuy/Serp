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
import org.springframework.web.multipart.MultipartFile;
import serp.project.first_mile.caller.PaymentServiceCaller;
import serp.project.first_mile.caller.dto.payment.PaymentCreateOrderRequest;
import serp.project.first_mile.caller.dto.payment.PaymentCreateOrderResponse;
import serp.project.first_mile.caller.dto.payment.PaymentQueryOrderResponse;
import serp.project.first_mile.domain.Order;
import serp.project.first_mile.domain.PostOffice;
import serp.project.first_mile.domain.PostOfficeStaff;
import serp.project.first_mile.domain.PickupCheckin;
import serp.project.first_mile.domain.Trip;
import serp.project.first_mile.dto.request.FileUploadRequest;
import serp.project.first_mile.dto.request.ConfirmOrderPaymentRequest;
import serp.project.first_mile.dto.request.InitiateOrderPaymentRequest;
import serp.project.first_mile.dto.request.PaymentOrderConfirmedWebhookRequest;
import serp.project.first_mile.dto.response.FileUploadResponse;
import serp.project.first_mile.dto.response.OrderConfirmationResponse;
import serp.project.first_mile.dto.response.OrderPaymentConfirmResponse;
import serp.project.first_mile.dto.response.OrderPaymentInitResponse;
import serp.project.first_mile.dto.response.OrderDropOffPostOfficeSuggestionResponse;
import serp.project.first_mile.dto.response.OrderTimelineResponse;
import serp.project.first_mile.dto.response.PaymentWebhookProcessResponse;
import serp.project.first_mile.dto.response.PickupCheckinResponse;
import serp.project.first_mile.enums.*;
import serp.project.first_mile.exception.AppException;
import serp.project.first_mile.exception.ErrorCode;
import serp.project.first_mile.kafka.impl.order.SyncOrder;
import serp.project.first_mile.kernel.utils.FirstMileAccessUtils;
import serp.project.first_mile.kernel.utils.ImageContentTypeUtils;
import serp.project.first_mile.kernel.utils.PostOfficeStaffCodeUtils;
import serp.project.first_mile.mapper.OrderMapper;
import serp.project.first_mile.repository.OrderRepository;
import serp.project.first_mile.repository.PickupCheckinRepository;
import serp.project.first_mile.repository.PostOfficeRepository;
import serp.project.first_mile.repository.PostOfficeStaffAssignmentRepository;
import serp.project.first_mile.repository.PostOfficeStaffRepository;
import serp.project.first_mile.repository.TripOrderRepository;
import serp.project.first_mile.service.FileStorageService;
import serp.project.first_mile.service.OrderService;
import serp.project.first_mile.service.OrderTimelineService;
import serp.project.first_mile.service.dto.OrderActorScope;
import serp.project.first_mile.service.dto.OrderTimelineContext;

import java.io.IOException;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.HashSet;
import java.util.List;
import java.util.Locale;
import java.util.Optional;
import java.util.Set;

@Service
@RequiredArgsConstructor
@Slf4j
@Transactional(readOnly = true)
public class OrderServiceImpl implements OrderService {

    private static final GeometryFactory GEOMETRY_FACTORY = new GeometryFactory(new PrecisionModel(), 4326);
    private static final int DEFAULT_DROP_OFF_SUGGESTION_LIMIT = 5;
    private static final int MAX_DROP_OFF_SUGGESTION_LIMIT = 20;
    private static final double DEFAULT_PICKUP_CHECKIN_RADIUS_METERS = 100.0;
    private static final double EARTH_RADIUS_METERS = 6_371_000.0;
    private static final String STORAGE_SERVICE_NAME = "first-mile";
    private static final String PICKUP_CHECKIN_IMAGE_FOLDER = "orders/pickup-checkin";
    private static final Set<OrderStatus> CONFIRMABLE_ORDER_STATUSES = Set.of(
            OrderStatus.CREATED,
            OrderStatus.PICKUP_FAILED
    );
    private static final Set<OrderStatus> PICKUP_CHECKIN_ORDER_STATUSES = Set.of(
            OrderStatus.ASSIGNED_TO_PICKUP,
            OrderStatus.PICKING_UP
    );
    private static final List<TripStatus> COURIER_VISIBLE_TRIP_STATUSES = List.of(
            TripStatus.PLANNED,
            TripStatus.IN_PROGRESS
    );

    @Value("${pickup.checkin.radius-meters:100}")
    private Double pickupCheckinRadiusMeters;

    @Value("${payment.service.redirect-url:http://localhost:3000/payment/result}")
    private String paymentRedirectUrl;

    private final OrderRepository orderRepository;
    private final PostOfficeRepository postOfficeRepository;
    private final FirstMileAccessUtils firstMileAccessUtils;
    private final PostOfficeStaffRepository postOfficeStaffRepository;
    private final PostOfficeStaffAssignmentRepository postOfficeStaffAssignmentRepository;
    private final TripOrderRepository tripOrderRepository;
    private final PickupCheckinRepository pickupCheckinRepository;
    private final FileStorageService fileStorageService;
    private final SyncOrder syncOrder;
    private final PaymentServiceCaller paymentServiceCaller;
    private final OrderTimelineService orderTimelineService;

    @Override
    public List<OrderTimelineResponse> getOrderTimeline(Long orderId, Long tenantId) {
        OrderActorScope actorScope = resolveActorScope(tenantId);
        Order order = orderRepository.findByIdAndTenantId(orderId, tenantId)
                .orElseThrow(() -> new AppException(ErrorCode.ORDER_NOT_FOUND));

        validateCanReadOrder(order, tenantId, actorScope);

        List<OrderTimelineResponse> timeline = orderTimelineService.getTimeline(orderId, tenantId);
        if (!timeline.isEmpty()) {
            return timeline;
        }

        return List.of(new OrderTimelineResponse(
                null,
                order.getId(),
                order.getOrderCode(),
                order.getCustomerOrderCode(),
                order.getStatus() == null ? OrderStatus.CREATED : order.getStatus(),
                "Order created.",
                order.getCreatedAt(),
                order.getCreatedBy(),
                null,
                null,
                null,
                order.getOriginPostOfficeCode(),
                null,
                null,
                null,
                null,
                null,
                null,
                toLatitude(order.getSenderLocation()),
                toLongitude(order.getSenderLocation()),
                buildSenderLocationLabel(order)
        ));
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public OrderPaymentInitResponse initiateOrderPayment(
            Long orderId,
            Long tenantId,
            InitiateOrderPaymentRequest request
    ) {
        OrderActorScope actorScope = resolveActorScope(tenantId);
        Order order = orderRepository.findByIdAndTenantIdForUpdate(orderId, tenantId)
                .orElseThrow(() -> new AppException(ErrorCode.ORDER_NOT_FOUND));

        validateCanMutateOrder(order, tenantId, actorScope);

        if (PaymentStatus.PAID.equals(order.getPaymentStatus())) {
            throw new AppException(ErrorCode.INVALID_REQUEST, "Order shipping fee is already paid.");
        }

        long shippingFee = resolveShippingFeeForPayment(order, request);
        order.setTotalShippingFee(shippingFee);
        orderRepository.save(order);
        Long actorId = firstMileAccessUtils.getCurrentUserIdOrThrow();
        String orderCode = order.getOrderCode();

        PaymentCreateOrderRequest paymentRequest = PaymentCreateOrderRequest.builder()
                .appUser(orderCode)
                .amount(shippingFee)
                .description("Thanh toan phi van chuyen cho don hang " + orderCode)
                .embedData(PaymentCreateOrderRequest.EmbedData.builder()
                        .redirectUrl(paymentRedirectUrl + "?source=first-mile&orderId=" + order.getId())
                        .build())
                .title("Phi van chuyen - " + orderCode)
                .tenantId(tenantId)
                .actorId(actorId)
                .userId(actorId)
                .items(List.of(PaymentCreateOrderRequest.Item.builder()
                        .itemId("shipping-fee-" + orderCode)
                        .itemName("Phi van chuyen don hang " + orderCode)
                        .itemPrice(shippingFee)
                        .itemQuantity(1)
                        .build()))
                .build();

        PaymentCreateOrderResponse paymentResponse = paymentServiceCaller.createOrder(paymentRequest);
        if (paymentResponse.getStatus() == null || !"SUCCESS".equalsIgnoreCase(paymentResponse.getStatus())) {
            throw new AppException(
                    ErrorCode.INVALID_REQUEST,
                    paymentResponse.getMessage() == null
                            ? "Cannot create payment order for shipping fee."
                            : paymentResponse.getMessage()
            );
        }

        return new OrderPaymentInitResponse(
                order.getId(),
                orderCode,
                shippingFee,
                paymentResponse.getAppTransId(),
                paymentResponse.getOrderUrl(),
                paymentResponse.getStatus(),
                paymentResponse.getMessage()
        );
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public OrderPaymentConfirmResponse confirmOrderPayment(Long orderId, Long tenantId, ConfirmOrderPaymentRequest request) {
        OrderActorScope actorScope = resolveActorScope(tenantId);
        Order order = orderRepository.findByIdAndTenantIdForUpdate(orderId, tenantId)
                .orElseThrow(() -> new AppException(ErrorCode.ORDER_NOT_FOUND));

        validateCanMutateOrder(order, tenantId, actorScope);

        if (PaymentStatus.PAID.equals(order.getPaymentStatus())) {
            return new OrderPaymentConfirmResponse(
                    order.getId(),
                    order.getOrderCode(),
                    order.getPaymentStatus(),
                    request.getAppTransId(),
                    "SUCCESS",
                    "Order shipping fee is already marked as paid."
            );
        }

        PaymentQueryOrderResponse queryResponse = paymentServiceCaller.queryOrderStatus(request.getAppTransId());
        String gatewayStatus = queryResponse.getStatus() == null ? "UNKNOWN" : queryResponse.getStatus();
        if (!"SUCCESS".equalsIgnoreCase(gatewayStatus)) {
            throw new AppException(
                    ErrorCode.INVALID_REQUEST,
                    "Payment status is not successful yet. Current status: " + gatewayStatus
            );
        }

        order.setPaymentStatus(PaymentStatus.PAID);
        Order savedOrder = orderRepository.save(order);
        syncOrder.sendOrderEvent(savedOrder);

        return new OrderPaymentConfirmResponse(
                savedOrder.getId(),
                savedOrder.getOrderCode(),
                savedOrder.getPaymentStatus(),
                request.getAppTransId(),
                gatewayStatus,
                queryResponse.getMessage()
        );
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public PaymentWebhookProcessResponse processPaymentOrderConfirmedWebhook(
            PaymentOrderConfirmedWebhookRequest request
    ) {
        if (request == null || !hasText(request.getOrderCode()) || request.getTenantId() == null) {
            throw new AppException(ErrorCode.INVALID_REQUEST, "Invalid webhook payload.");
        }

        String orderCode = request.getOrderCode().trim();
        Order order = orderRepository.findByOrderCodeAndTenantIdForUpdate(orderCode, request.getTenantId())
                .orElseThrow(() -> new AppException(ErrorCode.ORDER_NOT_FOUND));

        if (PaymentStatus.PAID.equals(order.getPaymentStatus())) {
            return new PaymentWebhookProcessResponse(
                    orderCode,
                    request.getAppTransId(),
                    false,
                    "Order shipping fee is already marked as paid."
            );
        }

        if (request.getAmount() != null && request.getAmount() > 0L) {
            order.setTotalShippingFee(request.getAmount());
        }
        order.setPaymentStatus(PaymentStatus.PAID);
        Order savedOrder = orderRepository.save(order);
        syncOrder.sendOrderEvent(savedOrder);

        return new PaymentWebhookProcessResponse(
                savedOrder.getOrderCode(),
                request.getAppTransId(),
                true,
                "Order payment status updated to PAID."
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
        if (orderId == null) {
            throw new AppException(ErrorCode.INVALID_REQUEST, "orderId is required.");
        }

        if (orderId <= 0) {
            throw new AppException(ErrorCode.INVALID_REQUEST, "orderId must be greater than 0.");
        }

        if (checkinLatitude == null && checkinLongitude == null) {
            throw new AppException(ErrorCode.INVALID_REQUEST, "latitude and longitude are required.");
        }

        if (checkinLatitude == null) {
            throw new AppException(ErrorCode.INVALID_REQUEST, "latitude is required.");
        }

        if (checkinLongitude == null) {
            throw new AppException(ErrorCode.INVALID_REQUEST, "longitude is required.");
        }

        if (!isValidCoordinate(checkinLatitude, checkinLongitude)) {
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

        Long courierStaffId = firstMileAccessUtils.resolveCurrentStaffIdByRoleOrThrow(
                tenantId,
                PostOfficeStaffRole.COURIER
        );

        Order order = orderRepository.findByIdAndTenantIdForUpdate(orderId, tenantId)
                .orElseThrow(() -> new AppException(ErrorCode.ORDER_NOT_FOUND));

        if (order.getStatus() == null || !PICKUP_CHECKIN_ORDER_STATUSES.contains(order.getStatus())) {
            throw new AppException(
                ErrorCode.ORDER_NOT_ASSIGNABLE,
                String.format(Locale.ROOT, "Order status '%s' does not allow pickup check-in.", order.getStatus())
            );
        }

        var tripOrder = tripOrderRepository
                .findFirstByTenantIdAndOrderIdAndTrip_CourierStaffIdAndTrip_StatusInOrderByTrip_IdDesc(
                        tenantId,
                        orderId,
                        courierStaffId,
                        COURIER_VISIBLE_TRIP_STATUSES
                )
                .orElseThrow(() -> new AppException(ErrorCode.UNAUTHORIZED));

        if (tripOrder.getId() == null || tripOrder.getTrip() == null || tripOrder.getTrip().getId() == null) {
            throw new AppException(
                    ErrorCode.INVALID_REQUEST,
                    "Pickup trip assignment is invalid. Please contact support."
            );
        }

        Point pickupLocation = order.getSenderLocation();
        if (pickupLocation == null || !isValidCoordinate(pickupLocation.getY(), pickupLocation.getX())) {
            throw new AppException(ErrorCode.ORDER_NOT_ASSIGNABLE, "Order pickup location is missing or invalid.");
        }

        double allowedRadiusMeters = resolvePickupCheckinRadiusMeters();
        double distanceMeters = calculateDistanceMeters(
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

        PickupCheckin pickupCheckin = pickupCheckinRepository
                .findByTenantIdAndTripOrderId(tenantId, tripOrder.getId())
                .orElseGet(PickupCheckin::new);

        pickupCheckin.setTenantId(tenantId);
        pickupCheckin.setTripOrderId(tripOrder.getId());
        pickupCheckin.setOrderId(order.getId());
        pickupCheckin.setTripId(tripOrder.getTrip().getId());
        pickupCheckin.setCourierStaffId(courierStaffId);
        pickupCheckin.setCheckinTime(LocalDateTime.now());
        pickupCheckin.setCheckinLocation(toPoint(checkinLatitude, checkinLongitude));
        pickupCheckin.setDistanceM(round3(distanceMeters));
        pickupCheckin.setAllowedRadiusM(round3(allowedRadiusMeters));
        pickupCheckin.setPhotoUrl(uploadResponse.getUrl());

        PickupCheckin savedCheckin = pickupCheckinRepository.save(pickupCheckin);

        if (TripStatus.PLANNED.equals(tripOrder.getTrip().getStatus())) {
            tripOrder.getTrip().setStatus(TripStatus.IN_PROGRESS);
        }

        order.setStatus(OrderStatus.PICKING_UP);
        Order savedOrder = orderRepository.save(order);
        orderTimelineService.recordStatusEvent(
                savedOrder,
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
        );

        tryAutoCompleteTripAfterCheckin(tripOrder.getTrip(), tenantId);

        return toPickupCheckinResponse(savedOrder, tripOrder.getTrip(), savedCheckin, pickupLocation);
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public OrderConfirmationResponse confirmOrder(Long orderId, Long tenantId) {
        OrderActorScope actorScope = resolveActorScope(tenantId);
        Order order = orderRepository.findByIdAndTenantIdForUpdate(orderId, tenantId)
                .orElseThrow(() -> new AppException(ErrorCode.ORDER_NOT_FOUND));

        validateCanMutateOrder(order, tenantId, actorScope);

        if (OrderPickupMethod.DROP_OFF_AT_POST_OFFICE.equals(resolveOrderPickupMethod(order))) {
            throw new AppException(
                    ErrorCode.INVALID_REQUEST,
                    "Drop-off orders must be confirmed by a post office manager at the receiving post office."
            );
        }

        if (Boolean.TRUE.equals(order.getIsConfirm())) {
            throw new AppException(
                    ErrorCode.INVALID_REQUEST,
                    "Order has already been confirmed. If you need to change the order details, please contact support."
            );
        }

        if (FeePayer.SENDER.equals(order.getFeePayer())
                && !PaymentStatus.PAID.equals(order.getPaymentStatus())) {
            throw new AppException(
                    ErrorCode.INVALID_REQUEST,
                    "Sender must complete shipping fee payment before order confirmation."
            );
        }

        if (hasText(order.getOriginPostOfficeCode())) {
            order.setIsConfirm(true);
            orderRepository.save(order);
            syncOrder.sendOrderEvent(order);
            Optional<PostOffice> assignedPostOffice = resolveAssignedPostOffice(order, tenantId);
            return OrderMapper.toOrderConfirmationResponse(order, assignedPostOffice.orElse(null), true);
        }

        validateOrderForConfirmation(order);

        PostOffice postOffice = postOfficeRepository.findBestAssignablePostOfficeForSenderForUpdate(
                        tenantId,
                        order.getSenderLocation(),
                        LocalDate.now()
                )
                .orElseThrow(() -> new AppException(ErrorCode.NO_SUITABLE_ORIGIN_POST_OFFICE));

        postOffice.addLoad(1);
        order.setOriginPostOfficeCode(postOffice.getCode());
        order.setIsConfirm(true);

        postOfficeRepository.save(postOffice);
        orderRepository.save(order);
        syncOrder.sendOrderEvent(order);
        return OrderMapper.toOrderConfirmationResponse(order, postOffice, false);
    }

    @Override
    public List<OrderDropOffPostOfficeSuggestionResponse> getDropOffPostOfficeSuggestions(
            Long orderId,
            Integer limit,
            Long tenantId
    ) {
        if (orderId == null || orderId <= 0) {
            throw new AppException(ErrorCode.INVALID_REQUEST);
        }

        OrderActorScope actorScope = resolveActorScope(tenantId);
        Order order = orderRepository.findByIdAndTenantId(orderId, tenantId)
                .orElseThrow(() -> new AppException(ErrorCode.ORDER_NOT_FOUND));

        validateCanMutateOrder(order, tenantId, actorScope);
        ensureDropOffPickupMethod(order);
        validateOrderForConfirmation(order);

        int suggestionLimit = normalizeDropOffSuggestionLimit(limit);
        Point senderLocation = order.getSenderLocation();
        LocalDate operationalDate = LocalDate.now();
        double senderLatitude = senderLocation.getY();
        double senderLongitude = senderLocation.getX();

        return postOfficeRepository.findAllByTenantId(tenantId)
                .stream()
                .map(postOffice -> buildSuggestionCandidate(postOffice, operationalDate, senderLatitude, senderLongitude))
                .flatMap(Optional::stream)
                .sorted(
                Comparator.comparingInt((DropOffSuggestionCandidate candidate) -> safePriority(candidate.postOffice().getPriority()))
                                .thenComparingDouble(DropOffSuggestionCandidate::distanceMeters)
                                .thenComparingInt(candidate -> safeInt(candidate.postOffice().getCurrentLoad()))
                                .thenComparingLong(candidate -> candidate.postOffice().getId() == null
                                        ? Long.MAX_VALUE
                                        : candidate.postOffice().getId())
                )
                .limit(suggestionLimit)
                .map(this::toDropOffSuggestionResponse)
                .toList();
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public OrderConfirmationResponse confirmDropOffOrderAtPostOffice(Long orderId, Long postOfficeId, Long tenantId) {
        if (orderId == null || orderId <= 0 || postOfficeId == null || postOfficeId <= 0) {
            throw new AppException(ErrorCode.INVALID_REQUEST);
        }

        PostOffice postOffice = postOfficeRepository.findByIdAndTenantIdForUpdate(postOfficeId, tenantId)
                .orElseThrow(() -> new AppException(ErrorCode.POST_OFFICE_NOT_FOUND));

        firstMileAccessUtils.ensureCurrentManagerAssignedToPostOfficeOrThrow(postOfficeId, tenantId);

        Order order = orderRepository.findByIdAndTenantIdForUpdate(orderId, tenantId)
                .orElseThrow(() -> new AppException(ErrorCode.ORDER_NOT_FOUND));

        ensureDropOffPickupMethod(order);

        if (Boolean.TRUE.equals(order.getIsConfirm())) {
            if (hasText(order.getOriginPostOfficeCode())
                    && postOffice.getCode() != null
                    && order.getOriginPostOfficeCode().equalsIgnoreCase(postOffice.getCode())) {
                if (!OrderStatus.AT_ORIGIN_POST_OFFICE.equals(order.getStatus())) {
                    order.setStatus(OrderStatus.AT_ORIGIN_POST_OFFICE);
                    Order savedOrder = orderRepository.save(order);
                    recordAtOriginPostOfficeTimeline(savedOrder, postOffice, "Order received at origin post office.");
                    syncOrder.sendOrderEvent(savedOrder);
                }
                return OrderMapper.toOrderConfirmationResponse(order, postOffice, true);
            }

            throw new AppException(
                    ErrorCode.INVALID_REQUEST,
                    "Order has already been confirmed at another post office."
            );
        }

        if (hasText(order.getOriginPostOfficeCode())) {
            if (postOffice.getCode() != null
                    && order.getOriginPostOfficeCode().equalsIgnoreCase(postOffice.getCode())) {
                order.setIsConfirm(true);
                order.setStatus(OrderStatus.AT_ORIGIN_POST_OFFICE);
                Order savedOrder = orderRepository.save(order);
                recordAtOriginPostOfficeTimeline(savedOrder, postOffice, "Order received at origin post office.");
                syncOrder.sendOrderEvent(savedOrder);
                return OrderMapper.toOrderConfirmationResponse(order, postOffice, true);
            }

            throw new AppException(
                    ErrorCode.INVALID_REQUEST,
                    "Order is already linked to another origin post office."
            );
        }

        validateOrderForConfirmation(order);

        Point senderLocation = order.getSenderLocation();
        if (!isPostOfficeSuitableForSender(postOffice, LocalDate.now(), senderLocation)) {
            throw new AppException(ErrorCode.NO_SUITABLE_ORIGIN_POST_OFFICE);
        }

        postOffice.addLoad(1);
        order.setOriginPostOfficeCode(postOffice.getCode());
        order.setIsConfirm(true);
        order.setStatus(OrderStatus.AT_ORIGIN_POST_OFFICE);

        postOfficeRepository.save(postOffice);
        Order savedOrder = orderRepository.save(order);
        recordAtOriginPostOfficeTimeline(savedOrder, postOffice, "Order confirmed and received at origin post office.");
        syncOrder.sendOrderEvent(savedOrder);

        return OrderMapper.toOrderConfirmationResponse(order, postOffice, false);
    }

    @Override
    public void publishOrderEvent(String orderCode) {
        var order = orderRepository.findByOrderCodeAndTenantId(orderCode, firstMileAccessUtils.getCurrentTenantIdOrThrow())
                .orElseThrow(() -> new AppException(ErrorCode.ORDER_NOT_FOUND));
        if (order.getIsConfirm()) {
            syncOrder.sendOrderEvent(order);
        } else {
            log.warn("The order with orderCode {} is not confirmed yet, skipping event publish.", orderCode);
        }
    }
    private void validateOrderForConfirmation(Order order) {
        if (order == null) {
            throw new AppException(ErrorCode.ORDER_NOT_FOUND);
        }
        if (order.getStatus() == null || !CONFIRMABLE_ORDER_STATUSES.contains(order.getStatus())) {
            throw new AppException(ErrorCode.ORDER_NOT_ASSIGNABLE);
        }

        Point senderLocation = order.getSenderLocation();
        if (senderLocation == null) {
            throw new AppException(ErrorCode.ORDER_NOT_ASSIGNABLE);
        }

        double latitude = senderLocation.getY();
        double longitude = senderLocation.getX();
        if (!isValidCoordinate(latitude, longitude)) {
            throw new AppException(ErrorCode.ORDER_NOT_ASSIGNABLE);
        }
    }

    private long resolveShippingFeeForPayment(Order order, InitiateOrderPaymentRequest request) {
        if (order == null) {
            throw new AppException(ErrorCode.ORDER_NOT_FOUND);
        }
        if (request != null && request.getAmount() != null) {
            Long amount = request.getAmount();
            if (amount <= 0L) {
                throw new AppException(ErrorCode.INVALID_REQUEST, "Order shipping fee must be greater than 0 for payment.");
            }
            return amount;
        }
        Long totalShippingFee = order.getTotalShippingFee();
        if (totalShippingFee == null || totalShippingFee <= 0L) {
            throw new AppException(ErrorCode.INVALID_REQUEST, "Order shipping fee must be greater than 0 for payment.");
        }
        return totalShippingFee;
    }

    private boolean isValidCoordinate(double latitude, double longitude) {
        return !Double.isNaN(latitude)
                && !Double.isNaN(longitude)
                && !Double.isInfinite(latitude)
                && !Double.isInfinite(longitude)
                && latitude >= -90.0
                && latitude <= 90.0
                && longitude >= -180.0
                && longitude <= 180.0;
    }

    private Double toLatitude(Point location) {
        return location == null ? null : location.getY();
    }

    private Double toLongitude(Point location) {
        return location == null ? null : location.getX();
    }

    private String buildSenderLocationLabel(Order order) {
        if (order == null) {
            return null;
        }

        List<String> addressParts = new ArrayList<>();
        if (hasText(order.getSenderAddressDetail())) {
            addressParts.add(order.getSenderAddressDetail().trim());
        }
        if (hasText(order.getSenderWardCode())) {
            addressParts.add(order.getSenderWardCode().trim());
        }
        if (hasText(order.getSenderProvinceCode())) {
            addressParts.add(order.getSenderProvinceCode().trim());
        }

        if (addressParts.isEmpty()) {
            return null;
        }
        return String.join(", ", addressParts);
    }

    private void recordAtOriginPostOfficeTimeline(Order order, PostOffice postOffice, String description) {
        orderTimelineService.recordStatusEvent(
                order,
                OrderStatus.AT_ORIGIN_POST_OFFICE,
                description,
                new OrderTimelineContext(
                        LocalDateTime.now(),
                        null,
                        null,
                        postOffice == null ? null : postOffice.getId(),
                        postOffice == null ? null : postOffice.getCode(),
                        postOffice == null ? null : postOffice.getName(),
                        null,
                        null,
                        null,
                        null,
                        null,
                        postOffice == null ? null : toLatitude(postOffice.getLocation()),
                        postOffice == null ? null : toLongitude(postOffice.getLocation()),
                        postOffice == null ? null : postOffice.getName()
                )
        );
    }

    private boolean hasText(String value) {
        return value != null && !value.trim().isEmpty();
    }

    private String normalizeText(String value) {
        if (value == null) {
            return null;
        }

        String trimmedValue = value.trim();
        return trimmedValue.isEmpty() ? null : trimmedValue;
    }

    private Optional<PostOffice> resolveAssignedPostOffice(Order order, Long tenantId) {
        if (order == null || !hasText(order.getOriginPostOfficeCode())) {
            return Optional.empty();
        }

        return postOfficeRepository.findByCodeIgnoreCaseAndTenantId(order.getOriginPostOfficeCode(), tenantId);
    }



    private OrderActorScope resolveActorScope(Long tenantId) {
        if (firstMileAccessUtils.isAdmin()) {
            return OrderActorScope.admin();
        }

        Long currentUserId = firstMileAccessUtils.getCurrentUserIdOrThrow();

        if (firstMileAccessUtils.isPostOfficerManager()) {
            Set<String> managedOriginCodes = resolveManagedOriginPostOfficeCodes(currentUserId, tenantId);
            return OrderActorScope.manager(managedOriginCodes);
        }

        if (firstMileAccessUtils.isCourier()) {
            Long courierStaffId = resolvePostOfficeStaffIdByUserAndRole(currentUserId, tenantId, PostOfficeStaffRole.COURIER);
            return OrderActorScope.courier(courierStaffId, COURIER_VISIBLE_TRIP_STATUSES);
        }

        if (firstMileAccessUtils.isCustomer()) {
            return OrderActorScope.customer(String.valueOf(currentUserId));
        }

        throw new AppException(ErrorCode.UNAUTHORIZED);
    }

    private void validateCanReadOrder(Order order, Long tenantId, OrderActorScope actorScope) {
        if (order == null) {
            throw new AppException(ErrorCode.ORDER_NOT_FOUND);
        }

        switch (actorScope.actorType()) {
            case ADMIN:
                return;
            case CUSTOMER:
                if (isCustomerOwner(order, actorScope.customerCreatedBy())) {
                    return;
                }
                break;
            case MANAGER:
                String originPostOfficeCode = normalizeText(order.getOriginPostOfficeCode());
                if (hasText(originPostOfficeCode)
                        && actorScope.managedOriginPostOfficeCodes().contains(originPostOfficeCode.toLowerCase(Locale.ROOT))) {
                    return;
                }
                break;
            case COURIER:
                if (actorScope.courierStaffId() != null
                        && tripOrderRepository.existsByTenantIdAndOrderIdAndCourierStaffIdAndTripStatusIn(
                        tenantId,
                        order.getId(),
                        actorScope.courierStaffId(),
                        actorScope.courierVisibleTripStatuses()
                )) {
                    return;
                }
                break;
            default:
                break;
        }

        throw new AppException(ErrorCode.UNAUTHORIZED);
    }

    private void validateCanMutate(OrderActorScope actorScope) {
        if (actorScope.actorType() == OrderActorType.ADMIN || actorScope.actorType() == OrderActorType.CUSTOMER) {
            return;
        }
        throw new AppException(ErrorCode.UNAUTHORIZED);
    }

    private void validateCanMutateOrder(Order order, Long tenantId, OrderActorScope actorScope) {
        validateCanMutate(actorScope);

        if (actorScope.actorType() == OrderActorType.CUSTOMER) {
            validateCanReadOrder(order, tenantId, actorScope);
        }
    }

    private Set<String> resolveManagedOriginPostOfficeCodes(Long currentUserId, Long tenantId) {
        Long managerStaffId = resolvePostOfficeStaffIdByUserAndRole(currentUserId, tenantId, PostOfficeStaffRole.MANAGER);

        Set<Long> managedPostOfficeIds = postOfficeStaffAssignmentRepository.findActivePostOfficeIdsByStaffIdAndTenantId(
                managerStaffId,
                tenantId,
                LocalDate.now()
        );

        if (managedPostOfficeIds == null || managedPostOfficeIds.isEmpty()) {
            return Set.of();
        }

        return postOfficeRepository.findAllByTenantIdAndIdIn(tenantId, managedPostOfficeIds)
                .stream()
                .map(PostOffice::getCode)
                .filter(this::hasText)
                .map(code -> code.trim().toLowerCase(Locale.ROOT))
                .collect(java.util.stream.Collectors.toCollection(HashSet::new));
    }

    private Long resolvePostOfficeStaffIdByUserAndRole(Long currentUserId, Long tenantId, PostOfficeStaffRole role) {
        String staffCode = PostOfficeStaffCodeUtils.buildStaffCode(currentUserId, role);

        PostOfficeStaff postOfficeStaff = postOfficeStaffRepository.findByCodeAndTenantId(staffCode, tenantId)
                .orElseThrow(() -> new AppException(ErrorCode.UNAUTHORIZED));

        if (!role.equals(postOfficeStaff.getRole())) {
            throw new AppException(ErrorCode.UNAUTHORIZED);
        }

        return postOfficeStaff.getId();
    }

    private boolean isCustomerOwner(Order order, String customerCreatedBy) {
        if (order == null || !hasText(customerCreatedBy)) {
            return false;
        }
        return customerCreatedBy.equals(order.getCreatedBy());
    }

    private OrderPickupMethod resolveOrderPickupMethod(Order order) {
        if (order == null || order.getPickupMethod() == null) {
            return OrderPickupMethod.COURIER_PICKUP;
        }
        return order.getPickupMethod();
    }

    private void ensureDropOffPickupMethod(Order order) {
        if (!OrderPickupMethod.DROP_OFF_AT_POST_OFFICE.equals(resolveOrderPickupMethod(order))) {
            throw new AppException(
                    ErrorCode.INVALID_REQUEST,
                    "Order pickup method is not drop-off at post office."
            );
        }
    }

    private int normalizeDropOffSuggestionLimit(Integer limit) {
        if (limit == null || limit <= 0) {
            return DEFAULT_DROP_OFF_SUGGESTION_LIMIT;
        }
        return Math.min(limit, MAX_DROP_OFF_SUGGESTION_LIMIT);
    }

    private Optional<DropOffSuggestionCandidate> buildSuggestionCandidate(
            PostOffice postOffice,
            LocalDate operationalDate,
            double senderLatitude,
            double senderLongitude
    ) {
        if (postOffice == null || postOffice.getLocation() == null) {
            return Optional.empty();
        }

        Point postOfficeLocation = postOffice.getLocation();
        if (!isValidCoordinate(postOfficeLocation.getY(), postOfficeLocation.getX())) {
            return Optional.empty();
        }

        if (!isPostOfficeSuitableForSender(postOffice, operationalDate, toPoint(senderLatitude, senderLongitude))) {
            return Optional.empty();
        }

        double distanceMeters = calculateDistanceMeters(
                senderLatitude,
                senderLongitude,
                postOfficeLocation.getY(),
                postOfficeLocation.getX()
        );

        return Optional.of(new DropOffSuggestionCandidate(postOffice, distanceMeters));
    }

    private OrderDropOffPostOfficeSuggestionResponse toDropOffSuggestionResponse(DropOffSuggestionCandidate candidate) {
        PostOffice postOffice = candidate.postOffice();
        Point location = postOffice.getLocation();

        int currentLoad = safeInt(postOffice.getCurrentLoad());
        int dailyCapacity = safeInt(postOffice.getDailyCapacity());
        int remainingCapacity = Math.max(dailyCapacity - currentLoad, 0);

        return new OrderDropOffPostOfficeSuggestionResponse(
                postOffice.getId(),
                postOffice.getCode(),
                postOffice.getName(),
                postOffice.getProvinceCode(),
                postOffice.getWardCode(),
                postOffice.getAddressDetail(),
                postOffice.getPriority(),
                postOffice.getCurrentLoad(),
                postOffice.getDailyCapacity(),
                remainingCapacity,
                location == null ? null : round3(location.getY()),
                location == null ? null : round3(location.getX()),
                round3(candidate.distanceMeters())
        );
    }

    private boolean isPostOfficeSuitableForSender(PostOffice postOffice, LocalDate operationalDate, Point senderLocation) {
        if (postOffice == null
                || senderLocation == null
                || postOffice.getLocation() == null
                || postOffice.getDailyCapacity() == null
                || postOffice.getCurrentLoad() == null
                || !postOffice.isActive()
                || !isPostOfficeOperationalOnDate(postOffice, operationalDate)
                || !postOffice.canAccept(1)) {
            return false;
        }

        Integer serviceRadiusMeters = postOffice.getServiceRadiusM();
        if (serviceRadiusMeters == null || serviceRadiusMeters <= 0) {
            return false;
        }

        double distanceMeters = calculateDistanceMeters(
                senderLocation.getY(),
                senderLocation.getX(),
                postOffice.getLocation().getY(),
                postOffice.getLocation().getX()
        );

        return distanceMeters <= serviceRadiusMeters;
    }

    private boolean isPostOfficeOperationalOnDate(PostOffice postOffice, LocalDate operationalDate) {
        LocalDate startDate = postOffice.getOperationalStartDate();
        if (startDate != null && startDate.isAfter(operationalDate)) {
            return false;
        }

        LocalDate endDate = postOffice.getOperationalEndDate();
        return endDate == null || !endDate.isBefore(operationalDate);
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

        long checkedInOrders = pickupCheckinRepository.countByTenantIdAndTripId(tenantId, trip.getId());
        if (checkedInOrders < totalOrders) {
            return;
        }

        trip.setStatus(TripStatus.COMPLETED);
        log.info(
                "Auto completed pickup trip tripId={} tenantId={} checkedInOrders={} totalOrders={}",
                trip.getId(),
                tenantId,
                checkedInOrders,
                totalOrders
        );
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

    private double calculateDistanceMeters(
            double fromLatitude,
            double fromLongitude,
            double toLatitude,
            double toLongitude
    ) {
        double latitudeDeltaRadians = Math.toRadians(toLatitude - fromLatitude);
        double longitudeDeltaRadians = Math.toRadians(toLongitude - fromLongitude);

        double fromLatitudeRadians = Math.toRadians(fromLatitude);
        double toLatitudeRadians = Math.toRadians(toLatitude);

        double haversineComponent = Math.sin(latitudeDeltaRadians / 2) * Math.sin(latitudeDeltaRadians / 2)
                + Math.cos(fromLatitudeRadians) * Math.cos(toLatitudeRadians)
                * Math.sin(longitudeDeltaRadians / 2) * Math.sin(longitudeDeltaRadians / 2);

        double normalizedHaversineComponent = Math.max(0D, Math.min(1D, haversineComponent));
        double centralAngle = 2 * Math.atan2(
                Math.sqrt(normalizedHaversineComponent),
                Math.sqrt(1D - normalizedHaversineComponent)
        );

        return EARTH_RADIUS_METERS * centralAngle;
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

    private PickupCheckinResponse toPickupCheckinResponse(
            Order order,
            Trip trip,
            PickupCheckin pickupCheckin,
            Point pickupLocation
    ) {
        Point checkinLocation = pickupCheckin == null ? null : pickupCheckin.getCheckinLocation();

        return new PickupCheckinResponse(
                pickupCheckin == null ? null : pickupCheckin.getId(),
                order == null ? null : order.getId(),
                order == null ? null : order.getOrderCode(),
                order == null ? null : order.getStatus(),
                trip == null ? null : trip.getId(),
                trip == null ? null : trip.getTripCode(),
                pickupCheckin == null ? null : pickupCheckin.getCourierStaffId(),
                pickupCheckin == null ? null : pickupCheckin.getCheckinTime(),
                pickupCheckin == null ? null : pickupCheckin.getPhotoUrl(),
                checkinLocation == null ? null : round3(checkinLocation.getY()),
                checkinLocation == null ? null : round3(checkinLocation.getX()),
                pickupLocation == null ? null : round3(pickupLocation.getY()),
                pickupLocation == null ? null : round3(pickupLocation.getX()),
                pickupCheckin == null ? null : pickupCheckin.getDistanceM(),
                pickupCheckin == null ? null : pickupCheckin.getAllowedRadiusM()
        );
    }

    private double round3(double value) {
        return Math.round(value * 1000.0) / 1000.0;
    }

    private Point toPoint(Double latitude, Double longitude) {
        if (latitude == null || longitude == null) {
            return null;
        }
        return GEOMETRY_FACTORY.createPoint(new Coordinate(longitude, latitude));
    }

    private int safeInt(Integer value) {
        return value == null ? 0 : value;
    }

    private int safePriority(Integer value) {
        return value == null ? Integer.MAX_VALUE : value;
    }

    private record DropOffSuggestionCandidate(PostOffice postOffice, double distanceMeters) {
    }
}
