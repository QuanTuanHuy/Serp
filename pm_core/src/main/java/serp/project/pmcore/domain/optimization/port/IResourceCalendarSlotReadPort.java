/**
 * Author: QuanTuanHuy
 * Description: Part of Serp Project
 */

package serp.project.pmcore.domain.optimization.port;

import serp.project.pmcore.domain.optimization.model.ResourceCapacitySlot;

import java.util.List;

public interface IResourceCalendarSlotReadPort {
    List<ResourceCapacitySlot> findOverlappingSlots(Long tenantId,
                                                    List<Long> userIds,
                                                    Long planningStart,
                                                    Long planningEnd);
}
