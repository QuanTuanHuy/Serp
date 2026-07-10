/*
Author: Nguyen The Anh
Description: Part of Serp Project
*/

package serp.project.tms_order.service.impl;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.apache.poi.ss.usermodel.Cell;
import org.apache.poi.ss.usermodel.CellType;
import org.apache.poi.ss.usermodel.DataFormatter;
import org.apache.poi.ss.usermodel.DateUtil;
import org.apache.poi.ss.usermodel.FormulaEvaluator;
import org.apache.poi.ss.usermodel.Row;
import org.apache.poi.ss.usermodel.Sheet;
import org.apache.poi.ss.usermodel.Workbook;
import org.apache.poi.ss.usermodel.WorkbookFactory;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.multipart.MultipartFile;
import serp.project.tms_order.domain.Dimension;
import serp.project.tms_order.domain.ImportHistory;
import serp.project.tms_order.domain.Order;
import serp.project.tms_order.domain.Product;
import serp.project.tms_order.domain.ProductType;
import serp.project.tms_order.domain.Ward;
import serp.project.tms_order.dto.request.OrderImportDTO;
import serp.project.tms_order.dto.response.ImportHistoryResponse;
import serp.project.tms_order.dto.response.ValidateImportFileDTO;
import serp.project.tms_order.enums.*;
import serp.project.tms_order.exception.AppException;
import serp.project.tms_order.exception.ErrorCode;
import serp.project.tms_order.exception.MessageService;
import serp.project.tms_order.kernel.utils.ExcelImportUtils;
import serp.project.tms_order.kernel.utils.ImportErrorUtils;
import serp.project.tms_order.kernel.utils.ImportHistoryFailureUtils;
import serp.project.tms_order.kernel.utils.ImportHistoryResponseUtils;
import serp.project.tms_order.repository.ImportHistoryRepository;
import serp.project.tms_order.repository.OrderRepository;
import serp.project.tms_order.repository.ProductTypeRepository;
import serp.project.tms_order.repository.ProvinceRepository;
import serp.project.tms_order.repository.WardRepository;
import serp.project.tms_order.repository.projection.CodeNameProjection;
import serp.project.tms_order.service.OrderImportExcelService;
import serp.project.tms_order.service.OrderTimelineService;
import serp.project.tms_order.service.dto.import_record.ImportExecutionResult;

import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.time.format.DateTimeParseException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.Objects;
import java.util.Set;
import java.util.UUID;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.Executor;
import java.util.regex.Pattern;
import java.util.stream.Collectors;

import static serp.project.tms_order.kernel.utils.ExcelImportUtils.*;
import static serp.project.tms_order.kernel.utils.NumberUtils.safeDouble;
import static serp.project.tms_order.kernel.utils.NumberUtils.safeInt;
import static serp.project.tms_order.kernel.utils.NumberUtils.safeLong;

@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
@Slf4j
public class OrderImportExcelServiceImpl implements OrderImportExcelService {

    private static final String ORDER_SHEET_NAME = "Order";
    private static final int HEADER_ROW_INDEX = 2;
    private static final int DATA_START_ROW_INDEX = 3;

    private static final int COLUMN_STT = 0;
    private static final int COLUMN_CUSTOMER_ORDER_CODE = 1;
    private static final int COLUMN_SENDER_NAME = 2;
    private static final int COLUMN_SENDER_PHONE = 3;
    private static final int COLUMN_SENDER_PROVINCE = 4;
    private static final int COLUMN_SENDER_WARD = 5;
    private static final int COLUMN_SENDER_ADDRESS = 6;
    private static final int COLUMN_RECEIVER_NAME = 7;
    private static final int COLUMN_RECEIVER_PHONE = 8;
    private static final int COLUMN_RECEIVER_PROVINCE = 9;
    private static final int COLUMN_RECEIVER_WARD = 10;
    private static final int COLUMN_RECEIVER_ADDRESS = 11;
    private static final int COLUMN_ORDER_TYPE = 12;
    private static final int COLUMN_NOTE = 13;
    private static final int COLUMN_PICKUP_DATE = 14;
    private static final int COLUMN_PICKUP_TIME = 15;
    private static final int COLUMN_DELIVERY_TIME = 16;
    private static final int COLUMN_COD_FLAG = 17;
    private static final int COLUMN_DIMENSIONS = 18;
    private static final int COLUMN_VOLUME = 19;
    private static final int COLUMN_FEE_PAYER = 20;
    private static final int COLUMN_PRODUCT_NAME = 21;
    private static final int COLUMN_PRODUCT_VALUE = 22;
    private static final int COLUMN_PRODUCT_QUANTITY = 23;
    private static final int COLUMN_PRODUCT_WEIGHT = 24;
    private static final int COLUMN_PRODUCT_TYPE = 25;
    private static final int LAST_COLUMN_INDEX = 25;

    private static final DateTimeFormatter IMPORT_DATE_FORMATTER = DateTimeFormatter.ofPattern("d/M/uuuu");
    private static final DateTimeFormatter ORDER_CODE_DATE_FORMATTER = DateTimeFormatter.BASIC_ISO_DATE;
    private static final Pattern PHONE_PATTERN = Pattern.compile("^[0-9]{8,15}$");
    private static final Pattern CODE_NAME_PATTERN = Pattern.compile("^(.+?)\\s+-\\s+(.+)$");

    private static final String ORDER_CODE_PREFIX = "OD_";
    private static final int ORDER_CODE_SEQUENCE_LENGTH = 6;
    private static final int MAX_ERROR_MESSAGE_LENGTH = 20000;

    private static final Object ORDER_CODE_LOCK = new Object();

    private static final List<String> EXPECTED_HEADERS = List.of(
            "STT",
            "Mã đơn hàng tự quản (*)",
            "Tên (*)",
            "Số điện thoại (*)",
            "Tỉnh/thành phố (*)",
            "Phường/xã (*)",
            "Địa chỉ chi tiết (*)",
            "Tên (*)",
            "Số điện thoại (*)",
            "Tỉnh/thành phố (*)",
            "Phường/xã (*)",
            "Địa chỉ chi tiết (*)",
            "Loại vận chuyển (*)",
            "Ghi chú",
            "Ngày hẹn lấy (dd/mm/yyyy)",
            "Giờ hẹn lấy",
            "Giờ hẹn giao",
            "Thu hộ tiền hàng (*)",
            "Kích cỡ (dài x rộng x cao) (cm) (*)",
            "Thể tích (m3) (*)",
            "Người trả cước (*)",
            "Tên hàng hóa (*)",
            "Giá trị hàng hóa (đ) (*)",
            "Số lượng (*)",
            "Khối lượng (g) (*)",
            "Loại sản phẩm (*)"
    );

