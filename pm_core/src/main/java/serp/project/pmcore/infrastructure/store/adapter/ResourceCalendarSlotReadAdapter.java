/**
 * Author: QuanTuanHuy
 * Description: Part of Serp Project
 */

package serp.project.pmcore.infrastructure.store.adapter;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;
import serp.project.pmcore.domain.optimization.model.ResourceCapacitySlot;
import serp.project.pmcore.domain.optimization.port.IResourceCalendarSlotReadPort;
import serp.project.pmcore.infrastructure.store.model.ResourceCalendarSlotModel;
import serp.project.pmcore.infrastructure.store.repository.IResourceCalendarSlotRepository;

import java.time.Instant;
import java.time.LocalDateTime;
import java.time.ZoneOffset;
import java.util.List;

@Component
@RequiredArgsConstructor
public class ResourceCalendarSlotReadAdapter implements IResourceCalendarSlotReadPort {
    private final IResourceCalendarSlotRepository resourceCalendarSlotRepository;

    @Override
    public List<ResourceCapacitySlot> findOverlappingSlots(Long tenantId,
                                                           List<Long> userIds,
                                                           Long planningStart,
                                                           Long planningEnd) {
        if (userIds == null || userIds.isEmpty() || planningStart == null || planningEnd == null) {
            return List.of();
        }
        return resourceCalendarSlotRepository.findOverlappingSlots(
                        tenantId,
                        userIds,
                        toLocalDateTime(planningStart),
                        toLocalDateTime(planningEnd)
                )
                .stream()
                .map(slot -> toCapacitySlot(slot, planningStart, planningEnd))
                .toList();
    }

    private ResourceCapacitySlot toCapacitySlot(ResourceCalendarSlotModel slot, long planningStart, long planningEnd) {
        long slotStart = Math.max(toEpochMillis(slot.getSlotStart()), planningStart);
        long slotEnd = Math.min(toEpochMillis(slot.getSlotEnd()), planningEnd);
        long availableMillis = Math.max(0L, slotEnd - slotStart);
        long capacityMillis = Math.min(slot.getCapacityMillis() == null ? 0L : slot.getCapacityMillis(), availableMillis);
        return new ResourceCapacitySlot(slot.getUserId(), slotStart, slotEnd, capacityMillis);
    }

    private LocalDateTime toLocalDateTime(long epochMillis) {
        return LocalDateTime.ofInstant(Instant.ofEpochMilli(epochMillis), ZoneOffset.UTC);
    }

    private long toEpochMillis(LocalDateTime dateTime) {
        return dateTime.toInstant(ZoneOffset.UTC).toEpochMilli();
    }
}
