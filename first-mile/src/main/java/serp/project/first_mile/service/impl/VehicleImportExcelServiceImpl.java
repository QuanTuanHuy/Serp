/*
Author: Nguyen The Anh
Description: Part of Serp Project
*/

package serp.project.first_mile.service.impl;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.apache.poi.ss.usermodel.Cell;
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
import serp.project.first_mile.domain.ImportHistory;
import serp.project.first_mile.domain.PostOffice;
import serp.project.first_mile.domain.PostOfficeStaff;
import serp.project.first_mile.domain.Vehicle;
import serp.project.first_mile.dto.request.VehicleImportDTO;
import serp.project.first_mile.dto.response.ImportHistoryResponse;
import serp.project.first_mile.dto.response.ValidateImportFileDTO;
import serp.project.first_mile.enums.ImportHistoryStatus;
import serp.project.first_mile.enums.ImportType;
import serp.project.first_mile.enums.PostOfficeStaffRole;
import serp.project.first_mile.enums.PostOfficeStaffStatus;
import serp.project.first_mile.enums.VehicleStatus;
import serp.project.first_mile.enums.VehicleType;
import serp.project.first_mile.exception.AppException;
import serp.project.first_mile.exception.ErrorCode;
import serp.project.first_mile.exception.MessageService;
import serp.project.first_mile.kernel.utils.ExcelImportUtils;
import serp.project.first_mile.kernel.utils.ExcelTemplateUtils;
import serp.project.first_mile.kernel.utils.FirstMileAccessUtils;
import serp.project.first_mile.kernel.utils.ImportErrorUtils;
import serp.project.first_mile.kernel.utils.ImportHistoryFailureUtils;
import serp.project.first_mile.kernel.utils.ImportHistoryResponseUtils;
import serp.project.first_mile.repository.ImportHistoryRepository;
import serp.project.first_mile.repository.PostOfficeRepository;
import serp.project.first_mile.repository.PostOfficeStaffAssignmentRepository;
import serp.project.first_mile.repository.PostOfficeStaffRepository;
import serp.project.first_mile.repository.VehicleRepository;
import serp.project.first_mile.repository.projection.CodeNameProjection;
import serp.project.first_mile.service.VehicleImportExcelService;
import serp.project.first_mile.service.dto.import_record.ImportExecutionResult;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.LinkedHashMap;
import java.util.LinkedHashSet;
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

import static serp.project.first_mile.kernel.utils.ExcelImportUtils.*;

@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
@Slf4j
public class VehicleImportExcelServiceImpl implements VehicleImportExcelService {

    private static final String TEMPLATE_PATH = "excel/vehicle_template.xlsx";
    private static final String UNIT_SHEET_NAME = "Unit";
    private static final String VEHICLE_SHEET_NAME = "VEHICLE";

    private static final int START_ROW_INDEX = 1;
    private static final int POST_OFFICE_COLUMN_INDEX = 2;
    private static final int COURIER_COLUMN_INDEX = 3;

    private static final int HEADER_ROW_INDEX = 0;
    private static final int DATA_START_ROW_INDEX = 1;

    private static final int COLUMN_STT = 0;
    private static final int COLUMN_LICENSE_PLATE = 1;
    private static final int COLUMN_MAX_WEIGHT = 2;
    private static final int COLUMN_MAX_VOLUME = 3;
    private static final int COLUMN_POST_OFFICE = 4;
    private static final int COLUMN_COURIER = 5;
    private static final int COLUMN_VEHICLE_TYPE = 6;
    private static final int COLUMN_STATUS = 7;
    private static final int LAST_COLUMN_INDEX = 7;

    private static final List<String> EXPECTED_HEADERS = List.of(
            "STT",
            "Biển số xe (dạng 17B6-72685)",
            "Tải trọng tối đa (kg)",
            "Dung tích tối đa (m3)",
            "Bưu cục",
            "Bưu tá",
            "Loại phương tiện",
            "Trạng thái"
    );

    private static final List<String> HEADER_KEYS = List.of(
            "stt",
            "license_plate",
            "max_weight",
            "max_volume",
            "post_office",
            "courier",
            "vehicle_type",
            "status"
    );

    private static final Pattern CODE_NAME_PATTERN = Pattern.compile("^(.+?)\\s+-\\s+(.+)$");
    private static final int MAX_ERROR_MESSAGE_LENGTH = 20000;

