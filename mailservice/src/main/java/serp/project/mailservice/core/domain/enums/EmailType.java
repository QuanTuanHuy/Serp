/*
Author: QuanTuanHuy
Description: Part of Serp Project
*/

package serp.project.mailservice.core.domain.enums;

import java.util.Set;

public enum EmailType {
    VERIFICATION,
    NOTIFICATION,
    MARKETING,
    TRANSACTIONAL,
    PASSWORD_RESET,
    ALERT,
    REMINDER,
    WELCOME;

    private static final Set<EmailType> SYSTEM_TYPES = Set.of(
            VERIFICATION, PASSWORD_RESET, ALERT
    );

    private static final Set<EmailType> USER_FACING_TYPES = Set.of(
            NOTIFICATION, MARKETING, REMINDER, WELCOME
    );

    public boolean isSystemType() {
        return SYSTEM_TYPES.contains(this);
    }

    public boolean isUserFacingType() {
        return USER_FACING_TYPES.contains(this);
    }

    public boolean isTransactional() {
        return this == TRANSACTIONAL;
    }
}
