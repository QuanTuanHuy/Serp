/**
 * Author: QuanTuanHuy
 * Description: Part of Serp Project
 */

package serp.project.pmcore.domain.workitem.port.read;

import serp.project.pmcore.domain.shared.pagination.PageResult;
import serp.project.pmcore.domain.project.entity.ProjectComponentEntity;
import serp.project.pmcore.domain.workitem.dto.ProjectSummaryActivityProjection;
import serp.project.pmcore.domain.workitem.dto.ProjectSummaryBreakdownProjection;
import serp.project.pmcore.domain.workitem.dto.ProjectSummaryCriteria;
import serp.project.pmcore.domain.workitem.dto.ProjectSummaryMetricsProjection;
import serp.project.pmcore.domain.workitem.dto.ProjectSummaryParentOptionProjection;
import serp.project.pmcore.domain.workitem.dto.WorkItemScheduleAllocationCalendarProjection;
import serp.project.pmcore.domain.workitem.dto.WorkItemScheduleCalendarCriteria;
import serp.project.pmcore.domain.workitem.dto.WorkItemTimelineCriteria;
import serp.project.pmcore.domain.workitem.dto.WorkItemTimelineDependencyProjection;
import serp.project.pmcore.domain.workitem.dto.WorkItemTimelineItemProjection;
import serp.project.pmcore.domain.workitem.dto.WorkItemActivityProjection;
import serp.project.pmcore.domain.workitem.dto.WorkItemBoardCriteria;
import serp.project.pmcore.domain.workitem.dto.WorkItemBoardItemProjection;
import serp.project.pmcore.domain.workitem.dto.WorkItemBoardStatusProjection;
import serp.project.pmcore.domain.workitem.dto.WorkItemChildProjection;
import serp.project.pmcore.domain.workitem.entity.WorkItemEntity;
import serp.project.pmcore.domain.workitem.dto.WorkItemDetailProjection;
import serp.project.pmcore.domain.workitem.dto.WorkItemLinkProjection;
import serp.project.pmcore.domain.workitem.dto.WorkItemSearchCriteria;

import java.util.List;
import java.util.Optional;

public interface IWorkItemReadPort {
    Optional<WorkItemEntity> getWorkItemById(Long id, Long tenantId);

    List<WorkItemEntity> getWorkItemsByProjectId(Long projectId, Long tenantId);

    List<WorkItemEntity> listActiveByWorkItemIds(Long tenantId, List<Long> workItemIds);

    List<WorkItemEntity> getWorkItemsByIssueTypeId(Long issueTypeId, Long tenantId);

    List<WorkItemEntity> getWorkItemsByPriorityId(Long priorityId, Long tenantId);

    List<WorkItemEntity> getWorkItemsByResolutionId(Long resolutionId, Long tenantId);

    boolean existsActiveWorkItemByStatusId(Long statusId, Long tenantId);

    List<Long> getActiveIssueTypeIdsInUseByProjectIds(Long tenantId, List<Long> projectIds, List<Long> issueTypeIds);

    List<Long> getActivePriorityIdsInUseByProjectIds(Long tenantId, List<Long> projectIds, List<Long> priorityIds);

    Optional<String> getLastRankByProjectId(Long projectId, Long tenantId);

    PageResult<WorkItemEntity> searchWorkItems(Long tenantId, WorkItemSearchCriteria criteria);

    Optional<WorkItemDetailProjection> getWorkItemDetailById(Long projectId, Long id, Long tenantId);

    List<WorkItemEntity> getActiveChildrenByParentId(Long parentId, Long tenantId);

    List<WorkItemChildProjection> listChildrenByParentId(Long projectId, Long parentId, Long tenantId);

    long countActiveChildrenByParentId(Long projectId, Long parentId, Long tenantId);

    long countDoneChildrenByParentId(Long projectId, Long parentId, Long tenantId);

    long countActiveLinksByWorkItemId(Long workItemId, Long tenantId);

    List<WorkItemLinkProjection> listLinksByWorkItemId(Long workItemId, Long tenantId);

    PageResult<WorkItemActivityProjection> listWorkItemActivities(Long workItemId,
                                                                  Long tenantId,
                                                                  String type,
                                                                  int page,
                                                                  int size);

    List<ProjectComponentEntity> getActiveComponentsByWorkItemId(Long workItemId, Long tenantId);

    PageResult<WorkItemTimelineItemProjection> listTimelineWorkItems(Long tenantId, WorkItemTimelineCriteria criteria);

    List<WorkItemTimelineDependencyProjection> listTimelineDependencies(Long tenantId, Long projectId, List<Long> workItemIds);

    PageResult<WorkItemScheduleAllocationCalendarProjection> listScheduleAllocationCalendarItems(
            Long tenantId,
            WorkItemScheduleCalendarCriteria criteria
    );

    List<WorkItemBoardStatusProjection> listBoardStatuses(Long tenantId, WorkItemBoardCriteria criteria);

    List<WorkItemBoardItemProjection> listBoardWorkItems(Long tenantId, WorkItemBoardCriteria criteria);

    ProjectSummaryMetricsProjection getProjectSummaryMetrics(Long tenantId,
                                                             ProjectSummaryCriteria criteria,
                                                             Long now,
                                                             Long sevenDaysAgo,
                                                             Long sevenDaysAhead);

    List<ProjectSummaryBreakdownProjection> listProjectSummaryStatuses(Long tenantId, ProjectSummaryCriteria criteria);

    List<ProjectSummaryBreakdownProjection> listProjectSummaryPriorities(Long tenantId, ProjectSummaryCriteria criteria);

    List<ProjectSummaryBreakdownProjection> listProjectSummaryIssueTypes(Long tenantId, ProjectSummaryCriteria criteria);

    PageResult<ProjectSummaryActivityProjection> listProjectSummaryActivities(Long tenantId, ProjectSummaryCriteria criteria);

    List<Long> listProjectSummaryAssigneeIds(Long tenantId, Long projectId);

    List<ProjectSummaryParentOptionProjection> listProjectSummaryParentOptions(Long tenantId, Long projectId);
}
