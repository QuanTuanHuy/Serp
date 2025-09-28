/**
 * Author: QuanTuanHuy
 * Description: Part of Serp Project
 */

package serp.project.crm.core.domain.enums;

import lombok.Getter;

@Getter
public enum ActiveStatus {
    ACTIVE("Active"),
    INACTIVE("Inactive"),
    ;

    private final String description;

    ActiveStatus(String description) {
        this.description = description;
    }
}
