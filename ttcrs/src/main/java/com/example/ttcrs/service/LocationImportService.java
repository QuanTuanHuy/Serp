package com.example.ttcrs.service;

import com.example.ttcrs.constant.LocationType;
import com.example.ttcrs.dto.response.LocationImportError;
import com.example.ttcrs.dto.response.LocationImportResult;
import com.example.ttcrs.dto.response.LocationResponseDTO;
import com.example.ttcrs.entity.LocationEntity;
import com.example.ttcrs.repository.LocationRepository;
import com.example.ttcrs.util.AuthUtils;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.apache.poi.ss.usermodel.*;
import org.apache.poi.ss.util.CellRangeAddressList;
import org.apache.poi.xssf.usermodel.XSSFDataValidationHelper;
import org.apache.poi.xssf.usermodel.XSSFSheet;
import org.apache.poi.xssf.usermodel.XSSFWorkbook;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.multipart.MultipartFile;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.util.*;

@Slf4j
@Service
@RequiredArgsConstructor
public class LocationImportService {

    private static final Set<String> VALID_TYPES = Set.of(
            "PORT", "WAREHOUSE", "DEPOT_CONTAINER", "DEPOT_TRUCK", "DEPOT_TRAILER"
    );

    private final LocationRepository locationRepository;
    private final AuthUtils authUtils;

    /**
     * Parse Excel file and import locations for the current tenant.
     *
     * <p>For each row (after the header):
     * <ol>
     *   <li>If any required field is missing → error for that row.</li>
     *   <li>If type is invalid → error.</li>
     *   <li>If lat/lng are not valid doubles or out of range → error.</li>
     *   <li>If locationCode already exists in DB → error.</li>
     *   <li>Otherwise → create the location.</li>
     * </ol>
     * Duplicate codes within the same file are also rejected.
     *
     * @param file the uploaded Excel file (.xlsx or .xls)
     * @return import result with created locations and per-row errors
     */
    @Transactional
    public LocationImportResult importLocations(MultipartFile file) throws IOException {
        Long tenantId = authUtils.getCurrentTenantId()
                .orElseThrow(() -> new IllegalStateException(
                        "Không thể xác định tenant từ token."));

        List<LocationImportError> errors = new ArrayList<>();
        List<LocationEntity> toCreate = new ArrayList<>();
        Set<String> seenCodes = new HashSet<>();

        try (Workbook workbook = WorkbookFactory.create(file.getInputStream())) {
            Sheet sheet = workbook.getSheetAt(0);
            int lastRow = sheet.getLastRowNum();

            for (int i = 1; i <= lastRow; i++) {
                Row row = sheet.getRow(i);
                if (row == null || isRowEmpty(row)) continue;

                int rowNum = i; // 1-based for display (header is row 1)

                String code = getCellString(row, 0);
                String typeStr = getCellString(row, 1);
                String latStr = getCellString(row, 2);
                String lngStr = getCellString(row, 3);

                // Check missing fields
                boolean hasError = false;
                if (code == null || code.isBlank()) {
                    errors.add(error(rowNum, "Location Code", "không được để trống"));
                    hasError = true;
                }
                if (typeStr == null || typeStr.isBlank()) {
                    errors.add(error(rowNum, "Type", "không được để trống"));
                    hasError = true;
                }
                if (latStr == null || latStr.isBlank()) {
                    errors.add(error(rowNum, "Latitude", "không được để trống"));
                    hasError = true;
                }
                if (lngStr == null || lngStr.isBlank()) {
                    errors.add(error(rowNum, "Longitude", "không được để trống"));
                    hasError = true;
                }
                if (hasError) continue;

                // Normalize code
                code = code.trim().toUpperCase();

                // Type validation (handles "Depot - Container", "Depot-Container", "DEPOT_CONTAINER", etc.)
                String typeUpper = typeStr.trim().toUpperCase()
                        .replace(" - ", "_")
                        .replace("-", "_")
                        .replace(" ", "_");
                if (!VALID_TYPES.contains(typeUpper)) {
                    errors.add(error(rowNum, "Type", "giá trị không hợp lệ '" + typeStr.trim()
                            + "'. Dùng: Port, Warehouse, Depot - Container, Depot - Trailer, Depot - Truck"));
                    continue;
                }
                LocationType type = LocationType.valueOf(typeUpper);

                // Lat / Lng validation
                Double lat = parseCoord(latStr, "Latitude", rowNum, errors);
                Double lng = parseCoord(lngStr, "Longitude", rowNum, errors);
                if (lat == null || lng == null) continue;

                if (lat < -90 || lat > 90) {
                    errors.add(error(rowNum, "Latitude", "phải nằm trong khoảng [-90, 90]"));
                    continue;
                }
                if (lng < -180 || lng > 180) {
                    errors.add(error(rowNum, "Longitude", "phải nằm trong khoảng [-180, 180]"));
                    continue;
                }

                // Duplicate within file
                if (!seenCodes.add(code)) {
                    errors.add(error(rowNum, "Location Code", "mã '" + code + "' bị trùng trong file"));
                    continue;
                }

                // Duplicate in DB
                if (locationRepository.existsByLocationCode(code)) {
                    errors.add(error(rowNum, "Location Code", "mã '" + code + "' đã tồn tại"));
                    continue;
                }

                toCreate.add(LocationEntity.builder()
                        .tenantId(tenantId)
                        .locationCode(code)
                        .type(type)
                        .lat(lat)
                        .lng(lng)
                        .build());
            }
        }

        int totalRows = errors.size() + toCreate.size();
        if (toCreate.isEmpty()) {
            return LocationImportResult.allErrors(totalRows, errors);
        }

        List<LocationEntity> saved = locationRepository.saveAll(toCreate);
        List<LocationResponseDTO> created = saved.stream()
                .map(LocationResponseDTO::fromEntity)
                .toList();

        if (errors.isEmpty()) {
            return LocationImportResult.success(created, totalRows);
        }
        return LocationImportResult.partial(totalRows, created, errors);
    }