    private static final List<String> HEADER_KEYS = List.of(
            "stt",
            "customer_order_code",
            "sender_name",
            "sender_phone",
            "sender_province_code",
            "sender_ward_code",
            "sender_address_detail",
            "receiver_name",
            "receiver_phone",
            "receiver_province_code",
            "receiver_ward_code",
            "receiver_address_detail",
            "order_type",
            "note",
            "pickup_date",
            "pickup_request_time",
            "delivery_request_time",
            "is_cod",
            "dimension",
            "total_volume_m3",
            "fee_payer",
            "product_name",
            "product_value",
            "product_quantity",
            "product_weight_gram",
            "product_type"
    );

    private static final Map<String, OrderType> ORDER_TYPE_MAP = Map.of(
            "tiêu chuẩn", OrderType.STANDARD_ORDER
    );

    private static final Map<String, FeePayer> FEE_PAYER_MAP = Map.of(
            "người nhận", FeePayer.RECEIVER,
            "người gửi", FeePayer.SENDER
    );

    private static final Map<String, DeliveryRequestTime> PICKUP_TIME_MAP = Map.of(
            "cả ngày", DeliveryRequestTime.FULL_DAY,
            "sáng (7h30 - 12h00)", DeliveryRequestTime.MORNING,
            "chiều (13h30 - 18h00)", DeliveryRequestTime.AFTERNOON
    );

    private static final Map<String, DeliveryRequestTime> DELIVERY_TIME_MAP = Map.of(
            "cả ngày", DeliveryRequestTime.FULL_DAY,
            "sáng (7h30 - 12h00)", DeliveryRequestTime.MORNING,
            "chiều (13h30 - 18h00)", DeliveryRequestTime.AFTERNOON,
            "chủ nhật", DeliveryRequestTime.SUNDAY,
            "ngày nghỉ lễ", DeliveryRequestTime.HOLIDAY,
            "giờ hành chính (7h30 - 11h30, 13h30 - 17h30)", DeliveryRequestTime.BUSINESS_HOURS
    );

    private static final Map<String, Boolean> COD_FLAG_MAP = Map.of(
            "có", true,
            "không", false
    );

    private final OrderRepository orderRepository;
    private final ProductTypeRepository productTypeRepository;
    private final ProvinceRepository provinceRepository;
    private final WardRepository wardRepository;
    private final ImportHistoryRepository importHistoryRepository;
    private final ImportHistoryFailureUtils importHistoryFailureUtils;
    private final MessageService messageService;
    private final OrderTimelineService orderTimelineService;

    @Qualifier("orderImportTaskExecutor")
    private final Executor orderImportTaskExecutor;

