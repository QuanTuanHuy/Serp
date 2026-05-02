/**
 * Author: QuanTuanHuy
 * Description: Part of Serp Project
 */

package serp.project.crm.core.domain.enums;

import lombok.Getter;

@Getter
public enum PreferredTimeSlot {
    MORNING(8, 12),
    AFTERNOON(13, 17);

    private final int startHour;
    private final int endHour;

    PreferredTimeSlot(int startHour, int endHour) {
        this.startHour = startHour;
        this.endHour = endHour;
    }

    public boolean contains(int hour) {
        return hour >= startHour && hour < endHour;
    }
}
