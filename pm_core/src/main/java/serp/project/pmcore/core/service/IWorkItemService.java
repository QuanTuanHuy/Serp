/**
 * Author: QuanTuanHuy
 * Description: Part of Serp Project
 */

package serp.project.pmcore.core.service;

import serp.project.pmcore.core.domain.entity.WorkItemEntity;

public interface IWorkItemService {

    WorkItemEntity createWorkItem(WorkItemEntity workItem, Long tenantId, Long userId);

    WorkItemEntity getWorkItemById(Long id, Long tenantId);

    long getNextIssueNumber(Long projectId, Long tenantId);

    void validateParentHierarchy(Long parentId, Long childIssueTypeId,
            Long projectId, Long tenantId);
}
