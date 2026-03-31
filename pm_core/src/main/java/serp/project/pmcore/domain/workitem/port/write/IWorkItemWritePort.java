/**
 * Author: QuanTuanHuy
 * Description: Part of Serp Project
 */

package serp.project.pmcore.domain.workitem.port.write;

import serp.project.pmcore.domain.workitem.dto.WorkItemDeleteExecutionResult;
import serp.project.pmcore.domain.workitem.entity.WorkItemEntity;

import java.util.Set;

public interface IWorkItemWritePort {
    WorkItemEntity saveWorkItem(WorkItemEntity workItem);

    WorkItemDeleteExecutionResult softDeleteWorkItems(Long projectId,
                                                      Long tenantId,
                                                      Set<Long> workItemIds,
                                                      Long userId,
                                                      Long deletedAt);
}
