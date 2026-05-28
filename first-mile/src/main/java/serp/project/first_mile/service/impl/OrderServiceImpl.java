/*
Author: Nguyen The Anh
Description: Part of Serp Project
*/

package serp.project.first_mile.service.impl;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.apache.poi.ss.usermodel.Sheet;
import org.apache.poi.ss.usermodel.Workbook;
import org.apache.poi.xssf.usermodel.XSSFWorkbook;
import org.locationtech.jts.geom.Coordinate;
import org.locationtech.jts.geom.GeometryFactory;
import org.locationtech.jts.geom.Point;
import org.locationtech.jts.geom.PrecisionModel;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.core.io.ClassPathResource;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.multipart.MultipartFile;
import serp.project.first_mile.caller.PaymentServiceCaller;
import serp.project.first_mile.caller.dto.payment.PaymentCreateOrderRequest;
import serp.project.first_mile.caller.dto.payment.PaymentCreateOrderResponse;
import serp.project.first_mile.caller.dto.payment.PaymentQueryOrderResponse;
import serp.project.first_mile.domain.Dimension;
import serp.project.first_mile.domain.Order;
import serp.project.first_mile.domain.PostOffice;
import serp.project.first_mile.domain.PostOfficeStaff;
import serp.project.first_mile.domain.PickupCheckin;
import serp.project.first_mile.domain.Product;
import serp.project.first_mile.domain.ProductType;
import serp.project.first_mile.domain.Trip;
import serp.project.first_mile.dto.request.FileUploadRequest;
import serp.project.first_mile.dto.PageResponse;
import serp.project.first_mile.dto.request.CancelOrderRequest;
import serp.project.first_mile.dto.request.ConfirmOrderPaymentRequest;
import serp.project.first_mile.dto.request.CreateOrderRequest;
import serp.project.first_mile.dto.request.OrderFilterRequest;
import serp.project.first_mile.dto.request.OrderImportDTO;
import serp.project.first_mile.dto.request.UpdateOrderRequest;
import serp.project.first_mile.dto.response.FileUploadResponse;
import serp.project.first_mile.dto.response.ImportHistoryResponse;
import serp.project.first_mile.dto.response.OrderConfirmationResponse;
import serp.project.first_mile.dto.response.OrderPaymentConfirmResponse;
import serp.project.first_mile.dto.response.OrderPaymentInitResponse;
import serp.project.first_mile.dto.response.OrderDetailResponse;
import serp.project.first_mile.dto.response.OrderDropOffPostOfficeSuggestionResponse;
import serp.project.first_mile.dto.response.OrderTimelineResponse;
import serp.project.first_mile.dto.response.PickupCheckinResponse;
import serp.project.first_mile.dto.response.ProductTypeTemplateDTO;
import serp.project.first_mile.dto.response.ProvinceExcelTemplateDTO;
import serp.project.first_mile.dto.response.ValidateImportFileDTO;
import serp.project.first_mile.dto.response.WardExcelTemplateDTO;
import serp.project.first_mile.enums.*;
import serp.project.first_mile.exception.AppException;
import serp.project.first_mile.exception.ErrorCode;
import serp.project.first_mile.kafka.impl.order.SyncOrder;
import serp.project.first_mile.kernel.utils.ExcelTemplateUtils;
import serp.project.first_mile.kernel.utils.FirstMileAccessUtils;
import serp.project.first_mile.kernel.utils.ImageContentTypeUtils;
import serp.project.first_mile.kernel.utils.PostOfficeStaffCodeUtils;
import serp.project.first_mile.mapper.OrderMapper;
import serp.project.first_mile.repository.OrderRepository;
import serp.project.first_mile.repository.PickupCheckinRepository;
import serp.project.first_mile.repository.PostOfficeRepository;
import serp.project.first_mile.repository.PostOfficeStaffAssignmentRepository;
import serp.project.first_mile.repository.PostOfficeStaffRepository;
import serp.project.first_mile.repository.ProductTypeRepository;
import serp.project.first_mile.repository.TripOrderRepository;
import serp.project.first_mile.repository.specification.OrderSpecification;
import serp.project.first_mile.service.FileStorageService;
import serp.project.first_mile.service.OrderExcelService;
import serp.project.first_mile.service.OrderImportExcelService;
import serp.project.first_mile.service.OrderService;
import serp.project.first_mile.service.OrderTimelineService;
import serp.project.first_mile.service.dto.ManualOrderPayload;
import serp.project.first_mile.service.dto.ManualOrderProductPayload;
import serp.project.first_mile.service.dto.OrderActorScope;
import serp.project.first_mile.service.dto.OrderTimelineContext;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.Optional;
import java.util.Set;

