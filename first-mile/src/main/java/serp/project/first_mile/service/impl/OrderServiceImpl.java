/*
Author: Nguyen The Anh
Description: Part of Serp Project
*/

package serp.project.first_mile.service.impl;

import lombok.RequiredArgsConstructor;
import org.apache.poi.ss.usermodel.Cell;
import org.apache.poi.ss.usermodel.Row;
import org.apache.poi.ss.usermodel.Sheet;
import org.apache.poi.ss.usermodel.Workbook;
import org.apache.poi.xssf.usermodel.XSSFWorkbook;
import org.springframework.core.io.ClassPathResource;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.multipart.MultipartFile;
import serp.project.first_mile.dto.request.OrderImportDTO;
import serp.project.first_mile.dto.response.ImportHistoryResponse;
import serp.project.first_mile.dto.response.ProductTypeTemplateDTO;
import serp.project.first_mile.dto.response.ProvinceExcelTemplateDTO;
import serp.project.first_mile.dto.response.ValidateImportFileDTO;
import serp.project.first_mile.dto.response.WardExcelTemplateDTO;
import serp.project.first_mile.exception.AppException;
import serp.project.first_mile.exception.ErrorCode;
import serp.project.first_mile.service.OrderExcelService;
import serp.project.first_mile.service.OrderImportExcelService;
import serp.project.first_mile.service.OrderService;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.util.List;

@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class OrderServiceImpl implements OrderService {

    private static final String TEMPLATE_PATH = "excel/order_template.xlsx";
    private static final String UNIT_SHEET_NAME = "Unit";
    private static final int START_ROW_INDEX = 1;
    private static final int WARD_COLUMN_INDEX = 0;
    private static final int PROVINCE_COLUMN_INDEX = 1;
    private static final int PRODUCT_TYPE_COLUMN_INDEX = 5;

    private final OrderExcelService orderExcelService;
    private final OrderImportExcelService orderImportExcelService;

    @Override
    public byte[] exportTemplate(Long tenantId) {
        List<WardExcelTemplateDTO> wards = orderExcelService.getWardExcelTemplate();
        List<ProvinceExcelTemplateDTO> provinces = orderExcelService.getProvinceExcelTemplate();
        List<ProductTypeTemplateDTO> productTypes = orderExcelService.getProductTypeTemplate(tenantId);

        try (InputStream inputStream = new ClassPathResource(TEMPLATE_PATH).getInputStream();
             Workbook workbook = new XSSFWorkbook(inputStream);
             ByteArrayOutputStream outputStream = new ByteArrayOutputStream()) {

            Sheet unitSheet = workbook.getSheet(UNIT_SHEET_NAME);
            if (unitSheet == null) {
                throw new AppException(ErrorCode.UNCATEGORIZED_EXCEPTION);
            }

            populateWardColumn(unitSheet, wards);
            populateProvinceColumn(unitSheet, provinces);
            populateProductTypeColumn(unitSheet, productTypes);

            workbook.write(outputStream);
            return outputStream.toByteArray();
        } catch (IOException exception) {
            throw new AppException(ErrorCode.UNCATEGORIZED_EXCEPTION);
        }
    }

    @Override
    public ValidateImportFileDTO<OrderImportDTO> validateImportFile(MultipartFile file, Long tenantId) {
        return orderImportExcelService.validateImportFile(file, tenantId);
    }

    @Override
    public ImportHistoryResponse importOrdersAsync(MultipartFile file, Long tenantId) {
        return orderImportExcelService.importOrdersAsync(file, tenantId);
    }

    @Override
    public ImportHistoryResponse getImportHistory(Long importHistoryId, Long tenantId) {
        return orderImportExcelService.getImportHistory(importHistoryId, tenantId);
    }

    private void populateWardColumn(Sheet sheet, List<WardExcelTemplateDTO> wards) {
        for (int i = 0; i < wards.size(); i++) {
            WardExcelTemplateDTO ward = wards.get(i);
            setTextCellValue(sheet, START_ROW_INDEX + i, WARD_COLUMN_INDEX,
                    formatCodeAndName(ward.getWardCode(), ward.getWardName()));
        }
    }

    private void populateProvinceColumn(Sheet sheet, List<ProvinceExcelTemplateDTO> provinces) {
        for (int i = 0; i < provinces.size(); i++) {
            ProvinceExcelTemplateDTO province = provinces.get(i);
            setTextCellValue(sheet, START_ROW_INDEX + i, PROVINCE_COLUMN_INDEX,
                    formatCodeAndName(province.getProvinceCode(), province.getProvinceName()));
        }
    }

    private void populateProductTypeColumn(Sheet sheet, List<ProductTypeTemplateDTO> productTypes) {
        for (int i = 0; i < productTypes.size(); i++) {
            ProductTypeTemplateDTO productType = productTypes.get(i);
            setTextCellValue(sheet, START_ROW_INDEX + i, PRODUCT_TYPE_COLUMN_INDEX,
                    formatCodeAndName(productType.getProductTypeCode(), productType.getProductTypeName()));
        }
    }

    private void setTextCellValue(Sheet sheet, int rowIndex, int columnIndex, String value) {
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

    private String formatCodeAndName(String code, String name) {
        String safeCode = code == null ? "" : code.trim();
        String safeName = name == null ? "" : name.trim();
        return safeCode + " - " + safeName;
    }
}
