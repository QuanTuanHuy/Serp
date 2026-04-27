/**
 * Author: QuanTuanHuy
 * Description: Part of Serp Project
 */

package serp.project.crm.core.domain.enums;

import lombok.Getter;

@Getter
public enum TeamMemberStatus {
    ACTIVE("Active"),
    INACTIVE("Inactive");

    private final String status;

    TeamMemberStatus(String status) {
        this.status = status;
    }
}
