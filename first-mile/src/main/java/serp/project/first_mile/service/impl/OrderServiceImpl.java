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
import org.locationtech.jts.geom.Point;
import org.springframework.core.io.ClassPathResource;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.multipart.MultipartFile;
import serp.project.first_mile.domain.Order;
import serp.project.first_mile.domain.PostOffice;
import serp.project.first_mile.dto.request.OrderImportDTO;
import serp.project.first_mile.dto.response.ImportHistoryResponse;
import serp.project.first_mile.dto.response.OrderConfirmationResponse;
import serp.project.first_mile.dto.response.ProductTypeTemplateDTO;
import serp.project.first_mile.dto.response.ProvinceExcelTemplateDTO;
import serp.project.first_mile.dto.response.ValidateImportFileDTO;
import serp.project.first_mile.dto.response.WardExcelTemplateDTO;
import serp.project.first_mile.enums.OrderStatus;
import serp.project.first_mile.exception.AppException;
import serp.project.first_mile.exception.ErrorCode;
import serp.project.first_mile.repository.OrderRepository;
import serp.project.first_mile.repository.PostOfficeRepository;
import serp.project.first_mile.service.OrderExcelService;
import serp.project.first_mile.service.OrderImportExcelService;
import serp.project.first_mile.service.OrderService;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.time.LocalDate;
import java.util.List;
import java.util.Optional;
import java.util.Set;

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
    private static final Set<OrderStatus> CONFIRMABLE_ORDER_STATUSES = Set.of(
            OrderStatus.CREATED,
            OrderStatus.PICKUP_FAILED
    );

    private final OrderExcelService orderExcelService;
    private final OrderImportExcelService orderImportExcelService;
    private final OrderRepository orderRepository;
    private final PostOfficeRepository postOfficeRepository;

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
    @Transactional(rollbackFor = Exception.class)
    public ImportHistoryResponse importOrdersAsync(MultipartFile file, Long tenantId) {
        return orderImportExcelService.importOrdersAsync(file, tenantId);
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public OrderConfirmationResponse confirmOrder(Long orderId, Long tenantId) {
        Order order = orderRepository.findByIdAndTenantIdForUpdate(orderId, tenantId)
                .orElseThrow(() -> new AppException(ErrorCode.ORDER_NOT_FOUND));

        if (hasText(order.getOriginPostOfficeCode())) {
            Optional<PostOffice> assignedPostOffice = postOfficeRepository.findByCodeIgnoreCaseAndTenantId(
                    order.getOriginPostOfficeCode(),
                    tenantId
            );
            return toOrderConfirmationResponse(order, assignedPostOffice.orElse(null), true);
        }

        validateOrderForConfirmation(order);

        PostOffice postOffice = postOfficeRepository.findBestAssignablePostOfficeForSenderForUpdate(
                        tenantId,
                        order.getSenderLocation(),
                        LocalDate.now()
                )
                .orElseThrow(() -> new AppException(ErrorCode.NO_SUITABLE_ORIGIN_POST_OFFICE));

        postOffice.addLoad(1);
        order.setOriginPostOfficeCode(postOffice.getCode());

        postOfficeRepository.save(postOffice);
        orderRepository.save(order);

        return toOrderConfirmationResponse(order, postOffice, false);
    }

    private void validateOrderForConfirmation(Order order) {
        if (order == null) {
            throw new AppException(ErrorCode.ORDER_NOT_FOUND);
        }
        if (order.getStatus() == null || !CONFIRMABLE_ORDER_STATUSES.contains(order.getStatus())) {
            throw new AppException(ErrorCode.ORDER_NOT_ASSIGNABLE);
        }

        Point senderLocation = order.getSenderLocation();
        if (senderLocation == null) {
            throw new AppException(ErrorCode.ORDER_NOT_ASSIGNABLE);
        }

        double latitude = senderLocation.getY();
        double longitude = senderLocation.getX();
        if (!isValidCoordinate(latitude, longitude)) {
            throw new AppException(ErrorCode.ORDER_NOT_ASSIGNABLE);
        }
    }

    private boolean isValidCoordinate(double latitude, double longitude) {
        return !Double.isNaN(latitude)
                && !Double.isNaN(longitude)
                && !Double.isInfinite(latitude)
                && !Double.isInfinite(longitude)
                && latitude >= -90.0
                && latitude <= 90.0
                && longitude >= -180.0
                && longitude <= 180.0;
    }

    private boolean hasText(String value) {
        return value != null && !value.trim().isEmpty();
    }

    private OrderConfirmationResponse toOrderConfirmationResponse(
            Order order,
            PostOffice postOffice,
            boolean alreadyConfirmed
    ) {
        String postOfficeCode = postOffice == null
                ? order.getOriginPostOfficeCode()
                : postOffice.getCode();

        OrderConfirmationResponse.OriginPostOfficeInfo originInfo =
                new OrderConfirmationResponse.OriginPostOfficeInfo(
                        postOffice == null ? null : postOffice.getId(),
                        postOfficeCode,
                        postOffice == null ? null : postOffice.getName(),
                        postOffice == null ? null : postOffice.getCurrentLoad(),
                        postOffice == null ? null : postOffice.getDailyCapacity()
                );

        return new OrderConfirmationResponse(
                order.getId(),
                order.getOrderCode(),
                order.getCustomerOrderCode(),
                order.getStatus(),
                alreadyConfirmed,
                originInfo
        );
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
