/*
Author: Nguyen The Anh
Description: Part of Serp Project
*/

package serp.project.first_mile.service.impl;

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
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.multipart.MultipartFile;
import serp.project.first_mile.domain.ProductType;
import serp.project.first_mile.domain.Ward;
import serp.project.first_mile.dto.request.OrderImportDTO;
import serp.project.first_mile.dto.response.ValidateImportFileDTO;
import serp.project.first_mile.enums.DeliveryRequestTime;
import serp.project.first_mile.enums.FeePayer;
import serp.project.first_mile.enums.OrderProductCategory;
import serp.project.first_mile.enums.OrderType;
import serp.project.first_mile.repository.OrderRepository;
import serp.project.first_mile.repository.ProductTypeRepository;
import serp.project.first_mile.repository.ProvinceRepository;
import serp.project.first_mile.repository.WardRepository;
import serp.project.first_mile.repository.projection.CodeNameProjection;
import serp.project.first_mile.service.OrderImportExcelService;

import java.io.InputStream;
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
import java.util.Set;
import java.util.UUID;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import java.util.stream.Collectors;

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
    private static final int COLUMN_ORDER_CATEGORY = 12;
    private static final int COLUMN_ORDER_TYPE = 13;
    private static final int COLUMN_NOTE = 14;
    private static final int COLUMN_PICKUP_DATE = 15;
    private static final int COLUMN_PICKUP_TIME = 16;
    private static final int COLUMN_DELIVERY_TIME = 17;
    private static final int COLUMN_COD_FLAG = 18;
    private static final int COLUMN_DIMENSIONS = 19;
    private static final int COLUMN_VOLUME = 20;
    private static final int COLUMN_FEE_PAYER = 21;
    private static final int COLUMN_PRODUCT_NAME = 22;
    private static final int COLUMN_PRODUCT_VALUE = 23;
    private static final int COLUMN_PRODUCT_QUANTITY = 24;
    private static final int COLUMN_PRODUCT_WEIGHT = 25;
    private static final int COLUMN_PRODUCT_TYPE = 26;
    private static final int LAST_COLUMN_INDEX = 26;

    private static final DateTimeFormatter IMPORT_DATE_FORMATTER = DateTimeFormatter.ofPattern("d/M/uuuu");
    private static final Pattern PHONE_PATTERN = Pattern.compile("^[0-9]{8,15}$");
    private static final Pattern CODE_NAME_PATTERN = Pattern.compile("^(.+?)\\s+-\\s+(.+)$");

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
            "Phân loại đơn hàng (*)",
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

    private static final Map<String, OrderProductCategory> ORDER_CATEGORY_MAP = Map.of(
            "dễ vỡ", OrderProductCategory.FRAGILE,
            "giá trị cao", OrderProductCategory.HIGH_VALUE,
            "nguyên khối", OrderProductCategory.SOLID,
            "quá khổ", OrderProductCategory.OVERSIZED,
            "chất lỏng", OrderProductCategory.LIQUID,
            "từ tính/pin", OrderProductCategory.MAGNETIC_BATTERY
    );

    private static final Map<String, OrderType> ORDER_TYPE_MAP = Map.of(
            "hỏa tốc", OrderType.EXPRESS_ORDER,
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

    @Override
    public ValidateImportFileDTO<OrderImportDTO> validateImportFile(MultipartFile file, Long tenantId) {
        ValidateImportFileDTO<OrderImportDTO> response = buildBaseValidateResponse();

        if (file == null || file.isEmpty()) {
            setValidationFailed(response, List.of("File import không được để trống."));
            return response;
        }

        try (InputStream inputStream = file.getInputStream();
             Workbook workbook = WorkbookFactory.create(inputStream)) {

            Sheet orderSheet = workbook.getSheet(ORDER_SHEET_NAME);
            if (orderSheet == null) {
                setValidationFailed(response, List.of("Không tìm thấy sheet Order trong file import."));
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
            ValidationResult validationResult = validateRows(orderSheet, tenantId, formatter, evaluator, masterData);

            response.setData(validationResult.orders());
            if (validationResult.orders().isEmpty() && validationResult.errors().isEmpty()) {
                setValidationFailed(response, List.of("Không tìm thấy dữ liệu đơn hàng từ dòng 4 trở đi."));
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
            setValidationFailed(response, List.of("Không thể đọc file Excel hoặc dữ liệu không đúng định dạng."));
            return response;
        }
    }

    private ValidateImportFileDTO<OrderImportDTO> buildBaseValidateResponse() {
        ValidateImportFileDTO<OrderImportDTO> response = new ValidateImportFileDTO<>();
        response.setFileId(UUID.randomUUID());
        response.setHeader(buildHeaderMap());
        response.setData(new ArrayList<>());
        response.setSuccess(false);
        response.setType(0);
        return response;
    }

    private LinkedHashMap<String, String> buildHeaderMap() {
        LinkedHashMap<String, String> headerMap = new LinkedHashMap<>();
        for (int i = 0; i <= LAST_COLUMN_INDEX; i++) {
            headerMap.put(toColumnName(i), EXPECTED_HEADERS.get(i));
        }
        return headerMap;
    }

    private void setValidationFailed(ValidateImportFileDTO<OrderImportDTO> response, List<String> errors) {
        response.setSuccess(false);
        response.setType(0);
        response.setErrorMessage(String.join("\n", errors));
    }

    private List<String> validateHeader(Sheet orderSheet, DataFormatter formatter, FormulaEvaluator evaluator) {
        List<String> errors = new ArrayList<>();
        Row headerRow = orderSheet.getRow(HEADER_ROW_INDEX);
        if (headerRow == null) {
            errors.add("Không tìm thấy dòng header ở dòng 3 của sheet Order.");
            return errors;
        }

        for (int columnIndex = 0; columnIndex <= LAST_COLUMN_INDEX; columnIndex++) {
            String actualHeader = normalizeWhitespace(getCellText(headerRow, columnIndex, formatter, evaluator));
            String expectedHeader = normalizeWhitespace(EXPECTED_HEADERS.get(columnIndex));
            if (!expectedHeader.equalsIgnoreCase(actualHeader)) {
                errors.add(String.format(
                        "Header không hợp lệ tại cột %s: kỳ vọng '%s' nhưng nhận '%s'.",
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
            Long tenantId,
            DataFormatter formatter,
            FormulaEvaluator evaluator,
            MasterDataLookup masterData
    ) {
        List<OrderImportDTO> orders = new ArrayList<>();
        List<String> errors = new ArrayList<>();
        Set<String> fileOrderCodeSet = new HashSet<>();

        OrderImportDTO currentOrder = null;
        for (int rowIndex = DATA_START_ROW_INDEX; rowIndex <= orderSheet.getLastRowNum(); rowIndex++) {
            Row row = orderSheet.getRow(rowIndex);
            if (isBlankRow(row, formatter, evaluator)) {
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
                        tenantId,
                        formatter,
                        evaluator,
                        masterData,
                        fileOrderCodeSet,
                        errors
                );
                orders.add(currentOrder);
                continue;
            }

            if (currentOrder == null) {
                errors.add(String.format(
                        "Dòng %d: thiếu Mã đơn hàng tự quản ở dòng đầu đơn hàng.",
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
                        : "(không xác định)";
                errors.add(String.format("Đơn hàng %s không có dòng sản phẩm hợp lệ.", code));
            }
        }

        return new ValidationResult(orders, errors);
    }

    private OrderImportDTO parseFirstOrderRow(
            Row row,
            int excelRowNumber,
            Long tenantId,
            DataFormatter formatter,
            FormulaEvaluator evaluator,
            MasterDataLookup masterData,
            Set<String> fileOrderCodeSet,
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
                errors.add(String.format(
                        "%s: mã đơn hàng tự quản bị trùng trong file.",
                        buildCellRef(excelRowNumber, COLUMN_CUSTOMER_ORDER_CODE)
                ));
            }

            if (orderRepository.existsByCustomerOrderCodeIgnoreCaseAndTenantId(customerOrderCode, tenantId)) {
                errors.add(String.format(
                        "%s: mã đơn hàng tự quản đã tồn tại trong hệ thống.",
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
                "tỉnh/thành phố",
                errors
        );
        String senderWardCode = resolveMasterCode(
                row,
                COLUMN_SENDER_WARD,
                excelRowNumber,
                formatter,
                evaluator,
                masterData.wardNameByCode(),
                "phường/xã",
                errors
        );
        order.setSenderProvinceCode(senderProvinceCode);
        order.setSenderWardCode(senderWardCode);
        validateWardProvinceRelation(
                senderWardCode,
                senderProvinceCode,
                excelRowNumber,
                COLUMN_SENDER_WARD,
                "người gửi",
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
                "tỉnh/thành phố",
                errors
        );
        String receiverWardCode = resolveMasterCode(
                row,
                COLUMN_RECEIVER_WARD,
                excelRowNumber,
                formatter,
                evaluator,
                masterData.wardNameByCode(),
                "phường/xã",
                errors
        );
        order.setReceiverProvinceCode(receiverProvinceCode);
        order.setReceiverWardCode(receiverWardCode);
        validateWardProvinceRelation(
                receiverWardCode,
                receiverProvinceCode,
                excelRowNumber,
                COLUMN_RECEIVER_WARD,
                "người nhận",
                masterData,
                errors
        );

        order.setOrderProductCategory(mapRequiredValue(
                row,
                COLUMN_ORDER_CATEGORY,
                excelRowNumber,
                formatter,
                evaluator,
                ORDER_CATEGORY_MAP,
                "Dễ vỡ, Giá trị cao, Nguyên khối, Quá khổ, Chất lỏng, Từ tính/pin",
                errors
        ));

        order.setOrderType(mapRequiredValue(
                row,
                COLUMN_ORDER_TYPE,
                excelRowNumber,
                formatter,
                evaluator,
                ORDER_TYPE_MAP,
                "Hỏa tốc, Tiêu chuẩn",
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
                "Cả ngày, Sáng (7h30 - 12h00), Chiều (13h30 - 18h00)",
                errors
        );
        order.setPickupDate(pickupDate);
        order.setPickupRequestTime(pickupRequestTime);
        if ((pickupDate == null) != (pickupRequestTime == null)) {
            errors.add(String.format(
                    "Dòng %d: Ngày hẹn lấy và Giờ hẹn lấy phải được nhập đồng thời.",
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
                "Cả ngày, Sáng (7h30 - 12h00), Chiều (13h30 - 18h00), Chủ nhật, Ngày nghỉ lễ, Giờ hành chính (7h30 - 11h30, 13h30 - 17h30)",
                errors
        ));

        Boolean codFlag = mapRequiredValue(
                row,
                COLUMN_COD_FLAG,
                excelRowNumber,
                formatter,
                evaluator,
                COD_FLAG_MAP,
                "Có, Không",
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
                "Người nhận, Người gửi",
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
            errors.add(String.format(
                    "Dòng %d: dòng sản phẩm bổ sung chỉ được nhập dữ liệu từ cột W đến cột AA.",
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
                    errors.add(String.format(
                            "%s: loại sản phẩm '%s' không tồn tại hoặc không hoạt động cho tenant hiện tại.",
                            buildCellRef(excelRowNumber, COLUMN_PRODUCT_TYPE),
                            codeName.code()
                    ));
                } else if (!normalizeWhitespace(productType.getName()).equalsIgnoreCase(normalizeWhitespace(codeName.name()))) {
                    errors.add(String.format(
                            "%s: tên loại sản phẩm không khớp mã. Kỳ vọng '%s - %s'.",
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
            errors.add(String.format("%s: không được để trống.", buildCellRef(excelRowNumber, columnIndex)));
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
            errors.add(String.format(
                    "%s: số điện thoại không hợp lệ.",
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
            errors.add(String.format(
                    "%s: %s '%s' không tồn tại.",
                    buildCellRef(excelRowNumber, columnIndex),
                    fieldDisplay,
                    codeName.code()
            ));
            return null;
        }

        if (!normalizeWhitespace(expectedName).equalsIgnoreCase(normalizeWhitespace(codeName.name()))) {
            errors.add(String.format(
                    "%s: tên %s không khớp mã. Kỳ vọng '%s - %s'.",
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
            errors.add(String.format(
                    "%s: phường/xã của %s không thuộc tỉnh/thành phố đã chọn.",
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
            errors.add(String.format(
                    "%s: giá trị không hợp lệ '%s'. Các giá trị hợp lệ: %s.",
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
            errors.add(String.format(
                    "%s: giá trị không hợp lệ '%s'. Các giá trị hợp lệ: %s.",
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
            errors.add(String.format(
                    "%s: ngày không đúng định dạng dd/mm/yyyy.",
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
            errors.add(String.format(
                    "%s: kích cỡ phải theo định dạng dài x rộng x cao (cm).",
                    buildCellRef(excelRowNumber, columnIndex)
            ));
            return null;
        }

        Double length = parseNumber(parts[0]);
        Double width = parseNumber(parts[1]);
        Double height = parseNumber(parts[2]);
        if (length == null || width == null || height == null || length <= 0 || width <= 0 || height <= 0) {
            errors.add(String.format(
                    "%s: kích cỡ phải gồm 3 số dương.",
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
            errors.add(String.format(
                    "%s: giá trị phải là số nguyên hợp lệ.",
                    buildCellRef(excelRowNumber, columnIndex)
            ));
            return null;
        }

        long parsed = value.longValue();
        if (parsed < minValue) {
            errors.add(String.format(
                    "%s: giá trị phải lớn hơn hoặc bằng %d.",
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
            errors.add(String.format(
                    "%s: số lượng vượt quá giới hạn cho phép.",
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
            errors.add(String.format(
                    "%s: giá trị phải là số hợp lệ.",
                    buildCellRef(excelRowNumber, columnIndex)
            ));
            return null;
        }

        if (strictlyPositive && value <= 0) {
            errors.add(String.format(
                    "%s: giá trị phải lớn hơn 0.",
                    buildCellRef(excelRowNumber, columnIndex)
            ));
            return null;
        }
        return value;
    }

    private Double parseNumber(String rawValue) {
        if (!hasText(rawValue)) {
            return null;
        }

        String value = rawValue.trim().replace(" ", "");
        if (value.matches("[-+]?\\d+(\\.\\d+)?")) {
            return Double.parseDouble(value);
        }

        if (value.matches("[-+]?\\d+(,\\d+)?")) {
            return Double.parseDouble(value.replace(",", "."));
        }

        if (value.matches("[-+]?\\d{1,3}(,\\d{3})+(\\.\\d+)?")) {
            return Double.parseDouble(value.replace(",", ""));
        }

        if (value.matches("[-+]?\\d{1,3}(\\.\\d{3})+(,\\d+)?")) {
            String normalized = value.replace(".", "").replace(",", ".");
            return Double.parseDouble(normalized);
        }

        return null;
    }

    private boolean isWholeNumber(Double value) {
        return Math.abs(value - Math.rint(value)) < 0.0000001;
    }

    private CodeNameValue parseCodeAndName(String value, int excelRowNumber, int columnIndex, List<String> errors) {
        String normalizedValue = normalizeWhitespace(value);
        Matcher matcher = CODE_NAME_PATTERN.matcher(normalizedValue);
        if (!matcher.matches()) {
            errors.add(String.format(
                    "%s: định dạng phải là 'code - name'.",
                    buildCellRef(excelRowNumber, columnIndex)
            ));
            return null;
        }

        String code = matcher.group(1).trim();
        String name = matcher.group(2).trim();
        if (!hasText(code) || !hasText(name)) {
            errors.add(String.format(
                    "%s: định dạng phải là 'code - name'.",
                    buildCellRef(excelRowNumber, columnIndex)
            ));
            return null;
        }

        return new CodeNameValue(code, name);
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

    private boolean isBlankRow(Row row, DataFormatter formatter, FormulaEvaluator evaluator) {
        if (row == null) {
            return true;
        }
        for (int columnIndex = 0; columnIndex <= LAST_COLUMN_INDEX; columnIndex++) {
            if (hasText(getCellText(row, columnIndex, formatter, evaluator))) {
                return false;
            }
        }
        return true;
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

    private String getCellText(Row row, int columnIndex, DataFormatter formatter, FormulaEvaluator evaluator) {
        Cell cell = getCell(row, columnIndex);
        if (cell == null) {
            return "";
        }
        return formatter.formatCellValue(cell, evaluator);
    }

    private Cell getCell(Row row, int columnIndex) {
        if (row == null) {
            return null;
        }
        return row.getCell(columnIndex, Row.MissingCellPolicy.RETURN_BLANK_AS_NULL);
    }

    private String buildCellRef(int excelRowNumber, int columnIndex) {
        return String.format(
                "Dòng %d, cột %s (%s)",
                excelRowNumber,
                toColumnName(columnIndex),
                EXPECTED_HEADERS.get(columnIndex)
        );
    }

    private String toColumnName(int columnIndex) {
        int current = columnIndex + 1;
        StringBuilder columnName = new StringBuilder();
        while (current > 0) {
            int remainder = (current - 1) % 26;
            columnName.insert(0, (char) ('A' + remainder));
            current = (current - 1) / 26;
        }
        return columnName.toString();
    }

    private String normalizeWhitespace(String value) {
        if (value == null) {
            return "";
        }
        return value.replace('\n', ' ').replaceAll("\\s+", " ").trim();
    }

    private String normalizeLookupKey(String value) {
        String normalized = normalizeWhitespace(value).replaceAll("\\s*/\\s*", "/");
        return normalized.toLowerCase(Locale.ROOT);
    }

    private String normalizeCodeKey(String code) {
        return normalizeWhitespace(code).toUpperCase(Locale.ROOT);
    }

    private boolean hasText(String value) {
        return value != null && !value.trim().isEmpty();
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
