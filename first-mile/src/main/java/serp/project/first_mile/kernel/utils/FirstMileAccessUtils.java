/*
Author: Nguyen The Anh
Description: Part of Serp Project
*/

package serp.project.first_mile.kernel.utils;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;
import serp.project.first_mile.domain.PostOfficeStaff;
import serp.project.first_mile.enums.PostOfficeStaffRole;
import serp.project.first_mile.exception.AppException;
import serp.project.first_mile.exception.ErrorCode;
import serp.project.first_mile.repository.PostOfficeStaffAssignmentRepository;
import serp.project.first_mile.repository.PostOfficeStaffRepository;

import java.time.LocalDate;
import java.util.Set;

@Component
@RequiredArgsConstructor
public class FirstMileAccessUtils {

    private static final String ROLE_TMS_ADMIN = "TMS_ADMIN";
    private static final String ROLE_TMS_CUSTOMER = "TMS_CUSTOMER";
    private static final String ROLE_TMS_POSTOFFICER_MANAGER = "TMS_POSTOFFICER_MANAGER";
    private static final String ROLE_TMS_POSTOFFICER = "TMS_POSTOFFICER";

    private final AuthUtils authUtils;
    private final PostOfficeStaffRepository postOfficeStaffRepository;
    private final PostOfficeStaffAssignmentRepository postOfficeStaffAssignmentRepository;

    public Long getCurrentTenantIdOrThrow() {
        return authUtils.getCurrentTenantId().orElseThrow(() -> new AppException(ErrorCode.UNAUTHORIZED));
    }

    public Long getCurrentUserIdOrThrow() {
        return authUtils.getCurrentUserId().orElseThrow(() -> new AppException(ErrorCode.UNAUTHORIZED));
    }

    public Long getCurrentUserIdOrNull() {
        return authUtils.getCurrentUserId().orElse(null);
    }

    public boolean isAdmin() {
        return authUtils.hasAnyRole(ROLE_TMS_ADMIN);
    }

    public boolean isCustomer() {
        return authUtils.hasAnyRole(ROLE_TMS_CUSTOMER);
    }

    public boolean isPostOfficerManager() {
        return authUtils.hasAnyRole(ROLE_TMS_POSTOFFICER_MANAGER);
    }

    public boolean isCourier() {
        return authUtils.hasAnyRole(ROLE_TMS_POSTOFFICER);
    }

    public boolean isManagerScopedAccess() {
        return isPostOfficerManager() && !isAdmin();
    }

    public Long resolveStaffIdByUserAndRoleOrThrow(Long userId, Long tenantId, PostOfficeStaffRole role) {
        String staffCode = PostOfficeStaffCodeUtils.buildStaffCode(userId, role);
        PostOfficeStaff postOfficeStaff = postOfficeStaffRepository.findByCodeAndTenantId(staffCode, tenantId)
                .orElseThrow(() -> new AppException(ErrorCode.UNAUTHORIZED));

        if (!role.equals(postOfficeStaff.getRole())) {
            throw new AppException(ErrorCode.UNAUTHORIZED);
        }

        return postOfficeStaff.getId();
    }

    public Long resolveCurrentStaffIdByRoleOrThrow(Long tenantId, PostOfficeStaffRole role) {
        return resolveStaffIdByUserAndRoleOrThrow(getCurrentUserIdOrThrow(), tenantId, role);
    }

    public Set<Long> getManagedPostOfficeIdsOrThrow(Long tenantId) {
        Long managerStaffId = resolveCurrentStaffIdByRoleOrThrow(tenantId, PostOfficeStaffRole.MANAGER);
        return postOfficeStaffAssignmentRepository.findActivePostOfficeIdsByStaffIdAndTenantId(
                managerStaffId,
                tenantId,
                LocalDate.now()
        );
    }

    public void ensureCurrentManagerAssignedToPostOfficeOrThrow(Long postOfficeId, Long tenantId) {
        Long managerStaffId = resolveCurrentStaffIdByRoleOrThrow(tenantId, PostOfficeStaffRole.MANAGER);

        boolean hasAssignment = postOfficeStaffAssignmentRepository.existsActiveAssignmentByStaffIdAndPostOfficeIdAndTenantId(
                managerStaffId,
                postOfficeId,
                tenantId,
                LocalDate.now()
        );

        if (!hasAssignment) {
            throw new AppException(ErrorCode.UNAUTHORIZED);
        }
    }
}