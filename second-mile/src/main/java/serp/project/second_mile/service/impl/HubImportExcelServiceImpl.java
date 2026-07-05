/*
Author: Nguyen The Anh
Description: Part of Serp Project
*/

package serp.project.second_mile.service.impl;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.apache.poi.ss.usermodel.DataFormatter;
import org.apache.poi.ss.usermodel.FormulaEvaluator;
import org.apache.poi.ss.usermodel.Row;
import org.apache.poi.ss.usermodel.Sheet;
import org.apache.poi.ss.usermodel.Workbook;
import org.apache.poi.ss.usermodel.WorkbookFactory;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.multipart.MultipartFile;
import serp.project.second_mile.domain.Hub;
import serp.project.second_mile.domain.ImportHistory;
import serp.project.second_mile.domain.Ward;
import serp.project.second_mile.dto.request.HubImportDTO;
import serp.project.second_mile.dto.response.ImportHistoryResponse;
import serp.project.second_mile.dto.response.ValidateImportFileDTO;
import serp.project.second_mile.enums.HubStatus;
import serp.project.second_mile.enums.ImportHistoryStatus;
import serp.project.second_mile.enums.ImportType;
import serp.project.second_mile.exception.AppException;
import serp.project.second_mile.exception.ErrorCode;
import serp.project.second_mile.exception.MessageService;
import serp.project.second_mile.kernel.utils.ExcelImportUtils;
import serp.project.second_mile.kernel.utils.ImportErrorUtils;
import serp.project.second_mile.kernel.utils.ImportHistoryFailureUtils;
import serp.project.second_mile.kernel.utils.ImportHistoryResponseUtils;
import serp.project.second_mile.repository.HubRepository;
import serp.project.second_mile.repository.ImportHistoryRepository;
import serp.project.second_mile.repository.ProvinceRepository;
import serp.project.second_mile.repository.WardRepository;
import serp.project.second_mile.repository.projection.CodeNameProjection;
import serp.project.second_mile.service.HubImportExcelService;
import serp.project.second_mile.service.dto.ImportExecutionResult;

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
import java.util.regex.Pattern;
import java.util.stream.Collectors;

import static serp.project.second_mile.kernel.utils.ExcelImportUtils.getCellText;
import static serp.project.second_mile.kernel.utils.ExcelImportUtils.hasText;
import static serp.project.second_mile.kernel.utils.ExcelImportUtils.isBlankRow;
import static serp.project.second_mile.kernel.utils.ExcelImportUtils.normalizeCodeKey;
import static serp.project.second_mile.kernel.utils.ExcelImportUtils.normalizeWhitespace;
import static serp.project.second_mile.kernel.utils.ExcelImportUtils.parseCodeAndName;
import static serp.project.second_mile.kernel.utils.ExcelImportUtils.setValidationFailed;
import static serp.project.second_mile.kernel.utils.ExcelImportUtils.toColumnName;

@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
@Slf4j
public class HubImportExcelServiceImpl implements HubImportExcelService {
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
    private static final int COLUMN_STATUS = 11;
    private static final int LAST_COLUMN_INDEX = 11;

    private static final String DATA_SHEET_NAME = "Hub";
    private static final LocalDate TIME_ANCHOR_DATE = LocalDate.of(1970, 1, 1);
    private static final DateTimeFormatter IMPORT_DATE_FORMATTER = DateTimeFormatter.ofPattern("d/M/uuuu");
    private static final DateTimeFormatter IMPORT_TIME_FORMATTER = DateTimeFormatter.ofPattern("H:mm");
    private static final Pattern PHONE_PATTERN = Pattern.compile("^0\\d{9,10}$");
    private static final Pattern CODE_NAME_PATTERN = Pattern.compile("^(.+?)\\s+-\\s+(.+)$");
    private static final int MAX_ERROR_MESSAGE_LENGTH = 20000;

    private static final List<String> EXPECTED_HEADERS = List.of(
            "STT",
            "Tên Hub (*)",
            "Mã Hub (*)",
            "Tỉnh/Thành (*)",
            "Phường/Xã (*)",
            "Địa chỉ chi tiết (*)",
            "Số điện thoại",
            "Ngày bắt đầu vận hành (dd/mm/yyyy)",
            "Ngày kết thúc vận hành (dd/mm/yyyy)",
            "Giờ bắt đầu làm việc (hh:mm)",
            "Giờ kết thúc làm việc (hh:mm)",
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
            "status"
    );

