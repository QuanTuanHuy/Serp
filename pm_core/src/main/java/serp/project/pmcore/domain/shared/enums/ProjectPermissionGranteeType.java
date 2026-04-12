/**
 * Author: QuanTuanHuy
 * Description: Part of Serp Project
 */

package serp.project.pmcore.domain.shared.enums;

import java.util.Locale;

public enum ProjectPermissionGranteeType {
    PROJECT_ROLE,
    GROUP,
    USER,
    PROJECT_LEAD,
    REPORTER,
    ASSIGNEE,
    ANY_LOGGED_IN_USER,
    AUTHENTICATED,
    APPLICATION_ACCESS,
    ANYONE_ON_WEB,
    USER_CUSTOM_FIELD_VALUE,
    GROUP_CUSTOM_FIELD_VALUE;

    public static ProjectPermissionGranteeType fromValue(String value) {
        if (value == null || value.isBlank()) {
            return null;
        }

        String normalized = value.trim().toUpperCase(Locale.ROOT);
        if ("LOGGED_IN_USER".equals(normalized)) {
            return ANY_LOGGED_IN_USER;
        }

        try {
            return ProjectPermissionGranteeType.valueOf(normalized);
        } catch (IllegalArgumentException ex) {
            return null;
        }
    }
}
