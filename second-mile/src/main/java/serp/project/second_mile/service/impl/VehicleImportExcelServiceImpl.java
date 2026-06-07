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
import org.apache.poi.xssf.usermodel.XSSFWorkbook;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.core.io.ClassPathResource;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.multipart.MultipartFile;
import serp.project.second_mile.domain.Hub;
import serp.project.second_mile.domain.HubStaff;
import serp.project.second_mile.domain.ImportHistory;
import serp.project.second_mile.domain.Vehicle;
import serp.project.second_mile.dto.request.VehicleImportDTO;
import serp.project.second_mile.dto.response.ImportHistoryResponse;
import serp.project.second_mile.dto.response.ValidateImportFileDTO;
import serp.project.second_mile.enums.HubStaffRole;
import serp.project.second_mile.enums.HubStaffStatus;
import serp.project.second_mile.enums.ImportHistoryStatus;
import serp.project.second_mile.enums.ImportType;
import serp.project.second_mile.enums.VehicleStatus;
import serp.project.second_mile.enums.VehicleType;
import serp.project.second_mile.exception.AppException;
import serp.project.second_mile.exception.ErrorCode;
import serp.project.second_mile.exception.MessageService;
import serp.project.second_mile.kernel.utils.ExcelImportUtils;
import serp.project.second_mile.kernel.utils.ExcelTemplateUtils;
import serp.project.second_mile.kernel.utils.ImportErrorUtils;
import serp.project.second_mile.kernel.utils.ImportHistoryFailureUtils;
import serp.project.second_mile.kernel.utils.ImportHistoryResponseUtils;
import serp.project.second_mile.kernel.utils.SecondMileAccessUtils;
import serp.project.second_mile.repository.HubRepository;
import serp.project.second_mile.repository.HubStaffRepository;
import serp.project.second_mile.repository.ImportHistoryRepository;
import serp.project.second_mile.repository.VehicleRepository;
import serp.project.second_mile.repository.projection.CodeNameProjection;
import serp.project.second_mile.service.VehicleImportExcelService;
import serp.project.second_mile.service.dto.ImportExecutionResult;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.LinkedHashMap;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.Set;
import java.util.UUID;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.Executor;
import java.util.regex.Pattern;
import java.util.stream.Collectors;

import static serp.project.second_mile.kernel.utils.ExcelImportUtils.buildBaseValidateResponse;
import static serp.project.second_mile.kernel.utils.ExcelImportUtils.getCellText;
import static serp.project.second_mile.kernel.utils.ExcelImportUtils.hasText;
import static serp.project.second_mile.kernel.utils.ExcelImportUtils.isBlankRow;
import static serp.project.second_mile.kernel.utils.ExcelImportUtils.normalizeCodeKey;
import static serp.project.second_mile.kernel.utils.ExcelImportUtils.normalizeWhitespace;
import static serp.project.second_mile.kernel.utils.ExcelImportUtils.parseCodeAndName;
import static serp.project.second_mile.kernel.utils.ExcelImportUtils.parseNumber;
import static serp.project.second_mile.kernel.utils.ExcelImportUtils.setValidationFailed;
import static serp.project.second_mile.kernel.utils.ExcelImportUtils.toColumnName;

@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
@Slf4j
public class VehicleImportExcelServiceImpl implements VehicleImportExcelService {

    private static final String TEMPLATE_PATH = "excel/vehicle_template.xlsx";
    private static final String UNIT_SHEET_NAME = "Unit";
    private static final String VEHICLE_SHEET_NAME = "VEHICLE";

    private static final int START_ROW_INDEX = 1;
    private static final int HUB_COLUMN_INDEX = 2;
    private static final int DRIVER_COLUMN_INDEX = 3;

    private static final int HEADER_ROW_INDEX = 0;
    private static final int DATA_START_ROW_INDEX = 1;

