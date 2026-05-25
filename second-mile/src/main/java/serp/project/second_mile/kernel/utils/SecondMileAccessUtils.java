/*
Author: Nguyen The Anh
Description: Part of Serp Project
*/

package serp.project.second_mile.kernel.utils;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;
import serp.project.second_mile.exception.AppException;
import serp.project.second_mile.exception.ErrorCode;

@Component
@RequiredArgsConstructor
public class SecondMileAccessUtils {

    private final AuthUtils authUtils;

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
}
