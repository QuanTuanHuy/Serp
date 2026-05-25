/**
 * Author: QuanTuanHuy
 * Description: Part of Serp Project
 */

package serp.project.crm.core.domain.constant;

import serp.project.crm.core.domain.entity.WorkingHoursEntity;

import java.time.DayOfWeek;
import java.util.List;

public final class WorkingHoursDefaults {

    private WorkingHoursDefaults() {
        // Utility class
    }

    public static final int DEFAULT_START_MINUTE = 8 * 60;  // 8:00 AM
    public static final int DEFAULT_END_MINUTE = 17 * 60;   // 5:00 PM

    public static final List<DayOfWeek> DEFAULT_WORKING_DAYS = List.of(
            DayOfWeek.MONDAY,
            DayOfWeek.TUESDAY,
            DayOfWeek.WEDNESDAY,
            DayOfWeek.THURSDAY,
            DayOfWeek.FRIDAY
    );

    public static WorkingHoursEntity createDefault(DayOfWeek dayOfWeek) {
        return WorkingHoursEntity.builder()
                .dayOfWeek(dayOfWeek)
                .workingDay(true)
                .startMinute(DEFAULT_START_MINUTE)
                .endMinute(DEFAULT_END_MINUTE)
                .build();
    }

    public static List<WorkingHoursEntity> createDefaultWeek() {
        return DEFAULT_WORKING_DAYS.stream()
                .map(WorkingHoursDefaults::createDefault)
                .toList();
    }
}
