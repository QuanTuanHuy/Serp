/*
Author: Nguyen The Anh
Description: Part of Serp Project
*/

package serp.project.second_mile.kernel.utils;

import org.apache.poi.ss.usermodel.Cell;
import org.apache.poi.ss.usermodel.DataFormatter;
import org.apache.poi.ss.usermodel.FormulaEvaluator;
import org.apache.poi.ss.usermodel.Row;
import serp.project.second_mile.dto.response.ValidateImportFileDTO;

import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Locale;
import java.util.UUID;
import java.util.regex.Pattern;

public final class ExcelImportUtils {

    private ExcelImportUtils() {
    }

    public static <T> ValidateImportFileDTO<T> buildBaseValidateResponse(
            List<String> headerKeys,
            List<String> expectedHeaders
    ) {
        ValidateImportFileDTO<T> response = new ValidateImportFileDTO<>();
        response.setFileId(UUID.randomUUID());
        response.setHeader(buildHeaderMap(headerKeys, expectedHeaders));
        response.setData(new ArrayList<>());
        response.setSuccess(false);
        response.setType(0);
        return response;
    }

    public static LinkedHashMap<String, String> buildHeaderMap(
            List<String> headerKeys,
            List<String> expectedHeaders
    ) {
        LinkedHashMap<String, String> headerMap = new LinkedHashMap<>();
        int size = Math.min(headerKeys.size(), expectedHeaders.size());
        for (int i = 0; i < size; i++) {
            headerMap.put(headerKeys.get(i), expectedHeaders.get(i));
        }
        return headerMap;
    }

    public static void setValidationFailed(ValidateImportFileDTO<?> response, List<String> errors) {
        response.setSuccess(false);
        response.setType(0);
        response.setErrorMessage(errors == null ? null : String.join("\n", errors));
    }

    public static boolean isBlankRow(
            Row row,
            int lastColumnIndex,
            DataFormatter formatter,
            FormulaEvaluator evaluator
    ) {
        if (row == null) {
            return true;
        }

        for (int columnIndex = 0; columnIndex <= lastColumnIndex; columnIndex++) {
            if (hasText(getCellText(row, columnIndex, formatter, evaluator))) {
                return false;
            }
        }
        return true;
    }

    public static String getCellText(Row row, int columnIndex, DataFormatter formatter, FormulaEvaluator evaluator) {
        Cell cell = getCell(row, columnIndex);
        if (cell == null) {
            return "";
        }
        return formatter.formatCellValue(cell, evaluator);
    }

    public static Cell getCell(Row row, int columnIndex) {
        if (row == null) {
            return null;
        }
        return row.getCell(columnIndex, Row.MissingCellPolicy.RETURN_BLANK_AS_NULL);
    }

    public static String toColumnName(int columnIndex) {
        int current = columnIndex + 1;
        StringBuilder columnName = new StringBuilder();
        while (current > 0) {
            int remainder = (current - 1) % 26;
            columnName.insert(0, (char) ('A' + remainder));
            current = (current - 1) / 26;
        }
        return columnName.toString();
    }

    public static String normalizeWhitespace(String value) {
        if (value == null) {
            return "";
        }
        return value.replace('\n', ' ').replaceAll("\\s+", " ").trim();
    }

    public static String normalizeCodeKey(String code) {
        return normalizeWhitespace(code).toUpperCase(Locale.ROOT);
    }

    public static boolean hasText(String value) {
        return value != null && !value.trim().isEmpty();
    }

    public static Double parseNumber(String rawValue) {
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

    public static CodeNameValue parseCodeAndName(String value, Pattern codeNamePattern) {
        String normalizedValue = normalizeWhitespace(value);
        var matcher = codeNamePattern.matcher(normalizedValue);
        if (!matcher.matches()) {
            return null;
        }

        String code = matcher.group(1).trim();
        String name = matcher.group(2).trim();
        if (!hasText(code) || !hasText(name)) {
            return null;
        }

        return new CodeNameValue(code, name);
    }

    public record CodeNameValue(String code, String name) {
    }
}
