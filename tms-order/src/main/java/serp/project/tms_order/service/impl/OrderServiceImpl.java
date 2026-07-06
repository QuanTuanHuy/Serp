/*
Author: Nguyen The Anh
Description: Part of Serp Project
*/

package serp.project.tms_order.service.impl;

import static serp.project.tms_order.kernel.utils.NumberUtils.safeDouble;
import static serp.project.tms_order.kernel.utils.NumberUtils.safeInt;
import static serp.project.tms_order.kernel.utils.NumberUtils.safeLong;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.locationtech.jts.geom.Point;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.multipart.MultipartFile;
import serp.project.tms_order.caller.FirstMilePostOfficeCaller;
import serp.project.tms_order.caller.dto.firstmile.DestinationPostOfficeReservationResponse;
import serp.project.tms_order.caller.dto.firstmile.OriginPostOfficeReservationResponse;
import serp.project.tms_order.domain.Order;
import serp.project.tms_order.domain.Product;
import serp.project.tms_order.domain.ProductType;
import serp.project.tms_order.dto.PageResponse;
import serp.project.tms_order.dto.request.CancelOrderRequest;
import serp.project.tms_order.dto.request.CreateOrderRequest;
import serp.project.tms_order.dto.request.OrderFilterRequest;
import serp.project.tms_order.dto.request.OrderImportDTO;
import serp.project.tms_order.dto.request.UpdateOrderRequest;
import serp.project.tms_order.dto.response.ImportHistoryResponse;
import serp.project.tms_order.dto.response.OrderConfirmationResponse;
import serp.project.tms_order.dto.response.OrderDetailResponse;
import serp.project.tms_order.dto.response.ValidateImportFileDTO;
import serp.project.tms_order.enums.FeePayer;
import serp.project.tms_order.enums.OrderPickupMethod;
import serp.project.tms_order.enums.OrderStatus;
import serp.project.tms_order.enums.PaymentStatus;
import serp.project.tms_order.exception.AppException;
import serp.project.tms_order.exception.ErrorCode;
import serp.project.tms_order.kafka.OrderEventDispatcher;
import serp.project.tms_order.mapper.OrderConfirmationMapper;
import serp.project.tms_order.mapper.OrderMapper;
import serp.project.tms_order.mapper.OrderProductMapper;
import serp.project.tms_order.repository.OrderRepository;
import serp.project.tms_order.repository.ProductTypeRepository;
import serp.project.tms_order.repository.specification.OrderSpecification;
import serp.project.tms_order.service.OrderImportExcelService;
import serp.project.tms_order.service.OrderService;
import serp.project.tms_order.service.OrderTimelineService;
import serp.project.tms_order.service.order.OrderAccessPolicy;
import serp.project.tms_order.service.order.OrderFilterNormalizer;
import serp.project.tms_order.service.order.OrderLocationUtils;
import serp.project.tms_order.service.order.OrderTemplateExporter;
import serp.project.tms_order.service.order.OrderTextUtils;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.EnumSet;
import java.util.HashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.Set;

@Service
@RequiredArgsConstructor
@Slf4j
@Transactional(readOnly = true)
public class OrderServiceImpl implements OrderService {

    private static final String ORDER_CODE_PREFIX = "ORD";
    private static final DateTimeFormatter ORDER_CODE_DATE_FORMATTER = DateTimeFormatter.BASIC_ISO_DATE;
    private static final int ORDER_CODE_SEQUENCE_LENGTH = 4;
    private static final Object ORDER_CODE_LOCK = new Object();
    private static final Set<OrderStatus> CONFIRMABLE_ORDER_STATUSES = EnumSet.of(
            OrderStatus.CREATED,
            OrderStatus.PICKUP_FAILED
    );

    private final OrderRepository orderRepository;
    private final ProductTypeRepository productTypeRepository;
    private final OrderImportExcelService orderImportExcelService;
    private final OrderTimelineService orderTimelineService;
    private final FirstMilePostOfficeCaller firstMilePostOfficeCaller;
    private final OrderTemplateExporter orderTemplateExporter;
    private final OrderFilterNormalizer orderFilterNormalizer;
    private final OrderAccessPolicy orderAccessPolicy;
    private final OrderEventDispatcher orderEventDispatcher;

