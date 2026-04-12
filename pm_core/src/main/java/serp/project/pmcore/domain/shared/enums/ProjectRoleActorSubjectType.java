/**
 * Author: QuanTuanHuy
 * Description: Part of Serp Project
 */

package serp.project.pmcore.domain.shared.enums;

public enum ProjectRoleActorSubjectType {
    USER,
    GROUP,
    SERVICE_ACCOUNT;

    public static ProjectRoleActorSubjectType fromValue(String value) {
        return ProjectRoleActorSubjectType.valueOf(value.toUpperCase());
    }
}
