/*
Author: Nguyen The Anh
Description: Part of Serp Project
*/

package serp.project.tms_order.service.impl;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
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
import serp.project.tms_order.domain.Dimension;
import serp.project.tms_order.domain.Order;
import serp.project.tms_order.domain.Product;
import serp.project.tms_order.domain.ProductType;
import serp.project.tms_order.dto.PageResponse;
import serp.project.tms_order.dto.request.CancelOrderRequest;
import serp.project.tms_order.dto.request.CreateOrderRequest;
import serp.project.tms_order.dto.request.OrderImportDTO;
import serp.project.tms_order.dto.request.OrderFilterRequest;
import serp.project.tms_order.dto.request.UpdateOrderRequest;
import serp.project.tms_order.dto.response.ImportHistoryResponse;
import serp.project.tms_order.dto.response.OrderDetailResponse;
import serp.project.tms_order.dto.response.ProductTypeTemplateDTO;
import serp.project.tms_order.dto.response.ProvinceExcelTemplateDTO;
import serp.project.tms_order.dto.response.ValidateImportFileDTO;
import serp.project.tms_order.dto.response.WardExcelTemplateDTO;
import serp.project.tms_order.enums.OrderPickupMethod;
import serp.project.tms_order.enums.OrderStatus;
import serp.project.tms_order.enums.PaymentStatus;
import serp.project.tms_order.exception.AppException;
import serp.project.tms_order.exception.ErrorCode;
import serp.project.tms_order.kernel.utils.AuthUtils;
import serp.project.tms_order.kernel.utils.ExcelTemplateUtils;
import serp.project.tms_order.mapper.OrderMapper;
import serp.project.tms_order.repository.OrderRepository;
import serp.project.tms_order.repository.ProductTypeRepository;
import serp.project.tms_order.repository.specification.OrderSpecification;
import serp.project.tms_order.service.OrderExcelService;
import serp.project.tms_order.service.OrderImportExcelService;
import serp.project.tms_order.service.OrderService;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;
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

    private static final GeometryFactory GEOMETRY_FACTORY = new GeometryFactory(new PrecisionModel(), 4326);
    private static final String ORDER_CODE_PREFIX = "ORD";
    private static final DateTimeFormatter ORDER_CODE_DATE_FORMATTER = DateTimeFormatter.BASIC_ISO_DATE;
    private static final int ORDER_CODE_SEQUENCE_LENGTH = 4;
    private static final Object ORDER_CODE_LOCK = new Object();
    private static final Set<OrderStatus> EDITABLE_ORDER_STATUSES = EnumSet.of(OrderStatus.CREATED);
    private static final String TEMPLATE_PATH = "excel/order_template.xlsx";
    private static final String UNIT_SHEET_NAME = "Unit";
    private static final int START_ROW_INDEX = 1;
    private static final int WARD_COLUMN_INDEX = 0;
    private static final int PROVINCE_COLUMN_INDEX = 1;
    private static final int PRODUCT_TYPE_COLUMN_INDEX = 5;

    private final OrderRepository orderRepository;
    private final ProductTypeRepository productTypeRepository;
    private final AuthUtils authUtils;
    private final OrderExcelService orderExcelService;
    private final OrderImportExcelService orderImportExcelService;

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
            log.error("Export TMS order template failed", exception);
            throw new AppException(ErrorCode.UNCATEGORIZED_EXCEPTION);
        }
    }

    @Override
    public ValidateImportFileDTO<OrderImportDTO> validateImportFile(MultipartFile file, Long tenantId) {
        return orderImportExcelService.validateImportFile(file, tenantId);
    }

    @Override
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

        OrderFilterRequest normalizedFilter = normalizeOrderFilterRequest(filterRequest);
        validateOrderFilterRanges(normalizedFilter);
        String customerCreatedBy = resolveCustomerCreatedByScope();
        Pageable pageable = PageRequest.of(page, size, Sort.by(Sort.Direction.DESC, "id"));

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

    @Override
    @Transactional(rollbackFor = Exception.class)
    public OrderDetailResponse createOrder(CreateOrderRequest request, Long tenantId) {
        String customerOrderCode = normalizeText(request.getCustomerOrderCode());
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

        applyCreateOrderRequest(order, request, customerOrderCode, tenantId);
        Order savedOrder = orderRepository.save(order);
        log.info("Created TMS order orderCode={} tenantId={}", savedOrder.getOrderCode(), tenantId);
        return OrderMapper.toOrderDetailResponse(savedOrder);
    }

    @Override
    public OrderDetailResponse getOrderById(Long orderId, Long tenantId) {
        Order order = orderRepository.findByIdAndTenantId(orderId, tenantId)
                .orElseThrow(() -> new AppException(ErrorCode.ORDER_NOT_FOUND));

        validateCanReadOrder(order);
        return OrderMapper.toOrderDetailResponse(order);
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public OrderDetailResponse updateOrder(Long orderId, UpdateOrderRequest request, Long tenantId) {
        Order order = orderRepository.findByIdAndTenantIdForUpdate(orderId, tenantId)
                .orElseThrow(() -> new AppException(ErrorCode.ORDER_NOT_FOUND));

        validateCanMutateOrder(order);
        ensureOrderEditable(order);

        String customerOrderCode = normalizeText(request.getCustomerOrderCode());
        validateUniqueCustomerOrderCode(customerOrderCode, tenantId, order.getId());
        applyCreateOrderRequest(order, request, customerOrderCode, tenantId);

        Order updatedOrder = orderRepository.save(order);
        return OrderMapper.toOrderDetailResponse(updatedOrder);
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public OrderDetailResponse cancelOrder(Long orderId, Long tenantId, CancelOrderRequest request) {
        Order order = orderRepository.findByIdAndTenantIdForUpdate(orderId, tenantId)
                .orElseThrow(() -> new AppException(ErrorCode.ORDER_NOT_FOUND));

        validateCanMutateOrder(order);
        ensureOrderEditable(order);

        order.setStatus(OrderStatus.CANCELLED);
        order.setCancelledAt(LocalDateTime.now());
        order.setCancelReason(request == null ? null : normalizeText(request.getCancelReason()));

        Order cancelledOrder = orderRepository.save(order);
        return OrderMapper.toOrderDetailResponse(cancelledOrder);
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public void deleteOrder(Long orderId, Long tenantId) {
        Order order = orderRepository.findByIdAndTenantIdForUpdate(orderId, tenantId)
                .orElseThrow(() -> new AppException(ErrorCode.ORDER_NOT_FOUND));

        validateCanMutateOrder(order);
        ensureOrderEditable(order);
        orderRepository.delete(order);
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

        if (orderRepository.existsByCustomerOrderCodeIgnoreCaseAndTenantIdAndIdNot(
                customerOrderCode,
                tenantId,
                excludedOrderId
        )) {
            throw new AppException(ErrorCode.ORDER_CUSTOMER_CODE_EXISTED);
        }
    }

    private void applyCreateOrderRequest(
            Order order,
            CreateOrderRequest request,
            String customerOrderCode,
            Long tenantId
    ) {
        order.setCustomerOrderCode(customerOrderCode);

        order.setSenderName(normalizeText(request.getSenderName()));
        order.setSenderPhone(normalizeText(request.getSenderPhone()));
        order.setSenderProvinceCode(normalizeText(request.getSenderProvinceCode()));
        order.setSenderWardCode(normalizeText(request.getSenderWardCode()));
        order.setSenderAddressDetail(normalizeText(request.getSenderAddressDetail()));
        order.setSenderLocation(toPoint(request.getSenderLatitude(), request.getSenderLongitude()));

        order.setReceiverName(normalizeText(request.getReceiverName()));
        order.setReceiverPhone(normalizeText(request.getReceiverPhone()));
        order.setReceiverProvinceCode(normalizeText(request.getReceiverProvinceCode()));
        order.setReceiverWardCode(normalizeText(request.getReceiverWardCode()));
        order.setReceiverAddressDetail(normalizeText(request.getReceiverAddressDetail()));
        order.setReceiverLocation(toPoint(request.getReceiverLatitude(), request.getReceiverLongitude()));

        order.setPickupTimeStart(request.getPickupTimeStart());
        order.setPickupTimeEnd(request.getPickupTimeEnd());
        order.setDeliveryRequestTime(request.getDeliveryRequestTime());
        order.setPickupMethod(request.getPickupMethod() == null
                ? OrderPickupMethod.COURIER_PICKUP
                : request.getPickupMethod());
        order.setOrderProductCategory(request.getOrderProductCategory());
        order.setOrderType(request.getOrderType());
        order.setFeePayer(request.getFeePayer());
        order.setNote(normalizeText(request.getNote()));
        order.setDimensions(buildDimensions(
                request.getDimensionLengthCm(),
                request.getDimensionWidthCm(),
                request.getDimensionHeightCm()
        ));
        order.setTotalVolume(request.getTotalVolumeM3());

        List<Product> mappedProducts = mapProducts(request.getProducts(), tenantId);
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

    private List<Product> mapProducts(List<CreateOrderRequest.ProductItem> productItems, Long tenantId) {
        List<CreateOrderRequest.ProductItem> safeProductItems =
                productItems == null ? List.of() : productItems;
        Map<Long, ProductType> productTypeById = loadProductTypeById(safeProductItems, tenantId);

        List<Product> mappedProducts = new ArrayList<>();
        for (CreateOrderRequest.ProductItem item : safeProductItems) {
            ProductType productType = productTypeById.get(item.getProductTypeId());
            if (productType == null) {
                throw new AppException(ErrorCode.PRODUCT_TYPE_NOT_FOUND);
            }

            Product product = Product.builder()
                    .name(normalizeText(item.getName()))
                    .value(item.getValue())
                    .quantity(item.getQuantity())
                    .weight(item.getWeightGram())
                    .productType(productType)
                    .tenantId(tenantId)
                    .build();
            mappedProducts.add(product);
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

    private String resolveCustomerCreatedByScope() {
        if (isTenantStaffReader()) {
            return null;
        }
        if (authUtils.hasAnyRole("TMS_CUSTOMER")) {
            return String.valueOf(getCurrentUserIdOrThrow());
        }
        throw new AppException(ErrorCode.UNAUTHORIZED);
    }

    private void validateCanReadOrder(Order order) {
        if (order == null) {
            throw new AppException(ErrorCode.ORDER_NOT_FOUND);
        }
        if (isTenantStaffReader()) {
            return;
        }
        if (authUtils.hasAnyRole("TMS_CUSTOMER") && isCustomerOwner(order)) {
            return;
        }
        throw new AppException(ErrorCode.UNAUTHORIZED);
    }

    private void validateCanMutateOrder(Order order) {
        if (order == null) {
            throw new AppException(ErrorCode.ORDER_NOT_FOUND);
        }
        if (authUtils.hasAnyRole("TMS_ADMIN")) {
            return;
        }
        if (authUtils.hasAnyRole("TMS_CUSTOMER") && isCustomerOwner(order)) {
            return;
        }
        throw new AppException(ErrorCode.UNAUTHORIZED);
    }

    private boolean isTenantStaffReader() {
        return authUtils.hasAnyRole(
                "TMS_ADMIN",
                "TMS_POSTOFFICER_MANAGER",
                "TMS_POSTOFFICER",
                "TMS_HUB_MANAGER",
                "TMS_HUB_EMPLOYEE"
        );
    }

    private boolean isCustomerOwner(Order order) {
        Long currentUserId = getCurrentUserIdOrThrow();
        return order != null
                && hasText(order.getCreatedBy())
                && String.valueOf(currentUserId).equals(order.getCreatedBy());
    }

    private Long getCurrentUserIdOrThrow() {
        return authUtils.getCurrentUserId().orElseThrow(() -> new AppException(ErrorCode.UNAUTHORIZED));
    }

    private void ensureOrderEditable(Order order) {
        if (order == null) {
            throw new AppException(ErrorCode.ORDER_NOT_FOUND);
        }
        if (order.getStatus() == null || !EDITABLE_ORDER_STATUSES.contains(order.getStatus())) {
            throw new AppException(ErrorCode.ORDER_NOT_EDITABLE);
        }
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
}
