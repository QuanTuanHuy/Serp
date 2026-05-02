/**
 * Author: QuanTuanHuy
 * Description: Part of Serp Project
 */

package serp.project.crm.core.domain.entity;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import lombok.experimental.SuperBuilder;

import java.time.DayOfWeek;
import java.time.LocalTime;

@NoArgsConstructor
@AllArgsConstructor
@Getter
@Setter
@SuperBuilder
public class WorkingHoursEntity extends BaseEntity {
    private Long teamMemberId;
    private DayOfWeek dayOfWeek;
    private Boolean workingDay;
    private Integer startMinute;
    private Integer endMinute;

    public boolean covers(LocalTime startTime, LocalTime endTime) {
        if (startTime == null || endTime == null) {
            return false;
        }
        if (!Boolean.TRUE.equals(workingDay)) {
            return false;
        }
        if (startMinute == null || endMinute == null) {
            return false;
        }

        LocalTime availableStart = minuteToLocalTime(startMinute);
        LocalTime availableEnd = minuteToLocalTime(endMinute);
        return !startTime.isBefore(availableStart) && !endTime.isAfter(availableEnd);
    }

    private LocalTime minuteToLocalTime(int minute) {
        return LocalTime.of(minute / 60, minute % 60);
    }
}
