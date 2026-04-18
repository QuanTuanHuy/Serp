/**
 * Author: QuanTuanHuy
 * Description: Part of Serp Project
 */

package serp.project.pmcore.domain.workitem.port.read;

import serp.project.pmcore.domain.shared.pagination.PageResult;
import serp.project.pmcore.domain.workitem.entity.WorkItemEntity;
import serp.project.pmcore.domain.workitem.dto.WorkItemDetailProjection;
import serp.project.pmcore.domain.workitem.dto.WorkItemSearchCriteria;

import java.util.List;
import java.util.Optional;

public interface IWorkItemReadPort {
    Optional<WorkItemEntity> getWorkItemById(Long id, Long tenantId);

    List<WorkItemEntity> getWorkItemsByProjectId(Long projectId, Long tenantId);

    List<WorkItemEntity> getWorkItemsByIssueTypeId(Long issueTypeId, Long tenantId);

    List<WorkItemEntity> getWorkItemsByPriorityId(Long priorityId, Long tenantId);

    boolean existsActiveWorkItemByProjectIdsAndIssueTypeId(Long tenantId, List<Long> projectIds, Long issueTypeId);

    Optional<String> getLastRankByProjectId(Long projectId, Long tenantId);

    PageResult<WorkItemEntity> searchWorkItems(Long tenantId, WorkItemSearchCriteria criteria);

    Optional<WorkItemDetailProjection> getWorkItemDetailById(Long id, Long tenantId);

    List<WorkItemEntity> getActiveChildrenByParentId(Long parentId, Long tenantId);
}
