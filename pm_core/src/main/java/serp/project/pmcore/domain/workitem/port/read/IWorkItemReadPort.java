/**
 * Author: QuanTuanHuy
 * Description: Part of Serp Project
 */

package serp.project.pmcore.domain.workitem.port.read;

import serp.project.pmcore.domain.shared.pagination.PageResult;
import serp.project.pmcore.domain.project.entity.ProjectComponentEntity;
import serp.project.pmcore.domain.workitem.dto.WorkItemTimelineCriteria;
import serp.project.pmcore.domain.workitem.dto.WorkItemTimelineDependencyProjection;
import serp.project.pmcore.domain.workitem.dto.WorkItemTimelineItemProjection;
import serp.project.pmcore.domain.workitem.dto.WorkItemBoardCriteria;
import serp.project.pmcore.domain.workitem.dto.WorkItemBoardItemProjection;
import serp.project.pmcore.domain.workitem.dto.WorkItemBoardStatusProjection;
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

    boolean existsActiveWorkItemByStatusId(Long statusId, Long tenantId);

    List<Long> getActiveIssueTypeIdsInUseByProjectIds(Long tenantId, List<Long> projectIds, List<Long> issueTypeIds);

    List<Long> getActivePriorityIdsInUseByProjectIds(Long tenantId, List<Long> projectIds, List<Long> priorityIds);

    Optional<String> getLastRankByProjectId(Long projectId, Long tenantId);

    PageResult<WorkItemEntity> searchWorkItems(Long tenantId, WorkItemSearchCriteria criteria);

    Optional<WorkItemDetailProjection> getWorkItemDetailById(Long id, Long tenantId);

    List<WorkItemEntity> getActiveChildrenByParentId(Long parentId, Long tenantId);

    List<ProjectComponentEntity> getActiveComponentsByWorkItemId(Long workItemId, Long tenantId);

    PageResult<WorkItemTimelineItemProjection> listTimelineWorkItems(Long tenantId, WorkItemTimelineCriteria criteria);

    List<WorkItemTimelineDependencyProjection> listTimelineDependencies(Long tenantId, Long projectId, List<Long> workItemIds);

    List<WorkItemBoardStatusProjection> listBoardStatuses(Long tenantId, WorkItemBoardCriteria criteria);

    List<WorkItemBoardItemProjection> listBoardWorkItems(Long tenantId, WorkItemBoardCriteria criteria);
}