    private static final int COLUMN_LICENSE_PLATE = 1;
    private static final int COLUMN_MAX_BAGS = 2;
    private static final int COLUMN_MAX_WEIGHT = 3;
    private static final int COLUMN_MAX_VOLUME = 4;
    private static final int COLUMN_HUB = 5;
    private static final int COLUMN_DRIVER = 6;
    private static final int COLUMN_VEHICLE_TYPE = 7;
    private static final int COLUMN_STATUS = 8;
    private static final int LAST_COLUMN_INDEX = 8;

    private static final List<String> EXPECTED_HEADERS = List.of(
            "STT",
            "Biển số xe (dạng 17B6-72685)",
            "Số lượng bags tối đa",
            "Tải trọng tối đa (kg)",
            "Dung tích tối đa (m3)",
            "Hub",
            "Lái xe",
            "Loại phương tiện",
            "Trạng thái"
    );

    private static final List<String> HEADER_KEYS = List.of(
            "stt",
            "license_plate",
            "max_bags",
            "max_weight",
            "max_volume",
            "hub",
            "driver",
            "vehicle_type",
            "status"
    );

    private static final Pattern CODE_NAME_PATTERN = Pattern.compile("^(.+?)\\s+-\\s+(.+)$");
    private static final int MAX_ERROR_MESSAGE_LENGTH = 20000;

    private static final Map<String, VehicleStatus> STATUS_MAP = Map.of(
            "active", VehicleStatus.ACTIVE,
            "inactive", VehicleStatus.INACTIVE,
            "maintenance", VehicleStatus.MAINTENANCE
    );

    private static final Map<String, VehicleType> VEHICLE_TYPE_MAP = Map.of(
            "truck", VehicleType.TRUCK,
            "van", VehicleType.VAN
    );

    private final HubRepository hubRepository;
    private final HubStaffRepository hubStaffRepository;
    private final VehicleRepository vehicleRepository;
    private final ImportHistoryRepository importHistoryRepository;
    private final ImportHistoryFailureUtils importHistoryFailureUtils;
    private final MessageService messageService;
    private final SecondMileAccessUtils secondMileAccessUtils;

    @Qualifier("vehicleImportTaskExecutor")
    private final Executor vehicleImportTaskExecutor;

    @Override
    public byte[] exportTemplate() {
        Long tenantId = secondMileAccessUtils.getCurrentTenantIdOrThrow();

        List<CodeNameProjection> hubs = hubRepository.findTemplateCodeNameListByTenantId(tenantId);
        List<CodeNameProjection> drivers = hubStaffRepository.findTemplateCodeNameListByTenantIdAndRoleAndStatus(
                tenantId,
                HubStaffRole.DRIVER,
                HubStaffStatus.ACTIVE
        );

        try (InputStream inputStream = new ClassPathResource(TEMPLATE_PATH).getInputStream();
             Workbook workbook = new XSSFWorkbook(inputStream);
             ByteArrayOutputStream outputStream = new ByteArrayOutputStream()) {

            Sheet unitSheet = workbook.getSheet(UNIT_SHEET_NAME);
            if (unitSheet == null) {
                throw new AppException(ErrorCode.UNCATEGORIZED_EXCEPTION);
            }

            populateTemplateColumn(unitSheet, hubs, HUB_COLUMN_INDEX);
            populateTemplateColumn(unitSheet, drivers, DRIVER_COLUMN_INDEX);

            workbook.write(outputStream);
            return outputStream.toByteArray();
        } catch (IOException exception) {
            throw new AppException(ErrorCode.UNCATEGORIZED_EXCEPTION);
        }
    }

