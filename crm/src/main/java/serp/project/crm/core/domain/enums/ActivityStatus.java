/**
 * Author: QuanTuanHuy
 * Description: Part of Serp Project
 */

package serp.project.crm.core.domain.enums;

import lombok.Getter;

@Getter
public enum ActivityStatus {
    PLANNED("Planned"),
    COMPLETED("Completed"),
    CANCELLED("Cancelled");

    private final String status;

    ActivityStatus(String status) {
        this.status = status;
    }
}
