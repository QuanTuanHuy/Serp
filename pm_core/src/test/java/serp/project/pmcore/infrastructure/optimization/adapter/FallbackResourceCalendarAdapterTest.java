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
import java.time.ZoneOffset;
import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

class FallbackResourceCalendarAdapterTest {
    private final FallbackResourceCalendarAdapter adapter = new FallbackResourceCalendarAdapter();

    @Test
    void resolveWorkingCapacityShouldReturnDeterministicUtcWeekdaySlots() {
        long monday = LocalDate.of(2024, 5, 6).atStartOfDay().toInstant(ZoneOffset.UTC).toEpochMilli();
        long sunday = LocalDate.of(2024, 5, 12).atStartOfDay().toInstant(ZoneOffset.UTC).toEpochMilli();

        CalendarCapacityResult result = adapter.resolveWorkingCapacity(1L, List.of(200L, 100L, 200L), monday, sunday);

        assertEquals(CapacityCoverageStatus.MISSING, result.coverageStatus());
        assertEquals(List.of(200L, 100L), result.fallbackUserIds());
        assertEquals(10, result.slots().size());
        assertEquals(200L, result.slots().get(0).assigneeId());
        assertEquals(monday, result.slots().get(0).slotStart());
        assertEquals(OptimizationConstants.DAILY_CAPACITY_MILLIS, result.slots().get(0).capacityMillis());
        assertTrue(result.slots().stream().noneMatch(slot -> Instant.ofEpochMilli(slot.slotStart()).atZone(ZoneOffset.UTC).getDayOfWeek().getValue() > 5));
    }

    @Test
    void resolveWorkingCapacityShouldEmitMissingCalendarWarning() {
        long monday = LocalDate.of(2024, 5, 6).atStartOfDay().toInstant(ZoneOffset.UTC).toEpochMilli();

        CalendarCapacityResult result = adapter.resolveWorkingCapacity(1L, List.of(100L), monday, monday);

        assertTrue(result.warnings().stream().anyMatch(warning -> warning.code() == OptimizationWarningCode.MISSING_CALENDAR));
    }

    @Test
    void resolveWorkingCapacityShouldReturnNotRequiredWhenNoUsers() {
        CalendarCapacityResult result = adapter.resolveWorkingCapacity(1L, List.of(), 1L, 2L);

        assertEquals(CapacityCoverageStatus.NOT_REQUIRED, result.coverageStatus());
        assertTrue(result.slots().isEmpty());
    }
}
