/**
 * Author: QuanTuanHuy
 * Description: Part of Serp Project
 */

package serp.project.account.core.domain.enums;

import lombok.Getter;

@Getter
public enum RoleEnum {
    USER("USER"),
    MANAGER("MANAGER"),
    COMPANY_ADMIN("COMPANY_ADMIN"),
    ADMIN("ADMIN"),
    SUPER_ADMIN("SUPER_ADMIN");

    private final String role;

    RoleEnum(String role) {
        this.role = role;
    }
}