    private static final Map<String, HubStatus> STATUS_MAP = Map.of(
            "hoạt động", HubStatus.ACTIVE,
            "active", HubStatus.ACTIVE,
            "ngừng hoạt động", HubStatus.INACTIVE,
            "inactive", HubStatus.INACTIVE
    );

    private final HubRepository hubRepository;
    private final ProvinceRepository provinceRepository;
    private final WardRepository wardRepository;
    private final ImportHistoryRepository importHistoryRepository;
    private final ImportHistoryFailureUtils importHistoryFailureUtils;
    private final MessageService messageService;

    @Qualifier("hubImportTaskExecutor")
    private final Executor hubImportTaskExecutor;

    @Override
    public ValidateImportFileDTO<HubImportDTO> validateImportFile(MultipartFile file, Long tenantId) {
        ValidateImportFileDTO<HubImportDTO> response = buildBaseValidateResponse();

        if (file == null || file.isEmpty()) {
            setValidationFailed(response, List.of(message("hub.import.validation.file.empty")));
            return response;
        }

        try {
            return validateImportFileBytes(file.getBytes(), tenantId);
        } catch (IOException exception) {
            log.error("Validate hub import file failed", exception);
            setValidationFailed(response, List.of(message("hub.import.validation.file.unreadable")));
            return response;
        }
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public ImportHistoryResponse importHubsAsync(MultipartFile file, Long tenantId) {
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
                hubImportTaskExecutor
        ).exceptionally(exception -> {
            log.error("Hub import async execution failed for importHistoryId={}", importHistoryId, exception);
            importHistoryFailureUtils.markImportFailed(
                    importHistoryId,
                    tenantId,
                    ImportErrorUtils.resolveExceptionMessage(exception, this::message),
                    MAX_ERROR_MESSAGE_LENGTH
            );
            return null;
        });

        return ImportHistoryResponseUtils.toResponse(savedImportHistory);
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
        importHistory.setType(ImportType.HUB);
        importHistoryRepository.save(importHistory);

        try {
            ValidateImportFileDTO<HubImportDTO> validationResult = validateImportFileBytes(fileBytes, tenantId);
            List<HubImportDTO> validHubs = validationResult.getData() == null
                    ? List.of()
                    : validationResult.getData();

            importHistory.setTotalRecords(validHubs.size());

            if (!validationResult.isSuccess()) {
                importHistory.setStatus(ImportHistoryStatus.FAILED);
                importHistory.setSuccessRecords(0);
                importHistory.setFailedRecords(validHubs.size());
                importHistory.setErrorMessage(
                        ImportErrorUtils.truncateErrorMessage(validationResult.getErrorMessage(), MAX_ERROR_MESSAGE_LENGTH)
                );
                importHistory.setFinishedAt(LocalDateTime.now());
                importHistoryRepository.save(importHistory);
                return;
            }

            ImportExecutionResult executionResult = saveImportedHubs(validHubs, tenantId);
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
            log.error("Process hub import failed for importHistoryId={}", importHistoryId, exception);
            importHistoryFailureUtils.markImportFailed(
                    importHistoryId,
                    tenantId,
                    ImportErrorUtils.resolveExceptionMessage(exception, this::message),
                    MAX_ERROR_MESSAGE_LENGTH
            );
        }
    }

    private ImportExecutionResult saveImportedHubs(List<HubImportDTO> hubImports, Long tenantId) {
        if (hubImports == null || hubImports.isEmpty()) {
            return new ImportExecutionResult(0, 0, 0, null);
        }

        int successRecords = 0;
        List<String> errors = new ArrayList<>();

        for (HubImportDTO hubImport : hubImports) {
            try {
                Hub hub = mapToHubEntity(hubImport, tenantId);
                hubRepository.save(hub);
                successRecords++;
            } catch (Exception exception) {
                errors.add(buildImportPersistError(hubImport, exception));
            }
        }

        int failedRecords = hubImports.size() - successRecords;
        String errorMessage = errors.isEmpty() ? null : String.join("\n", errors);

        return new ImportExecutionResult(hubImports.size(), successRecords, failedRecords, errorMessage);
    }

