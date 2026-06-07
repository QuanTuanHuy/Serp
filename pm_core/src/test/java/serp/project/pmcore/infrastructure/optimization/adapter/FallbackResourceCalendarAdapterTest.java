/**
 * Author: QuanTuanHuy
 * Description: Part of Serp Project
 */

package serp.project.pmcore.infrastructure.optimization.adapter;

import org.junit.jupiter.api.Test;
import serp.project.pmcore.domain.optimization.constant.OptimizationConstants;
import serp.project.pmcore.domain.optimization.enums.CapacityCoverageStatus;
import serp.project.pmcore.domain.optimization.enums.OptimizationWarningCode;
import serp.project.pmcore.domain.optimization.model.CalendarCapacityResult;

import java.time.Instant;
import java.time.LocalDate;
import java.time.LocalTime;
import java.time.ZoneOffset;
import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

class FallbackResourceCalendarAdapterTest {
    private static final ZoneOffset VIETNAM_ZONE = ZoneOffset.ofHours(7);
    private final FallbackResourceCalendarAdapter adapter = new FallbackResourceCalendarAdapter();

    @Test
    void resolveWorkingCapacityShouldReturnDeterministicVietnamWeekdaySlots() {
        long monday = epochMillis(2024, 5, 6, 0, 0);
        long sunday = epochMillis(2024, 5, 12, 23, 59);

        CalendarCapacityResult result = adapter.resolveWorkingCapacity(1L, List.of(200L, 100L, 200L), monday, sunday);

        assertEquals(CapacityCoverageStatus.MISSING, result.coverageStatus());
        assertEquals(List.of(200L, 100L), result.fallbackUserIds());
        assertEquals(10, result.slots().size());
        assertEquals(200L, result.slots().get(0).assigneeId());
        assertEquals(epochMillis(2024, 5, 6, 9, 0), result.slots().get(0).slotStart());
        assertEquals(OptimizationConstants.DAILY_CAPACITY_MILLIS, result.slots().get(0).capacityMillis());
        assertTrue(result.slots().stream()
                .noneMatch(slot -> Instant.ofEpochMilli(slot.slotStart()).atZone(VIETNAM_ZONE).getDayOfWeek().getValue() > 5));
    }

    @Test
    void resolveWorkingCapacityShouldUseVietnamWorkdaySlots() {
        long planningStart = epochMillis(2026, 6, 11, 0, 0);
        long planningEnd = epochMillis(2026, 6, 11, 23, 59);

        var result = adapter.resolveWorkingCapacity(1L, List.of(100L), planningStart, planningEnd);

        assertEquals(1, result.slots().size());
        assertEquals(epochMillis(2026, 6, 11, 9, 0), result.slots().get(0).slotStart());
        assertEquals(epochMillis(2026, 6, 11, 17, 0), result.slots().get(0).slotEnd());
        assertEquals(28_800_000L, result.slots().get(0).capacityMillis());
    }

    @Test
    void resolveWorkingCapacityShouldEmitMissingCalendarWarning() {
        long monday = epochMillis(2024, 5, 6, 0, 0);

        CalendarCapacityResult result = adapter.resolveWorkingCapacity(1L, List.of(100L), monday, monday);

        assertTrue(result.warnings().stream().anyMatch(warning -> warning.code() == OptimizationWarningCode.MISSING_CALENDAR));
    }

    @Test
    void resolveWorkingCapacityShouldReturnNotRequiredWhenNoUsers() {
        CalendarCapacityResult result = adapter.resolveWorkingCapacity(1L, List.of(), 1L, 2L);

        assertEquals(CapacityCoverageStatus.NOT_REQUIRED, result.coverageStatus());
        assertTrue(result.slots().isEmpty());
    }

    private long epochMillis(int year, int month, int day, int hour, int minute) {
        return LocalDate.of(year, month, day)
                .atTime(LocalTime.of(hour, minute))
                .toInstant(VIETNAM_ZONE)
                .toEpochMilli();
    }
}
