/**
 * Author: QuanTuanHuy
 * Description: Part of Serp Project
 */

package serp.project.pmcore.infrastructure.optimization.adapter;

import org.springframework.context.annotation.Primary;
import org.springframework.stereotype.Component;
import serp.project.pmcore.domain.optimization.enums.CapacityCoverageStatus;
import serp.project.pmcore.domain.optimization.model.CalendarCapacityResult;
import serp.project.pmcore.domain.optimization.model.OptimizationConstraintViolation;
import serp.project.pmcore.domain.optimization.model.ResourceCapacitySlot;
import serp.project.pmcore.domain.optimization.port.IResourceCalendarPort;
import serp.project.pmcore.infrastructure.store.model.ResourceCalendarSlotModel;
import serp.project.pmcore.infrastructure.store.repository.IResourceCalendarSlotRepository;

import java.time.Instant;
import java.time.LocalDateTime;
import java.time.ZoneOffset;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;

@Primary
@Component
public class DatabaseResourceCalendarAdapter implements IResourceCalendarPort {
    private final IResourceCalendarSlotRepository resourceCalendarSlotRepository;
    private final FallbackResourceCalendarAdapter fallbackResourceCalendarAdapter;

    public DatabaseResourceCalendarAdapter(IResourceCalendarSlotRepository resourceCalendarSlotRepository,
                                           FallbackResourceCalendarAdapter fallbackResourceCalendarAdapter) {
        this.resourceCalendarSlotRepository = resourceCalendarSlotRepository;
        this.fallbackResourceCalendarAdapter = fallbackResourceCalendarAdapter;
    }

    @Override
    public CalendarCapacityResult resolveWorkingCapacity(Long tenantId,
                                                         List<Long> userIds,
                                                         Long planningStart,
                                                         Long planningEnd) {
        if (userIds == null || userIds.isEmpty() || planningStart == null || planningEnd == null) {
            return fallbackResourceCalendarAdapter.resolveWorkingCapacity(tenantId, userIds, planningStart, planningEnd);
        }

        List<Long> uniqueUserIds = new ArrayList<>(new LinkedHashSet<>(userIds));
        List<ResourceCalendarSlotModel> realSlots = resourceCalendarSlotRepository.findOverlappingSlots(
                tenantId,
                uniqueUserIds,
                toLocalDateTime(planningStart),
                toLocalDateTime(planningEnd)
        );
        Set<Long> usersWithRealSlots = realSlots.stream()
                .map(ResourceCalendarSlotModel::getUserId)
                .collect(Collectors.toCollection(LinkedHashSet::new));
        List<Long> missingUserIds = uniqueUserIds.stream()
                .filter(userId -> !usersWithRealSlots.contains(userId))
                .toList();

        List<ResourceCapacitySlot> slots = new ArrayList<>(realSlots.stream()
                .map(slot -> toCapacitySlot(slot, planningStart, planningEnd))
                .filter(slot -> slot.capacityMillis() > 0)
                .toList());
        List<OptimizationConstraintViolation> warnings = new ArrayList<>();
        if (!missingUserIds.isEmpty()) {
            CalendarCapacityResult fallback = fallbackResourceCalendarAdapter.resolveWorkingCapacity(
                    tenantId,
                    missingUserIds,
                    planningStart,
                    planningEnd
            );
            slots.addAll(fallback.slots());
            warnings.addAll(fallback.warnings());
        }

        slots.sort(Comparator.comparing(ResourceCapacitySlot::slotStart)
                .thenComparing(ResourceCapacitySlot::assigneeId));
        CapacityCoverageStatus coverageStatus = coverageStatus(realSlots, missingUserIds);
        return new CalendarCapacityResult(slots, coverageStatus, missingUserIds, System.currentTimeMillis(), warnings);
    }

    private ResourceCapacitySlot toCapacitySlot(ResourceCalendarSlotModel slot, long planningStart, long planningEnd) {
        long slotStart = Math.max(toEpochMillis(slot.getSlotStart()), planningStart);
        long slotEnd = Math.min(toEpochMillis(slot.getSlotEnd()), planningEnd);
        long availableMillis = Math.max(0L, slotEnd - slotStart);
        long capacityMillis = Math.min(slot.getCapacityMillis() == null ? 0L : slot.getCapacityMillis(), availableMillis);
        return new ResourceCapacitySlot(slot.getUserId(), slotStart, slotEnd, capacityMillis);
    }

    private CapacityCoverageStatus coverageStatus(List<ResourceCalendarSlotModel> realSlots, List<Long> missingUserIds) {
        if (realSlots.isEmpty()) {
            return missingUserIds.isEmpty() ? CapacityCoverageStatus.NOT_REQUIRED : CapacityCoverageStatus.MISSING;
        }
        return missingUserIds.isEmpty() ? CapacityCoverageStatus.FULL : CapacityCoverageStatus.PARTIAL;
    }

    private LocalDateTime toLocalDateTime(long epochMillis) {
        return LocalDateTime.ofInstant(Instant.ofEpochMilli(epochMillis), ZoneOffset.UTC);
    }

    private long toEpochMillis(LocalDateTime dateTime) {
        return dateTime.toInstant(ZoneOffset.UTC).toEpochMilli();
    }
}