    private String buildImportPersistError(HubImportDTO hubImport, Exception exception) {
        String hubCode = hasText(hubImport.getCode())
                ? hubImport.getCode()
                : message("hub.import.import_job.unknown_hub_code");
        return message(
                "hub.import.import_job.persist_error",
                hubCode,
                ImportErrorUtils.resolveExceptionMessage(exception, this::message)
        );
    }

    private ValidateImportFileDTO<HubImportDTO> validateImportFileBytes(byte[] fileBytes, Long tenantId) {
        ValidateImportFileDTO<HubImportDTO> response = buildBaseValidateResponse();

        try (ByteArrayInputStream inputStream = new ByteArrayInputStream(fileBytes);
             Workbook workbook = WorkbookFactory.create(inputStream)) {

            FormulaEvaluator evaluator = workbook.getCreationHelper().createFormulaEvaluator();
            DataFormatter formatter = new DataFormatter(Locale.forLanguageTag("vi-VN"));

            Sheet hubSheet = workbook.getSheet(DATA_SHEET_NAME);
            if (hubSheet == null) {
                setValidationFailed(response, List.of(message("hub.import.validation.sheet.hub.missing")));
                return response;
            }

            List<String> headerErrors = validateHeader(hubSheet, formatter, evaluator);
            if (!headerErrors.isEmpty()) {
                setValidationFailed(response, headerErrors);
                return response;
            }

            MasterDataLookup masterData = loadMasterData();
            ValidationResult validationResult = validateRows(hubSheet, formatter, evaluator, masterData, tenantId);
            response.setData(validationResult.hubs());

            if (validationResult.hubs().isEmpty() && validationResult.errors().isEmpty()) {
                setValidationFailed(response, List.of(message("hub.import.validation.data.empty")));
                return response;
            }

            if (!validationResult.errors().isEmpty()) {
                setValidationFailed(response, validationResult.errors());
                return response;
            }

            response.setSuccess(true);
            response.setType(1);
            response.setErrorMessage(null);
            return response;
        } catch (Exception exception) {
            log.error("Validate hub import file failed", exception);
            setValidationFailed(response, List.of(message("hub.import.validation.file.unreadable")));
            return response;
        }
    }

    private List<String> validateHeader(Sheet hubSheet, DataFormatter formatter, FormulaEvaluator evaluator) {
        List<String> errors = new ArrayList<>();
        Row headerRow = hubSheet.getRow(HEADER_ROW_INDEX);
        if (headerRow == null) {
            errors.add(message("hub.import.validation.header.row.missing"));
            return errors;
        }

        for (int columnIndex = 0; columnIndex < EXPECTED_HEADERS.size(); columnIndex++) {
            String actual = normalizeWhitespace(getCellText(headerRow, columnIndex, formatter, evaluator));
            String expected = EXPECTED_HEADERS.get(columnIndex);
            if (!expected.equals(actual)) {
                errors.add(message(
                        "hub.import.validation.header.invalid",
                        toColumnName(columnIndex),
                        expected,
                        actual
                ));
            }
        }

        return errors;
    }