    @Override
    public ValidateImportFileDTO<VehicleImportDTO> validateImportFile(MultipartFile file, Long tenantId) {
        ValidateImportFileDTO<VehicleImportDTO> response = buildBaseValidateResponse(HEADER_KEYS, EXPECTED_HEADERS);

        if (file == null || file.isEmpty()) {
            setValidationFailed(response, List.of(message("vehicle.import.validation.file.empty")));
            return response;
        }

        try {
            return validateImportFileBytes(file.getBytes(), tenantId);
        } catch (IOException exception) {
            log.error("Validate vehicle import file failed", exception);
            setValidationFailed(response, List.of(message("vehicle.import.validation.file.unreadable")));
            return response;
        }
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public ImportHistoryResponse importVehiclesAsync(MultipartFile file, Long tenantId) {
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
                vehicleImportTaskExecutor
        ).exceptionally(exception -> {
            log.error("Vehicle import async execution failed for importHistoryId={}", importHistoryId, exception);
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
        importHistory.setType(ImportType.VEHICLE);
        importHistoryRepository.save(importHistory);

        try {
            ValidateImportFileDTO<VehicleImportDTO> validationResult = validateImportFileBytes(fileBytes, tenantId);
            List<VehicleImportDTO> validVehicles = validationResult.getData() == null
                    ? List.of()
                    : validationResult.getData();

            importHistory.setTotalRecords(validVehicles.size());

            if (!validationResult.isSuccess()) {
                importHistory.setStatus(ImportHistoryStatus.FAILED);
                importHistory.setSuccessRecords(0);
                importHistory.setFailedRecords(validVehicles.size());
                importHistory.setErrorMessage(
                        ImportErrorUtils.truncateErrorMessage(validationResult.getErrorMessage(), MAX_ERROR_MESSAGE_LENGTH)
                );
                importHistory.setFinishedAt(LocalDateTime.now());
                importHistoryRepository.save(importHistory);
                return;
            }

            ImportExecutionResult executionResult = saveImportedVehicles(validVehicles, tenantId);
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
            log.error("Process vehicle import failed for importHistoryId={}", importHistoryId, exception);
            importHistoryFailureUtils.markImportFailed(
                    importHistoryId,
                    tenantId,
                    ImportErrorUtils.resolveExceptionMessage(exception, this::message),
                    MAX_ERROR_MESSAGE_LENGTH
            );
        }
    }

    private ImportExecutionResult saveImportedVehicles(List<VehicleImportDTO> vehicleImports, Long tenantId) {
        if (vehicleImports == null || vehicleImports.isEmpty()) {
            return new ImportExecutionResult(0, 0, 0, null);
        }

        int successRecords = 0;
        List<String> errors = new ArrayList<>();

        for (VehicleImportDTO vehicleImport : vehicleImports) {
            try {
                Vehicle vehicle = mapToVehicleEntity(vehicleImport, tenantId);
                vehicleRepository.save(vehicle);
                successRecords++;
            } catch (Exception exception) {
                errors.add(buildImportPersistError(vehicleImport, exception));
            }
        }

        int failedRecords = vehicleImports.size() - successRecords;
        String errorMessage = errors.isEmpty() ? null : String.join("\n", errors);
        return new ImportExecutionResult(vehicleImports.size(), successRecords, failedRecords, errorMessage);
    }

    private Vehicle mapToVehicleEntity(VehicleImportDTO vehicleImport, Long tenantId) {
        Vehicle vehicle = new Vehicle();
        vehicle.setLicensePlate(vehicleImport.getLicensePlate());
        vehicle.setMaxBags(vehicleImport.getMaxBags() == null ? 0 : vehicleImport.getMaxBags());
        vehicle.setMaxWeight(vehicleImport.getMaxWeight() == null ? 0 : vehicleImport.getMaxWeight());
        vehicle.setMaxVolume(vehicleImport.getMaxVolume() == null ? 0 : vehicleImport.getMaxVolume());
        vehicle.setHubId(vehicleImport.getHubId());
        vehicle.setAssignedStaffId(vehicleImport.getAssignedStaffId());
        vehicle.setVehicleType(vehicleImport.getVehicleType());
        vehicle.setStatus(vehicleImport.getStatus() == null ? VehicleStatus.ACTIVE : vehicleImport.getStatus());
        vehicle.setTenantId(tenantId);
        return vehicle;
    }

    private ValidateImportFileDTO<VehicleImportDTO> validateImportFileBytes(byte[] fileBytes, Long tenantId) {
        ValidateImportFileDTO<VehicleImportDTO> response = buildBaseValidateResponse(HEADER_KEYS, EXPECTED_HEADERS);

        try (ByteArrayInputStream inputStream = new ByteArrayInputStream(fileBytes);
             Workbook workbook = WorkbookFactory.create(inputStream)) {

            FormulaEvaluator evaluator = workbook.getCreationHelper().createFormulaEvaluator();
            DataFormatter formatter = new DataFormatter(Locale.forLanguageTag("vi-VN"));

            Sheet vehicleSheet = resolveVehicleSheet(workbook);
            if (vehicleSheet == null) {
                setValidationFailed(response, List.of(message("vehicle.import.validation.sheet.vehicle.missing")));
                return response;
            }

            List<String> headerErrors = validateHeader(vehicleSheet, formatter, evaluator);
            if (!headerErrors.isEmpty()) {
                setValidationFailed(response, headerErrors);
                return response;
            }

            MasterDataLookup masterData = loadMasterData(tenantId);
            Set<String> existingLicensePlates = loadExistingLicensePlates(vehicleSheet, tenantId, formatter, evaluator);
            ValidationResult validationResult = validateRows(
                    vehicleSheet,
                    formatter,
                    evaluator,
                    masterData,
                    existingLicensePlates
            );

            response.setData(validationResult.vehicles());
            if (validationResult.vehicles().isEmpty() && validationResult.errors().isEmpty()) {
                setValidationFailed(response, List.of(message("vehicle.import.validation.data.empty")));
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
            log.error("Validate vehicle import file failed", exception);
            setValidationFailed(response, List.of(message("vehicle.import.validation.file.unreadable")));
            return response;
        }
    }

    private Sheet resolveVehicleSheet(Workbook workbook) {
        Sheet vehicleSheet = workbook.getSheet(VEHICLE_SHEET_NAME);
        if (vehicleSheet != null) {
            return vehicleSheet;
        }

        for (int sheetIndex = 0; sheetIndex < workbook.getNumberOfSheets(); sheetIndex++) {
            Sheet candidate = workbook.getSheetAt(sheetIndex);
            if (VEHICLE_SHEET_NAME.equalsIgnoreCase(candidate.getSheetName())) {
                return candidate;
            }
        }

        return null;
    }

    private List<String> validateHeader(Sheet vehicleSheet, DataFormatter formatter, FormulaEvaluator evaluator) {
        List<String> errors = new ArrayList<>();
        Row headerRow = vehicleSheet.getRow(HEADER_ROW_INDEX);
        if (headerRow == null) {
            errors.add(message("vehicle.import.validation.header.row.missing"));
            return errors;
        }

        for (int columnIndex = 0; columnIndex <= LAST_COLUMN_INDEX; columnIndex++) {
            String actualHeader = normalizeWhitespace(getCellText(headerRow, columnIndex, formatter, evaluator));
            String expectedHeader = normalizeWhitespace(EXPECTED_HEADERS.get(columnIndex));
            if (!expectedHeader.equalsIgnoreCase(actualHeader)) {
                errors.add(message(
                        "vehicle.import.validation.header.invalid",
                        toColumnName(columnIndex),
                        EXPECTED_HEADERS.get(columnIndex),
                        actualHeader
                ));
            }
        }
        return errors;
    }

    private ValidationResult validateRows(
            Sheet vehicleSheet,
            DataFormatter formatter,
            FormulaEvaluator evaluator,
            MasterDataLookup masterData,
            Set<String> existingLicensePlates
    ) {
        List<VehicleImportDTO> vehicles = new ArrayList<>();
        List<String> errors = new ArrayList<>();
        Set<String> fileLicensePlates = new HashSet<>();

        for (int rowIndex = DATA_START_ROW_INDEX; rowIndex <= vehicleSheet.getLastRowNum(); rowIndex++) {
            Row row = vehicleSheet.getRow(rowIndex);
            if (isBlankRow(row, LAST_COLUMN_INDEX, formatter, evaluator)) {
                continue;
            }

            int excelRowNumber = rowIndex + 1;
            VehicleImportDTO vehicle = VehicleImportDTO.builder()
                    .sourceRows(new ArrayList<>(List.of(excelRowNumber)))
                    .build();

            String licensePlate = requireText(
                    row,
                    COLUMN_LICENSE_PLATE,
                    excelRowNumber,
                    formatter,
                    evaluator,
                    errors
            );
            if (hasText(licensePlate)) {
                String normalizedKey = normalizeLookupKey(licensePlate);
                if (!fileLicensePlates.add(normalizedKey)) {
                    errors.add(message(
                            "vehicle.import.validation.license_plate.duplicate_in_file",
                            buildCellRef(excelRowNumber, COLUMN_LICENSE_PLATE)
                    ));
                }

                if (existingLicensePlates.contains(normalizedKey)) {
                    errors.add(message(
                            "vehicle.import.validation.license_plate.exists",
                            buildCellRef(excelRowNumber, COLUMN_LICENSE_PLATE)
                    ));
                }
            }
            vehicle.setLicensePlate(normalizeLicensePlate(licensePlate));

            vehicle.setMaxBags(parseRequiredInteger(
                    row,
                    COLUMN_MAX_BAGS,
                    excelRowNumber,
                    formatter,
                    evaluator,
                    errors
            ));

            vehicle.setMaxWeight(parseRequiredDouble(
                    row,
                    COLUMN_MAX_WEIGHT,
                    excelRowNumber,
                    formatter,
                    evaluator,
                    errors
            ));

            vehicle.setMaxVolume(parseRequiredDouble(
                    row,
                    COLUMN_MAX_VOLUME,
                    excelRowNumber,
                    formatter,
                    evaluator,
                    errors
            ));

            HubResolution hub = resolveHub(
                    row,
                    excelRowNumber,
                    formatter,
                    evaluator,
                    masterData,
                    errors
            );
            if (hub != null) {
                vehicle.setHubId(hub.hub().getId());
                vehicle.setHubCode(hub.hub().getCode());
                vehicle.setHubName(hub.hub().getName());
            }

            DriverResolution driver = resolveDriver(
                    row,
                    excelRowNumber,
                    formatter,
                    evaluator,
                    masterData,
                    errors
            );
            if (driver != null) {
                vehicle.setAssignedStaffId(driver.driver().getId());
                vehicle.setDriverCode(driver.driver().getCode());
                vehicle.setDriverName(driver.driver().getFullName());
            }

            vehicle.setVehicleType(mapRequiredValue(
                    row,
                    COLUMN_VEHICLE_TYPE,
                    excelRowNumber,
                    formatter,
                    evaluator,
                    VEHICLE_TYPE_MAP,
                    message("vehicle.import.allowed.vehicle_type"),
                    errors
            ));

            vehicle.setStatus(mapRequiredValue(
                    row,
                    COLUMN_STATUS,
                    excelRowNumber,
                    formatter,
                    evaluator,
                    STATUS_MAP,
                    message("vehicle.import.allowed.status"),
                    errors
            ));

            vehicles.add(vehicle);
        }

        return new ValidationResult(vehicles, errors);
    }

    private Set<String> loadExistingLicensePlates(
            Sheet vehicleSheet,
            Long tenantId,
            DataFormatter formatter,
            FormulaEvaluator evaluator
    ) {
        Set<String> normalizedLicensePlates = new LinkedHashSet<>();
        for (int rowIndex = DATA_START_ROW_INDEX; rowIndex <= vehicleSheet.getLastRowNum(); rowIndex++) {
            Row row = vehicleSheet.getRow(rowIndex);
            if (row == null) {
                continue;
            }

            String licensePlate = normalizeWhitespace(getCellText(row, COLUMN_LICENSE_PLATE, formatter, evaluator));
            if (hasText(licensePlate)) {
                normalizedLicensePlates.add(normalizeLookupKey(licensePlate));
            }
        }

        if (normalizedLicensePlates.isEmpty()) {
            return Set.of();
        }

        return vehicleRepository.findExistingLicensePlatesByTenantId(tenantId, normalizedLicensePlates);
    }

    private MasterDataLookup loadMasterData(Long tenantId) {
        Map<String, Hub> hubByCode = hubRepository.findAllByTenantId(tenantId).stream()
                .collect(Collectors.toMap(
                        hub -> normalizeCodeKey(hub.getCode()),
                        hub -> hub,
                        (first, second) -> first,
                        LinkedHashMap::new
                ));

        Map<String, HubStaff> driverByCode = hubStaffRepository
                .findByTenantIdAndRoleAndStatus(tenantId, HubStaffRole.DRIVER, HubStaffStatus.ACTIVE)
                .stream()
                .collect(Collectors.toMap(
                        driver -> normalizeCodeKey(driver.getCode()),
                        driver -> driver,
                        (first, second) -> first,
                        HashMap::new
                ));

        return new MasterDataLookup(hubByCode, driverByCode);
    }

    private HubResolution resolveHub(
            Row row,
            int excelRowNumber,
            DataFormatter formatter,
            FormulaEvaluator evaluator,
            MasterDataLookup masterData,
            List<String> errors
    ) {
        String rawValue = requireText(row, COLUMN_HUB, excelRowNumber, formatter, evaluator, errors);
        if (!hasText(rawValue)) {
            return null;
        }

        ExcelImportUtils.CodeNameValue codeName = parseCodeAndName(rawValue, CODE_NAME_PATTERN);
        if (codeName == null) {
            errors.add(message(
                    "vehicle.import.validation.code_name.invalid_format",
                    buildCellRef(excelRowNumber, COLUMN_HUB)
            ));
            return null;
        }

        Hub hub = masterData.hubByCode().get(normalizeCodeKey(codeName.code()));
        if (hub == null) {
            errors.add(message(
                    "vehicle.import.validation.hub.not_found",
                    buildCellRef(excelRowNumber, COLUMN_HUB),
                    codeName.code()
            ));
            return null;
        }

        if (!normalizeWhitespace(hub.getName()).equalsIgnoreCase(normalizeWhitespace(codeName.name()))) {
            errors.add(message(
                    "vehicle.import.validation.hub.name_mismatch",
                    buildCellRef(excelRowNumber, COLUMN_HUB),
                    hub.getCode(),
                    hub.getName()
            ));
        }

        return new HubResolution(hub);
    }

    private DriverResolution resolveDriver(
            Row row,
            int excelRowNumber,
            DataFormatter formatter,
            FormulaEvaluator evaluator,
            MasterDataLookup masterData,
            List<String> errors
    ) {
        String rawValue = requireText(row, COLUMN_DRIVER, excelRowNumber, formatter, evaluator, errors);
        if (!hasText(rawValue)) {
            return null;
        }

        ExcelImportUtils.CodeNameValue codeName = parseCodeAndName(rawValue, CODE_NAME_PATTERN);
        if (codeName == null) {
            errors.add(message(
                    "vehicle.import.validation.code_name.invalid_format",
                    buildCellRef(excelRowNumber, COLUMN_DRIVER)
            ));
            return null;
        }

        HubStaff driver = masterData.driverByCode().get(normalizeCodeKey(codeName.code()));
        if (driver == null) {
            errors.add(message(
                    "vehicle.import.validation.driver.not_found_or_inactive",
                    buildCellRef(excelRowNumber, COLUMN_DRIVER),
                    codeName.code()
            ));
            return null;
        }

        if (!normalizeWhitespace(driver.getFullName()).equalsIgnoreCase(normalizeWhitespace(codeName.name()))) {
            errors.add(message(
                    "vehicle.import.validation.driver.name_mismatch",
                    buildCellRef(excelRowNumber, COLUMN_DRIVER),
                    driver.getCode(),
                    driver.getFullName()
            ));
        }

        return new DriverResolution(driver);
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
            errors.add(message("vehicle.import.validation.required", buildCellRef(excelRowNumber, columnIndex)));
            return null;
        }
        return value;
    }

    private Double parseRequiredDouble(
            Row row,
            int columnIndex,
            int excelRowNumber,
            DataFormatter formatter,
            FormulaEvaluator evaluator,
            List<String> errors
    ) {
        String rawValue = requireText(row, columnIndex, excelRowNumber, formatter, evaluator, errors);
        if (!hasText(rawValue)) {
            return null;
        }

        Double value = parseNumber(rawValue);
        if (value == null) {
            errors.add(message(
                    "vehicle.import.validation.number.invalid",
                    buildCellRef(excelRowNumber, columnIndex)
            ));
            return null;
        }

        if (value <= 0) {
            errors.add(message(
                    "vehicle.import.validation.number.must_be_positive",
                    buildCellRef(excelRowNumber, columnIndex)
            ));
            return null;
        }

        return value;
    }

    private Integer parseRequiredInteger(
            Row row,
            int columnIndex,
            int excelRowNumber,
            DataFormatter formatter,
            FormulaEvaluator evaluator,
            List<String> errors
    ) {
        String rawValue = requireText(row, columnIndex, excelRowNumber, formatter, evaluator, errors);
        if (!hasText(rawValue)) {
            return null;
        }

        Double value = parseNumber(rawValue);
        if (value == null || value % 1 != 0) {
            errors.add(message(
                    "vehicle.import.validation.number.invalid",
                    buildCellRef(excelRowNumber, columnIndex)
            ));
            return null;
        }

        int intValue = value.intValue();
        if (intValue < 0) {
            errors.add(message(
                    "vehicle.import.validation.number.must_not_be_negative",
                    buildCellRef(excelRowNumber, columnIndex)
            ));
            return null;
        }

        return intValue;
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
                    "vehicle.import.validation.value.invalid_option",
                    buildCellRef(excelRowNumber, columnIndex),
                    rawValue,
                    allowedValues
            ));
        }
        return mappedValue;
    }

    private void populateTemplateColumn(Sheet sheet, List<CodeNameProjection> values, int columnIndex) {
        for (int i = 0; i < values.size(); i++) {
            CodeNameProjection value = values.get(i);
            ExcelTemplateUtils.setTextCellValue(
                    sheet,
                    START_ROW_INDEX + i,
                    columnIndex,
                    ExcelTemplateUtils.formatCodeAndName(value.getCode(), value.getName())
            );
        }
    }

    private String buildCellRef(int excelRowNumber, int columnIndex) {
        return message(
                "vehicle.import.validation.cell_ref",
                excelRowNumber,
                toColumnName(columnIndex),
                EXPECTED_HEADERS.get(columnIndex)
        );
    }

    private String normalizeLookupKey(String value) {
        return normalizeWhitespace(value).toLowerCase(Locale.ROOT);
    }

    private String normalizeLicensePlate(String value) {
        if (!hasText(value)) {
            return null;
        }
        return normalizeWhitespace(value).toUpperCase(Locale.ROOT);
    }

    private String buildImportPersistError(VehicleImportDTO vehicleImport, Exception exception) {
        String licensePlate = hasText(vehicleImport.getLicensePlate())
                ? vehicleImport.getLicensePlate()
                : message("vehicle.import.import_job.unknown_license_plate");
        return message(
                "vehicle.import.import_job.persist_error",
                licensePlate,
                ImportErrorUtils.resolveExceptionMessage(exception, this::message)
        );
    }

    private String message(String key, Object... args) {
        return messageService.getMessage(key, args);
    }

    private record ValidationResult(List<VehicleImportDTO> vehicles, List<String> errors) {
    }

    private record MasterDataLookup(Map<String, Hub> hubByCode, Map<String, HubStaff> driverByCode) {
    }

    private record HubResolution(Hub hub) {
    }

    private record DriverResolution(HubStaff driver) {
    }
}