@Service
@RequiredArgsConstructor
@Slf4j
@Transactional(readOnly = true)
public class OrderServiceImpl implements OrderService {

    private static final String TEMPLATE_PATH = "excel/order_template.xlsx";
    private static final String UNIT_SHEET_NAME = "Unit";
    private static final int START_ROW_INDEX = 1;
    private static final int WARD_COLUMN_INDEX = 0;
    private static final int PROVINCE_COLUMN_INDEX = 1;
    private static final int PRODUCT_TYPE_COLUMN_INDEX = 5;
    private static final GeometryFactory GEOMETRY_FACTORY = new GeometryFactory(new PrecisionModel(), 4326);
    private static final String ORDER_CODE_PREFIX = "ORD";
    private static final DateTimeFormatter ORDER_CODE_DATE_FORMATTER = DateTimeFormatter.BASIC_ISO_DATE;
    private static final int ORDER_CODE_SEQUENCE_LENGTH = 4;
    private static final Object ORDER_CODE_LOCK = new Object();
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
    private static final Set<OrderStatus> EDITABLE_ORDER_STATUSES = Set.of(OrderStatus.CREATED);
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

    private final OrderExcelService orderExcelService;
    private final OrderImportExcelService orderImportExcelService;
    private final OrderRepository orderRepository;
    private final PostOfficeRepository postOfficeRepository;
    private final ProductTypeRepository productTypeRepository;
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
    public byte[] exportTemplate(Long tenantId) {
        List<WardExcelTemplateDTO> wards = orderExcelService.getWardExcelTemplate();
        List<ProvinceExcelTemplateDTO> provinces = orderExcelService.getProvinceExcelTemplate();
        List<ProductTypeTemplateDTO> productTypes = orderExcelService.getProductTypeTemplate(tenantId);

        try (InputStream inputStream = new ClassPathResource(TEMPLATE_PATH).getInputStream();
             Workbook workbook = new XSSFWorkbook(inputStream);
             ByteArrayOutputStream outputStream = new ByteArrayOutputStream()) {

            Sheet unitSheet = workbook.getSheet(UNIT_SHEET_NAME);
            if (unitSheet == null) {
                throw new AppException(ErrorCode.UNCATEGORIZED_EXCEPTION);
            }

            populateWardColumn(unitSheet, wards);
            populateProvinceColumn(unitSheet, provinces);
            populateProductTypeColumn(unitSheet, productTypes);

            workbook.write(outputStream);
            return outputStream.toByteArray();
        } catch (IOException exception) {
            throw new AppException(ErrorCode.UNCATEGORIZED_EXCEPTION);
        }
    }

