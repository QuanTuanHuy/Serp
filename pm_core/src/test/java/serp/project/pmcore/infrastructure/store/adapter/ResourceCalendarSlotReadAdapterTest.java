/**
 * Author: QuanTuanHuy
 * Description: Part of Serp Project
 */

package serp.project.pmcore.infrastructure.store.adapter;

import org.junit.jupiter.api.Test;
import serp.project.pmcore.domain.optimization.model.ResourceCapacitySlot;
import serp.project.pmcore.infrastructure.store.model.ResourceCalendarSlotModel;
import serp.project.pmcore.infrastructure.store.repository.IResourceCalendarSlotRepository;

import java.time.Instant;
import java.time.LocalDateTime;
import java.time.ZoneOffset;
import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

class ResourceCalendarSlotReadAdapterTest {
    private static final long START = 1_714_960_800_000L;
    private static final long HOUR = 3_600_000L;

    private final IResourceCalendarSlotRepository repository = mock(IResourceCalendarSlotRepository.class);
    private final ResourceCalendarSlotReadAdapter adapter = new ResourceCalendarSlotReadAdapter(repository);

    @Test
    void findOverlappingSlotsShouldQueryRepositoryAndMapClippedCapacitySlots() {
        when(repository.findOverlappingSlots(1L, List.of(100L), toLocal(START), toLocal(START + 4 * HOUR)))
                .thenReturn(List.of(slot(100L, START - HOUR, START + 3 * HOUR, 8 * HOUR)));

        List<ResourceCapacitySlot> slots = adapter.findOverlappingSlots(1L, List.of(100L), START, START + 4 * HOUR);

        assertEquals(1, slots.size());
        assertEquals(100L, slots.getFirst().assigneeId());
        assertEquals(START, slots.getFirst().slotStart());
        assertEquals(START + 3 * HOUR, slots.getFirst().slotEnd());
        assertEquals(3 * HOUR, slots.getFirst().capacityMillis());
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
