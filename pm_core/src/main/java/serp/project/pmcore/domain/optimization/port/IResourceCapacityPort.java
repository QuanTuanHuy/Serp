/**
 * Author: QuanTuanHuy
 * Description: Part of Serp Project
 */

package serp.project.pmcore.domain.optimization.port;

import serp.project.pmcore.domain.optimization.model.ResourceCapacitySlot;

import java.util.List;

public interface IResourceCapacityPort {
    List<ResourceCapacitySlot> getCapacitySlots(Long tenantId,
                                                List<Long> userIds,
                                                Long planningStart,
                                                Long planningEnd);
}
