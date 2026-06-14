/**
 * Author: QuanTuanHuy
 * Description: Part of Serp Project
 */

package serp.project.pmcore.domain.optimization.port;

import serp.project.pmcore.domain.optimization.model.ResourceWorkloadAllocation;
import serp.project.pmcore.domain.optimization.model.ResourceWorkloadItem;
import serp.project.pmcore.domain.optimization.model.ResourceWorkloadPlan;

import java.util.List;

public interface IResourceWorkloadReadPort {
    List<ResourceWorkloadPlan> findActiveWorkloadPlans(Long tenantId,
                                                       List<Long> userIds,
                                                       Long planningStart,
                                                       Long planningEnd,
                                                       List<Long> excludedWorkItemIds);

    List<ResourceWorkloadItem> findActiveUnplannedWorkloadItems(Long tenantId,
                                                                List<Long> userIds,
                                                                Long planningStart,
                                                                Long planningEnd,
                                                                List<Long> excludedWorkItemIds);

    List<ResourceWorkloadItem> findWorkItemsByIds(Long tenantId, List<Long> workItemIds);

    List<ResourceWorkloadAllocation> findAllocationsByPlanIds(Long tenantId, List<Long> workItemPlanIds);
}
