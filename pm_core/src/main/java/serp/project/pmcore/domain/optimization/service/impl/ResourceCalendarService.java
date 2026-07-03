/**
 * Author: QuanTuanHuy
 * Description: Part of Serp Project
 */

package serp.project.pmcore.domain.optimization.service.impl;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import serp.project.pmcore.domain.optimization.constant.OptimizationConstants;
import serp.project.pmcore.domain.optimization.enums.CapacityCoverageStatus;
import serp.project.pmcore.domain.optimization.enums.OptimizationWarningCode;
import serp.project.pmcore.domain.optimization.model.CalendarCapacityResult;
import serp.project.pmcore.domain.optimization.model.OptimizationConstraintViolation;
import serp.project.pmcore.domain.optimization.model.ResourceCapacitySlot;
import serp.project.pmcore.domain.optimization.port.IResourceCalendarSlotReadPort;
import serp.project.pmcore.domain.optimization.service.IResourceCalendarService;

import java.time.DayOfWeek;
import java.time.Instant;
import java.time.LocalDate;
import java.time.LocalTime;
import java.time.ZoneOffset;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;

/**
 * Implementation of {@link IResourceCalendarService} that fetches working calendars from database
 * storage ports and constructs capacity slots.
 * <p>
 * If some or all users lack pre-configured calendar entries, the service generates a fallback weekday
 * calendar (8-hour workday starting at 8:00 AM local time, UTC+7) and emits warnings indicating
 * missing calendar data.
 */
@Service
@RequiredArgsConstructor
public class ResourceCalendarService implements IResourceCalendarService {
    private static final ZoneOffset VIETNAM_ZONE = ZoneOffset.ofHours(7);

    private final IResourceCalendarSlotReadPort resourceCalendarSlotReadPort;

    @Override
    public CalendarCapacityResult resolveWorkingCapacity(Long tenantId,
                                                         List<Long> userIds,
                                                         Long planningStart,
                                                         Long planningEnd) {
        if (userIds == null || userIds.isEmpty() || planningStart == null || planningEnd == null) {
            return notRequiredResult();
        }

        List<Long> uniqueUserIds = new ArrayList<>(new LinkedHashSet<>(userIds));
        
        // Query configured calendar slots from database that overlap with the planning window
        List<ResourceCapacitySlot> realSlots = resourceCalendarSlotReadPort.findOverlappingSlots(
                tenantId,
                uniqueUserIds,
                planningStart,
                planningEnd
        );
        
        // Identify users who lack configured calendars
        Set<Long> usersWithRealSlots = realSlots.stream()
                .map(ResourceCapacitySlot::assigneeId)
                .collect(Collectors.toCollection(LinkedHashSet::new));
        List<Long> missingUserIds = uniqueUserIds.stream()
                .filter(userId -> !usersWithRealSlots.contains(userId))
                .toList();

        List<ResourceCapacitySlot> slots = new ArrayList<>(realSlots.stream()
                .filter(slot -> slot.capacityMillis() > 0)
                .toList());
        List<OptimizationConstraintViolation> warnings = new ArrayList<>();
        
        // Supplement missing users with default fallback calendars
        if (!missingUserIds.isEmpty()) {
            CalendarCapacityResult fallback = fallbackWorkingCapacity(missingUserIds, planningStart, planningEnd);
            slots.addAll(fallback.slots());
            warnings.addAll(fallback.warnings());
        }

        slots.sort(Comparator.comparing(ResourceCapacitySlot::slotStart)
                .thenComparing(ResourceCapacitySlot::assigneeId));
        return new CalendarCapacityResult(
                slots,
                coverageStatus(realSlots, missingUserIds),
                missingUserIds,
                System.currentTimeMillis(),
                warnings
        );
    }

    private CalendarCapacityResult notRequiredResult() {
        return new CalendarCapacityResult(List.of(), CapacityCoverageStatus.NOT_REQUIRED, List.of(),
                System.currentTimeMillis(), List.of());
    }

    private CalendarCapacityResult fallbackWorkingCapacity(List<Long> userIds, Long planningStart, Long planningEnd) {
        if (userIds == null || userIds.isEmpty() || planningStart == null || planningEnd == null) {
            return notRequiredResult();
        }
        List<Long> uniqueUserIds = new ArrayList<>(new LinkedHashSet<>(userIds));
        List<ResourceCapacitySlot> slots = new ArrayList<>();
        
        // Generate daily capacity slots (8 hours per weekday, excluding weekends) in the local UTC+7 time zone
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

    private CapacityCoverageStatus coverageStatus(List<ResourceCapacitySlot> realSlots, List<Long> missingUserIds) {
        if (realSlots.isEmpty()) {
            return missingUserIds.isEmpty() ? CapacityCoverageStatus.NOT_REQUIRED : CapacityCoverageStatus.MISSING;
        }
        return missingUserIds.isEmpty() ? CapacityCoverageStatus.FULL : CapacityCoverageStatus.PARTIAL;
    }
}
