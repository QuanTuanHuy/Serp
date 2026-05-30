/*
Author: Nguyen The Anh
Description: Part of Serp Project
*/

package serp.project.second_mile.kernel.utils;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;
import serp.project.second_mile.enums.HubStaffRole;
import serp.project.second_mile.enums.HubStaffStatus;
import serp.project.second_mile.exception.AppException;
import serp.project.second_mile.exception.ErrorCode;
import serp.project.second_mile.repository.HubStaffRepository;

import java.util.List;

@Component
@RequiredArgsConstructor
public class SecondMileAccessUtils {

    private final AuthUtils authUtils;
    private final HubStaffRepository hubStaffRepository;

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
        return authUtils.hasAnyRole("TMS_ADMIN");
    }

    public boolean isHubManager() {
        return authUtils.hasAnyRole("TMS_HUB_MANAGER");
    }

    public boolean isHubEmployee() {
        return authUtils.hasAnyRole("TMS_HUB_EMPLOYEE");
    }

    public boolean hasHubOperationRole() {
        return isAdmin() || isHubManager() || isHubEmployee();
    }

    public void ensureHubOperationRoleOrThrow() {
        if (!hasHubOperationRole()) {
            throw new AppException(ErrorCode.UNAUTHORIZED);
        }
    }

    public void ensureCurrentUserHasActiveHubStaffRoleOrThrow() {
        if (isAdmin()) {
            return;
        }

        Long tenantId = getCurrentTenantIdOrThrow();
        Long userId = getCurrentUserIdOrThrow();

        boolean hasActiveHubRole = hubStaffRepository.existsByTenantIdAndUserIdAndRoleInAndStatus(
                tenantId,
                userId,
                List.of(HubStaffRole.MANAGER, HubStaffRole.EMPLOYEE),
                HubStaffStatus.ACTIVE
        );
        if (!hasActiveHubRole) {
            throw new AppException(ErrorCode.UNAUTHORIZED);
        }
    }
}
