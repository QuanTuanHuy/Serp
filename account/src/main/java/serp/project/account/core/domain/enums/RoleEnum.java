/**
 * Author: QuanTuanHuy
 * Description: Part of Serp Project
 */

package serp.project.account.core.domain.enums;

import lombok.Getter;

@Getter
public enum RoleEnum {
    SUPER_ADMIN("SUPER_ADMIN", 1),
    ADMIN("ADMIN", 2),
    MODERATOR("MODERATOR", 3),
    USER("USER", 4),
    ORGANIZATION_ADMIN("ORGANIZATION_ADMIN", 1);

    private final String role;
    private final Integer priority;

    RoleEnum(String role, Integer priority) {
        this.role = role;
        this.priority = priority;
    }
}
