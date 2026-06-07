/**
 * Author: QuanTuanHuy
 * Description: Part of Serp Project
 */

package serp.project.pmcore.domain.resourcecalendar.model;

import serp.project.pmcore.domain.resourcecalendar.enums.ResourceCalendarSlotSource;

public record GeneratedResourceCalendarSlot(
        Long tenantId,
        Long userId,
        Long slotStart,
        Long slotEnd,
        Long capacityMillis,
        ResourceCalendarSlotSource source,
        String externalRef
) {
}
