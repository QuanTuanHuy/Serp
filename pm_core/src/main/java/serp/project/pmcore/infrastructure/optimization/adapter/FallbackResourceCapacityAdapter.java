/**
 * Author: QuanTuanHuy
 * Description: Part of Serp Project
 */

package serp.project.pmcore.infrastructure.optimization.adapter;

import org.springframework.stereotype.Component;
import serp.project.pmcore.domain.optimization.constant.OptimizationConstants;
import serp.project.pmcore.domain.optimization.model.ResourceCapacitySlot;
import serp.project.pmcore.domain.optimization.port.IResourceCapacityPort;

import java.time.DayOfWeek;
import java.time.Instant;
import java.time.LocalDate;
import java.time.ZoneOffset;
import java.util.ArrayList;
import java.util.LinkedHashSet;
import java.util.List;

@Component
public class FallbackResourceCapacityAdapter implements IResourceCapacityPort {
    @Override
    public List<ResourceCapacitySlot> getCapacitySlots(Long tenantId,
                                                       List<Long> userIds,
                                                       Long planningStart,
                                                       Long planningEnd) {
        if (userIds == null || userIds.isEmpty() || planningStart == null || planningEnd == null) {
            return List.of();
        }
        List<ResourceCapacitySlot> slots = new ArrayList<>();
        List<Long> uniqueUserIds = new ArrayList<>(new LinkedHashSet<>(userIds));
        LocalDate start = Instant.ofEpochMilli(planningStart).atZone(ZoneOffset.UTC).toLocalDate();
        LocalDate end = Instant.ofEpochMilli(planningEnd).atZone(ZoneOffset.UTC).toLocalDate();
        for (LocalDate day = start; !day.isAfter(end); day = day.plusDays(1)) {
            DayOfWeek dow = day.getDayOfWeek();
            if (dow == DayOfWeek.SATURDAY || dow == DayOfWeek.SUNDAY) {
                continue;
            }
            long slotStart = day.atStartOfDay().toInstant(ZoneOffset.UTC).toEpochMilli();
            long slotEnd = slotStart + OptimizationConstants.DAY_MILLIS;
            for (Long userId : uniqueUserIds) {
                slots.add(new ResourceCapacitySlot(userId, slotStart, slotEnd, OptimizationConstants.DAILY_CAPACITY_MILLIS));
            }
        }
        return slots;
    }
}
