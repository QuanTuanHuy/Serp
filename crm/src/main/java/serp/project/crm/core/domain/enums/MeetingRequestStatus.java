/**
 * Author: QuanTuanHuy
 * Description: Part of Serp Project
 */

package serp.project.crm.core.domain.enums;

import lombok.Getter;

@Getter
public enum MeetingRequestStatus {
    PENDING("Pending"),
    SCHEDULED("Scheduled"),
    FAILED("Failed"),
    CANCELLED("Cancelled");

    private final String status;

    MeetingRequestStatus(String status) {
        this.status = status;
    }
}