    @Override
    public ValidateImportFileDTO<OrderImportDTO> validateImportFile(MultipartFile file, Long tenantId) {
        return orderImportExcelService.validateImportFile(file, tenantId);
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public ImportHistoryResponse importOrdersAsync(MultipartFile file, Long tenantId) {
        return orderImportExcelService.importOrdersAsync(file, tenantId);
    }

    @Override
    public PageResponse<OrderDetailResponse> getOrders(int page, int size, OrderFilterRequest filterRequest, Long tenantId) {
        if (page < 0 || size <= 0) {
            throw new AppException(ErrorCode.INVALID_REQUEST);
        }

        OrderActorScope actorScope = resolveActorScope(tenantId);
        OrderFilterRequest normalizedFilter = normalizeOrderFilterRequest(filterRequest);
        validateOrderFilterRanges(normalizedFilter);

        Pageable pageable = PageRequest.of(page, size, Sort.by(Sort.Direction.DESC, "id"));

        Page<Order> orderPage = orderRepository.findAll(
                OrderSpecification.byFilter(
                        tenantId,
                        normalizedFilter,
                        actorScope.customerCreatedBy(),
                        actorScope.managedOriginPostOfficeCodes(),
                        actorScope.courierStaffId(),
                        actorScope.courierVisibleTripStatuses()
                ),
                pageable
        );

        return PageResponse.<OrderDetailResponse>builder()
                .items(orderPage.getContent().stream().map(OrderMapper::toOrderDetailResponse).toList())
                .page(orderPage.getNumber())
                .size(orderPage.getSize())
                .totalElements(orderPage.getTotalElements())
                .totalPages(orderPage.getTotalPages())
                .hasNext(orderPage.hasNext())
                .hasPrevious(orderPage.hasPrevious())
                .build();
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public OrderDetailResponse createOrder(CreateOrderRequest request, Long tenantId) {
        OrderActorScope actorScope = resolveActorScope(tenantId);
        validateCanMutate(actorScope);

        ManualOrderPayload payload = OrderMapper.toManualOrderPayload(request);
        validateUniqueCustomerOrderCode(payload.customerOrderCode(), tenantId, null);

        Order order = Order.builder()
                .orderCode(generateNextOrderCode())
                .status(OrderStatus.CREATED)
                .isConfirm(false)
                .pickupAttempts(0)
                .baseShippingFee(0L)
                .codFee(0L)
                .extraFee(0L)
                .totalShippingFee(0L)
                .paymentStatus(PaymentStatus.UNPAID)
                .tenantId(tenantId)
                .build();

        applyManualOrderPayload(order, payload, tenantId);

        Order savedOrder = orderRepository.save(order);
        orderTimelineService.recordStatusEvent(
                savedOrder,
                OrderStatus.CREATED,
                "Order created.",
                new OrderTimelineContext(
                        savedOrder.getCreatedAt(),
                        null,
                        null,
                        null,
                        savedOrder.getOriginPostOfficeCode(),
                        null,
                        null,
                        null,
                        null,
                        null,
                        null,
                        toLatitude(savedOrder.getSenderLocation()),
                        toLongitude(savedOrder.getSenderLocation()),
                        buildSenderLocationLabel(savedOrder)
                )
        );
        return OrderMapper.toOrderDetailResponse(savedOrder);
    }

    @Override
    public OrderDetailResponse getOrderById(Long orderId, Long tenantId) {
        OrderActorScope actorScope = resolveActorScope(tenantId);
        Order order = orderRepository.findByIdAndTenantId(orderId, tenantId)
                .orElseThrow(() -> new AppException(ErrorCode.ORDER_NOT_FOUND));

        validateCanReadOrder(order, tenantId, actorScope);
        return OrderMapper.toOrderDetailResponse(order);
    }

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
    public OrderDetailResponse updateOrder(Long orderId, UpdateOrderRequest request, Long tenantId) {
        OrderActorScope actorScope = resolveActorScope(tenantId);
        Order order = orderRepository.findByIdAndTenantIdForUpdate(orderId, tenantId)
                .orElseThrow(() -> new AppException(ErrorCode.ORDER_NOT_FOUND));

        validateCanMutateOrder(order, tenantId, actorScope);
        ensureOrderEditable(order);

        ManualOrderPayload payload = OrderMapper.toManualOrderPayload(request);
        validateUniqueCustomerOrderCode(payload.customerOrderCode(), tenantId, order.getId());

        releaseConfirmationIfNeeded(order, tenantId);
        applyManualOrderPayload(order, payload, tenantId);

        Order updatedOrder = orderRepository.save(order);
        return OrderMapper.toOrderDetailResponse(updatedOrder);
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public OrderDetailResponse cancelOrder(Long orderId, Long tenantId, CancelOrderRequest request) {
        OrderActorScope actorScope = resolveActorScope(tenantId);
        Order order = orderRepository.findByIdAndTenantIdForUpdate(orderId, tenantId)
                .orElseThrow(() -> new AppException(ErrorCode.ORDER_NOT_FOUND));

        validateCanMutateOrder(order, tenantId, actorScope);
        ensureOrderEditable(order);

        releaseConfirmationIfNeeded(order, tenantId);
        order.setStatus(OrderStatus.CANCELLED);
        order.setCancelledAt(LocalDateTime.now());
        order.setCancelReason(request == null ? null : normalizeText(request.getCancelReason()));

        Order cancelledOrder = orderRepository.save(order);
        orderTimelineService.recordStatusEvent(
                cancelledOrder,
                OrderStatus.CANCELLED,
                hasText(cancelledOrder.getCancelReason())
                        ? "Order cancelled. Reason: " + cancelledOrder.getCancelReason()
                        : "Order cancelled.",
                new OrderTimelineContext(
                        cancelledOrder.getCancelledAt(),
                        null,
                        null,
                        null,
                        cancelledOrder.getOriginPostOfficeCode(),
                        null,
                        null,
                        null,
                        null,
                        null,
                        null,
                        toLatitude(cancelledOrder.getSenderLocation()),
                        toLongitude(cancelledOrder.getSenderLocation()),
                        buildSenderLocationLabel(cancelledOrder)
                )
        );
        // Gửi sự kiện kafka
        syncOrder.sendOrderEvent(order);
        return OrderMapper.toOrderDetailResponse(cancelledOrder);
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public OrderPaymentInitResponse initiateOrderPayment(Long orderId, Long tenantId) {
        OrderActorScope actorScope = resolveActorScope(tenantId);
        Order order = orderRepository.findByIdAndTenantIdForUpdate(orderId, tenantId)
                .orElseThrow(() -> new AppException(ErrorCode.ORDER_NOT_FOUND));

        validateCanMutateOrder(order, tenantId, actorScope);

        if (PaymentStatus.PAID.equals(order.getPaymentStatus())) {
            throw new AppException(ErrorCode.INVALID_REQUEST, "Order shipping fee is already paid.");
        }

        long shippingFee = resolveShippingFee(order);
        Long actorId = firstMileAccessUtils.getCurrentUserIdOrThrow();
        String orderCode = order.getOrderCode();

        PaymentCreateOrderRequest paymentRequest = PaymentCreateOrderRequest.builder()
                .appUser(orderCode)
                .amount(shippingFee)
                .description("Thanh toan phi van chuyen cho don hang " + orderCode)
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

        if (hasText(order.getOriginPostOfficeCode())) {
            order.setIsConfirm(true);
            orderRepository.save(order);
            // Gửi sự kiện kafka
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
        // Gửi sự kiện kafka
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

    private long resolveShippingFee(Order order) {
        if (order == null) {
            throw new AppException(ErrorCode.ORDER_NOT_FOUND);
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



    private OrderFilterRequest normalizeOrderFilterRequest(OrderFilterRequest filterRequest) {
        if (filterRequest == null) {
            return OrderFilterRequest.builder().build();
        }

        return OrderFilterRequest.builder()
                .keyword(normalizeText(filterRequest.getKeyword()))
                .orderCode(normalizeText(filterRequest.getOrderCode()))
                .customerOrderCode(normalizeText(filterRequest.getCustomerOrderCode()))
                .senderPhone(normalizeText(filterRequest.getSenderPhone()))
                .receiverPhone(normalizeText(filterRequest.getReceiverPhone()))
                .originPostOfficeCode(normalizeText(filterRequest.getOriginPostOfficeCode()))
                .destinationPostOfficeCode(normalizeText(filterRequest.getDestinationPostOfficeCode()))
                .status(filterRequest.getStatus())
                .isConfirm(filterRequest.getIsConfirm())
                .createdFrom(filterRequest.getCreatedFrom())
                .createdTo(filterRequest.getCreatedTo())
                .pickupFrom(filterRequest.getPickupFrom())
                .pickupTo(filterRequest.getPickupTo())
                .build();
    }

    private void validateOrderFilterRanges(OrderFilterRequest filterRequest) {
        if (filterRequest.getCreatedFrom() != null
                && filterRequest.getCreatedTo() != null
                && filterRequest.getCreatedFrom().isAfter(filterRequest.getCreatedTo())) {
            throw new AppException(ErrorCode.INVALID_REQUEST);
        }

        if (filterRequest.getPickupFrom() != null
                && filterRequest.getPickupTo() != null
                && filterRequest.getPickupFrom().isAfter(filterRequest.getPickupTo())) {
            throw new AppException(ErrorCode.INVALID_REQUEST);
        }
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

    private void ensureOrderEditable(Order order) {
        if (order == null) {
            throw new AppException(ErrorCode.ORDER_NOT_FOUND);
        }

        if (order.getStatus() == null || !EDITABLE_ORDER_STATUSES.contains(order.getStatus())) {
            throw new AppException(ErrorCode.ORDER_NOT_EDITABLE);
        }
    }

    private void validateUniqueCustomerOrderCode(String customerOrderCode, Long tenantId, Long excludedOrderId) {
        if (!hasText(customerOrderCode)) {
            throw new AppException(ErrorCode.INVALID_REQUEST);
        }

        if (excludedOrderId == null) {
            if (orderRepository.existsByCustomerOrderCodeIgnoreCaseAndTenantId(customerOrderCode, tenantId)) {
                throw new AppException(ErrorCode.ORDER_CUSTOMER_CODE_EXISTED);
            }
            return;
        }

        if (orderRepository.existsByCustomerOrderCodeIgnoreCaseAndTenantIdAndIdNot(customerOrderCode, tenantId, excludedOrderId)) {
            throw new AppException(ErrorCode.ORDER_CUSTOMER_CODE_EXISTED);
        }
    }

    private List<Product> mapProducts(List<ManualOrderProductPayload> productPayloads, Long tenantId) {
        List<ManualOrderProductPayload> safeProductPayloads =
                productPayloads == null ? List.of() : productPayloads;

        Map<Long, ProductType> productTypeById = loadProductTypeById(safeProductPayloads, tenantId);

        List<Product> mappedProducts = new ArrayList<>();
        for (ManualOrderProductPayload payload : safeProductPayloads) {
            ProductType productType = productTypeById.get(payload.productTypeId());
            if (productType == null) {
                throw new AppException(ErrorCode.PRODUCT_TYPE_NOT_FOUND);
            }

            Product product = Product.builder()
                    .name(payload.name())
                    .value(payload.value())
                    .quantity(payload.quantity())
                    .weight(payload.weightGram())
                    .productType(productType)
                    .tenantId(tenantId)
                    .build();
            mappedProducts.add(product);
        }

        return mappedProducts;
    }

    private Map<Long, ProductType> loadProductTypeById(List<ManualOrderProductPayload> payloads, Long tenantId) {
        Map<Long, ProductType> productTypeById = new HashMap<>();
        for (ManualOrderProductPayload payload : payloads) {
            Long productTypeId = payload.productTypeId();
            if (productTypeId == null || productTypeById.containsKey(productTypeId)) {
                continue;
            }

            ProductType productType = productTypeRepository.findByIdAndTenantId(productTypeId, tenantId)
                    .orElseThrow(() -> new AppException(ErrorCode.PRODUCT_TYPE_NOT_FOUND));
            productTypeById.put(productTypeId, productType);
        }

        return productTypeById;
    }

    private void applyManualOrderPayload(Order order, ManualOrderPayload payload, Long tenantId) {
        order.setCustomerOrderCode(payload.customerOrderCode());

        order.setSenderName(payload.senderName());
        order.setSenderPhone(payload.senderPhone());
        order.setSenderProvinceCode(payload.senderProvinceCode());
        order.setSenderWardCode(payload.senderWardCode());
        order.setSenderAddressDetail(payload.senderAddressDetail());
        order.setSenderLocation(toPoint(payload.senderLatitude(), payload.senderLongitude()));

        order.setReceiverName(payload.receiverName());
        order.setReceiverPhone(payload.receiverPhone());
        order.setReceiverProvinceCode(payload.receiverProvinceCode());
        order.setReceiverWardCode(payload.receiverWardCode());
        order.setReceiverAddressDetail(payload.receiverAddressDetail());
        order.setReceiverLocation(toPoint(payload.receiverLatitude(), payload.receiverLongitude()));

        order.setPickupTimeStart(payload.pickupTimeStart());
        order.setPickupTimeEnd(payload.pickupTimeEnd());
        order.setDeliveryRequestTime(payload.deliveryRequestTime());

        OrderPickupMethod pickupMethod = payload.pickupMethod();
        if (pickupMethod == null) {
            pickupMethod = order.getPickupMethod() == null
                ? OrderPickupMethod.COURIER_PICKUP
                : order.getPickupMethod();
        }
        order.setPickupMethod(pickupMethod);

        order.setOrderProductCategory(payload.orderProductCategory());
        order.setOrderType(payload.orderType());
        order.setFeePayer(payload.feePayer());
        order.setNote(payload.note());

        order.setDimensions(buildDimensions(
                payload.dimensionLengthCm(),
                payload.dimensionWidthCm(),
                payload.dimensionHeightCm()
        ));
        order.setTotalVolume(payload.totalVolumeM3());

        if (order.getPaymentStatus() == null) {
            order.setPaymentStatus(PaymentStatus.UNPAID);
        }
        if (order.getPickupAttempts() == null) {
            order.setPickupAttempts(0);
        }
        if (order.getBaseShippingFee() == null) {
            order.setBaseShippingFee(0L);
        }
        if (order.getCodFee() == null) {
            order.setCodFee(0L);
        }
        if (order.getExtraFee() == null) {
            order.setExtraFee(0L);
        }
        if (order.getTotalShippingFee() == null) {
            order.setTotalShippingFee(0L);
        }

        List<Product> mappedProducts = mapProducts(payload.products(), tenantId);
        replaceProducts(order, mappedProducts);

        double totalWeight = mappedProducts.stream()
                .mapToDouble(product -> safeDouble(product.getWeight()) * safeInt(product.getQuantity()))
                .sum();

        long totalValueAmount = mappedProducts.stream()
                .mapToLong(product -> safeLong(product.getValue()) * safeInt(product.getQuantity()))
                .sum();

        order.setTotalWeight(totalWeight);
        order.setTotalValue((double) totalValueAmount);
        order.setCodAmount(Boolean.TRUE.equals(payload.isCod()) ? totalValueAmount : 0L);
        order.setTenantId(tenantId);
    }

    private void replaceProducts(Order order, List<Product> products) {
        if (order.getProducts() == null) {
            order.setProducts(new ArrayList<>());
        }

        List<Product> existingProducts = new ArrayList<>(order.getProducts());
        for (Product existingProduct : existingProducts) {
            order.removeProduct(existingProduct);
        }

        for (Product product : products) {
            order.addProduct(product);
        }
    }

    private void releaseConfirmationIfNeeded(Order order, Long tenantId) {
        if (!Boolean.TRUE.equals(order.getIsConfirm())) {
            return;
        }

        resolveAssignedPostOffice(order, tenantId).ifPresent(postOffice -> {
            Integer currentLoad = postOffice.getCurrentLoad();
            if (currentLoad != null && currentLoad > 0) {
                postOffice.setCurrentLoad(currentLoad - 1);
                postOfficeRepository.save(postOffice);
            }
        });

        order.setIsConfirm(false);
        order.setOriginPostOfficeCode(null);
    }

    private String generateNextOrderCode() {
        String orderCodePrefix = ORDER_CODE_PREFIX + LocalDate.now().format(ORDER_CODE_DATE_FORMATTER);
        synchronized (ORDER_CODE_LOCK) {
            int currentSequence = resolveCurrentOrderCodeSequence(orderCodePrefix);
            return formatOrderCode(orderCodePrefix, currentSequence + 1);
        }
    }

    private int resolveCurrentOrderCodeSequence(String orderCodePrefix) {
        String maxOrderCode = orderRepository.findMaxOrderCodeByPrefix(orderCodePrefix);
        if (!hasText(maxOrderCode) || !maxOrderCode.startsWith(orderCodePrefix)) {
            return 0;
        }

        String sequencePart = maxOrderCode.substring(orderCodePrefix.length());
        if (!hasText(sequencePart)) {
            return 0;
        }

        try {
            return Integer.parseInt(sequencePart);
        } catch (NumberFormatException exception) {
            return 0;
        }
    }

    private String formatOrderCode(String orderCodePrefix, int sequence) {
        return String.format(Locale.ROOT, "%s%0" + ORDER_CODE_SEQUENCE_LENGTH + "d", orderCodePrefix, sequence);
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

    private Dimension buildDimensions(Double length, Double width, Double height) {
        if (length == null || width == null || height == null) {
            return null;
        }

        Dimension dimensions = new Dimension();
        dimensions.setLength(length);
        dimensions.setWidth(width);
        dimensions.setHeight(height);
        return dimensions;
    }



    private int safeInt(Integer value) {
        return value == null ? 0 : value;
    }

    private int safePriority(Integer value) {
        return value == null ? Integer.MAX_VALUE : value;
    }

    private long safeLong(Long value) {
        return value == null ? 0L : value;
    }

    private double safeDouble(Double value) {
        return value == null ? 0D : value;
    }

    private void populateWardColumn(Sheet sheet, List<WardExcelTemplateDTO> wards) {
        for (int i = 0; i < wards.size(); i++) {
            WardExcelTemplateDTO ward = wards.get(i);
            ExcelTemplateUtils.setTextCellValue(
                    sheet,
                    START_ROW_INDEX + i,
                    WARD_COLUMN_INDEX,
                    ExcelTemplateUtils.formatCodeAndName(ward.getWardCode(), ward.getWardName())
            );
        }
    }

    private void populateProvinceColumn(Sheet sheet, List<ProvinceExcelTemplateDTO> provinces) {
        for (int i = 0; i < provinces.size(); i++) {
            ProvinceExcelTemplateDTO province = provinces.get(i);
            ExcelTemplateUtils.setTextCellValue(
                    sheet,
                    START_ROW_INDEX + i,
                    PROVINCE_COLUMN_INDEX,
                    ExcelTemplateUtils.formatCodeAndName(province.getProvinceCode(), province.getProvinceName())
            );
        }
    }

    private void populateProductTypeColumn(Sheet sheet, List<ProductTypeTemplateDTO> productTypes) {
        for (int i = 0; i < productTypes.size(); i++) {
            ProductTypeTemplateDTO productType = productTypes.get(i);
            ExcelTemplateUtils.setTextCellValue(
                    sheet,
                    START_ROW_INDEX + i,
                    PRODUCT_TYPE_COLUMN_INDEX,
                    ExcelTemplateUtils.formatCodeAndName(productType.getProductTypeCode(), productType.getProductTypeName())
            );
        }
    }

    private record DropOffSuggestionCandidate(PostOffice postOffice, double distanceMeters) {
    }
}
