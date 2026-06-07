package serp.project.school_bus_service.shared.export;

import org.apache.poi.ss.usermodel.*;
import org.apache.poi.xssf.usermodel.XSSFWorkbook;
import org.springframework.stereotype.Component;
import serp.project.school_bus_service.shared.exception.AppErrorCode;
import serp.project.school_bus_service.shared.exception.AppException;

import java.io.ByteArrayOutputStream;
import java.io.InputStream;
import java.util.*;

@Component
public class ExcelTemplateEngine {

    /**
     * Renders an Excel template by replacing scalar placeholders and filling dynamic tables/matrices.
     *
     * @param templatePath Path to the template in the classpath.
     * @param scalarValues Map of scalar placeholders (e.g. ${trace.id} -> 123).
     * @param tableValues  Map of table values (e.g. "timeline.stops" -> List of Maps).
     * @return Rendered Excel file content as byte array.
     */
    @SuppressWarnings("unchecked")
    public byte[] render(String templatePath, Map<String, Object> scalarValues, Map<String, Object> tableValues) {
        try (InputStream is = getClass().getClassLoader().getResourceAsStream(templatePath)) {
            if (is == null) {
                throw new AppException(AppErrorCode.Export.TEMPLATE_NOT_FOUND,
                        "Export template not found in classpath: " + templatePath);
            }

            try (Workbook workbook = new XSSFWorkbook(is)) {
                // 1. Process each sheet
                for (int s = 0; s < workbook.getNumberOfSheets(); s++) {
                    Sheet sheet = workbook.getSheetAt(s);
                    String sheetName = sheet.getSheetName();

                    // Special handling for matrix sheets
                    if (sheetName.contains("Ma_tran_thoi_gian") || sheetName.contains("Ma_tran_khoang_cach")) {
                        processMatrixSheet(sheet, tableValues, sheetName.contains("Ma_tran_thoi_gian"));
                        continue;
                    }

                    // Scan and handle table loops
                    processTableLoops(sheet, tableValues);

                    // Replace scalar placeholders
                    processScalars(sheet, scalarValues);
                }

                // Write to byte array
                try (ByteArrayOutputStream bos = new ByteArrayOutputStream()) {
                    workbook.write(bos);
                    return bos.toByteArray();
                }
            }
        } catch (AppException e) {
            throw e;
        } catch (Exception e) {
            throw new AppException(AppErrorCode.Export.EXPORT_FAILED,
                    "Failed to render Excel template: " + e.getMessage());
        }
    }

    private void processScalars(Sheet sheet, Map<String, Object> scalarValues) {
        for (Row row : sheet) {
            for (Cell cell : row) {
                if (cell.getCellType() == CellType.STRING) {
                    String value = cell.getStringCellValue();
                    if (value != null && value.contains("${")) {
                        String replaced = replacePlaceholderString(value, scalarValues);
                        cell.setCellValue(replaced);
                    }
                }
            }
        }
    }

    private String replacePlaceholderString(String template, Map<String, Object> scalarValues) {
        String result = template;
        for (Map.Entry<String, Object> entry : scalarValues.entrySet()) {
            String placeholder = "${" + entry.getKey() + "}";
            if (result.contains(placeholder)) {
                Object val = entry.getValue();
                result = result.replace(placeholder, val != null ? val.toString() : "");
            }
        }
        // Clean remaining unreplaced placeholders
        if (result.contains("${")) {
            result = result.replaceAll("\\$\\{[^}]+\\}", "-");
        }
        return result;
    }

