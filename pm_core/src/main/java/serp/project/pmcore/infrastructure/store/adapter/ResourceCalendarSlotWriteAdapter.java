/**
 * Author: QuanTuanHuy
 * Description: Part of Serp Project
 */

package serp.project.pmcore.infrastructure.store.adapter;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;
import serp.project.pmcore.domain.resourcecalendar.enums.ResourceCalendarSlotSource;
import serp.project.pmcore.domain.resourcecalendar.model.GeneratedResourceCalendarSlot;
import serp.project.pmcore.domain.resourcecalendar.port.IResourceCalendarSlotWritePort;
import serp.project.pmcore.infrastructure.store.model.ResourceCalendarSlotModel;
import serp.project.pmcore.infrastructure.store.repository.IResourceCalendarSlotRepository;

import java.time.Instant;
import java.time.LocalDateTime;
import java.time.ZoneOffset;
import java.util.List;

@Component
@RequiredArgsConstructor
public class ResourceCalendarSlotWriteAdapter implements IResourceCalendarSlotWritePort {
    private static final List<String> GENERATED_SOURCES = List.of(
            ResourceCalendarSlotSource.PROFILE.name(),
            ResourceCalendarSlotSource.EXCEPTION.name()
    );

    private final IResourceCalendarSlotRepository resourceCalendarSlotRepository;

    @Override
    public void replaceGeneratedSlots(Long tenantId,
                                      List<Long> userIds,
                                      Long windowStart,
                                      Long windowEnd,
                                      List<GeneratedResourceCalendarSlot> slots) {
        if (tenantId == null || userIds == null || userIds.isEmpty() || windowStart == null || windowEnd == null) {
            return;
        }
        resourceCalendarSlotRepository.hardDeleteGeneratedSlots(
                tenantId,
                userIds,
                toLocalDateTime(windowStart),
                toLocalDateTime(windowEnd),
                GENERATED_SOURCES
        );
        if (slots == null || slots.isEmpty()) {
            return;
        }
        resourceCalendarSlotRepository.saveAll(slots.stream()
                .filter(slot -> slot.capacityMillis() != null && slot.capacityMillis() > 0)
                .map(this::toModel)
                .toList());
    }

    private ResourceCalendarSlotModel toModel(GeneratedResourceCalendarSlot slot) {
        return ResourceCalendarSlotModel.builder()
                .tenantId(slot.tenantId())
                .userId(slot.userId())
                .slotStart(toLocalDateTime(slot.slotStart()))
                .slotEnd(toLocalDateTime(slot.slotEnd()))
                .capacityMillis(slot.capacityMillis())
                .source(slot.source().name())
                .externalRef(slot.externalRef())
                .build();
    }

    private LocalDateTime toLocalDateTime(long epochMillis) {
        return LocalDateTime.ofInstant(Instant.ofEpochMilli(epochMillis), ZoneOffset.UTC);
    }
}
