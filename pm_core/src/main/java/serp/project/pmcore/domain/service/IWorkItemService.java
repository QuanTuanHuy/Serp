/**
 * Author: QuanTuanHuy
 * Description: Part of Serp Project
 */

package serp.project.pmcore.domain.service;

import org.springframework.data.util.Pair;
import serp.project.pmcore.domain.dto.filter.WorkItemFilterRequest;
import serp.project.pmcore.domain.entity.workitem.WorkItemEntity;

import java.util.List;

public interface IWorkItemService {

    WorkItemEntity createWorkItem(WorkItemEntity workItem, Long tenantId, Long userId);

    WorkItemEntity getWorkItemById(Long id, Long tenantId);

    Pair<List<WorkItemEntity>, Long> searchWorkItems(Long tenantId, WorkItemFilterRequest filter);

    long getNextIssueNumber(Long projectId, Long tenantId);

    String getNextRank(Long projectId, Long tenantId);

    void validateParentHierarchy(Long parentId, Long childIssueTypeId,
            Long projectId, Long tenantId);
}
