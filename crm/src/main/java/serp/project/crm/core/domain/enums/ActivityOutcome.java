/**
 * Author: QuanTuanHuy
 * Description: Part of Serp Project
 */

package serp.project.crm.core.domain.enums;

import lombok.Getter;

@Getter
public enum ActivityOutcome {
    REACHED("Reached"),
    VOICEMAIL("Voicemail"),
    NO_ANSWER("No Answer"),
    BUSY("Busy"),
    WRONG_NUMBER("Wrong Number"),
    OCCURRED("Occurred"),
    NO_SHOW("No Show"),
    RESCHEDULED("Rescheduled"),
    CANCELLED_BY_CUSTOMER("Cancelled By Customer");

    private final String description;

    ActivityOutcome(String description) {
        this.description = description;
    }

    public boolean isCallOutcome() {
        return this == REACHED || this == VOICEMAIL || this == NO_ANSWER || this == BUSY || this == WRONG_NUMBER;
    }

    public boolean isMeetingOutcome() {
        return this == OCCURRED || this == NO_SHOW || this == RESCHEDULED || this == CANCELLED_BY_CUSTOMER;
    }
}
