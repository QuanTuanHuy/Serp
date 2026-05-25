/**
 * Author: QuanTuanHuy
 * Description: Part of Serp Project
 */

package serp.project.pmcore.domain.optimization.port;

import serp.project.pmcore.domain.optimization.entity.WorkItemPlanEntity;

import java.util.List;
import java.util.Optional;

public interface IWorkItemPlanPort {
    List<WorkItemPlanEntity> listActivePlansByWorkItemIds(Long tenantId, List<Long> workItemIds);

    Optional<WorkItemPlanEntity> getActivePlanByWorkItemId(Long tenantId, Long workItemId);

    WorkItemPlanEntity upsertActivePlan(WorkItemPlanEntity plan);
}
