/**
 * Author: QuanTuanHuy
 * Description: Part of Serp Project
 */

package serp.project.pmcore.domain.shared.enums;

public enum ProjectPermissionGranteeType {
    PROJECT_ROLE,
    GROUP,
    USER,
    PROJECT_LEAD,
    REPORTER,
    ASSIGNEE,
    ANY_LOGGED_IN_USER,
    AUTHENTICATED;

    public static ProjectPermissionGranteeType fromValue(String value) {
        return ProjectPermissionGranteeType.valueOf(value.toUpperCase());
    }
}
