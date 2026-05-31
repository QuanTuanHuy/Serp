/**
 * Author: QuanTuanHuy
 * Description: Part of Serp Project
 */

package serp.project.pmcore.infrastructure.optimization.adapter;

import org.junit.jupiter.api.Test;
import serp.project.pmcore.domain.optimization.enums.CapacityCoverageStatus;
import serp.project.pmcore.domain.optimization.model.CalendarCapacityResult;
import serp.project.pmcore.infrastructure.store.model.ResourceCalendarSlotModel;
import serp.project.pmcore.infrastructure.store.repository.IResourceCalendarSlotRepository;

import java.time.Instant;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.time.ZoneOffset;
import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

class DatabaseResourceCalendarAdapterTest {
    private static final ZoneOffset VIETNAM_ZONE = ZoneOffset.ofHours(7);
    private static final long START = LocalDate.of(2024, 5, 6)
            .atTime(LocalTime.MIDNIGHT)
            .toInstant(VIETNAM_ZONE)
            .toEpochMilli();
    private static final long HOUR = 3_600_000L;

    private final IResourceCalendarSlotRepository repository = mock(IResourceCalendarSlotRepository.class);
    private final FallbackResourceCalendarAdapter fallbackAdapter = new FallbackResourceCalendarAdapter();
    private final DatabaseResourceCalendarAdapter adapter = new DatabaseResourceCalendarAdapter(repository, fallbackAdapter);

    @Test
    void resolveWorkingCapacityShouldUseRealCalendarSlotsWhenAllUsersHaveCoverage() {
        when(repository.findOverlappingSlots(1L, List.of(100L), toLocal(START), toLocal(START + 8 * HOUR)))
                .thenReturn(List.of(slot(100L, START + HOUR, START + 3 * HOUR, 2 * HOUR)));

        CalendarCapacityResult result = adapter.resolveWorkingCapacity(1L, List.of(100L), START, START + 8 * HOUR);

        assertEquals(CapacityCoverageStatus.FULL, result.coverageStatus());
        assertTrue(result.fallbackUserIds().isEmpty());
        assertEquals(1, result.slots().size());
        assertEquals(START + HOUR, result.slots().get(0).slotStart());
        assertEquals(2 * HOUR, result.slots().get(0).capacityMillis());
    }

    @Test
    void resolveWorkingCapacityShouldFallbackOnlyForUsersMissingRealCalendarSlots() {
        when(repository.findOverlappingSlots(1L, List.of(100L, 200L), toLocal(START), toLocal(START + 8 * HOUR)))
                .thenReturn(List.of(slot(100L, START, START + 4 * HOUR, 4 * HOUR)));

        CalendarCapacityResult result = adapter.resolveWorkingCapacity(1L, List.of(100L, 200L), START, START + 8 * HOUR);

        assertEquals(CapacityCoverageStatus.PARTIAL, result.coverageStatus());
        assertEquals(List.of(200L), result.fallbackUserIds());
        assertTrue(result.slots().stream().anyMatch(slot -> slot.assigneeId().equals(100L)));
        assertTrue(result.slots().stream().anyMatch(slot -> slot.assigneeId().equals(200L)));
    }

    private ResourceCalendarSlotModel slot(Long userId, long start, long end, long capacityMillis) {
        return ResourceCalendarSlotModel.builder()
                .tenantId(1L)
                .userId(userId)
                .slotStart(toLocal(start))
                .slotEnd(toLocal(end))
                .capacityMillis(capacityMillis)
                .source("TEST")
                .build();
    }

    private LocalDateTime toLocal(long epochMillis) {
        return LocalDateTime.ofInstant(Instant.ofEpochMilli(epochMillis), ZoneOffset.UTC);
    }
}