    /**
     * Generate an Excel template with headers and a dropdown on the Type column.
     */
    public byte[] generateTemplate() throws IOException {
        try (XSSFWorkbook workbook = new XSSFWorkbook()) {
            Sheet sheet = workbook.createSheet("Locations");

            // Header style
            CellStyle headerStyle = workbook.createCellStyle();
            Font headerFont = workbook.createFont();
            headerFont.setBold(true);
            headerFont.setFontHeightInPoints((short) 11);
            headerStyle.setFont(headerFont);
            headerStyle.setFillForegroundColor(IndexedColors.GREY_25_PERCENT.getIndex());
            headerStyle.setFillPattern(FillPatternType.SOLID_FOREGROUND);
            headerStyle.setBorderBottom(BorderStyle.THIN);
            headerStyle.setAlignment(HorizontalAlignment.CENTER);

            // Header row
            Row header = sheet.createRow(0);
            String[] headers = {"Location Code", "Type", "Latitude", "Longitude"};
            for (int i = 0; i < headers.length; i++) {
                Cell cell = header.createCell(i);
                cell.setCellValue(headers[i]);
                cell.setCellStyle(headerStyle);
            }

            // Column widths
            sheet.setColumnWidth(0, 20 * 256);
            sheet.setColumnWidth(1, 22 * 256);
            sheet.setColumnWidth(2, 14 * 256);
            sheet.setColumnWidth(3, 14 * 256);

            // Data validation dropdown for Type column (B2:B1000)
            String[] typeOptions = {"Port", "Warehouse", "Depot - Container", "Depot - Trailer", "Depot - Truck"};
            CellRangeAddressList addressList = new CellRangeAddressList(1, 999, 1, 1);
            XSSFDataValidationHelper dvHelper = new XSSFDataValidationHelper((XSSFSheet) sheet);
            DataValidationConstraint dvConstraint =
                    dvHelper.createExplicitListConstraint(typeOptions);
            DataValidation validation = dvHelper.createValidation(dvConstraint, addressList);
            validation.setSuppressDropDownArrow(true);
            validation.setShowErrorBox(true);
            validation.createErrorBox("Invalid Type", "Please select a valid type from the dropdown.");
            sheet.addValidationData(validation);

            // Sample data row
            Row sample = sheet.createRow(1);
            sample.createCell(0).setCellValue("HN-PORT");
            sample.createCell(1).setCellValue("Port");
            sample.createCell(2).setCellValue(21.0285);
            sample.createCell(3).setCellValue(105.8542);

            ByteArrayOutputStream bos = new ByteArrayOutputStream();
            workbook.write(bos);
            return bos.toByteArray();
        }
    }

    // -------------------------------------------------------------------------
    // Helpers
    // -------------------------------------------------------------------------

    private String getCellString(Row row, int colIdx) {
        Cell cell = row.getCell(colIdx);
        if (cell == null) return null;
        return switch (cell.getCellType()) {
            case STRING -> cell.getStringCellValue();
            case NUMERIC -> {
                double val = cell.getNumericCellValue();
                if (val == Math.floor(val) && !Double.isInfinite(val)) {
                    yield String.valueOf((long) val);
                }
                yield String.valueOf(val);
            }
            case BOOLEAN -> String.valueOf(cell.getBooleanCellValue());
            case FORMULA -> {
                try {
                    yield cell.getStringCellValue();
                } catch (Exception e) {
                    yield String.valueOf(cell.getNumericCellValue());
                }
            }
            default -> null;
        };
    }

    private boolean isRowEmpty(Row row) {
        for (int i = 0; i < 4; i++) {
            Cell cell = row.getCell(i);
            if (cell != null && cell.getCellType() != CellType.BLANK
                    && getCellString(row, i) != null && !getCellString(row, i).isBlank()) {
                return false;
            }
        }
        return true;
    }

    private Double parseCoord(String raw, String field, int rowNum, List<LocationImportError> errors) {
        try {
            return Double.parseDouble(raw.trim());
        } catch (NumberFormatException e) {
            errors.add(error(rowNum, field, "giá trị '" + raw.trim() + "' không phải số hợp lệ"));
            return null;
        }
    }

    private LocationImportError error(int row, String field, String message) {
        return LocationImportError.builder()
                .row(row)
                .field(field)
                .message(message)
                .build();
    }
}
