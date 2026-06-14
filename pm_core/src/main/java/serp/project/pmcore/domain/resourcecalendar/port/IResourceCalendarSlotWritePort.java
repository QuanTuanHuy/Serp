/**
 * Author: QuanTuanHuy
 * Description: Part of Serp Project
 */

package serp.project.pmcore.domain.resourcecalendar.port;

import serp.project.pmcore.domain.resourcecalendar.model.GeneratedResourceCalendarSlot;

import java.util.List;

public interface IResourceCalendarSlotWritePort {
    void replaceGeneratedSlots(Long tenantId,
                               List<Long> userIds,
                               Long windowStart,
                               Long windowEnd,
                               List<GeneratedResourceCalendarSlot> slots);
}