    private static final Map<String, VehicleStatus> STATUS_MAP = Map.of(
            "active", VehicleStatus.ACTIVE,
            "inactive", VehicleStatus.INACTIVE
    );

    private static final Map<String, VehicleType> VEHICLE_TYPE_MAP = Map.of(
            "bike", VehicleType.BIKE,
            "truck", VehicleType.TRUCK
    );

    private final PostOfficeRepository postOfficeRepository;
    private final PostOfficeStaffAssignmentRepository postOfficeStaffAssignmentRepository;
    private final PostOfficeStaffRepository postOfficeStaffRepository;
    private final VehicleRepository vehicleRepository;
    private final ImportHistoryRepository importHistoryRepository;
    private final ImportHistoryFailureUtils importHistoryFailureUtils;
    private final MessageService messageService;
    private final FirstMileAccessUtils firstMileAccessUtils;

    @Qualifier("orderImportTaskExecutor")
    private final Executor orderImportTaskExecutor;

    @Override
    public byte[] exportTemplate() {
        Long tenantId = getCurrentTenantIdOrThrow();
        LocalDate today = LocalDate.now();

        Set<Long> managedPostOfficeIds = null;
        if (isManagerScopedAccess()) {
            managedPostOfficeIds = getManagedPostOfficeIdsOrThrow(tenantId);
        }

        List<CodeNameProjection> postOffices = loadTemplatePostOffices(tenantId, managedPostOfficeIds);
        List<CodeNameProjection> couriers = loadTemplateCouriers(tenantId, managedPostOfficeIds, today);

        try (InputStream inputStream = new ClassPathResource(TEMPLATE_PATH).getInputStream();
             Workbook workbook = new XSSFWorkbook(inputStream);
             ByteArrayOutputStream outputStream = new ByteArrayOutputStream()) {

            Sheet unitSheet = workbook.getSheet(UNIT_SHEET_NAME);
            if (unitSheet == null) {
                throw new AppException(ErrorCode.UNCATEGORIZED_EXCEPTION);
            }

            populateTemplateColumn(unitSheet, postOffices, POST_OFFICE_COLUMN_INDEX);
            populateTemplateColumn(unitSheet, couriers, COURIER_COLUMN_INDEX);

            workbook.write(outputStream);
            return outputStream.toByteArray();
        } catch (IOException exception) {
            throw new AppException(ErrorCode.UNCATEGORIZED_EXCEPTION);
        }
    }

