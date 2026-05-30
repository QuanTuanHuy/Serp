/**
 * Author: QuanTuanHuy
 * Description: Part of Serp Project
 */

package serp.project.pmcore.infrastructure.optimization.adapter;

import org.springframework.stereotype.Component;
import serp.project.pmcore.domain.optimization.constant.OptimizationConstants;
import serp.project.pmcore.domain.optimization.enums.CapacityCoverageStatus;
import serp.project.pmcore.domain.optimization.enums.OptimizationWarningCode;
import serp.project.pmcore.domain.optimization.model.CalendarCapacityResult;
import serp.project.pmcore.domain.optimization.model.OptimizationConstraintViolation;
import serp.project.pmcore.domain.optimization.model.ResourceCapacitySlot;
import serp.project.pmcore.domain.optimization.port.IResourceCalendarPort;

import java.time.DayOfWeek;
import java.time.Instant;
import java.time.LocalDate;
import java.time.LocalTime;
import java.time.ZoneOffset;
import java.util.ArrayList;
import java.util.LinkedHashSet;
import java.util.List;

@Component
public class FallbackResourceCalendarAdapter implements IResourceCalendarPort {
    private static final ZoneOffset VIETNAM_ZONE = ZoneOffset.ofHours(7);

    @Override
    public CalendarCapacityResult resolveWorkingCapacity(Long tenantId,
                                                         List<Long> userIds,
                                                         Long planningStart,
                                                         Long planningEnd) {
        if (userIds == null || userIds.isEmpty() || planningStart == null || planningEnd == null) {
            return new CalendarCapacityResult(List.of(), CapacityCoverageStatus.NOT_REQUIRED, List.of(),
                    System.currentTimeMillis(), List.of());
        }
        List<Long> uniqueUserIds = new ArrayList<>(new LinkedHashSet<>(userIds));
        List<ResourceCapacitySlot> slots = new ArrayList<>();
        LocalDate start = Instant.ofEpochMilli(planningStart).atZone(VIETNAM_ZONE).toLocalDate();
        LocalDate end = Instant.ofEpochMilli(planningEnd).atZone(VIETNAM_ZONE).toLocalDate();
        for (LocalDate day = start; !day.isAfter(end); day = day.plusDays(1)) {
            DayOfWeek dow = day.getDayOfWeek();
            if (dow == DayOfWeek.SATURDAY || dow == DayOfWeek.SUNDAY) {
                continue;
            }
            long slotStart = day.atTime(LocalTime.of(OptimizationConstants.FALLBACK_WORKDAY_START_HOUR_UTC_PLUS_7, 0))
                    .toInstant(VIETNAM_ZONE)
                    .toEpochMilli();
            long slotEnd = slotStart + OptimizationConstants.DAILY_CAPACITY_MILLIS;
            for (Long userId : uniqueUserIds) {
                slots.add(new ResourceCapacitySlot(userId, slotStart, slotEnd, OptimizationConstants.DAILY_CAPACITY_MILLIS));
            }
        }
        return new CalendarCapacityResult(slots, CapacityCoverageStatus.MISSING, uniqueUserIds, System.currentTimeMillis(),
                List.of(new OptimizationConstraintViolation(OptimizationWarningCode.MISSING_CALENDAR,
                        null, "Calendar data is unavailable; fallback weekday calendar used", null)));
    }
}