    @SuppressWarnings("unchecked")
    private void processTableLoops(Sheet sheet, Map<String, Object> tableValues) {
        int lastRowNum = sheet.getLastRowNum();
        List<Integer> rowsToRemove = new ArrayList<>();

        for (int r = 0; r <= lastRowNum; r++) {
            Row row = sheet.getRow(r);
            if (row == null) continue;

            // Find list name from placeholders in this row
            String listKey = null;
            Map<Integer, String> cellFields = new HashMap<>();

            for (int c = 0; c < row.getLastCellNum(); c++) {
                Cell cell = row.getCell(c);
                if (cell != null && cell.getCellType() == CellType.STRING) {
                    String val = cell.getStringCellValue();
                    if (val != null && val.contains("${") && val.contains("[].")) {
                        // Pattern: ${listName[].fieldName}
                        int startIdx = val.indexOf("${") + 2;
                        int bracketIdx = val.indexOf("[].");
                        int endIdx = val.indexOf("}", bracketIdx);
                        if (bracketIdx > startIdx && endIdx > bracketIdx) {
                            listKey = val.substring(startIdx, bracketIdx);
                            String fieldName = val.substring(bracketIdx + 3, endIdx);
                            cellFields.put(c, fieldName);
                        }
                    }
                }
            }

            if (listKey != null && tableValues.containsKey(listKey)) {
                List<Map<String, Object>> items = (List<Map<String, Object>>) tableValues.get(listKey);
                if (items == null) {
                    items = Collections.emptyList();
                }

                // If empty list, put a single "No data" row
                if (items.isEmpty()) {
                    Row newRow = sheet.createRow(r + 1);
                    Cell cell = newRow.createCell(0);
                    cell.setCellValue("Không có dữ liệu");
                    CellStyle style = sheet.getWorkbook().createCellStyle();
                    Font font = sheet.getWorkbook().createFont();
                    font.setItalic(true);
                    style.setFont(font);
                    cell.setCellStyle(style);
                    rowsToRemove.add(r);
                    continue;
                }

                // Fill items
                int insertStartRow = r + 1;
                // Shift rows down to make space
                int numItems = items.size();
                if (insertStartRow <= sheet.getLastRowNum() && numItems > 1) {
                    sheet.shiftRows(insertStartRow, sheet.getLastRowNum(), numItems - 1);
                }

                // Copy styles and fill cells
                for (int i = 0; i < numItems; i++) {
                    Map<String, Object> item = items.get(i);
                    Row destRow = (i == 0) ? row : sheet.createRow(insertStartRow + i - 1);

                    // Copy row height
                    destRow.setHeight(row.getHeight());

                    // Create cells based on template cell configuration
                    for (int c = 0; c < row.getLastCellNum(); c++) {
                        Cell sourceCell = row.getCell(c);
                        Cell destCell = destRow.getCell(c);
                        if (destCell == null) {
                            destCell = destRow.createCell(c);
                        }

                        if (sourceCell != null) {
                            destCell.setCellStyle(sourceCell.getCellStyle());
                        }

                        if (cellFields.containsKey(c)) {
                            String field = cellFields.get(c);
                            Object val = item.get(field);
                            if (val instanceof Number) {
                                destCell.setCellValue(((Number) val).doubleValue());
                            } else {
                                destCell.setCellValue(val != null ? val.toString() : "");
                            }
                        } else if (sourceCell != null && sourceCell.getCellType() == CellType.STRING) {
                            // Copy static value or replace normal scalar in loop cell
                            String srcVal = sourceCell.getStringCellValue();
                            destCell.setCellValue(srcVal);
                        }
                    }
                }

                // We modified the row in-place for first item, no need to remove template row
                // Skip the row iteration forward to avoid scanning the newly inserted rows
                r += numItems - 1;
                lastRowNum = sheet.getLastRowNum();
            }
        }
    }

