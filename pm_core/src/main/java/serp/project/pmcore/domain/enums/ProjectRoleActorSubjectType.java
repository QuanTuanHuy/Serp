/**
 * Author: QuanTuanHuy
 * Description: Part of Serp Project
 */

package serp.project.pmcore.domain.enums;

public enum ProjectRoleActorSubjectType {
    USER,
    GROUP,
    SERVICE_ACCOUNT;

    public static ProjectRoleActorSubjectType fromValue(String value) {
        return ProjectRoleActorSubjectType.valueOf(value.toUpperCase());
    }
}
