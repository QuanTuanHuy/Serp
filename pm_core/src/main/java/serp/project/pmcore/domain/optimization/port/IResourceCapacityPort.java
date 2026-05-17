/**
 * Author: QuanTuanHuy
 * Description: Part of Serp Project
 */

package serp.project.pmcore.domain.optimization.port;

import serp.project.pmcore.domain.optimization.model.ResourceCapacitySlot;
import serp.project.pmcore.domain.optimization.model.CapacityResolutionResult;

import java.util.List;

public interface IResourceCapacityPort {
    List<ResourceCapacitySlot> getCapacitySlots(Long tenantId,
                                                 List<Long> userIds,
                                                 Long planningStart,
                                                 Long planningEnd);

    CapacityResolutionResult resolveCapacity(Long tenantId,
                                             Long projectId,
                                             List<Long> userIds,
                                             Long planningStart,
                                             Long planningEnd,
                                             List<Long> excludedWorkItemIds);
}
