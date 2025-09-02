/**
 * Author: QuanTuanHuy
 * Description: Part of Serp Project
 */

package serp.project.account.core.domain.enums;

import lombok.Getter;

@Getter
public enum PermissionEnum {
    ACCOUNT_ROLE_READ("ACCOUNT.ROLE.READ"),
    ACCOUNT_ROLE_WRITE("ACCOUNT.ROLE.WRITE"),
    ACCOUNT_PERMISSION_READ("ACCOUNT.PERMISSION.READ"),
    ACCOUNT_PERMISSION_WRITE("ACCOUNT.PERMISSION.WRITE"),
    ACCOUNT_USER_READ("ACCOUNT.USER.READ"),
    ACCOUNT_USER_WRITE("ACCOUNT.USER.WRITE");

    private final String name;

    PermissionEnum(String name) {
        this.name = name;
    }
}
