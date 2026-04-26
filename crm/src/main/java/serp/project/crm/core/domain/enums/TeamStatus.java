/**
 * Author: QuanTuanHuy
 * Description: Part of Serp Project
 */

package serp.project.crm.core.domain.enums;

import lombok.Getter;

@Getter
public enum TeamStatus {
    ACTIVE("Active"),
    INACTIVE("Inactive");

    private final String status;

    TeamStatus(String status) {
        this.status = status;
    }
}
