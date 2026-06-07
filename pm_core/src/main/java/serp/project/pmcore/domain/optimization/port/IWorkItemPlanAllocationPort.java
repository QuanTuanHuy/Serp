/**
 * Author: QuanTuanHuy
 * Description: Part of Serp Project
 */

package serp.project.pmcore.domain.optimization.port;

import serp.project.pmcore.domain.optimization.entity.WorkItemPlanAllocationEntity;

import java.util.List;

public interface IWorkItemPlanAllocationPort {
    List<WorkItemPlanAllocationEntity> listByPlanIds(Long tenantId, List<Long> workItemPlanIds);

    List<WorkItemPlanAllocationEntity> replaceForPlan(Long tenantId,
                                                       Long workItemPlanId,
                                                       List<WorkItemPlanAllocationEntity> allocations);
}
