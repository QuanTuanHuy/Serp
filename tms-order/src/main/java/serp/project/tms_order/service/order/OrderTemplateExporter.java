/*
Author: Nguyen The Anh
Description: Part of Serp Project
*/

package serp.project.tms_order.service.order;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.apache.poi.ss.usermodel.Sheet;
import org.apache.poi.ss.usermodel.Workbook;
import org.apache.poi.xssf.usermodel.XSSFWorkbook;
import org.springframework.core.io.ClassPathResource;
import org.springframework.stereotype.Component;
import serp.project.tms_order.dto.response.ProductTypeTemplateDTO;
import serp.project.tms_order.dto.response.ProvinceExcelTemplateDTO;
import serp.project.tms_order.dto.response.WardExcelTemplateDTO;
import serp.project.tms_order.exception.AppException;
import serp.project.tms_order.exception.ErrorCode;
import serp.project.tms_order.kernel.utils.ExcelTemplateUtils;
import serp.project.tms_order.service.OrderExcelService;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.util.List;

@Component
@RequiredArgsConstructor
@Slf4j
public class OrderTemplateExporter {

    private static final String TEMPLATE_PATH = "excel/order_template.xlsx";
    private static final String UNIT_SHEET_NAME = "Unit";
    private static final int START_ROW_INDEX = 1;
    private static final int WARD_COLUMN_INDEX = 0;
    private static final int PROVINCE_COLUMN_INDEX = 1;
    private static final int PRODUCT_TYPE_COLUMN_INDEX = 4;

    private final OrderExcelService orderExcelService;

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
            log.error("Export TMS order template failed", exception);
            throw new AppException(ErrorCode.UNCATEGORIZED_EXCEPTION);
        }
    }

    private void populateWardColumn(Sheet sheet, List<WardExcelTemplateDTO> wards) {
        for (int i = 0; i < wards.size(); i++) {
            WardExcelTemplateDTO ward = wards.get(i);
            ExcelTemplateUtils.setTextCellValue(
                    sheet,
                    START_ROW_INDEX + i,
                    WARD_COLUMN_INDEX,
                    ExcelTemplateUtils.formatCodeAndName(ward.getWardCode(), ward.getWardName())
            );
        }
    }

    private void populateProvinceColumn(Sheet sheet, List<ProvinceExcelTemplateDTO> provinces) {
        for (int i = 0; i < provinces.size(); i++) {
            ProvinceExcelTemplateDTO province = provinces.get(i);
            ExcelTemplateUtils.setTextCellValue(
                    sheet,
                    START_ROW_INDEX + i,
                    PROVINCE_COLUMN_INDEX,
                    ExcelTemplateUtils.formatCodeAndName(province.getProvinceCode(), province.getProvinceName())
            );
        }
    }

    private void populateProductTypeColumn(Sheet sheet, List<ProductTypeTemplateDTO> productTypes) {
        for (int i = 0; i < productTypes.size(); i++) {
            ProductTypeTemplateDTO productType = productTypes.get(i);
            ExcelTemplateUtils.setTextCellValue(
                    sheet,
                    START_ROW_INDEX + i,
                    PRODUCT_TYPE_COLUMN_INDEX,
                    ExcelTemplateUtils.formatCodeAndName(productType.getProductTypeCode(), productType.getProductTypeName())
            );
        }
    }
}
