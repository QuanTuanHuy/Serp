/*
Author: QuanTuanHuy
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
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.multipart.MultipartFile;
import serp.project.first_mile.domain.ImportHistory;
import serp.project.first_mile.domain.PostOffice;
import serp.project.first_mile.domain.Ward;
import serp.project.first_mile.dto.request.PostOfficeImportDTO;
import serp.project.first_mile.dto.response.ImportHistoryResponse;
import serp.project.first_mile.dto.response.ValidateImportFileDTO;
import serp.project.first_mile.enums.ImportHistoryStatus;
import serp.project.first_mile.enums.ImportType;
import serp.project.first_mile.enums.PostOfficeStatus;
import serp.project.first_mile.exception.AppException;
import serp.project.first_mile.exception.ErrorCode;
import serp.project.first_mile.exception.MessageService;
import serp.project.first_mile.repository.ImportHistoryRepository;
import serp.project.first_mile.repository.PostOfficeRepository;
import serp.project.first_mile.repository.ProvinceRepository;
import serp.project.first_mile.repository.WardRepository;
import serp.project.first_mile.repository.projection.CodeNameProjection;
import serp.project.first_mile.service.PostOfficeImportExcelService;

import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
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
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.Executor;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
@Slf4j
public class PostOfficeImportExcelServiceImpl implements PostOfficeImportExcelService {

    private static final int HEADER_ROW_INDEX = 0;
    private static final int DATA_START_ROW_INDEX = 1;

    private static final int COLUMN_STT = 0;
    private static final int COLUMN_NAME = 1;
    private static final int COLUMN_CODE = 2;
    private static final int COLUMN_PROVINCE = 3;
    private static final int COLUMN_WARD = 4;
    private static final int COLUMN_ADDRESS_DETAIL = 5;
    private static final int COLUMN_PHONE = 6;
    private static final int COLUMN_OPERATIONAL_START_DATE = 7;
    private static final int COLUMN_OPERATIONAL_END_DATE = 8;
    private static final int COLUMN_WORKING_START_TIME = 9;
    private static final int COLUMN_WORKING_END_TIME = 10;
    private static final int COLUMN_SERVICE_RADIUS_M = 11;
    private static final int COLUMN_STATUS = 12;
    private static final int LAST_COLUMN_INDEX = 12;

    private static final DateTimeFormatter IMPORT_DATE_FORMATTER = DateTimeFormatter.ofPattern("d/M/uuuu");
    private static final DateTimeFormatter IMPORT_TIME_FORMATTER = DateTimeFormatter.ofPattern("H:mm");
    private static final Pattern PHONE_PATTERN = Pattern.compile("^0\\d{9,10}$");
    private static final Pattern CODE_NAME_PATTERN = Pattern.compile("^(.+?)\\s+-\\s+(.+)$");
    private static final int MAX_ERROR_MESSAGE_LENGTH = 20000;

    private static final List<String> DATA_SHEET_CANDIDATE_NAMES = List.of("PostOffice", "Post Office");

    private static final List<String> EXPECTED_HEADERS = List.of(
            "STT",
            "Tên Bưu cục (*)",
            "Mã bưu cục (*)",
            "Tỉnh/Thành (*)",
            "Phường/Xã (*)",
            "Địa chỉ chi tiết (*)",
            "Số điện thoại",
            "Ngày bắt đầu vận hành (dd/mm/yyyy)",
            "Ngày kết thúc vận hành (dd/mm/yyyy)",
            "Giờ bắt đầu làm việc (hh:mm)",
            "Giờ kết thúc làm việc (hh:mm)",
            "Bán kính phục vụ (m)",
            "Trạng thái"
    );

    private static final List<String> HEADER_KEYS = List.of(
            "stt",
            "name",
            "code",
            "province_code",
            "ward_code",
            "address_detail",
            "phone_number",
            "operational_start_date",
            "operational_end_date",
            "working_start_time",
            "working_end_time",
            "service_radius_m",
            "status"
    );

    private static final Map<String, PostOfficeStatus> STATUS_MAP = Map.of(
            "hoạt động", PostOfficeStatus.ACTIVE,
            "ngừng hoạt động", PostOfficeStatus.INACTIVE,
            "bị đình chỉ", PostOfficeStatus.SUSPENDED,
            "bảo trì", PostOfficeStatus.MAINTENANCE
    );

    private final PostOfficeRepository postOfficeRepository;
    private final ProvinceRepository provinceRepository;
    private final WardRepository wardRepository;
    private final ImportHistoryRepository importHistoryRepository;
    private final MessageService messageService;

    @Qualifier("orderImportTaskExecutor")
    private final Executor orderImportTaskExecutor;

    @Override
    public ValidateImportFileDTO<PostOfficeImportDTO> validateImportFile(MultipartFile file, Long tenantId) {
        ValidateImportFileDTO<PostOfficeImportDTO> response = buildBaseValidateResponse();

        if (file == null || file.isEmpty()) {
            setValidationFailed(response, List.of(message("post.office.import.validation.file.empty")));
            return response;
        }

        try {
            return validateImportFileBytes(file.getBytes(), tenantId);
        } catch (IOException exception) {
            log.error("Validate post office import file failed", exception);
            setValidationFailed(response, List.of(message("post.office.import.validation.file.unreadable")));
            return response;
        }
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public ImportHistoryResponse importPostOfficesAsync(MultipartFile file, Long tenantId) {
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
            log.error("Post office import async execution failed for importHistoryId={}", importHistoryId, exception);
            markImportFailed(importHistoryId, tenantId, resolveExceptionMessage(exception));
            return null;
        });

        return toImportHistoryResponse(savedImportHistory);
    }

    private ValidateImportFileDTO<PostOfficeImportDTO> validateImportFileBytes(byte[] fileBytes, Long tenantId) {
        ValidateImportFileDTO<PostOfficeImportDTO> response = buildBaseValidateResponse();

        try (ByteArrayInputStream inputStream = new ByteArrayInputStream(fileBytes);
             Workbook workbook = WorkbookFactory.create(inputStream)) {

            FormulaEvaluator evaluator = workbook.getCreationHelper().createFormulaEvaluator();
            DataFormatter formatter = new DataFormatter(Locale.forLanguageTag("vi-VN"));

            Sheet postOfficeSheet = resolveDataSheet(workbook, formatter, evaluator);
            if (postOfficeSheet == null) {
                setValidationFailed(response, List.of(message("post.office.import.validation.sheet.post_office.missing")));
                return response;
            }

            List<String> headerErrors = validateHeader(postOfficeSheet, formatter, evaluator);
            if (!headerErrors.isEmpty()) {
                setValidationFailed(response, headerErrors);
                return response;
            }

            MasterDataLookup masterData = loadMasterData();
            Set<String> existingPostOfficeCodes = loadExistingPostOfficeCodes(postOfficeSheet, tenantId, formatter, evaluator);
            ValidationResult validationResult = validateRows(
                    postOfficeSheet,
                    formatter,
                    evaluator,
                    masterData,
                    existingPostOfficeCodes
            );

            response.setData(validationResult.postOffices());
            if (validationResult.postOffices().isEmpty() && validationResult.errors().isEmpty()) {
                setValidationFailed(response, List.of(message("post.office.import.validation.data.empty")));
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
            log.error("Validate post office import file failed", exception);
            setValidationFailed(response, List.of(message("post.office.import.validation.file.unreadable")));
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
        importHistory.setType(ImportType.POST_OFFICE);
        importHistoryRepository.save(importHistory);

        try {
            ValidateImportFileDTO<PostOfficeImportDTO> validationResult = validateImportFileBytes(fileBytes, tenantId);
            List<PostOfficeImportDTO> validPostOffices = validationResult.getData() == null
                    ? List.of()
                    : validationResult.getData();

            importHistory.setTotalRecords(validPostOffices.size());

            if (!validationResult.isSuccess()) {
                importHistory.setStatus(ImportHistoryStatus.FAILED);
                importHistory.setSuccessRecords(0);
                importHistory.setFailedRecords(validPostOffices.size());
                importHistory.setErrorMessage(truncateErrorMessage(validationResult.getErrorMessage()));
                importHistory.setFinishedAt(LocalDateTime.now());
                importHistoryRepository.save(importHistory);
                return;
            }

            ImportExecutionResult executionResult = saveImportedPostOffices(validPostOffices, tenantId);
            importHistory.setSuccessRecords(executionResult.successRecords());
            importHistory.setFailedRecords(executionResult.failedRecords());
            importHistory.setErrorMessage(truncateErrorMessage(executionResult.errorMessage()));
            importHistory.setStatus(
                    executionResult.failedRecords() > 0
                            ? ImportHistoryStatus.PARTIAL_SUCCESS
                            : ImportHistoryStatus.COMPLETED
            );
            importHistory.setFinishedAt(LocalDateTime.now());
            importHistoryRepository.save(importHistory);
        } catch (Exception exception) {
            log.error("Process post office import failed for importHistoryId={}", importHistoryId, exception);
            markImportFailed(importHistoryId, tenantId, resolveExceptionMessage(exception));
        }
    }

    private ImportExecutionResult saveImportedPostOffices(List<PostOfficeImportDTO> postOfficeImports, Long tenantId) {
        if (postOfficeImports == null || postOfficeImports.isEmpty()) {
            return new ImportExecutionResult(0, 0, 0, null);
        }

        int successRecords = 0;
        List<String> errors = new ArrayList<>();

        for (PostOfficeImportDTO postOfficeImport : postOfficeImports) {
            try {
                PostOffice postOffice = mapToPostOfficeEntity(postOfficeImport, tenantId);
                postOfficeRepository.save(postOffice);
                successRecords++;
            } catch (Exception exception) {
                errors.add(buildImportPersistError(postOfficeImport, exception));
            }
        }

        int totalRecords = postOfficeImports.size();
        int failedRecords = totalRecords - successRecords;
        String errorMessage = errors.isEmpty() ? null : String.join("\n", errors);

        return new ImportExecutionResult(totalRecords, successRecords, failedRecords, errorMessage);
    }

    private PostOffice mapToPostOfficeEntity(PostOfficeImportDTO postOfficeImport, Long tenantId) {
        PostOffice postOffice = new PostOffice();
        postOffice.setCode(postOfficeImport.getCode());
        postOffice.setName(postOfficeImport.getName());
        postOffice.setProvinceCode(postOfficeImport.getProvinceCode());
        postOffice.setWardCode(postOfficeImport.getWardCode());
        postOffice.setAddressDetail(postOfficeImport.getAddressDetail());
        postOffice.setPhoneNumber(postOfficeImport.getPhoneNumber());
        postOffice.setOperationalStartDate(postOfficeImport.getOperationalStartDate());
        postOffice.setOperationalEndDate(postOfficeImport.getOperationalEndDate());
        postOffice.setWorkingStartTime(postOfficeImport.getWorkingStartTime());
        postOffice.setWorkingEndTime(postOfficeImport.getWorkingEndTime());
        if (postOfficeImport.getServiceRadiusM() != null) {
            postOffice.setServiceRadiusM(postOfficeImport.getServiceRadiusM());
        }
        if (postOfficeImport.getStatus() != null) {
            postOffice.setStatus(postOfficeImport.getStatus());
        }
        postOffice.setTenantId(tenantId);
        return postOffice;
    }

    private Sheet resolveDataSheet(Workbook workbook, DataFormatter formatter, FormulaEvaluator evaluator) {
        for (String candidateName : DATA_SHEET_CANDIDATE_NAMES) {
            Sheet candidate = workbook.getSheet(candidateName);
            if (candidate != null) {
                return candidate;
            }
        }

        for (int sheetIndex = 0; sheetIndex < workbook.getNumberOfSheets(); sheetIndex++) {
            Sheet candidate = workbook.getSheetAt(sheetIndex);
            if (looksLikeDataSheet(candidate, formatter, evaluator)) {
                return candidate;
            }
        }

        return null;
    }

    private boolean looksLikeDataSheet(Sheet sheet, DataFormatter formatter, FormulaEvaluator evaluator) {
        Row headerRow = sheet.getRow(HEADER_ROW_INDEX);
        if (headerRow == null) {
            return false;
        }

        String actualHeader = normalizeWhitespace(getCellText(headerRow, COLUMN_STT, formatter, evaluator));
        String expectedHeader = normalizeWhitespace(EXPECTED_HEADERS.get(COLUMN_STT));
        return expectedHeader.equalsIgnoreCase(actualHeader);
    }

    private List<String> validateHeader(Sheet postOfficeSheet, DataFormatter formatter, FormulaEvaluator evaluator) {
        List<String> errors = new ArrayList<>();
        Row headerRow = postOfficeSheet.getRow(HEADER_ROW_INDEX);
        if (headerRow == null) {
            errors.add(message("post.office.import.validation.header.row.missing"));
            return errors;
        }

        for (int columnIndex = 0; columnIndex <= LAST_COLUMN_INDEX; columnIndex++) {
            String actualHeader = normalizeWhitespace(getCellText(headerRow, columnIndex, formatter, evaluator));
            String expectedHeader = normalizeWhitespace(EXPECTED_HEADERS.get(columnIndex));
            if (!expectedHeader.equalsIgnoreCase(actualHeader)) {
                errors.add(message(
                        "post.office.import.validation.header.invalid",
                        toColumnName(columnIndex),
                        EXPECTED_HEADERS.get(columnIndex),
                        actualHeader
                ));
            }
        }
        return errors;
    }

    private ValidationResult validateRows(
            Sheet postOfficeSheet,
            DataFormatter formatter,
            FormulaEvaluator evaluator,
            MasterDataLookup masterData,
            Set<String> existingPostOfficeCodes
    ) {
        List<PostOfficeImportDTO> postOffices = new ArrayList<>();
        List<String> errors = new ArrayList<>();
        Set<String> fileCodeSet = new HashSet<>();

        for (int rowIndex = DATA_START_ROW_INDEX; rowIndex <= postOfficeSheet.getLastRowNum(); rowIndex++) {
            Row row = postOfficeSheet.getRow(rowIndex);
            if (isBlankRow(row, formatter, evaluator)) {
                continue;
            }

            int excelRowNumber = rowIndex + 1;
            PostOfficeImportDTO postOffice = PostOfficeImportDTO.builder()
                    .sourceRows(new ArrayList<>())
                    .build();
            postOffice.getSourceRows().add(excelRowNumber);

            postOffice.setName(requireText(row, COLUMN_NAME, excelRowNumber, formatter, evaluator, errors));

            String code = requireText(row, COLUMN_CODE, excelRowNumber, formatter, evaluator, errors);
            postOffice.setCode(code);
            if (hasText(code)) {
                String normalizedCode = normalizeCodeKey(code);
                if (!fileCodeSet.add(normalizedCode)) {
                    errors.add(message(
                            "post.office.import.validation.code.duplicate_in_file",
                            buildCellRef(excelRowNumber, COLUMN_CODE)
                    ));
                }

                if (existingPostOfficeCodes.contains(normalizeLookupKey(code))) {
                    errors.add(message(
                            "post.office.import.validation.code.exists",
                            buildCellRef(excelRowNumber, COLUMN_CODE)
                    ));
                }
            }

            String provinceCode = resolveMasterCode(
                    row,
                    COLUMN_PROVINCE,
                    excelRowNumber,
                    formatter,
                    evaluator,
                    masterData.provinceNameByCode(),
                    message("post.office.import.field.province"),
                    errors
            );
            String wardCode = resolveMasterCode(
                    row,
                    COLUMN_WARD,
                    excelRowNumber,
                    formatter,
                    evaluator,
                    masterData.wardNameByCode(),
                    message("post.office.import.field.ward"),
                    errors
            );
            postOffice.setProvinceCode(provinceCode);
            postOffice.setWardCode(wardCode);
            validateWardProvinceRelation(
                    wardCode,
                    provinceCode,
                    excelRowNumber,
                    COLUMN_WARD,
                    masterData,
                    errors
            );

            postOffice.setAddressDetail(requireText(
                    row,
                    COLUMN_ADDRESS_DETAIL,
                    excelRowNumber,
                    formatter,
                    evaluator,
                    errors
            ));

            String rawPhone = normalizeWhitespace(getCellText(row, COLUMN_PHONE, formatter, evaluator));
            postOffice.setPhoneNumber(validatePhone(rawPhone, excelRowNumber, COLUMN_PHONE, errors));

            postOffice.setOperationalStartDate(parseOptionalDate(
                    row,
                    COLUMN_OPERATIONAL_START_DATE,
                    excelRowNumber,
                    formatter,
                    evaluator,
                    errors
            ));
            postOffice.setOperationalEndDate(parseOptionalDate(
                    row,
                    COLUMN_OPERATIONAL_END_DATE,
                    excelRowNumber,
                    formatter,
                    evaluator,
                    errors
            ));
            postOffice.setWorkingStartTime(parseOptionalTime(
                    row,
                    COLUMN_WORKING_START_TIME,
                    excelRowNumber,
                    formatter,
                    evaluator,
                    errors
            ));
            postOffice.setWorkingEndTime(parseOptionalTime(
                    row,
                    COLUMN_WORKING_END_TIME,
                    excelRowNumber,
                    formatter,
                    evaluator,
                    errors
            ));

            postOffice.setServiceRadiusM(parseOptionalInteger(
                    row,
                    COLUMN_SERVICE_RADIUS_M,
                    excelRowNumber,
                    formatter,
                    evaluator,
                    1,
                    errors
            ));

            postOffice.setStatus(mapOptionalStatus(
                    row,
                    COLUMN_STATUS,
                    excelRowNumber,
                    formatter,
                    evaluator,
                    errors
            ));

            validateOperationalTimeline(postOffice, excelRowNumber, errors);
            postOffices.add(postOffice);
        }

        return new ValidationResult(postOffices, errors);
    }

    private Set<String> loadExistingPostOfficeCodes(
            Sheet postOfficeSheet,
            Long tenantId,
            DataFormatter formatter,
            FormulaEvaluator evaluator
    ) {
        Set<String> normalizedCodes = new HashSet<>();
        for (int rowIndex = DATA_START_ROW_INDEX; rowIndex <= postOfficeSheet.getLastRowNum(); rowIndex++) {
            Row row = postOfficeSheet.getRow(rowIndex);
            if (row == null) {
                continue;
            }

            String code = normalizeWhitespace(getCellText(row, COLUMN_CODE, formatter, evaluator));
            if (hasText(code)) {
                normalizedCodes.add(normalizeLookupKey(code));
            }
        }

        if (normalizedCodes.isEmpty()) {
            return Set.of();
        }

        return postOfficeRepository.findExistingCodesByTenantId(tenantId, normalizedCodes);
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
            errors.add(message("post.office.import.validation.required", buildCellRef(excelRowNumber, columnIndex)));
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
                    "post.office.import.validation.phone.invalid",
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
                    "post.office.import.validation.master_code.not_found",
                    buildCellRef(excelRowNumber, columnIndex),
                    fieldDisplay,
                    codeName.code()
            ));
            return null;
        }

        if (!normalizeWhitespace(expectedName).equalsIgnoreCase(normalizeWhitespace(codeName.name()))) {
            errors.add(message(
                    "post.office.import.validation.master_code.name_mismatch",
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
                    "post.office.import.validation.ward_province.mismatch",
                    buildCellRef(excelRowNumber, wardColumnIndex)
            ));
        }
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
                    // Fallback to text parsing.
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
                    "post.office.import.validation.date.invalid_format",
                    buildCellRef(excelRowNumber, columnIndex)
            ));
            return null;
        }
    }

    private LocalTime parseOptionalTime(
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
                return normalizeTime(cell.getLocalDateTimeCellValue().toLocalTime());
            }
            if (cell.getCellType() == CellType.FORMULA && DateUtil.isCellDateFormatted(cell)) {
                try {
                    return normalizeTime(cell.getLocalDateTimeCellValue().toLocalTime());
                } catch (Exception ignored) {
                    // Fallback to text parsing.
                }
            }
        }

        String rawTime = normalizeWhitespace(getCellText(row, columnIndex, formatter, evaluator));
        if (!hasText(rawTime)) {
            return null;
        }

        LocalTime parsed = parseTime(rawTime);
        if (parsed == null) {
            errors.add(message(
                    "post.office.import.validation.time.invalid_format",
                    buildCellRef(excelRowNumber, columnIndex)
            ));
            return null;
        }
        return parsed;
    }

    private LocalTime parseTime(String rawTime) {
        try {
            return LocalTime.parse(rawTime, IMPORT_TIME_FORMATTER);
        } catch (DateTimeParseException ignored) {
            // Try second-level precision.
        }

        try {
            return LocalTime.parse(rawTime, DateTimeFormatter.ofPattern("H:mm:ss"));
        } catch (DateTimeParseException ignored) {
            return null;
        }
    }

    private LocalTime normalizeTime(LocalTime time) {
        return time == null ? null : time.withSecond(0).withNano(0);
    }

    private Integer parseOptionalInteger(
            Row row,
            int columnIndex,
            int excelRowNumber,
            DataFormatter formatter,
            FormulaEvaluator evaluator,
            int minValue,
            List<String> errors
    ) {
        String rawValue = normalizeWhitespace(getCellText(row, columnIndex, formatter, evaluator));
        if (!hasText(rawValue)) {
            return null;
        }

        Double value = parseNumber(rawValue);
        if (value == null || !isWholeNumber(value)) {
            errors.add(message(
                    "post.office.import.validation.number.invalid_integer",
                    buildCellRef(excelRowNumber, columnIndex)
            ));
            return null;
        }

        int parsed = value.intValue();
        if (parsed < minValue) {
            errors.add(message(
                    "post.office.import.validation.number.min_value",
                    buildCellRef(excelRowNumber, columnIndex),
                    minValue
            ));
            return null;
        }

        return parsed;
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

    private PostOfficeStatus mapOptionalStatus(
            Row row,
            int columnIndex,
            int excelRowNumber,
            DataFormatter formatter,
            FormulaEvaluator evaluator,
            List<String> errors
    ) {
        String rawValue = normalizeWhitespace(getCellText(row, columnIndex, formatter, evaluator));
        if (!hasText(rawValue)) {
            return null;
        }

        PostOfficeStatus mappedStatus = STATUS_MAP.get(normalizeLookupKey(rawValue));
        if (mappedStatus == null) {
            errors.add(message(
                    "post.office.import.validation.value.invalid_option",
                    buildCellRef(excelRowNumber, columnIndex),
                    rawValue,
                    message("post.office.import.allowed.status")
            ));
        }
        return mappedStatus;
    }

    private void validateOperationalTimeline(PostOfficeImportDTO postOffice, int excelRowNumber, List<String> errors) {
        LocalDate startDate = postOffice.getOperationalStartDate();
        LocalDate endDate = postOffice.getOperationalEndDate();
        if (startDate != null && endDate != null && endDate.isBefore(startDate)) {
            errors.add(message("post.office.import.validation.timeline.date.invalid_range", excelRowNumber));
        }

        LocalTime startTime = postOffice.getWorkingStartTime();
        LocalTime endTime = postOffice.getWorkingEndTime();
        if ((startTime == null) != (endTime == null)) {
            errors.add(message("post.office.import.validation.timeline.time.paired", excelRowNumber));
            return;
        }

        if (startTime != null && !endTime.isAfter(startTime)) {
            errors.add(message("post.office.import.validation.timeline.time.invalid_range", excelRowNumber));
        }
    }

    private CodeNameValue parseCodeAndName(String value, int excelRowNumber, int columnIndex, List<String> errors) {
        String normalizedValue = normalizeWhitespace(value);
        Matcher matcher = CODE_NAME_PATTERN.matcher(normalizedValue);
        if (!matcher.matches()) {
            errors.add(message(
                    "post.office.import.validation.code_name.invalid_format",
                    buildCellRef(excelRowNumber, columnIndex)
            ));
            return null;
        }

        String code = matcher.group(1).trim();
        String name = matcher.group(2).trim();
        if (!hasText(code) || !hasText(name)) {
            errors.add(message(
                    "post.office.import.validation.code_name.invalid_format",
                    buildCellRef(excelRowNumber, columnIndex)
            ));
            return null;
        }

        return new CodeNameValue(code, name);
    }

    private MasterDataLookup loadMasterData() {
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

        return new MasterDataLookup(provinceNameByCode, wardNameByCode, wardProvinceByCode);
    }

    private ValidateImportFileDTO<PostOfficeImportDTO> buildBaseValidateResponse() {
        ValidateImportFileDTO<PostOfficeImportDTO> response = new ValidateImportFileDTO<>();
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
            headerMap.put(HEADER_KEYS.get(i), EXPECTED_HEADERS.get(i));
        }
        return headerMap;
    }

    private void setValidationFailed(ValidateImportFileDTO<PostOfficeImportDTO> response, List<String> errors) {
        response.setSuccess(false);
        response.setType(0);
        response.setErrorMessage(String.join("\n", errors));
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
        return message(
                "post.office.import.validation.cell_ref",
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
        return normalizeWhitespace(value).toLowerCase(Locale.ROOT);
    }

    private String normalizeCodeKey(String code) {
        return normalizeWhitespace(code).toUpperCase(Locale.ROOT);
    }

    private boolean hasText(String value) {
        return value != null && !value.trim().isEmpty();
    }

    private String message(String key, Object... args) {
        if (args == null || args.length == 0) {
            return messageService.getMessage(key);
        }
        return messageService.getMessage(key, args);
    }

    private void markImportFailed(Long importHistoryId, Long tenantId, String errorMessage) {
        importHistoryRepository.findByIdAndTenantId(importHistoryId, tenantId).ifPresent(importHistory -> {
            importHistory.setStatus(ImportHistoryStatus.FAILED);
            if (importHistory.getStartedAt() == null) {
                importHistory.setStartedAt(LocalDateTime.now());
            }
            if (importHistory.getTotalRecords() == null) {
                importHistory.setTotalRecords(0);
            }
            if (importHistory.getSuccessRecords() == null) {
                importHistory.setSuccessRecords(0);
            }
            if (importHistory.getFailedRecords() == null) {
                importHistory.setFailedRecords(importHistory.getTotalRecords());
            }
            importHistory.setErrorMessage(truncateErrorMessage(errorMessage));
            importHistory.setFinishedAt(LocalDateTime.now());
            importHistoryRepository.save(importHistory);
        });
    }

    private ImportHistoryResponse toImportHistoryResponse(ImportHistory importHistory) {
        return ImportHistoryResponse.builder()
                .id(importHistory.getId())
                .fileId(importHistory.getFileId())
                .fileName(importHistory.getFileName())
                .status(importHistory.getStatus())
                .totalRecords(importHistory.getTotalRecords())
                .successRecords(importHistory.getSuccessRecords())
                .failedRecords(importHistory.getFailedRecords())
                .errorMessage(importHistory.getErrorMessage())
                .startedAt(importHistory.getStartedAt())
                .finishedAt(importHistory.getFinishedAt())
                .build();
    }

    private String buildImportPersistError(PostOfficeImportDTO postOfficeImport, Exception exception) {
        String postOfficeCode = hasText(postOfficeImport.getCode())
                ? postOfficeImport.getCode()
                : message("post.office.import.import_job.unknown_post_office_code");
        return message(
                "post.office.import.import_job.persist_error",
                postOfficeCode,
                resolveExceptionMessage(exception)
        );
    }

    private String resolveExceptionMessage(Throwable throwable) {
        if (throwable == null) {
            return message(ErrorCode.UNCATEGORIZED_EXCEPTION.getMessageKey());
        }

        Throwable rootCause = throwable;
        while (rootCause.getCause() != null) {
            rootCause = rootCause.getCause();
        }

        if (rootCause instanceof AppException appException) {
            return message(appException.getErrorCode().getMessageKey());
        }

        String rootMessage = rootCause.getMessage();
        if (!hasText(rootMessage)) {
            return message(ErrorCode.UNCATEGORIZED_EXCEPTION.getMessageKey());
        }

        if (rootMessage.startsWith("error.")) {
            return message(rootMessage);
        }
        return rootMessage;
    }

    private String truncateErrorMessage(String errorMessage) {
        if (!hasText(errorMessage)) {
            return null;
        }

        if (errorMessage.length() <= MAX_ERROR_MESSAGE_LENGTH) {
            return errorMessage;
        }

        return errorMessage.substring(0, MAX_ERROR_MESSAGE_LENGTH);
    }

    private record ValidationResult(List<PostOfficeImportDTO> postOffices, List<String> errors) {
    }

    private record CodeNameValue(String code, String name) {
    }

    private record MasterDataLookup(
            Map<String, String> provinceNameByCode,
            Map<String, String> wardNameByCode,
            Map<String, String> wardProvinceByCode
    ) {
    }

        private record ImportExecutionResult(
            int totalRecords,
            int successRecords,
            int failedRecords,
            String errorMessage
        ) {
        }
}
