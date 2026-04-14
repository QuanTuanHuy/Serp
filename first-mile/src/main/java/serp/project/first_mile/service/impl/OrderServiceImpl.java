/*
Author: Nguyen The Anh
Description: Part of Serp Project
*/

package serp.project.first_mile.service.impl;

import lombok.RequiredArgsConstructor;
import org.apache.poi.ss.usermodel.Sheet;
import org.apache.poi.ss.usermodel.Workbook;
import org.apache.poi.xssf.usermodel.XSSFWorkbook;
import org.locationtech.jts.geom.Coordinate;
import org.locationtech.jts.geom.GeometryFactory;
import org.locationtech.jts.geom.Point;
import org.locationtech.jts.geom.PrecisionModel;
import org.springframework.core.io.ClassPathResource;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.multipart.MultipartFile;
import serp.project.first_mile.domain.Dimension;
import serp.project.first_mile.domain.Order;
import serp.project.first_mile.domain.PostOffice;
import serp.project.first_mile.domain.PostOfficeStaff;
import serp.project.first_mile.domain.Product;
import serp.project.first_mile.domain.ProductType;
import serp.project.first_mile.dto.PageResponse;
import serp.project.first_mile.dto.request.CancelOrderRequest;
import serp.project.first_mile.dto.request.CreateOrderRequest;
import serp.project.first_mile.dto.request.OrderFilterRequest;
import serp.project.first_mile.dto.request.OrderImportDTO;
import serp.project.first_mile.dto.request.UpdateOrderRequest;
import serp.project.first_mile.dto.response.ImportHistoryResponse;
import serp.project.first_mile.dto.response.OrderConfirmationResponse;
import serp.project.first_mile.dto.response.OrderDetailResponse;
import serp.project.first_mile.dto.response.ProductTypeTemplateDTO;
import serp.project.first_mile.dto.response.ProvinceExcelTemplateDTO;
import serp.project.first_mile.dto.response.ValidateImportFileDTO;
import serp.project.first_mile.dto.response.WardExcelTemplateDTO;
import serp.project.first_mile.enums.*;
import serp.project.first_mile.exception.AppException;
import serp.project.first_mile.exception.ErrorCode;
import serp.project.first_mile.kernel.utils.ExcelTemplateUtils;
import serp.project.first_mile.kernel.utils.FirstMileAccessUtils;
import serp.project.first_mile.kernel.utils.PostOfficeStaffCodeUtils;
import serp.project.first_mile.mapper.OrderMapper;
import serp.project.first_mile.repository.OrderRepository;
import serp.project.first_mile.repository.PostOfficeRepository;
import serp.project.first_mile.repository.PostOfficeStaffAssignmentRepository;
import serp.project.first_mile.repository.PostOfficeStaffRepository;
import serp.project.first_mile.repository.ProductTypeRepository;
import serp.project.first_mile.repository.TripOrderRepository;
import serp.project.first_mile.repository.specification.OrderSpecification;
import serp.project.first_mile.service.OrderExcelService;
import serp.project.first_mile.service.OrderImportExcelService;
import serp.project.first_mile.service.OrderService;
import serp.project.first_mile.service.dto.ManualOrderPayload;
import serp.project.first_mile.service.dto.ManualOrderProductPayload;
import serp.project.first_mile.service.dto.OrderActorScope;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.Collection;
import java.util.HashSet;
import java.util.HashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.Optional;
import java.util.Set;

@Service
@RequiredArgsConstructor
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
    private static final Set<OrderStatus> CONFIRMABLE_ORDER_STATUSES = Set.of(
            OrderStatus.CREATED,
            OrderStatus.PICKUP_FAILED
    );
    private static final Set<OrderStatus> EDITABLE_ORDER_STATUSES = Set.of(OrderStatus.CREATED);
    private static final List<TripStatus> COURIER_VISIBLE_TRIP_STATUSES = List.of(
            TripStatus.PLANNED,
            TripStatus.IN_PROGRESS
    );

    private final OrderExcelService orderExcelService;
    private final OrderImportExcelService orderImportExcelService;
    private final OrderRepository orderRepository;
    private final PostOfficeRepository postOfficeRepository;
    private final ProductTypeRepository productTypeRepository;
    private final FirstMileAccessUtils firstMileAccessUtils;
    private final PostOfficeStaffRepository postOfficeStaffRepository;
    private final PostOfficeStaffAssignmentRepository postOfficeStaffAssignmentRepository;
    private final TripOrderRepository tripOrderRepository;

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
        return OrderMapper.toOrderDetailResponse(cancelledOrder);
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public OrderConfirmationResponse confirmOrder(Long orderId, Long tenantId) {
        OrderActorScope actorScope = resolveActorScope(tenantId);
        Order order = orderRepository.findByIdAndTenantIdForUpdate(orderId, tenantId)
                .orElseThrow(() -> new AppException(ErrorCode.ORDER_NOT_FOUND));

        validateCanMutateOrder(order, tenantId, actorScope);

        if (Boolean.TRUE.equals(order.getIsConfirm())) {
            Optional<PostOffice> assignedPostOffice = resolveAssignedPostOffice(order, tenantId);
            return OrderMapper.toOrderConfirmationResponse(order, assignedPostOffice.orElse(null), true);
        }

        if (hasText(order.getOriginPostOfficeCode())) {
            order.setIsConfirm(true);
            orderRepository.save(order);

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

        return OrderMapper.toOrderConfirmationResponse(order, postOffice, false);
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
}
