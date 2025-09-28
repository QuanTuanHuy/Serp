/**
 * Author: QuanTuanHuy
 * Description: Part of Serp Project
 */

package serp.project.crm.core.domain.enums;

import lombok.Getter;

@Getter
public enum TeamMemberStatus {
    INVITED("Invited"),
    CONFIRMED("Confirmed"),
    ARCHIVED("Archived");

    private final String status;

    TeamMemberStatus(String status) {
        this.status = status;
    }
}