    private ValidationResult validateRows(
            Sheet hubSheet,
            DataFormatter formatter,
            FormulaEvaluator evaluator,
            MasterDataLookup masterData,
            Long tenantId
    ) {
        List<HubImportDTO> hubs = new ArrayList<>();
        List<String> errors = new ArrayList<>();
        Set<String> seenCodes = new HashSet<>();

        for (int rowIndex = DATA_START_ROW_INDEX; rowIndex <= hubSheet.getLastRowNum(); rowIndex++) {
            Row row = hubSheet.getRow(rowIndex);
            if (isBlankRow(row, LAST_COLUMN_INDEX, formatter, evaluator)) {
                continue;
            }

            int excelRowNumber = rowIndex + 1;
            int errorCountBeforeRow = errors.size();
            String stt = normalizeWhitespace(getCellText(row, COLUMN_STT, formatter, evaluator));
            String name = requireText(row, COLUMN_NAME, formatter, evaluator, excelRowNumber, errors);
            String code = requireText(row, COLUMN_CODE, formatter, evaluator, excelRowNumber, errors);
            String addressDetail = requireText(row, COLUMN_ADDRESS_DETAIL, formatter, evaluator, excelRowNumber, errors);
            String phoneNumber = normalizeWhitespace(getCellText(row, COLUMN_PHONE, formatter, evaluator));

            validatePhone(phoneNumber, excelRowNumber, errors);

            if (hasText(code)) {
                String normalizedCode = normalizeCodeKey(code);
                if (!seenCodes.add(normalizedCode)) {
                    errors.add(message(
                            "hub.import.validation.code.duplicate_in_file",
                            buildCellRef(excelRowNumber, COLUMN_CODE)
                    ));
                } else if (hubRepository.existsByCode(code)) {
                    errors.add(message(
                            "hub.import.validation.code.exists",
                            buildCellRef(excelRowNumber, COLUMN_CODE)
                    ));
                }
            }

            String provinceCode = resolveMasterCode(
                    row,
                    COLUMN_PROVINCE,
                    formatter,
                    evaluator,
                    excelRowNumber,
                    masterData.provinceNameByCode(),
                    message("hub.import.field.province"),
                    errors
            );
            String wardCode = resolveMasterCode(
                    row,
                    COLUMN_WARD,
                    formatter,
                    evaluator,
                    excelRowNumber,
                    masterData.wardNameByCode(),
                    message("hub.import.field.ward"),
                    errors
            );

            validateWardProvince(wardCode, provinceCode, excelRowNumber, errors, masterData.wardProvinceByCode());

            LocalDate operationalStartDate = resolveDate(
                    row,
                    COLUMN_OPERATIONAL_START_DATE,
                    formatter,
                    evaluator,
                    excelRowNumber,
                    errors
            );
            LocalDate operationalEndDate = resolveDate(
                    row,
                    COLUMN_OPERATIONAL_END_DATE,
                    formatter,
                    evaluator,
                    excelRowNumber,
                    errors
            );
            LocalTime workingStartTime = resolveTime(
                    row,
                    COLUMN_WORKING_START_TIME,
                    formatter,
                    evaluator,
                    excelRowNumber,
                    errors
            );
            LocalTime workingEndTime = resolveTime(
                    row,
                    COLUMN_WORKING_END_TIME,
                    formatter,
                    evaluator,
                    excelRowNumber,
                    errors
            );
            HubStatus status = resolveStatus(
                    row,
                    COLUMN_STATUS,
                    formatter,
                    evaluator,
                    excelRowNumber,
                    errors
            );

            validateTimelines(operationalStartDate, operationalEndDate, workingStartTime, workingEndTime, excelRowNumber, errors);

            if (errors.size() > errorCountBeforeRow) {
                continue;
            }

            hubs.add(HubImportDTO.builder()
                    .stt(stt)
                    .name(name)
                    .code(code)
                    .provinceCode(provinceCode)
                    .wardCode(wardCode)
                    .addressDetail(addressDetail)
                    .phoneNumber(hasText(phoneNumber) ? phoneNumber : null)
                    .operationalStartDate(operationalStartDate)
                    .operationalEndDate(operationalEndDate)
                    .workingStartTime(workingStartTime)
                    .workingEndTime(workingEndTime)
                    .status(status == null ? HubStatus.ACTIVE : status)
                    .build());
        }

        return new ValidationResult(hubs, errors);
    }

    private String requireText(
            Row row,
            int columnIndex,
            DataFormatter formatter,
            FormulaEvaluator evaluator,
            int excelRowNumber,
            List<String> errors
    ) {
        String value = normalizeWhitespace(getCellText(row, columnIndex, formatter, evaluator));
        if (!hasText(value)) {
            errors.add(message("hub.import.validation.required", buildCellRef(excelRowNumber, columnIndex)));
            return null;
        }
        return value;
    }

    private void validatePhone(String phoneNumber, int excelRowNumber, List<String> errors) {
        if (hasText(phoneNumber) && !PHONE_PATTERN.matcher(phoneNumber).matches()) {
            errors.add(message(
                    "hub.import.validation.phone.invalid",
                    buildCellRef(excelRowNumber, COLUMN_PHONE)
            ));
        }
    }

