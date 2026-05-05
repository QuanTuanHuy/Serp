/*
Author: Nguyen The Anh
Description: Part of Serp Project
*/

package serp.project.second_mile.kernel.utils;

import org.apache.poi.ss.usermodel.Cell;
import org.apache.poi.ss.usermodel.Row;
import org.apache.poi.ss.usermodel.Sheet;

public final class ExcelTemplateUtils {

    private ExcelTemplateUtils() {
    }

    public static void setTextCellValue(Sheet sheet, int rowIndex, int columnIndex, String value) {
        Row row = sheet.getRow(rowIndex);
        if (row == null) {
            row = sheet.createRow(rowIndex);
        }

        Cell cell = row.getCell(columnIndex);
        if (cell == null) {
            cell = row.createCell(columnIndex);
        }

        cell.setCellValue(value);
    }

    public static String formatCodeAndName(String code, String name) {
        String safeCode = code == null ? "" : code.trim();
        String safeName = name == null ? "" : name.trim();
        return safeCode + " - " + safeName;
    }
}