    @Override
    public byte[] exportTemplate(Long tenantId) {
        return orderTemplateExporter.exportTemplate(tenantId);
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
    public PageResponse<OrderDetailResponse> getOrders(
            int page,
            int size,
            OrderFilterRequest filterRequest,
            Long tenantId
    ) {
        if (page < 0 || size <= 0) {
            throw new AppException(ErrorCode.INVALID_REQUEST);
        }

        OrderFilterRequest normalizedFilter = orderFilterNormalizer.normalize(filterRequest);
        orderFilterNormalizer.validateRanges(normalizedFilter);
        String customerCreatedBy = orderAccessPolicy.resolveCustomerCreatedByScope();
        Pageable pageable = PageRequest.of(page, size, resolveOrderSort(normalizedFilter));

        Page<Order> orderPage = orderRepository.findAll(
                OrderSpecification.byFilter(tenantId, normalizedFilter, customerCreatedBy),
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

    private Sort resolveOrderSort(OrderFilterRequest filterRequest) {
        String sortBy = filterRequest.getSortBy();
        if (!"updated_at".equals(sortBy)) {
            return Sort.by(Sort.Direction.DESC, "id");
        }

        Sort.Direction direction = "asc".equalsIgnoreCase(filterRequest.getSortDirection())
                ? Sort.Direction.ASC
                : Sort.Direction.DESC;
        return Sort.by(direction, "updatedAt").and(Sort.by(Sort.Direction.DESC, "id"));
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public OrderDetailResponse createOrder(CreateOrderRequest request, Long tenantId) {
        String customerOrderCode = OrderTextUtils.normalizeText(request.getCustomerOrderCode());
        validateUniqueCustomerOrderCode(customerOrderCode, tenantId, null);

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

        applyOrderRequest(order, request, customerOrderCode, tenantId);
        Order savedOrder = orderRepository.save(order);
        orderTimelineService.recordStatusEvent(
                savedOrder,
                OrderStatus.CREATED,
                "Order created.",
                null
        );
        log.info("Created TMS order orderCode={} tenantId={}", savedOrder.getOrderCode(), tenantId);
        return OrderMapper.toOrderDetailResponse(savedOrder);
    }

    @Override
    public OrderDetailResponse getOrderById(Long orderId, Long tenantId) {
        Order order = orderRepository.findByIdAndTenantId(orderId, tenantId)
                .orElseThrow(() -> new AppException(ErrorCode.ORDER_NOT_FOUND));

        orderAccessPolicy.validateCanReadOrder(order);
        return OrderMapper.toOrderDetailResponse(order);
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public OrderDetailResponse updateOrder(Long orderId, UpdateOrderRequest request, Long tenantId) {
        Order order = orderRepository.findByIdAndTenantIdForUpdate(orderId, tenantId)
                .orElseThrow(() -> new AppException(ErrorCode.ORDER_NOT_FOUND));

        orderAccessPolicy.validateCanMutateOrder(order);
        orderAccessPolicy.ensureOrderEditable(order);

        String customerOrderCode = OrderTextUtils.normalizeText(request.getCustomerOrderCode());
        validateUniqueCustomerOrderCode(customerOrderCode, tenantId, order.getId());
        applyOrderRequest(order, request, customerOrderCode, tenantId);

        Order updatedOrder = orderRepository.save(order);
        return OrderMapper.toOrderDetailResponse(updatedOrder);
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public OrderDetailResponse cancelOrder(Long orderId, Long tenantId, CancelOrderRequest request) {
        Order order = orderRepository.findByIdAndTenantIdForUpdate(orderId, tenantId)
                .orElseThrow(() -> new AppException(ErrorCode.ORDER_NOT_FOUND));

        orderAccessPolicy.validateCanMutateOrder(order);
        orderAccessPolicy.ensureOrderEditable(order);

        order.setStatus(OrderStatus.CANCELLED);
        order.setCancelledAt(LocalDateTime.now());
        order.setCancelReason(request == null ? null : OrderTextUtils.normalizeText(request.getCancelReason()));

        Order cancelledOrder = orderRepository.save(order);
        orderTimelineService.recordStatusEvent(
                cancelledOrder,
                OrderStatus.CANCELLED,
                OrderTextUtils.hasText(order.getCancelReason()) ? order.getCancelReason() : "Order cancelled.",
                null
        );
        orderEventDispatcher.publishOrderAfterCommit(cancelledOrder);
        orderEventDispatcher.publishOrderCancelledNotificationAfterCommit(cancelledOrder);
        return OrderMapper.toOrderDetailResponse(cancelledOrder);
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public void deleteOrder(Long orderId, Long tenantId) {
        Order order = orderRepository.findByIdAndTenantIdForUpdate(orderId, tenantId)
                .orElseThrow(() -> new AppException(ErrorCode.ORDER_NOT_FOUND));

        orderAccessPolicy.validateCanMutateOrder(order);
        orderAccessPolicy.ensureOrderEditable(order);
        orderRepository.delete(order);
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public OrderConfirmationResponse confirmOrder(Long orderId, Long tenantId) {
        Order order = orderRepository.findByIdAndTenantIdForUpdate(orderId, tenantId)
                .orElseThrow(() -> new AppException(ErrorCode.ORDER_NOT_FOUND));

        orderAccessPolicy.validateCanMutateOrder(order);

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

        ensureSenderPaymentCompletedBeforeConfirmation(order);
        validateOrderForConfirmation(order);

        DestinationPostOfficeReservationResponse reservedDestinationPostOffice =
                reserveDestinationPostOfficeIfNeeded(order);

        if (OrderTextUtils.hasText(order.getOriginPostOfficeCode())) {
            order.setIsConfirm(true);
            Order savedOrder = saveConfirmedOrder(order);
            return OrderConfirmationMapper.toResponse(savedOrder, null, reservedDestinationPostOffice, true);
        }

        OriginPostOfficeReservationResponse reservedPostOffice =
                firstMilePostOfficeCaller.reserveBestOriginPostOffice(
                        OrderLocationUtils.toLatitude(order.getSenderLocation()),
                        OrderLocationUtils.toLongitude(order.getSenderLocation())
                );

        order.setOriginPostOfficeCode(reservedPostOffice.getCode());
        order.setIsConfirm(true);

        Order savedOrder = saveConfirmedOrder(order);
        return OrderConfirmationMapper.toResponse(
                savedOrder,
                reservedPostOffice,
                reservedDestinationPostOffice,
                false
        );
    }

    private void validateUniqueCustomerOrderCode(String customerOrderCode, Long tenantId, Long excludedOrderId) {
        if (!OrderTextUtils.hasText(customerOrderCode)) {
            throw new AppException(ErrorCode.INVALID_REQUEST);
        }

        if (excludedOrderId == null) {
            if (orderRepository.existsByCustomerOrderCodeIgnoreCaseAndTenantId(customerOrderCode, tenantId)) {
                throw new AppException(ErrorCode.ORDER_CUSTOMER_CODE_EXISTED);
            }
            return;
        }

        if (orderRepository.existsByCustomerOrderCodeIgnoreCaseAndTenantIdAndIdNot(
                customerOrderCode,
                tenantId,
                excludedOrderId
        )) {
            throw new AppException(ErrorCode.ORDER_CUSTOMER_CODE_EXISTED);
        }
    }

    private void applyOrderRequest(
            Order order,
            CreateOrderRequest request,
            String customerOrderCode,
            Long tenantId
    ) {
        order.setCustomerOrderCode(customerOrderCode);

        order.setSenderName(OrderTextUtils.normalizeText(request.getSenderName()));
        order.setSenderPhone(OrderTextUtils.normalizeText(request.getSenderPhone()));
        order.setSenderProvinceCode(OrderTextUtils.normalizeText(request.getSenderProvinceCode()));
        order.setSenderWardCode(OrderTextUtils.normalizeText(request.getSenderWardCode()));
        order.setSenderAddressDetail(OrderTextUtils.normalizeText(request.getSenderAddressDetail()));
        order.setSenderLocation(OrderLocationUtils.toPoint(request.getSenderLatitude(), request.getSenderLongitude()));

        order.setReceiverName(OrderTextUtils.normalizeText(request.getReceiverName()));
        order.setReceiverPhone(OrderTextUtils.normalizeText(request.getReceiverPhone()));
        order.setReceiverProvinceCode(OrderTextUtils.normalizeText(request.getReceiverProvinceCode()));
        order.setReceiverWardCode(OrderTextUtils.normalizeText(request.getReceiverWardCode()));
        order.setReceiverAddressDetail(OrderTextUtils.normalizeText(request.getReceiverAddressDetail()));
        order.setReceiverLocation(OrderLocationUtils.toPoint(request.getReceiverLatitude(), request.getReceiverLongitude()));

        order.setPickupTimeStart(request.getPickupTimeStart());
        order.setPickupTimeEnd(request.getPickupTimeEnd());
        order.setDeliveryRequestTime(request.getDeliveryRequestTime());
        order.setPickupMethod(request.getPickupMethod() == null
                ? OrderPickupMethod.COURIER_PICKUP
                : request.getPickupMethod());
        order.setOrderType(request.getOrderType());
        order.setOrderProductCategory(request.getOrderProductCategory());
        order.setFeePayer(request.getFeePayer());
        order.setNote(OrderTextUtils.normalizeText(request.getNote()));
        order.setDimensions(OrderLocationUtils.buildDimensions(
                request.getDimensionLengthCm(),
                request.getDimensionWidthCm(),
                request.getDimensionHeightCm()
        ));
        order.setTotalVolume(request.getTotalVolumeM3());

        List<Product> mappedProducts = resolveProducts(request.getProducts(), tenantId);
        replaceProducts(order, mappedProducts);

        double totalWeight = mappedProducts.stream()
                .mapToDouble(product -> safeDouble(product.getWeight()) * safeInt(product.getQuantity()))
                .sum();

        long totalValueAmount = mappedProducts.stream()
                .mapToLong(product -> safeLong(product.getValue()) * safeInt(product.getQuantity()))
                .sum();

        order.setTotalWeight(totalWeight);
        order.setTotalValue((double) totalValueAmount);
        order.setCodAmount(Boolean.TRUE.equals(request.getIsCod()) ? totalValueAmount : 0L);
        order.setTenantId(tenantId);
    }

    private List<Product> resolveProducts(List<CreateOrderRequest.ProductItem> productItems, Long tenantId) {
        List<CreateOrderRequest.ProductItem> safeProductItems =
                productItems == null ? List.of() : productItems;
        Map<Long, ProductType> productTypeById = loadProductTypeById(safeProductItems, tenantId);

        List<Product> mappedProducts = new ArrayList<>();
        for (CreateOrderRequest.ProductItem item : safeProductItems) {
            ProductType productType = productTypeById.get(item.getProductTypeId());
            if (productType == null) {
                throw new AppException(ErrorCode.PRODUCT_TYPE_NOT_FOUND);
            }

            mappedProducts.add(OrderProductMapper.toProduct(item, productType, tenantId));
        }

        return mappedProducts;
    }

    private Map<Long, ProductType> loadProductTypeById(
            List<CreateOrderRequest.ProductItem> productItems,
            Long tenantId
    ) {
        Map<Long, ProductType> productTypeById = new HashMap<>();
        for (CreateOrderRequest.ProductItem item : productItems) {
            Long productTypeId = item.getProductTypeId();
            if (productTypeId == null || productTypeById.containsKey(productTypeId)) {
                continue;
            }

            ProductType productType = productTypeRepository.findByIdAndTenantId(productTypeId, tenantId)
                    .orElseThrow(() -> new AppException(ErrorCode.PRODUCT_TYPE_NOT_FOUND));
            productTypeById.put(productTypeId, productType);
        }

        return productTypeById;
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

    private String generateNextOrderCode() {
        String orderCodePrefix = ORDER_CODE_PREFIX + LocalDate.now().format(ORDER_CODE_DATE_FORMATTER);
        synchronized (ORDER_CODE_LOCK) {
            int currentSequence = resolveCurrentOrderCodeSequence(orderCodePrefix);
            return formatOrderCode(orderCodePrefix, currentSequence + 1);
        }
    }

    private int resolveCurrentOrderCodeSequence(String orderCodePrefix) {
        String maxOrderCode = orderRepository.findMaxOrderCodeByPrefix(orderCodePrefix);
        if (!OrderTextUtils.hasText(maxOrderCode) || !maxOrderCode.startsWith(orderCodePrefix)) {
            return 0;
        }

        String sequencePart = maxOrderCode.substring(orderCodePrefix.length());
        if (!OrderTextUtils.hasText(sequencePart)) {
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

    private Order saveConfirmedOrder(Order order) {
        Order savedOrder = orderRepository.save(order);
        orderTimelineService.recordStatusEvent(
                savedOrder,
                savedOrder.getStatus(),
                "Order confirmed.",
                null
        );
        orderEventDispatcher.publishOrderAfterCommit(savedOrder);
        orderEventDispatcher.publishOrderConfirmedNotificationAfterCommit(savedOrder);
        return savedOrder;
    }

    private OrderPickupMethod resolveOrderPickupMethod(Order order) {
        if (order == null || order.getPickupMethod() == null) {
            return OrderPickupMethod.COURIER_PICKUP;
        }
        return order.getPickupMethod();
    }

    private void ensureSenderPaymentCompletedBeforeConfirmation(Order order) {
        if (FeePayer.SENDER.equals(order.getFeePayer())
                && !PaymentStatus.PAID.equals(order.getPaymentStatus())) {
            throw new AppException(
                    ErrorCode.INVALID_REQUEST,
                    "Sender must complete shipping fee payment before order confirmation."
            );
        }
    }

    private void validateOrderForConfirmation(Order order) {
        if (order == null) {
            throw new AppException(ErrorCode.ORDER_NOT_FOUND);
        }
        if (order.getStatus() == null || !CONFIRMABLE_ORDER_STATUSES.contains(order.getStatus())) {
            throw new AppException(ErrorCode.ORDER_NOT_ASSIGNABLE);
        }

        validateOrderLocation(
                order.getSenderLocation(),
                "Sender coordinates are required before order confirmation."
        );
        validateOrderLocation(
                order.getReceiverLocation(),
                "Receiver coordinates are required before order confirmation."
        );
    }

    private void validateOrderLocation(Point location, String detail) {
        if (location == null) {
            throw new AppException(ErrorCode.ORDER_NOT_ASSIGNABLE, detail);
        }

        double latitude = location.getY();
        double longitude = location.getX();
        if (!OrderLocationUtils.isValidCoordinate(latitude, longitude)) {
            throw new AppException(ErrorCode.ORDER_NOT_ASSIGNABLE, detail);
        }
    }

    private DestinationPostOfficeReservationResponse reserveDestinationPostOfficeIfNeeded(Order order) {
        if (OrderTextUtils.hasText(order.getDestinationPostOfficeCode())) {
            return null;
        }

        DestinationPostOfficeReservationResponse reservedDestinationPostOffice =
                firstMilePostOfficeCaller.reserveBestDestinationPostOffice(
                        OrderLocationUtils.toLatitude(order.getReceiverLocation()),
                        OrderLocationUtils.toLongitude(order.getReceiverLocation())
                );
        order.setDestinationPostOfficeCode(reservedDestinationPostOffice.getCode());
        return reservedDestinationPostOffice;
    }

}