    @Override
    public ValidateImportFileDTO<VehicleImportDTO> validateImportFile(MultipartFile file, Long tenantId) {
        ValidateImportFileDTO<VehicleImportDTO> response = buildBaseValidateResponse();

        if (file == null || file.isEmpty()) {
            setValidationFailed(response, List.of(message("vehicle.import.validation.file.empty")));
            return response;
        }

        try {
            AccessScope accessScope = resolveAccessScope(tenantId);
            return validateImportFileBytes(file.getBytes(), tenantId, accessScope);
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

        AccessScope accessScope = resolveAccessScope(tenantId);

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
                () -> processImportJob(fileBytes, tenantId, importHistoryId, accessScope),
                orderImportTaskExecutor
        ).exceptionally(exception -> {
            log.error("Vehicle import async execution failed for importHistoryId={}", importHistoryId, exception);
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

    private ValidateImportFileDTO<VehicleImportDTO> validateImportFileBytes(
            byte[] fileBytes,
            Long tenantId,
            AccessScope accessScope
    ) {
        ValidateImportFileDTO<VehicleImportDTO> response = buildBaseValidateResponse();

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

            MasterDataLookup masterData = loadMasterData(tenantId, accessScope.managedPostOfficeIds());
            Set<String> existingLicensePlates = loadExistingLicensePlates(vehicleSheet, tenantId, formatter, evaluator);
            ValidationResult validationResult = validateRows(
                    vehicleSheet,
                    formatter,
                    evaluator,
                    masterData,
                    existingLicensePlates,
                    tenantId,
                    accessScope
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
            response.setErrorMessage(null);
            response.setType(1);
            return response;
        } catch (Exception exception) {
            log.error("Validate vehicle import file failed", exception);
            setValidationFailed(response, List.of(message("vehicle.import.validation.file.unreadable")));
            return response;
        }
    }

    private void processImportJob(
            byte[] fileBytes,
            Long tenantId,
            Long importHistoryId,
            AccessScope accessScope
    ) {
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
            ValidateImportFileDTO<VehicleImportDTO> validationResult = validateImportFileBytes(
                    fileBytes,
                    tenantId,
                    accessScope
            );
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
                    ImportErrorUtils.resolveExceptionMessage(exception, key -> message(key)),
                    MAX_ERROR_MESSAGE_LENGTH
            );
        }
    }

    private ImportExecutionResult saveImportedVehicles(List<VehicleImportDTO> vehicleImports, Long tenantId) {
        if (vehicleImports == null || vehicleImports.isEmpty()) {
            return new ImportExecutionResult(0, 0, 0, null);
        }

        Map<Long, PostOffice> postOfficeById = loadPostOfficeById(vehicleImports, tenantId);

        int successRecords = 0;
        List<String> errors = new ArrayList<>();

        for (VehicleImportDTO vehicleImport : vehicleImports) {
            try {
                Vehicle vehicle = mapToVehicleEntity(vehicleImport, tenantId, postOfficeById);
                vehicleRepository.save(vehicle);
                successRecords++;
            } catch (Exception exception) {
                errors.add(buildImportPersistError(vehicleImport, exception));
            }
        }

        int totalRecords = vehicleImports.size();
        int failedRecords = totalRecords - successRecords;
        String errorMessage = errors.isEmpty() ? null : String.join("\n", errors);

        return new ImportExecutionResult(totalRecords, successRecords, failedRecords, errorMessage);
    }

    private Map<Long, PostOffice> loadPostOfficeById(List<VehicleImportDTO> vehicleImports, Long tenantId) {
        Set<Long> postOfficeIds = vehicleImports.stream()
                .filter(Objects::nonNull)
                .map(VehicleImportDTO::getPostOfficeId)
                .filter(Objects::nonNull)
                .collect(Collectors.toSet());

        if (postOfficeIds.isEmpty()) {
            return Map.of();
        }

        return postOfficeRepository.findAllByTenantIdAndIdIn(tenantId, postOfficeIds)
                .stream()
                .collect(Collectors.toMap(PostOffice::getId, value -> value));
    }

    private Vehicle mapToVehicleEntity(
            VehicleImportDTO vehicleImport,
            Long tenantId,
            Map<Long, PostOffice> postOfficeById
    ) {
        PostOffice postOffice = null;
        if (vehicleImport.getPostOfficeId() != null) {
            postOffice = postOfficeById.get(vehicleImport.getPostOfficeId());
            if (postOffice == null) {
                throw new AppException(ErrorCode.POST_OFFICE_NOT_FOUND);
            }
        }

        Vehicle vehicle = new Vehicle();
        vehicle.setLicensePlate(vehicleImport.getLicensePlate());
        vehicle.setMaxWeight(vehicleImport.getMaxWeight());
        vehicle.setMaxVolume(vehicleImport.getMaxVolume());
        vehicle.setPostOffice(postOffice);
        vehicle.setPostOfficeStaffId(vehicleImport.getPostOfficeStaffId());
        vehicle.setVehicleType(vehicleImport.getVehicleType());
        vehicle.setStatus(vehicleImport.getStatus());
        vehicle.setTenantId(tenantId);
        return vehicle;
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
            Set<String> existingLicensePlates,
            Long tenantId,
            AccessScope accessScope
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
                    .sourceRows(new ArrayList<>())
                    .build();
            vehicle.getSourceRows().add(excelRowNumber);

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

            PostOfficeResolution postOffice = resolvePostOffice(
                    row,
                    excelRowNumber,
                    formatter,
                    evaluator,
                    masterData,
                    accessScope,
                    errors
            );
            if (postOffice != null) {
                vehicle.setPostOfficeId(postOffice.postOffice().getId());
                vehicle.setPostOfficeCode(postOffice.postOffice().getCode());
                vehicle.setPostOfficeName(postOffice.postOffice().getName());
            }

            CourierResolution courier = resolveCourier(
                    row,
                    excelRowNumber,
                    formatter,
                    evaluator,
                    masterData,
                    errors
            );
            if (courier != null) {
                vehicle.setPostOfficeStaffId(courier.courier().getId());
                vehicle.setPostOfficeStaffCode(courier.courier().getCode());
                vehicle.setPostOfficeStaffName(courier.courier().getFullName());
            }

            if (postOffice != null && courier != null) {
                validateCourierBelongsToPostOffice(
                        courier.courier().getId(),
                        postOffice.postOffice().getId(),
                        tenantId,
                        excelRowNumber,
                        errors
                );
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

    private PostOfficeResolution resolvePostOffice(
            Row row,
            int excelRowNumber,
            DataFormatter formatter,
            FormulaEvaluator evaluator,
            MasterDataLookup masterData,
            AccessScope accessScope,
            List<String> errors
    ) {
        String rawValue = requireText(row, COLUMN_POST_OFFICE, excelRowNumber, formatter, evaluator, errors);
        if (!hasText(rawValue)) {
            return null;
        }

        CodeNameValue codeName = parseCodeAndName(rawValue, excelRowNumber, COLUMN_POST_OFFICE, errors);
        if (codeName == null) {
            return null;
        }

        PostOffice postOffice = masterData.postOfficeByCode().get(normalizeCodeKey(codeName.code()));
        if (postOffice == null) {
            errors.add(message(
                    "vehicle.import.validation.post_office.not_found",
                    buildCellRef(excelRowNumber, COLUMN_POST_OFFICE),
                    codeName.code()
            ));
            return null;
        }

        if (!normalizeWhitespace(postOffice.getName()).equalsIgnoreCase(normalizeWhitespace(codeName.name()))) {
            errors.add(message(
                    "vehicle.import.validation.post_office.name_mismatch",
                    buildCellRef(excelRowNumber, COLUMN_POST_OFFICE),
                    postOffice.getCode(),
                    postOffice.getName()
            ));
        }

        if (accessScope.managerScoped()
                && (accessScope.managedPostOfficeIds() == null
                || !accessScope.managedPostOfficeIds().contains(postOffice.getId()))) {
            errors.add(message(
                    "vehicle.import.validation.post_office.not_allowed",
                    buildCellRef(excelRowNumber, COLUMN_POST_OFFICE)
            ));
            return null;
        }

        return new PostOfficeResolution(postOffice);
    }

    private CourierResolution resolveCourier(
            Row row,
            int excelRowNumber,
            DataFormatter formatter,
            FormulaEvaluator evaluator,
            MasterDataLookup masterData,
            List<String> errors
    ) {
        String rawValue = requireText(row, COLUMN_COURIER, excelRowNumber, formatter, evaluator, errors);
        if (!hasText(rawValue)) {
            return null;
        }

        CodeNameValue codeName = parseCodeAndName(rawValue, excelRowNumber, COLUMN_COURIER, errors);
        if (codeName == null) {
            return null;
        }

        PostOfficeStaff courier = masterData.courierByCode().get(normalizeCodeKey(codeName.code()));
        if (courier == null) {
            errors.add(message(
                    "vehicle.import.validation.courier.not_found_or_inactive",
                    buildCellRef(excelRowNumber, COLUMN_COURIER),
                    codeName.code()
            ));
            return null;
        }

        if (!normalizeWhitespace(courier.getFullName()).equalsIgnoreCase(normalizeWhitespace(codeName.name()))) {
            errors.add(message(
                    "vehicle.import.validation.courier.name_mismatch",
                    buildCellRef(excelRowNumber, COLUMN_COURIER),
                    courier.getCode(),
                    courier.getFullName()
            ));
        }

        return new CourierResolution(courier);
    }

    private void validateCourierBelongsToPostOffice(
            Long courierId,
            Long postOfficeId,
            Long tenantId,
            int excelRowNumber,
            List<String> errors
    ) {
        boolean belongs = postOfficeStaffAssignmentRepository.existsActiveAssignmentByStaffIdAndPostOfficeIdAndTenantId(
                courierId,
                postOfficeId,
                tenantId,
                LocalDate.now()
        );
        if (!belongs) {
            errors.add(message(
                    "vehicle.import.validation.courier.not_belong_post_office",
                    excelRowNumber
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
                    "vehicle.import.validation.value.invalid_option",
                    buildCellRef(excelRowNumber, columnIndex),
                    rawValue,
                    allowedValues
            ));
        }
        return mappedValue;
    }

    private CodeNameValue parseCodeAndName(String value, int excelRowNumber, int columnIndex, List<String> errors) {
        ExcelImportUtils.CodeNameValue parsedValue = ExcelImportUtils.parseCodeAndName(value, CODE_NAME_PATTERN);
        if (parsedValue == null) {
            errors.add(message(
                    "vehicle.import.validation.code_name.invalid_format",
                    buildCellRef(excelRowNumber, columnIndex)
            ));
            return null;
        }

        return new CodeNameValue(parsedValue.code(), parsedValue.name());
    }

    private MasterDataLookup loadMasterData(Long tenantId, Set<Long> managedPostOfficeIds) {
        List<PostOffice> postOffices = postOfficeRepository.findAllByTenantId(tenantId);
        Map<String, PostOffice> postOfficeByCode = postOffices.stream()
                .collect(Collectors.toMap(
                        value -> normalizeCodeKey(value.getCode()),
                        value -> value,
                        (first, second) -> first,
                        LinkedHashMap::new
                ));

        List<PostOfficeStaff> activeCouriers = postOfficeStaffRepository.findByTenantIdAndRoleAndStatus(
                tenantId,
                PostOfficeStaffRole.COURIER,
                PostOfficeStaffStatus.ACTIVE
        );
        Map<String, PostOfficeStaff> courierByCode = activeCouriers.stream()
                .collect(Collectors.toMap(
                        value -> normalizeCodeKey(value.getCode()),
                        value -> value,
                        (first, second) -> first,
                        HashMap::new
                ));

        return new MasterDataLookup(postOfficeByCode, courierByCode, managedPostOfficeIds);
    }

    private ValidateImportFileDTO<VehicleImportDTO> buildBaseValidateResponse() {
        return ExcelImportUtils.buildBaseValidateResponse(HEADER_KEYS, EXPECTED_HEADERS);
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

    private String message(String key, Object... args) {
        if (args == null || args.length == 0) {
            return messageService.getMessage(key);
        }
        return messageService.getMessage(key, args);
    }

    private String buildImportPersistError(VehicleImportDTO vehicleImport, Exception exception) {
        String licensePlate = hasText(vehicleImport.getLicensePlate())
                ? vehicleImport.getLicensePlate()
                : message("vehicle.import.import_job.unknown_license_plate");
        return message(
                "vehicle.import.import_job.persist_error",
                licensePlate,
                ImportErrorUtils.resolveExceptionMessage(exception, key -> message(key))
        );
    }

    private List<CodeNameProjection> loadTemplatePostOffices(Long tenantId, Set<Long> managedPostOfficeIds) {
        if (managedPostOfficeIds == null) {
            return postOfficeRepository.findTemplateCodeNameListByTenantId(tenantId);
        }

        if (managedPostOfficeIds.isEmpty()) {
            return List.of();
        }

        return postOfficeRepository.findTemplateCodeNameListByTenantIdAndIds(tenantId, managedPostOfficeIds);
    }

    private List<CodeNameProjection> loadTemplateCouriers(
            Long tenantId,
            Set<Long> managedPostOfficeIds,
            LocalDate today
    ) {
        if (managedPostOfficeIds == null) {
            return postOfficeStaffAssignmentRepository.findActiveCourierTemplateCodeNameListByTenantId(
                    tenantId,
                    today,
                    PostOfficeStaffRole.COURIER,
                    PostOfficeStaffStatus.ACTIVE
            );
        }

        if (managedPostOfficeIds.isEmpty()) {
            return List.of();
        }

        return postOfficeStaffAssignmentRepository.findActiveCourierTemplateCodeNameListByTenantIdAndPostOfficeIds(
                tenantId,
                managedPostOfficeIds,
                today,
                PostOfficeStaffRole.COURIER,
                PostOfficeStaffStatus.ACTIVE
        );
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

    private AccessScope resolveAccessScope(Long tenantId) {
        if (isManagerScopedAccess()) {
            return new AccessScope(true, getManagedPostOfficeIdsOrThrow(tenantId));
        }
        return new AccessScope(false, null);
    }

    private Set<Long> getManagedPostOfficeIdsOrThrow(Long tenantId) {
        return firstMileAccessUtils.getManagedPostOfficeIdsOrThrow(tenantId);
    }

    private Long getCurrentTenantIdOrThrow() {
        return firstMileAccessUtils.getCurrentTenantIdOrThrow();
    }

    private boolean isManagerScopedAccess() {
        return firstMileAccessUtils.isManagerScopedAccess();
    }

    private record ValidationResult(List<VehicleImportDTO> vehicles, List<String> errors) {
    }

    private record CodeNameValue(String code, String name) {
    }

    private record PostOfficeResolution(PostOffice postOffice) {
    }

    private record CourierResolution(PostOfficeStaff courier) {
    }

    private record MasterDataLookup(
            Map<String, PostOffice> postOfficeByCode,
            Map<String, PostOfficeStaff> courierByCode,
            Set<Long> managedPostOfficeIds
    ) {
    }

    private record AccessScope(boolean managerScoped, Set<Long> managedPostOfficeIds) {
    }
}
