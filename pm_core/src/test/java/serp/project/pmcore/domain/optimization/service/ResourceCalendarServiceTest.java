/**
 * Author: QuanTuanHuy
 * Description: Part of Serp Project
 */

package serp.project.pmcore.domain.optimization.service;

import org.junit.jupiter.api.Test;
import serp.project.pmcore.domain.optimization.enums.CapacityCoverageStatus;
import serp.project.pmcore.domain.optimization.enums.OptimizationWarningCode;
import serp.project.pmcore.domain.optimization.model.CalendarCapacityResult;
import serp.project.pmcore.domain.optimization.model.ResourceCapacitySlot;
import serp.project.pmcore.domain.optimization.port.IResourceCalendarSlotReadPort;
import serp.project.pmcore.domain.optimization.service.impl.ResourceCalendarService;

import java.time.Instant;
import java.time.LocalDate;
import java.time.LocalTime;
import java.time.ZoneOffset;
import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

class ResourceCalendarServiceTest {
    private static final ZoneOffset VIETNAM_ZONE = ZoneOffset.ofHours(7);
    private static final long START = LocalDate.of(2024, 5, 6)
            .atTime(LocalTime.MIDNIGHT)
            .toInstant(VIETNAM_ZONE)
            .toEpochMilli();
    private static final long HOUR = 3_600_000L;

    @Test
    void resolveWorkingCapacityShouldUseRealCalendarSlotsWhenAllUsersHaveCoverage() {
        IResourceCalendarSlotReadPort readPort = (tenantId, userIds, planningStart, planningEnd) ->
                List.of(new ResourceCapacitySlot(100L, START + HOUR, START + 3 * HOUR, 2 * HOUR));
        ResourceCalendarService service = new ResourceCalendarService(readPort);

        CalendarCapacityResult result = service.resolveWorkingCapacity(1L, List.of(100L), START, START + 8 * HOUR);

        assertEquals(CapacityCoverageStatus.FULL, result.coverageStatus());
        assertTrue(result.fallbackUserIds().isEmpty());
        assertEquals(1, result.slots().size());
        assertEquals(START + HOUR, result.slots().getFirst().slotStart());
        assertEquals(2 * HOUR, result.slots().getFirst().capacityMillis());
    }

    @Test
    void resolveWorkingCapacityShouldFallbackOnlyForUsersMissingRealCalendarSlots() {
        IResourceCalendarSlotReadPort readPort = (tenantId, userIds, planningStart, planningEnd) ->
                List.of(new ResourceCapacitySlot(100L, START, START + 4 * HOUR, 4 * HOUR));
        ResourceCalendarService service = new ResourceCalendarService(readPort);

        CalendarCapacityResult result = service.resolveWorkingCapacity(1L, List.of(100L, 200L), START, START + 8 * HOUR);

        assertEquals(CapacityCoverageStatus.PARTIAL, result.coverageStatus());
        assertEquals(List.of(200L), result.fallbackUserIds());
        assertTrue(result.slots().stream().anyMatch(slot -> slot.assigneeId().equals(100L)));
        assertTrue(result.slots().stream().anyMatch(slot -> slot.assigneeId().equals(200L)));
        assertTrue(result.warnings().stream().anyMatch(warning -> warning.code() == OptimizationWarningCode.MISSING_CALENDAR));
    }

    @Test
    void resolveWorkingCapacityShouldReturnDeterministicVietnamWeekdayFallbackSlots() {
        IResourceCalendarSlotReadPort readPort = (tenantId, userIds, planningStart, planningEnd) -> List.of();
        ResourceCalendarService service = new ResourceCalendarService(readPort);
        long monday = epochMillis(2024, 5, 6, 0, 0);
        long sunday = epochMillis(2024, 5, 12, 23, 59);

        CalendarCapacityResult result = service.resolveWorkingCapacity(1L, List.of(200L, 100L, 200L), monday, sunday);

        assertEquals(CapacityCoverageStatus.MISSING, result.coverageStatus());
        assertEquals(List.of(200L, 100L), result.fallbackUserIds());
        assertEquals(10, result.slots().size());
        assertEquals(100L, result.slots().getFirst().assigneeId());
        assertEquals(epochMillis(2024, 5, 6, 9, 0), result.slots().getFirst().slotStart());
        assertTrue(result.slots().stream()
                .noneMatch(slot -> Instant.ofEpochMilli(slot.slotStart()).atZone(VIETNAM_ZONE).getDayOfWeek().getValue() > 5));
    }

    @Test
    void resolveWorkingCapacityShouldReturnNotRequiredWhenNoUsers() {
        IResourceCalendarSlotReadPort readPort = (tenantId, userIds, planningStart, planningEnd) -> {
            throw new AssertionError("calendar slot port should not be called");
        };
        ResourceCalendarService service = new ResourceCalendarService(readPort);

        CalendarCapacityResult result = service.resolveWorkingCapacity(1L, List.of(), 1L, 2L);

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