    @SuppressWarnings("unchecked")
    private void processMatrixSheet(Sheet sheet, Map<String, Object> tableValues, boolean isDuration) {
        // Find if this is leg segment or full matrix
        Map<String, Object> matrixData = (Map<String, Object>) (isDuration ? tableValues.get("matrixDuration") : tableValues.get("matrixDistance"));
        if (matrixData == null) return;

        List<Map<String, Object>> points = (List<Map<String, Object>>) matrixData.get("points");
        Object valuesObj = isDuration ? matrixData.get("durations") : matrixData.get("distances");

        if (points == null || points.isEmpty() || valuesObj == null) {
            Row newRow = sheet.createRow(2);
            Cell cell = newRow.createCell(0);
            cell.setCellValue("Không có dữ liệu");
            return;
        }

        boolean isFullMatrix = false;
        if (valuesObj instanceof List) {
            List<?> list = (List<?>) valuesObj;
            if (!list.isEmpty() && list.get(0) instanceof List) {
                isFullMatrix = true;
            }
        }

        Workbook wb = sheet.getWorkbook();

        // Style for Headers
        CellStyle headerStyle = wb.createCellStyle();
        headerStyle.cloneStyleFrom(sheet.getRow(2).getCell(1).getCellStyle());
        headerStyle.setAlignment(HorizontalAlignment.CENTER);
        headerStyle.setVerticalAlignment(VerticalAlignment.CENTER);

        // Style for Values
        CellStyle valueStyle = wb.createCellStyle();
        valueStyle.cloneStyleFrom(sheet.getRow(3).getCell(1).getCellStyle());
        valueStyle.setAlignment(HorizontalAlignment.RIGHT);

        if (isFullMatrix) {
            // Render N x N grid
            List<List<Number>> matrix2D = (List<List<Number>>) valuesObj;
            int n = points.size();

            // Clear placeholder text on row 2 (headers)
            Row headerRow = sheet.getRow(2);
            for (int c = 1; c < headerRow.getLastCellNum(); c++) {
                Cell cell = headerRow.getCell(c);
                if (cell != null) cell.setCellValue("");
            }

            // Fill header columns
            for (int j = 0; j < n; j++) {
                Cell cell = headerRow.getCell(j + 1);
                if (cell == null) cell = headerRow.createCell(j + 1);
                cell.setCellStyle(headerStyle);
                cell.setCellValue((String) points.get(j).get("name"));
            }

            // Fill rows
            Row templateValueRow = sheet.getRow(3);
            for (int i = 0; i < n; i++) {
                Row row = (i == 0) ? templateValueRow : sheet.createRow(3 + i);
                row.setHeight(templateValueRow.getHeight());

                // Row Header
                Cell rowHeaderCell = row.getCell(0);
                if (rowHeaderCell == null) rowHeaderCell = row.createCell(0);
                rowHeaderCell.setCellStyle(templateValueRow.getCell(0).getCellStyle());
                rowHeaderCell.setCellValue((String) points.get(i).get("name"));

                // Matrix Cell Values
                List<Number> rowValues = matrix2D.get(i);
                for (int j = 0; j < n; j++) {
                    Cell cell = row.getCell(j + 1);
                    if (cell == null) cell = row.createCell(j + 1);
                    cell.setCellStyle(valueStyle);
                    if (rowValues != null && j < rowValues.size()) {
                        cell.setCellValue(rowValues.get(j).doubleValue());
                    } else {
                        cell.setCellValue(0.0);
                    }
                }
            }
        } else {
            // Render leg segment list: From -> To -> Value
            List<Number> legs = (List<Number>) valuesObj;

            // Clear Row 2 & 3 completely
            for (int r = 2; r <= 3; r++) {
                Row rToDelete = sheet.getRow(r);
                if (rToDelete != null) {
                    sheet.removeRow(rToDelete);
                }
            }

            // Create new Leg table headers at Row 2
            Row headerRow = sheet.createRow(2);
            headerRow.setHeightInPoints(25);
            String[] headers = {"STT", "From Point Key", "From Location Name", "To Point Key", "To Location Name", isDuration ? "Duration (Minutes)" : "Distance (Km)"};
            for (int c = 0; c < headers.length; c++) {
                Cell cell = headerRow.createCell(c);
                cell.setCellValue(headers[c]);
                cell.setCellStyle(headerStyle);
            }

            // Fill leg rows starting at Row 3
            int numLegs = Math.min(points.size() - 1, legs.size());
            for (int i = 0; i < numLegs; i++) {
                Row row = sheet.createRow(3 + i);
                row.setHeightInPoints(20);

                Map<String, Object> fromPoint = points.get(i);
                Map<String, Object> toPoint = points.get(i + 1);
                Number val = legs.get(i);

                // STT
                Cell cell0 = row.createCell(0);
                cell0.setCellValue(i + 1);
                cell0.setCellStyle(valueStyle);

                // From Key
                Cell cell1 = row.createCell(1);
                cell1.setCellValue((String) fromPoint.get("pointKey"));
                cell1.setCellStyle(valueStyle);

                // From Name
                Cell cell2 = row.createCell(2);
                cell2.setCellValue((String) fromPoint.get("name"));
                cell2.setCellStyle(valueStyle);

                // To Key
                Cell cell3 = row.createCell(3);
                cell3.setCellValue((String) toPoint.get("pointKey"));
                cell3.setCellStyle(valueStyle);

                // To Name
                Cell cell4 = row.createCell(4);
                cell4.setCellValue((String) toPoint.get("name"));
                cell4.setCellStyle(valueStyle);

                // Value
                Cell cell5 = row.createCell(5);
                cell5.setCellValue(val.doubleValue());
                cell5.setCellStyle(valueStyle);
            }
        }
    }
}
