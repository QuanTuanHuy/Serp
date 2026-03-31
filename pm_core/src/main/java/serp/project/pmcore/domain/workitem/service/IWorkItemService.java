/**
 * Author: QuanTuanHuy
 * Description: Part of Serp Project
 */

package serp.project.pmcore.domain.workitem.service;

import serp.project.pmcore.domain.workitem.dto.WorkItemDeleteExecutionResult;
import serp.project.pmcore.domain.workitem.entity.WorkItemEntity;

public interface IWorkItemService {

    WorkItemEntity createWorkItem(WorkItemEntity workItem, Long tenantId, Long userId);

    WorkItemEntity getWorkItemById(Long id, Long tenantId);

    long getNextIssueNumber(Long projectId, Long tenantId);

    String getNextRank(Long projectId, Long tenantId);

    void validateParentHierarchy(Long parentId, Long childIssueTypeId,
            Long projectId, Long tenantId);

    WorkItemDeleteExecutionResult softDeleteWorkItem(Long rootWorkItemId,
                                                     Long projectId,
                                                     Long tenantId,
                                                     Long userId,
                                                     Long deletedAt);
}