    @Override
    public ValidateImportFileDTO<OrderImportDTO> validateImportFile(MultipartFile file, Long tenantId) {
        ValidateImportFileDTO<OrderImportDTO> response = buildBaseValidateResponse();

        if (file == null || file.isEmpty()) {
            setValidationFailed(response, List.of(message("order.import.validation.file.empty")));
            return response;
        }

        try {
            return validateImportFileBytes(file.getBytes(), tenantId);
        } catch (IOException exception) {
            log.error("Validate order import file failed", exception);
            setValidationFailed(response, List.of(message("order.import.validation.file.unreadable")));
            return response;
        }
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public ImportHistoryResponse importOrdersAsync(MultipartFile file, Long tenantId) {
        if (file == null || file.isEmpty()) {
            throw new AppException(ErrorCode.INVALID_REQUEST);
        }

        byte[] fileBytes;
        try {
            fileBytes = file.getBytes();
        } catch (IOException exception) {
            throw new AppException(ErrorCode.INVALID_REQUEST);
        }

        ImportHistory importHistory = ImportHistory.builder()
                .fileId(UUID.randomUUID())
                .fileName(file.getOriginalFilename())
                .status(ImportHistoryStatus.PENDING)
                .totalRecords(0)
                .successRecords(0)
                .failedRecords(0)
                .tenantId(tenantId)
                .build();

        ImportHistory savedImportHistory = importHistoryRepository.save(importHistory);
        Long importHistoryId = savedImportHistory.getId();

        CompletableFuture.runAsync(
                () -> processImportJob(fileBytes, tenantId, importHistoryId),
                orderImportTaskExecutor
        ).exceptionally(exception -> {
            log.error("Order import async execution failed for importHistoryId={}", importHistoryId, exception);
            importHistoryFailureUtils.markImportFailed(
                importHistoryId,
                tenantId,
                ImportErrorUtils.resolveExceptionMessage(exception, key -> message(key)),
                MAX_ERROR_MESSAGE_LENGTH
            );
            return null;
        });

        return ImportHistoryResponseUtils.toResponse(savedImportHistory);
    }

    private ValidateImportFileDTO<OrderImportDTO> validateImportFileBytes(byte[] fileBytes, Long tenantId) {
        ValidateImportFileDTO<OrderImportDTO> response = buildBaseValidateResponse();

        try (ByteArrayInputStream inputStream = new ByteArrayInputStream(fileBytes);
             Workbook workbook = WorkbookFactory.create(inputStream)) {

            Sheet orderSheet = workbook.getSheet(ORDER_SHEET_NAME);
            if (orderSheet == null) {
                setValidationFailed(response, List.of(message("order.import.validation.sheet.order.missing")));
                return response;
            }

            FormulaEvaluator evaluator = workbook.getCreationHelper().createFormulaEvaluator();
            DataFormatter formatter = new DataFormatter(Locale.forLanguageTag("vi-VN"));

            List<String> headerErrors = validateHeader(orderSheet, formatter, evaluator);
            if (!headerErrors.isEmpty()) {
                setValidationFailed(response, headerErrors);
                return response;
            }

            MasterDataLookup masterData = loadMasterData(tenantId);
            Set<String> existingCustomerOrderCodes = loadExistingCustomerOrderCodes(orderSheet, tenantId, formatter, evaluator);
            ValidationResult validationResult = validateRows(
                    orderSheet,
                    formatter,
                    evaluator,
                    masterData,
                    existingCustomerOrderCodes
            );

            response.setData(validationResult.orders());
            if (validationResult.orders().isEmpty() && validationResult.errors().isEmpty()) {
                setValidationFailed(response, List.of(message("order.import.validation.data.empty")));
                return response;
            }

            if (!validationResult.errors().isEmpty()) {
                setValidationFailed(response, validationResult.errors());
                return response;
            }

            response.setSuccess(true);
            response.setErrorMessage(null);
            response.setType(1);
            return response;
        } catch (Exception exception) {
            log.error("Validate order import file failed", exception);
            setValidationFailed(response, List.of(message("order.import.validation.file.unreadable")));
            return response;
        }
    }

    private void processImportJob(byte[] fileBytes, Long tenantId, Long importHistoryId) {
        ImportHistory importHistory = importHistoryRepository.findByIdAndTenantId(importHistoryId, tenantId)
                .orElse(null);
        if (importHistory == null) {
            log.warn("Import history not found: id={}, tenantId={}", importHistoryId, tenantId);
            return;
        }

        importHistory.setStatus(ImportHistoryStatus.PROCESSING);
        importHistory.setStartedAt(LocalDateTime.now());
        importHistory.setType(ImportType.ORDER);
        importHistoryRepository.save(importHistory);

        try {
            ValidateImportFileDTO<OrderImportDTO> validationResult = validateImportFileBytes(fileBytes, tenantId);
            List<OrderImportDTO> validOrders = validationResult.getData() == null
                    ? List.of()
                    : validationResult.getData();

            importHistory.setTotalRecords(validOrders.size());

            if (!validationResult.isSuccess()) {
                importHistory.setStatus(ImportHistoryStatus.FAILED);
                importHistory.setSuccessRecords(0);
                importHistory.setFailedRecords(validOrders.size());
                importHistory.setErrorMessage(
                        ImportErrorUtils.truncateErrorMessage(validationResult.getErrorMessage(), MAX_ERROR_MESSAGE_LENGTH)
                );
                importHistory.setFinishedAt(LocalDateTime.now());
                importHistoryRepository.save(importHistory);
                return;
            }

            ImportExecutionResult executionResult = saveImportedOrders(validOrders, tenantId);
            importHistory.setSuccessRecords(executionResult.successRecords());
            importHistory.setFailedRecords(executionResult.failedRecords());
            importHistory.setErrorMessage(
                    ImportErrorUtils.truncateErrorMessage(executionResult.errorMessage(), MAX_ERROR_MESSAGE_LENGTH)
            );
            importHistory.setStatus(
                    executionResult.failedRecords() > 0
                            ? ImportHistoryStatus.PARTIAL_SUCCESS
                            : ImportHistoryStatus.COMPLETED
            );
            importHistory.setFinishedAt(LocalDateTime.now());
            importHistoryRepository.save(importHistory);
        } catch (Exception exception) {
            log.error("Process order import failed for importHistoryId={}", importHistoryId, exception);
            importHistoryFailureUtils.markImportFailed(
                    importHistoryId,
                    tenantId,
                    ImportErrorUtils.resolveExceptionMessage(exception, key -> message(key)),
                    MAX_ERROR_MESSAGE_LENGTH
            );
        }
    }

    private ImportExecutionResult saveImportedOrders(List<OrderImportDTO> orderImports, Long tenantId) {
        if (orderImports == null || orderImports.isEmpty()) {
            return new ImportExecutionResult(0, 0, 0, null);
        }

        Map<Long, ProductType> productTypeById = loadProductTypeById(orderImports);

        int successRecords = 0;
        List<String> errors = new ArrayList<>();

        String orderCodePrefix = ORDER_CODE_PREFIX + LocalDate.now().format(ORDER_CODE_DATE_FORMATTER);
        synchronized (ORDER_CODE_LOCK) {
            int currentSequence = resolveCurrentOrderCodeSequence(orderCodePrefix);

            for (OrderImportDTO orderImport : orderImports) {
                currentSequence++;
                String orderCode = formatOrderCode(orderCodePrefix, currentSequence);

                try {
                    Order order = mapToOrderEntity(orderImport, tenantId, orderCode, productTypeById);
                    Order savedOrder = orderRepository.save(order);
                    orderTimelineService.recordStatusEvent(
                            savedOrder,
                            OrderStatus.CREATED,
                            "Đơn hàng đã được import.",
                            null
                    );
                    successRecords++;
                } catch (Exception exception) {
                    errors.add(buildImportPersistError(orderImport, exception));
                }
            }
        }

        int totalRecords = orderImports.size();
        int failedRecords = totalRecords - successRecords;
        String errorMessage = errors.isEmpty() ? null : String.join("\n", errors);

        return new ImportExecutionResult(totalRecords, successRecords, failedRecords, errorMessage);
    }

    private Map<Long, ProductType> loadProductTypeById(List<OrderImportDTO> orderImports) {
        Set<Long> productTypeIds = orderImports.stream()
                .filter(Objects::nonNull)
                .flatMap(orderImport -> orderImport.getProducts().stream())
                .map(OrderImportDTO.ProductImportItemDTO::getProductTypeId)
                .filter(Objects::nonNull)
                .collect(Collectors.toSet());

        return productTypeRepository.findAllById(productTypeIds)
                .stream()
                .collect(Collectors.toMap(ProductType::getId, value -> value));
    }

    private Order mapToOrderEntity(
            OrderImportDTO orderImport,
            Long tenantId,
            String orderCode,
            Map<Long, ProductType> productTypeById
    ) {
        Dimension dimensions = buildDimensions(orderImport);
        long totalValueAmount = calculateTotalValueAmount(orderImport);
        long codAmount = Boolean.TRUE.equals(orderImport.getIsCod()) ? totalValueAmount : 0L;

        Order order = Order.builder()
                .orderCode(orderCode)
                .customerOrderCode(orderImport.getCustomerOrderCode())
                .senderName(orderImport.getSenderName())
                .senderPhone(orderImport.getSenderPhone())
                .senderProvinceCode(orderImport.getSenderProvinceCode())
                .senderWardCode(orderImport.getSenderWardCode())
                .senderAddressDetail(orderImport.getSenderAddressDetail())
                .pickupTimeStart(orderImport.getPickupTimeStart())
                .pickupTimeEnd(orderImport.getPickupTimeEnd())
                .deliveryRequestTime(orderImport.getDeliveryRequestTime())
                .receiverName(orderImport.getReceiverName())
                .receiverPhone(orderImport.getReceiverPhone())
                .receiverProvinceCode(orderImport.getReceiverProvinceCode())
                .receiverWardCode(orderImport.getReceiverWardCode())
                .receiverAddressDetail(orderImport.getReceiverAddressDetail())
                .status(OrderStatus.CREATED)
                .isConfirm(false)
                .totalWeight(calculateTotalWeight(orderImport))
                .totalValue((double) totalValueAmount)
                .dimensions(dimensions)
                .totalVolume(orderImport.getTotalVolumeM3())
                .pickupAttempts(0)
                .orderType(orderImport.getOrderType())
                .baseShippingFee(0L)
                .codFee(0L)
                .extraFee(0L)
                .totalShippingFee(0L)
                .codAmount(codAmount)
                .feePayer(orderImport.getFeePayer())
                .paymentStatus(PaymentStatus.UNPAID)
                .note(orderImport.getNote())
                .tenantId(tenantId)
                .build();

        for (OrderImportDTO.ProductImportItemDTO productImport : orderImport.getProducts()) {
            ProductType productType = productTypeById.get(productImport.getProductTypeId());
            if (productType == null) {
                throw new AppException(ErrorCode.PRODUCT_TYPE_NOT_FOUND);
            }

            Product product = Product.builder()
                    .name(productImport.getName())
                    .value(productImport.getValue())
                    .quantity(productImport.getQuantity())
                    .weight(productImport.getWeightGram())
                    .productType(productType)
                    .tenantId(tenantId)
                    .build();
            order.addProduct(product);
        }

        return order;
    }

    private Dimension buildDimensions(OrderImportDTO orderImport) {
        if (orderImport.getDimensionLengthCm() == null
                || orderImport.getDimensionWidthCm() == null
                || orderImport.getDimensionHeightCm() == null) {
            return null;
        }

        Dimension dimensions = new Dimension();
        dimensions.setLength(orderImport.getDimensionLengthCm());
        dimensions.setWidth(orderImport.getDimensionWidthCm());
        dimensions.setHeight(orderImport.getDimensionHeightCm());
        return dimensions;
    }

    private Double calculateTotalWeight(OrderImportDTO orderImport) {
        if (orderImport.getProducts() == null) {
            return 0D;
        }

        return orderImport.getProducts().stream()
                .mapToDouble(product -> safeDouble(product.getWeightGram()) * safeInt(product.getQuantity()))
                .sum();
    }

    private long calculateTotalValueAmount(OrderImportDTO orderImport) {
        if (orderImport.getProducts() == null) {
            return 0L;
        }

        return orderImport.getProducts().stream()
                .mapToLong(product -> safeLong(product.getValue()) * safeInt(product.getQuantity()))
                .sum();
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

    private String buildImportPersistError(OrderImportDTO orderImport, Exception exception) {
        String customerOrderCode = hasText(orderImport.getCustomerOrderCode())
                ? orderImport.getCustomerOrderCode()
                : message("order.import.import_job.unknown_order_code");
        return message(
                "order.import.import_job.persist_error",
                customerOrderCode,
                ImportErrorUtils.resolveExceptionMessage(exception, key -> message(key))
        );
    }

    private ValidateImportFileDTO<OrderImportDTO> buildBaseValidateResponse() {
        return ExcelImportUtils.buildBaseValidateResponse(HEADER_KEYS, EXPECTED_HEADERS);
    }

    private List<String> validateHeader(Sheet orderSheet, DataFormatter formatter, FormulaEvaluator evaluator) {
        List<String> errors = new ArrayList<>();
        Row headerRow = orderSheet.getRow(HEADER_ROW_INDEX);
        if (headerRow == null) {
            errors.add(message("order.import.validation.header.row.missing"));
            return errors;
        }

        for (int columnIndex = 0; columnIndex <= LAST_COLUMN_INDEX; columnIndex++) {
            String actualHeader = normalizeWhitespace(getCellText(headerRow, columnIndex, formatter, evaluator));
            String expectedHeader = normalizeWhitespace(EXPECTED_HEADERS.get(columnIndex));
            if (!expectedHeader.equalsIgnoreCase(actualHeader)) {
                errors.add(message(
                    "order.import.validation.header.invalid",
                        toColumnName(columnIndex),
                        EXPECTED_HEADERS.get(columnIndex),
                        actualHeader
                ));
            }
        }
        return errors;
    }

    private ValidationResult validateRows(
            Sheet orderSheet,
            DataFormatter formatter,
            FormulaEvaluator evaluator,
            MasterDataLookup masterData,
            Set<String> existingCustomerOrderCodes
    ) {
        List<OrderImportDTO> orders = new ArrayList<>();
        List<String> errors = new ArrayList<>();
        Set<String> fileOrderCodeSet = new HashSet<>();

        OrderImportDTO currentOrder = null;
        for (int rowIndex = DATA_START_ROW_INDEX; rowIndex <= orderSheet.getLastRowNum(); rowIndex++) {
            Row row = orderSheet.getRow(rowIndex);
            if (isBlankRow(row, LAST_COLUMN_INDEX, formatter, evaluator)) {
                continue;
            }

            int excelRowNumber = rowIndex + 1;
            String customerOrderCode = normalizeWhitespace(
                    getCellText(row, COLUMN_CUSTOMER_ORDER_CODE, formatter, evaluator)
            );

            if (hasText(customerOrderCode)) {
                currentOrder = parseFirstOrderRow(
                        row,
                        excelRowNumber,
                        formatter,
                        evaluator,
                        masterData,
                        fileOrderCodeSet,
                        existingCustomerOrderCodes,
                        errors
                );
                orders.add(currentOrder);
                continue;
            }

            if (currentOrder == null) {
                errors.add(message(
                    "order.import.validation.row.customer_code.required_first_row",
                        excelRowNumber
                ));
                continue;
            }

            currentOrder.getSourceRows().add(excelRowNumber);
            validateExtraProductRowGeneralColumns(row, excelRowNumber, formatter, evaluator, errors);

            OrderImportDTO.ProductImportItemDTO product = parseProductRow(
                    row,
                    excelRowNumber,
                    formatter,
                    evaluator,
                    masterData,
                    errors
            );
            if (product != null) {
                currentOrder.getProducts().add(product);
            }
        }

        for (OrderImportDTO order : orders) {
            if (order.getProducts() == null || order.getProducts().isEmpty()) {
                String code = hasText(order.getCustomerOrderCode())
                        ? order.getCustomerOrderCode()
                        : message("order.import.import_job.unknown_order_code");
                errors.add(message("order.import.validation.order.no_valid_product_rows", code));
            }
        }

        return new ValidationResult(orders, errors);
    }

    private Set<String> loadExistingCustomerOrderCodes(
            Sheet orderSheet,
            Long tenantId,
            DataFormatter formatter,
            FormulaEvaluator evaluator
    ) {
        Set<String> normalizedCodes = new HashSet<>();
        for (int rowIndex = DATA_START_ROW_INDEX; rowIndex <= orderSheet.getLastRowNum(); rowIndex++) {
            Row row = orderSheet.getRow(rowIndex);
            if (row == null) {
                continue;
            }

            String customerOrderCode = normalizeWhitespace(
                    getCellText(row, COLUMN_CUSTOMER_ORDER_CODE, formatter, evaluator)
            );
            if (hasText(customerOrderCode)) {
                normalizedCodes.add(normalizeLookupKey(customerOrderCode));
            }
        }

        if (normalizedCodes.isEmpty()) {
            return Set.of();
        }

        return orderRepository.findExistingCustomerOrderCodes(tenantId, normalizedCodes);
    }

    private OrderImportDTO parseFirstOrderRow(
            Row row,
            int excelRowNumber,
            DataFormatter formatter,
            FormulaEvaluator evaluator,
            MasterDataLookup masterData,
            Set<String> fileOrderCodeSet,
            Set<String> existingCustomerOrderCodes,
            List<String> errors
    ) {
        OrderImportDTO order = OrderImportDTO.builder()
                .products(new ArrayList<>())
                .sourceRows(new ArrayList<>())
                .build();
        order.getSourceRows().add(excelRowNumber);

        String customerOrderCode = requireText(
                row,
                COLUMN_CUSTOMER_ORDER_CODE,
                excelRowNumber,
                formatter,
                evaluator,
                errors
        );
        order.setCustomerOrderCode(customerOrderCode);

        if (hasText(customerOrderCode)) {
            String normalizedCode = normalizeCodeKey(customerOrderCode);
            if (!fileOrderCodeSet.add(normalizedCode)) {
            errors.add(message(
                "order.import.validation.customer_order_code.duplicate_in_file",
                        buildCellRef(excelRowNumber, COLUMN_CUSTOMER_ORDER_CODE)
                ));
            }

            if (existingCustomerOrderCodes.contains(normalizeLookupKey(customerOrderCode))) {
            errors.add(message(
                "order.import.validation.customer_order_code.exists",
                        buildCellRef(excelRowNumber, COLUMN_CUSTOMER_ORDER_CODE)
                ));
            }
        }

        order.setSenderName(requireText(row, COLUMN_SENDER_NAME, excelRowNumber, formatter, evaluator, errors));
        order.setSenderPhone(validatePhone(
                requireText(row, COLUMN_SENDER_PHONE, excelRowNumber, formatter, evaluator, errors),
                excelRowNumber,
                COLUMN_SENDER_PHONE,
                errors
        ));
        order.setSenderAddressDetail(requireText(
                row,
                COLUMN_SENDER_ADDRESS,
                excelRowNumber,
                formatter,
                evaluator,
                errors
        ));

        String senderProvinceCode = resolveMasterCode(
                row,
                COLUMN_SENDER_PROVINCE,
                excelRowNumber,
                formatter,
                evaluator,
                masterData.provinceNameByCode(),
                message("order.import.field.province"),
                errors
        );
        String senderWardCode = resolveMasterCode(
                row,
                COLUMN_SENDER_WARD,
                excelRowNumber,
                formatter,
                evaluator,
                masterData.wardNameByCode(),
                message("order.import.field.ward"),
                errors
        );
        order.setSenderProvinceCode(senderProvinceCode);
        order.setSenderWardCode(senderWardCode);
        validateWardProvinceRelation(
                senderWardCode,
                senderProvinceCode,
                excelRowNumber,
                COLUMN_SENDER_WARD,
                message("order.import.owner.sender"),
                masterData,
                errors
        );

        order.setReceiverName(requireText(row, COLUMN_RECEIVER_NAME, excelRowNumber, formatter, evaluator, errors));
        order.setReceiverPhone(validatePhone(
                requireText(row, COLUMN_RECEIVER_PHONE, excelRowNumber, formatter, evaluator, errors),
                excelRowNumber,
                COLUMN_RECEIVER_PHONE,
                errors
        ));
        order.setReceiverAddressDetail(requireText(
                row,
                COLUMN_RECEIVER_ADDRESS,
                excelRowNumber,
                formatter,
                evaluator,
                errors
        ));

        String receiverProvinceCode = resolveMasterCode(
                row,
                COLUMN_RECEIVER_PROVINCE,
                excelRowNumber,
                formatter,
                evaluator,
                masterData.provinceNameByCode(),
                message("order.import.field.province"),
                errors
        );
        String receiverWardCode = resolveMasterCode(
                row,
                COLUMN_RECEIVER_WARD,
                excelRowNumber,
                formatter,
                evaluator,
                masterData.wardNameByCode(),
                message("order.import.field.ward"),
                errors
        );
        order.setReceiverProvinceCode(receiverProvinceCode);
        order.setReceiverWardCode(receiverWardCode);
        validateWardProvinceRelation(
                receiverWardCode,
                receiverProvinceCode,
                excelRowNumber,
                COLUMN_RECEIVER_WARD,
                message("order.import.owner.receiver"),
                masterData,
                errors
        );

        order.setOrderType(mapRequiredValue(
                row,
                COLUMN_ORDER_TYPE,
                excelRowNumber,
                formatter,
                evaluator,
                ORDER_TYPE_MAP,
                message("order.import.allowed.order_type"),
                errors
        ));

        order.setNote(normalizeWhitespace(getCellText(row, COLUMN_NOTE, formatter, evaluator)));

        LocalDate pickupDate = parseOptionalDate(row, COLUMN_PICKUP_DATE, excelRowNumber, formatter, evaluator, errors);
        DeliveryRequestTime pickupRequestTime = mapOptionalValue(
                row,
                COLUMN_PICKUP_TIME,
                excelRowNumber,
                formatter,
                evaluator,
                PICKUP_TIME_MAP,
                message("order.import.allowed.pickup_time"),
                errors
        );
        order.setPickupDate(pickupDate);
        order.setPickupRequestTime(pickupRequestTime);
        if ((pickupDate == null) != (pickupRequestTime == null)) {
            errors.add(message(
                "order.import.validation.pickup.date_time.paired",
                    excelRowNumber
            ));
        }
        if (pickupDate != null && pickupRequestTime != null) {
            order.setPickupTimeStart(resolvePickupStart(pickupDate, pickupRequestTime));
            order.setPickupTimeEnd(resolvePickupEnd(pickupDate, pickupRequestTime));
        }

        order.setDeliveryRequestTime(mapOptionalValue(
                row,
                COLUMN_DELIVERY_TIME,
                excelRowNumber,
                formatter,
                evaluator,
                DELIVERY_TIME_MAP,
                message("order.import.allowed.delivery_time"),
                errors
        ));

        Boolean codFlag = mapRequiredValue(
                row,
                COLUMN_COD_FLAG,
                excelRowNumber,
                formatter,
                evaluator,
                COD_FLAG_MAP,
                message("order.import.allowed.cod_flag"),
                errors
        );
        order.setIsCod(codFlag);

        DimensionValue dimensions = parseRequiredDimensions(
                row,
                COLUMN_DIMENSIONS,
                excelRowNumber,
                formatter,
                evaluator,
                errors
        );
        if (dimensions != null) {
            order.setDimensionLengthCm(dimensions.length());
            order.setDimensionWidthCm(dimensions.width());
            order.setDimensionHeightCm(dimensions.height());
        }

        order.setTotalVolumeM3(parseRequiredDouble(
                row,
                COLUMN_VOLUME,
                excelRowNumber,
                formatter,
                evaluator,
                true,
                errors
        ));

        order.setFeePayer(mapRequiredValue(
                row,
                COLUMN_FEE_PAYER,
                excelRowNumber,
                formatter,
                evaluator,
                FEE_PAYER_MAP,
                message("order.import.allowed.fee_payer"),
                errors
        ));

        OrderImportDTO.ProductImportItemDTO product = parseProductRow(
                row,
                excelRowNumber,
                formatter,
                evaluator,
                masterData,
                errors
        );
        if (product != null) {
            order.getProducts().add(product);
        }

        return order;
    }

    private void validateExtraProductRowGeneralColumns(
            Row row,
            int excelRowNumber,
            DataFormatter formatter,
            FormulaEvaluator evaluator,
            List<String> errors
    ) {
        if (hasAnyDataInRange(row, COLUMN_CUSTOMER_ORDER_CODE, COLUMN_FEE_PAYER, formatter, evaluator)) {
            errors.add(message(
                "order.import.validation.extra_product_row.invalid_columns",
                    excelRowNumber
            ));
        }
    }

    private OrderImportDTO.ProductImportItemDTO parseProductRow(
            Row row,
            int excelRowNumber,
            DataFormatter formatter,
            FormulaEvaluator evaluator,
            MasterDataLookup masterData,
            List<String> errors
    ) {
        int errorCountBefore = errors.size();

        String productName = requireText(
                row,
                COLUMN_PRODUCT_NAME,
                excelRowNumber,
                formatter,
                evaluator,
                errors
        );

        Long productValue = parseRequiredLong(
                row,
                COLUMN_PRODUCT_VALUE,
                excelRowNumber,
                formatter,
                evaluator,
                0,
                errors
        );

        Integer quantity = parseRequiredInteger(
                row,
                COLUMN_PRODUCT_QUANTITY,
                excelRowNumber,
                formatter,
                evaluator,
                1,
                errors
        );

        Double weightGram = parseRequiredDouble(
                row,
                COLUMN_PRODUCT_WEIGHT,
                excelRowNumber,
                formatter,
                evaluator,
                true,
                errors
        );

        String rawProductType = requireText(
                row,
                COLUMN_PRODUCT_TYPE,
                excelRowNumber,
                formatter,
                evaluator,
                errors
        );

        ProductType productType = null;
        if (hasText(rawProductType)) {
            CodeNameValue codeName = parseCodeAndName(rawProductType, excelRowNumber, COLUMN_PRODUCT_TYPE, errors);
            if (codeName != null) {
                productType = masterData.productTypeByCode().get(normalizeCodeKey(codeName.code()));
                if (productType == null) {
                    errors.add(message(
                        "order.import.validation.product_type.not_found_or_inactive",
                            buildCellRef(excelRowNumber, COLUMN_PRODUCT_TYPE),
                            codeName.code()
                    ));
                } else if (!normalizeWhitespace(productType.getName()).equalsIgnoreCase(normalizeWhitespace(codeName.name()))) {
                    errors.add(message(
                        "order.import.validation.product_type.name_mismatch",
                            buildCellRef(excelRowNumber, COLUMN_PRODUCT_TYPE),
                            productType.getCode(),
                            productType.getName()
                    ));
                }
            }
        }

        if (errors.size() > errorCountBefore) {
            return null;
        }

        return OrderImportDTO.ProductImportItemDTO.builder()
                .name(productName)
                .value(productValue)
                .quantity(quantity)
                .weightGram(weightGram)
                .productTypeId(productType == null ? null : productType.getId())
                .productTypeCode(productType == null ? null : productType.getCode())
                .productTypeName(productType == null ? null : productType.getName())
                .build();
    }

    private String requireText(
            Row row,
            int columnIndex,
            int excelRowNumber,
            DataFormatter formatter,
            FormulaEvaluator evaluator,
            List<String> errors
    ) {
        String value = normalizeWhitespace(getCellText(row, columnIndex, formatter, evaluator));
        if (!hasText(value)) {
            errors.add(message("order.import.validation.required", buildCellRef(excelRowNumber, columnIndex)));
            return null;
        }
        return value;
    }

    private String validatePhone(String phone, int excelRowNumber, int columnIndex, List<String> errors) {
        if (!hasText(phone)) {
            return null;
        }

        String normalizedPhone = phone.replaceAll("\\s+", "");
        if (!PHONE_PATTERN.matcher(normalizedPhone).matches()) {
            errors.add(message(
                "order.import.validation.phone.invalid",
                    buildCellRef(excelRowNumber, columnIndex)
            ));
        }
        return normalizedPhone;
    }

    private String resolveMasterCode(
            Row row,
            int columnIndex,
            int excelRowNumber,
            DataFormatter formatter,
            FormulaEvaluator evaluator,
            Map<String, String> masterCodeNameMap,
            String fieldDisplay,
            List<String> errors
    ) {
        String rawValue = requireText(row, columnIndex, excelRowNumber, formatter, evaluator, errors);
        if (!hasText(rawValue)) {
            return null;
        }

        CodeNameValue codeName = parseCodeAndName(rawValue, excelRowNumber, columnIndex, errors);
        if (codeName == null) {
            return null;
        }

        String lookupCode = normalizeCodeKey(codeName.code());
        String expectedName = masterCodeNameMap.get(lookupCode);
        if (!hasText(expectedName)) {
            errors.add(message(
                    "order.import.validation.master_code.not_found",
                    buildCellRef(excelRowNumber, columnIndex),
                    fieldDisplay,
                    codeName.code()
            ));
            return null;
        }

        if (!normalizeWhitespace(expectedName).equalsIgnoreCase(normalizeWhitespace(codeName.name()))) {
            errors.add(message(
                "order.import.validation.master_code.name_mismatch",
                    buildCellRef(excelRowNumber, columnIndex),
                    fieldDisplay,
                    codeName.code(),
                    expectedName
            ));
        }

        return codeName.code();
    }

    private void validateWardProvinceRelation(
            String wardCode,
            String provinceCode,
            int excelRowNumber,
            int wardColumnIndex,
            String ownerDisplay,
            MasterDataLookup masterData,
            List<String> errors
    ) {
        if (!hasText(wardCode) || !hasText(provinceCode)) {
            return;
        }

        String expectedProvinceCode = masterData.wardProvinceByCode().get(normalizeCodeKey(wardCode));
        if (!hasText(expectedProvinceCode)) {
            return;
        }

        if (!normalizeCodeKey(provinceCode).equals(normalizeCodeKey(expectedProvinceCode))) {
            errors.add(message(
                    "order.import.validation.ward_province.mismatch",
                    buildCellRef(excelRowNumber, wardColumnIndex),
                    ownerDisplay
            ));
        }
    }

    private <T> T mapRequiredValue(
            Row row,
            int columnIndex,
            int excelRowNumber,
            DataFormatter formatter,
            FormulaEvaluator evaluator,
            Map<String, T> valueMap,
            String allowedValues,
            List<String> errors
    ) {
        String rawValue = requireText(row, columnIndex, excelRowNumber, formatter, evaluator, errors);
        if (!hasText(rawValue)) {
            return null;
        }

        String key = normalizeLookupKey(rawValue);
        T mappedValue = valueMap.get(key);
        if (mappedValue == null) {
            errors.add(message(
                "order.import.validation.value.invalid_option",
                    buildCellRef(excelRowNumber, columnIndex),
                    rawValue,
                    allowedValues
            ));
        }
        return mappedValue;
    }

    private <T> T mapOptionalValue(
            Row row,
            int columnIndex,
            int excelRowNumber,
            DataFormatter formatter,
            FormulaEvaluator evaluator,
            Map<String, T> valueMap,
            String allowedValues,
            List<String> errors
    ) {
        String rawValue = normalizeWhitespace(getCellText(row, columnIndex, formatter, evaluator));
        if (!hasText(rawValue)) {
            return null;
        }

        String key = normalizeLookupKey(rawValue);
        T mappedValue = valueMap.get(key);
        if (mappedValue == null) {
            errors.add(message(
                "order.import.validation.value.invalid_option",
                    buildCellRef(excelRowNumber, columnIndex),
                    rawValue,
                    allowedValues
            ));
        }
        return mappedValue;
    }

    private LocalDate parseOptionalDate(
            Row row,
            int columnIndex,
            int excelRowNumber,
            DataFormatter formatter,
            FormulaEvaluator evaluator,
            List<String> errors
    ) {
        Cell cell = getCell(row, columnIndex);
        if (cell != null) {
            if (cell.getCellType() == CellType.NUMERIC && DateUtil.isCellDateFormatted(cell)) {
                return cell.getLocalDateTimeCellValue().toLocalDate();
            }
            if (cell.getCellType() == CellType.FORMULA && DateUtil.isCellDateFormatted(cell)) {
                try {
                    return cell.getLocalDateTimeCellValue().toLocalDate();
                } catch (Exception ignored) {
                    // Fallback to text parsing below.
                }
            }
        }

        String rawDate = normalizeWhitespace(getCellText(row, columnIndex, formatter, evaluator));
        if (!hasText(rawDate)) {
            return null;
        }

        try {
            return LocalDate.parse(rawDate, IMPORT_DATE_FORMATTER);
        } catch (DateTimeParseException exception) {
            errors.add(message(
                    "order.import.validation.date.invalid_format",
                    buildCellRef(excelRowNumber, columnIndex)
            ));
            return null;
        }
    }

    private LocalDateTime resolvePickupStart(LocalDate pickupDate, DeliveryRequestTime pickupRequestTime) {
        return switch (pickupRequestTime) {
            case MORNING -> pickupDate.atTime(7, 30);
            case AFTERNOON -> pickupDate.atTime(13, 30);
            default -> pickupDate.atTime(0, 0);
        };
    }

    private LocalDateTime resolvePickupEnd(LocalDate pickupDate, DeliveryRequestTime pickupRequestTime) {
        return switch (pickupRequestTime) {
            case MORNING -> pickupDate.atTime(12, 0);
            case AFTERNOON -> pickupDate.atTime(18, 0);
            default -> pickupDate.atTime(23, 59, 59);
        };
    }

    private DimensionValue parseRequiredDimensions(
            Row row,
            int columnIndex,
            int excelRowNumber,
            DataFormatter formatter,
            FormulaEvaluator evaluator,
            List<String> errors
    ) {
        String rawDimensions = requireText(row, columnIndex, excelRowNumber, formatter, evaluator, errors);
        if (!hasText(rawDimensions)) {
            return null;
        }

        String[] parts = rawDimensions.trim().split("\\s*[xX×]\\s*");
        if (parts.length != 3) {
            errors.add(message(
                "order.import.validation.dimension.invalid_format",
                    buildCellRef(excelRowNumber, columnIndex)
            ));
            return null;
        }

        Double length = parseNumber(parts[0]);
        Double width = parseNumber(parts[1]);
        Double height = parseNumber(parts[2]);
        if (length == null || width == null || height == null || length <= 0 || width <= 0 || height <= 0) {
            errors.add(message(
                "order.import.validation.dimension.invalid_positive",
                    buildCellRef(excelRowNumber, columnIndex)
            ));
            return null;
        }

        return new DimensionValue(length, width, height);
    }

    private Long parseRequiredLong(
            Row row,
            int columnIndex,
            int excelRowNumber,
            DataFormatter formatter,
            FormulaEvaluator evaluator,
            long minValue,
            List<String> errors
    ) {
        String rawValue = requireText(row, columnIndex, excelRowNumber, formatter, evaluator, errors);
        if (!hasText(rawValue)) {
            return null;
        }

        Double value = parseNumber(rawValue);
        if (value == null || !isWholeNumber(value)) {
            errors.add(message(
                "order.import.validation.number.invalid_integer",
                    buildCellRef(excelRowNumber, columnIndex)
            ));
            return null;
        }

        long parsed = value.longValue();
        if (parsed < minValue) {
            errors.add(message(
                "order.import.validation.number.min_value",
                    buildCellRef(excelRowNumber, columnIndex),
                    minValue
            ));
            return null;
        }

        return parsed;
    }

    private Integer parseRequiredInteger(
            Row row,
            int columnIndex,
            int excelRowNumber,
            DataFormatter formatter,
            FormulaEvaluator evaluator,
            int minValue,
            List<String> errors
    ) {
        Long value = parseRequiredLong(
                row,
                columnIndex,
                excelRowNumber,
                formatter,
                evaluator,
                minValue,
                errors
        );
        if (value == null) {
            return null;
        }

        if (value > Integer.MAX_VALUE) {
            errors.add(message(
                    "order.import.validation.quantity.exceeds_max",
                    buildCellRef(excelRowNumber, columnIndex)
            ));
            return null;
        }

        return value.intValue();
    }

    private Double parseRequiredDouble(
            Row row,
            int columnIndex,
            int excelRowNumber,
            DataFormatter formatter,
            FormulaEvaluator evaluator,
            boolean strictlyPositive,
            List<String> errors
    ) {
        String rawValue = requireText(row, columnIndex, excelRowNumber, formatter, evaluator, errors);
        if (!hasText(rawValue)) {
            return null;
        }

        Double value = parseNumber(rawValue);
        if (value == null) {
            errors.add(message(
                "order.import.validation.number.invalid",
                    buildCellRef(excelRowNumber, columnIndex)
            ));
            return null;
        }

        if (strictlyPositive && value <= 0) {
            errors.add(message(
                "order.import.validation.number.must_be_positive",
                    buildCellRef(excelRowNumber, columnIndex)
            ));
            return null;
        }
        return value;
    }

    private CodeNameValue parseCodeAndName(String value, int excelRowNumber, int columnIndex, List<String> errors) {
        ExcelImportUtils.CodeNameValue parsedValue = ExcelImportUtils.parseCodeAndName(value, CODE_NAME_PATTERN);
        if (parsedValue == null) {
            errors.add(message(
                    "order.import.validation.code_name.invalid_format",
                    buildCellRef(excelRowNumber, columnIndex)
            ));
            return null;
        }

        return new CodeNameValue(parsedValue.code(), parsedValue.name());
    }

    private MasterDataLookup loadMasterData(Long tenantId) {
        Map<String, String> provinceNameByCode = provinceRepository.findTemplateCodeNameList().stream()
                .collect(Collectors.toMap(
                        value -> normalizeCodeKey(value.getCode()),
                        CodeNameProjection::getName,
                        (first, second) -> first,
                        LinkedHashMap::new
                ));

        Map<String, String> wardNameByCode = wardRepository.findTemplateCodeNameList().stream()
                .collect(Collectors.toMap(
                        value -> normalizeCodeKey(value.getCode()),
                        CodeNameProjection::getName,
                        (first, second) -> first,
                        LinkedHashMap::new
                ));

        Map<String, String> wardProvinceByCode = wardRepository.findAll().stream()
                .collect(Collectors.toMap(
                        ward -> normalizeCodeKey(ward.getWardCode()),
                        Ward::getProvinceCode,
                        (first, second) -> first,
                        HashMap::new
                ));

        Map<String, ProductType> productTypeByCode = productTypeRepository
                .findByTenantIdAndIsActiveTrueOrderByNameAsc(tenantId)
                .stream()
                .collect(Collectors.toMap(
                        productType -> normalizeCodeKey(productType.getCode()),
                        productType -> productType,
                        (first, second) -> first,
                        LinkedHashMap::new
                ));

        return new MasterDataLookup(
                provinceNameByCode,
                wardNameByCode,
                wardProvinceByCode,
                productTypeByCode
        );
    }

    private boolean hasAnyDataInRange(
            Row row,
            int startColumn,
            int endColumn,
            DataFormatter formatter,
            FormulaEvaluator evaluator
    ) {
        if (row == null) {
            return false;
        }

        for (int columnIndex = startColumn; columnIndex <= endColumn; columnIndex++) {
            if (hasText(getCellText(row, columnIndex, formatter, evaluator))) {
                return true;
            }
        }
        return false;
    }

    private String buildCellRef(int excelRowNumber, int columnIndex) {
        return message(
                "order.import.validation.cell_ref",
                excelRowNumber,
                toColumnName(columnIndex),
                EXPECTED_HEADERS.get(columnIndex)
        );
    }

    private String normalizeLookupKey(String value) {
        String normalized = normalizeWhitespace(value).replaceAll("\\s*/\\s*", "/");
        return normalized.toLowerCase(Locale.ROOT);
    }

    private String message(String key, Object... args) {
        if (args == null || args.length == 0) {
            return messageService.getMessage(key);
        }
        return messageService.getMessage(key, args);
    }

    private record ValidationResult(List<OrderImportDTO> orders, List<String> errors) {
    }

    private record CodeNameValue(String code, String name) {
    }

    private record DimensionValue(Double length, Double width, Double height) {
    }

    private record MasterDataLookup(
            Map<String, String> provinceNameByCode,
            Map<String, String> wardNameByCode,
            Map<String, String> wardProvinceByCode,
            Map<String, ProductType> productTypeByCode
    ) {
    }
}

