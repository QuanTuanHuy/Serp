/**
 * Author: QuanTuanHuy
 * Description: Part of Serp Project
 */

package serp.project.pmcore.core.service;

import org.springframework.data.util.Pair;
import serp.project.pmcore.core.domain.dto.filter.WorkItemFilterRequest;
import serp.project.pmcore.core.domain.entity.WorkItemEntity;

import java.util.List;

public interface IWorkItemService {

    WorkItemEntity createWorkItem(WorkItemEntity workItem, Long tenantId, Long userId);

    WorkItemEntity getWorkItemById(Long id, Long tenantId);

    Pair<List<WorkItemEntity>, Long> searchWorkItems(Long tenantId, WorkItemFilterRequest filter);

    long getNextIssueNumber(Long projectId, Long tenantId);

    void validateParentHierarchy(Long parentId, Long childIssueTypeId,
            Long projectId, Long tenantId);
}