    private String resolveMasterCode(
            Row row,
            int columnIndex,
            DataFormatter formatter,
            FormulaEvaluator evaluator,
            int excelRowNumber,
            Map<String, String> masterNameByCode,
            String fieldLabel,
            List<String> errors
    ) {
        String rawValue = normalizeWhitespace(getCellText(row, columnIndex, formatter, evaluator));
        if (!hasText(rawValue)) {
            errors.add(message("hub.import.validation.required", buildCellRef(excelRowNumber, columnIndex)));
            return null;
        }

        ExcelImportUtils.CodeNameValue parsed = parseCodeAndName(rawValue, CODE_NAME_PATTERN);
        if (parsed == null) {
            errors.add(message(
                    "hub.import.validation.code_name.invalid_format",
                    buildCellRef(excelRowNumber, columnIndex)
            ));
            return null;
        }

        String code = parsed.code();
        String expectedName = masterNameByCode.get(normalizeCodeKey(code));
        if (!hasText(expectedName)) {
            errors.add(message(
                    "hub.import.validation.master_code.not_found",
                    buildCellRef(excelRowNumber, columnIndex),
                    fieldLabel,
                    code
            ));
            return null;
        }

        if (!normalizeWhitespace(parsed.name()).equalsIgnoreCase(normalizeWhitespace(expectedName))) {
            errors.add(message(
                    "hub.import.validation.master_code.name_mismatch",
                    buildCellRef(excelRowNumber, columnIndex),
                    fieldLabel,
                    code,
                    expectedName
            ));
            return null;
        }

        return code;
    }

    private void validateWardProvince(
            String wardCode,
            String provinceCode,
            int excelRowNumber,
            List<String> errors,
            Map<String, String> wardProvinceByCode
    ) {
        if (!hasText(wardCode) || !hasText(provinceCode)) {
            return;
        }

        String expectedProvinceCode = wardProvinceByCode.get(normalizeCodeKey(wardCode));
        if (expectedProvinceCode == null) {
            return;
        }

        if (!normalizeCodeKey(provinceCode).equals(normalizeCodeKey(expectedProvinceCode))) {
            errors.add(message(
                    "hub.import.validation.ward_province.mismatch",
                    buildCellRef(excelRowNumber, COLUMN_WARD)
            ));
        }
    }

    private HubStatus resolveStatus(
            Row row,
            int columnIndex,
            DataFormatter formatter,
            FormulaEvaluator evaluator,
            int excelRowNumber,
            List<String> errors
    ) {
        String rawValue = normalizeWhitespace(getCellText(row, columnIndex, formatter, evaluator));
        if (!hasText(rawValue)) {
            return HubStatus.ACTIVE;
        }

        HubStatus status = STATUS_MAP.get(rawValue.toLowerCase(Locale.ROOT));
        if (status == null) {
            errors.add(message(
                    "hub.import.validation.value.invalid_option",
                    buildCellRef(excelRowNumber, columnIndex),
                    rawValue,
                    message("hub.import.allowed.status")
            ));
        }
        return status;
    }

    private LocalDate resolveDate(
            Row row,
            int columnIndex,
            DataFormatter formatter,
            FormulaEvaluator evaluator,
            int excelRowNumber,
            List<String> errors
    ) {
        String rawValue = normalizeWhitespace(getCellText(row, columnIndex, formatter, evaluator));
        if (!hasText(rawValue)) {
            return null;
        }

        try {
            return LocalDate.parse(rawValue, IMPORT_DATE_FORMATTER);
        } catch (DateTimeParseException exception) {
            errors.add(message(
                    "hub.import.validation.date.invalid_format",
                    buildCellRef(excelRowNumber, columnIndex)
            ));
            return null;
        }
    }

    private LocalTime resolveTime(
            Row row,
            int columnIndex,
            DataFormatter formatter,
            FormulaEvaluator evaluator,
            int excelRowNumber,
            List<String> errors
    ) {
        String rawValue = normalizeWhitespace(getCellText(row, columnIndex, formatter, evaluator));
        if (!hasText(rawValue)) {
            return null;
        }

        try {
            return LocalTime.parse(rawValue, IMPORT_TIME_FORMATTER);
        } catch (DateTimeParseException exception) {
            errors.add(message(
                    "hub.import.validation.time.invalid_format",
                    buildCellRef(excelRowNumber, columnIndex)
            ));
            return null;
        }
    }

