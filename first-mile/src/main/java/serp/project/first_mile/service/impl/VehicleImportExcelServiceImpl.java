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
import serp.project.first_mile.domain.PostOfficeStaff;
import serp.project.first_mile.enums.PostOfficeStaffRole;
import serp.project.first_mile.enums.PostOfficeStaffStatus;
import serp.project.first_mile.exception.AppException;
import serp.project.first_mile.exception.ErrorCode;
import serp.project.first_mile.kernel.utils.AuthUtils;
import serp.project.first_mile.repository.PostOfficeRepository;
import serp.project.first_mile.repository.PostOfficeStaffAssignmentRepository;
import serp.project.first_mile.repository.PostOfficeStaffRepository;
import serp.project.first_mile.repository.projection.CodeNameProjection;
import serp.project.first_mile.service.VehicleImportExcelService;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.time.LocalDate;
import java.util.List;
import java.util.Set;

@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class VehicleImportExcelServiceImpl implements VehicleImportExcelService {

    private static final String ROLE_TMS_ADMIN = "TMS_ADMIN";
    private static final String ROLE_TMS_POSTOFFICER_MANAGER = "TMS_POSTOFFICER_MANAGER";
    private static final String TEMPLATE_PATH = "excel/vehicle_template.xlsx";
    private static final String UNIT_SHEET_NAME = "Unit";
    private static final int START_ROW_INDEX = 1;
    private static final int POST_OFFICE_COLUMN_INDEX = 2;
    private static final int COURIER_COLUMN_INDEX = 3;

    private final PostOfficeRepository postOfficeRepository;
    private final PostOfficeStaffAssignmentRepository postOfficeStaffAssignmentRepository;
    private final PostOfficeStaffRepository postOfficeStaffRepository;
    private final AuthUtils authUtils;

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
            setTextCellValue(
                    sheet,
                    START_ROW_INDEX + i,
                    columnIndex,
                    formatCodeAndName(value.getCode(), value.getName())
            );
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

    private Set<Long> getManagedPostOfficeIdsOrThrow(Long tenantId) {
        Long currentUserId = authUtils.getCurrentUserId().orElseThrow(() -> new AppException(ErrorCode.UNAUTHORIZED));
        String managerCode = buildStaffCode(currentUserId, PostOfficeStaffRole.MANAGER);

        PostOfficeStaff managerStaff = postOfficeStaffRepository.findByCodeAndTenantId(managerCode, tenantId)
                .orElseThrow(() -> new AppException(ErrorCode.UNAUTHORIZED));

        return postOfficeStaffAssignmentRepository.findActivePostOfficeIdsByStaffIdAndTenantId(
                managerStaff.getId(),
                tenantId,
                LocalDate.now()
        );
    }

    private String buildStaffCode(Long userId, PostOfficeStaffRole role) {
        return "USR_" + userId + "_" + role.name();
    }

    private Long getCurrentTenantIdOrThrow() {
        return authUtils.getCurrentTenantId().orElseThrow(() -> new AppException(ErrorCode.UNAUTHORIZED));
    }

    private boolean isManagerScopedAccess() {
        return isPostOfficerManager() && !isAdmin();
    }

    private boolean isAdmin() {
        return authUtils.hasAnyRole(ROLE_TMS_ADMIN);
    }

    private boolean isPostOfficerManager() {
        return authUtils.hasAnyRole(ROLE_TMS_POSTOFFICER_MANAGER);
    }
}