    private void validateTimelines(
            LocalDate operationalStartDate,
            LocalDate operationalEndDate,
            LocalTime workingStartTime,
            LocalTime workingEndTime,
            int excelRowNumber,
            List<String> errors
    ) {
        if (operationalStartDate != null && operationalEndDate != null
                && operationalEndDate.isBefore(operationalStartDate)) {
            errors.add(message("hub.import.validation.timeline.date.invalid_range", excelRowNumber));
        }

        if ((workingStartTime == null) != (workingEndTime == null)) {
            errors.add(message("hub.import.validation.timeline.time.paired", excelRowNumber));
            return;
        }

        if (workingStartTime != null && !workingEndTime.isAfter(workingStartTime)) {
            errors.add(message("hub.import.validation.timeline.time.invalid_range", excelRowNumber));
        }
    }

    private MasterDataLookup loadMasterData() {
        Map<String, String> provinceNameByCode = provinceRepository.findTemplateCodeNameList().stream()
                .collect(Collectors.toMap(
                        projection -> normalizeCodeKey(projection.getCode()),
                        CodeNameProjection::getName,
                        (left, right) -> left,
                        LinkedHashMap::new
                ));

        Map<String, String> wardNameByCode = wardRepository.findTemplateCodeNameList().stream()
                .collect(Collectors.toMap(
                        projection -> normalizeCodeKey(projection.getCode()),
                        CodeNameProjection::getName,
                        (left, right) -> left,
                        LinkedHashMap::new
                ));

        Map<String, String> wardProvinceByCode = wardRepository.findAll().stream()
                .collect(Collectors.toMap(
                        ward -> normalizeCodeKey(ward.getWardCode()),
                        Ward::getProvinceCode,
                        (left, right) -> left,
                        HashMap::new
                ));

        return new MasterDataLookup(provinceNameByCode, wardNameByCode, wardProvinceByCode);
    }

    private Hub mapToHubEntity(HubImportDTO hubImportDTO, Long tenantId) {
        Hub hub = new Hub();
        hub.setName(hubImportDTO.getName());
        hub.setCode(hubImportDTO.getCode());
        hub.setProvinceCode(hubImportDTO.getProvinceCode());
        hub.setWardCode(hubImportDTO.getWardCode());
        hub.setAddressDetail(hubImportDTO.getAddressDetail());
        hub.setPhoneNumber(hubImportDTO.getPhoneNumber());
        hub.setOperationalStartDate(hubImportDTO.getOperationalStartDate());
        hub.setOperationalEndDate(hubImportDTO.getOperationalEndDate());
        hub.setWorkingStartTime(toAnchoredDateTime(hubImportDTO.getWorkingStartTime()));
        hub.setWorkingEndTime(toAnchoredDateTime(hubImportDTO.getWorkingEndTime()));
        hub.setStatus(hubImportDTO.getStatus() == null ? HubStatus.ACTIVE : hubImportDTO.getStatus());
        hub.setTenantId(tenantId);
        return hub;
    }

    private LocalDateTime toAnchoredDateTime(LocalTime localTime) {
        return localTime == null ? null : LocalDateTime.of(TIME_ANCHOR_DATE, localTime);
    }

    private ValidateImportFileDTO<HubImportDTO> buildBaseValidateResponse() {
        return ExcelImportUtils.buildBaseValidateResponse(HEADER_KEYS, EXPECTED_HEADERS);
    }

    private String buildCellRef(int excelRowNumber, int columnIndex) {
        return message(
                "hub.import.validation.cell_ref",
                excelRowNumber,
                toColumnName(columnIndex),
                EXPECTED_HEADERS.get(columnIndex)
        );
    }

    private String message(String key, Object... args) {
        return messageService.getMessage(key, args);
    }

    private record ValidationResult(List<HubImportDTO> hubs, List<String> errors) {
    }

    private record MasterDataLookup(
            Map<String, String> provinceNameByCode,
            Map<String, String> wardNameByCode,
            Map<String, String> wardProvinceByCode
    ) {
    }
}
