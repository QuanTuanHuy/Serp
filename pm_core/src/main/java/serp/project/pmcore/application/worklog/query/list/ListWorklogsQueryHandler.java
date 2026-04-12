/**
 * Author: QuanTuanHuy
 * Description: Part of Serp Project
 */

package serp.project.pmcore.application.worklog.query.list;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import serp.project.pmcore.application.shared.cqrs.query.IQueryHandler;
import serp.project.pmcore.application.worklog.WorklogListPageView;
import serp.project.pmcore.application.worklog.WorklogValidator;
import serp.project.pmcore.application.worklog.WorklogView;
import serp.project.pmcore.domain.project.dto.ProjectPermissionEvaluationContext;
import serp.project.pmcore.domain.project.entity.ProjectEntity;
import serp.project.pmcore.domain.project.service.IProjectService;
import serp.project.pmcore.domain.shared.exception.ResourceNotFoundException;
import serp.project.pmcore.domain.shared.pagination.PageResult;
import serp.project.pmcore.domain.workitem.entity.WorkItemEntity;
import serp.project.pmcore.domain.workitem.service.IWorkItemAuthorizationSupportService;
import serp.project.pmcore.domain.workitem.service.IWorkItemService;
import serp.project.pmcore.domain.worklog.entity.WorklogEntity;
import serp.project.pmcore.domain.worklog.query.WorklogListCriteria;
import serp.project.pmcore.domain.worklog.service.IWorklogAuthorizationService;
import serp.project.pmcore.domain.worklog.service.IWorklogService;

import java.util.Objects;

@Service
@RequiredArgsConstructor
public class ListWorklogsQueryHandler implements IQueryHandler<ListWorklogsQuery, WorklogListPageView> {

    private final WorklogValidator worklogValidator;
    private final IProjectService projectService;
    private final IWorkItemService workItemService;
    private final IWorkItemAuthorizationSupportService workItemAuthorizationSupportService;
    private final IWorklogAuthorizationService worklogAuthorizationService;
    private final IWorklogService worklogService;

    @Override
    @Transactional(readOnly = true)
    public WorklogListPageView handle(ListWorklogsQuery query) {
        worklogValidator.validateListRequest(
                query.projectId(),
                query.workItemId(),
                query.tenantId(),
                query.userId()
        );

        ProjectEntity project = projectService.getProjectById(query.projectId(), query.tenantId());
        WorkItemEntity workItem = workItemService.getWorkItemById(query.workItemId(), query.tenantId());
        ensureWorkItemBelongsToProject(workItem, project);

        ProjectPermissionEvaluationContext actorContext = workItemAuthorizationSupportService.buildActorContext(
                query.userId(),
                query.groupKeys(),
                workItem.getReporterId(),
                workItem.getAssigneeId()
        );
        worklogAuthorizationService.checkReadAccess(project, workItem, actorContext);

        WorklogListCriteria criteria = query.toCriteria();
        PageResult<WorklogEntity> result = worklogService.listWorklogs(query.tenantId(), criteria);
        int pageSize = criteria.getPageSize();
        int currentPage = criteria.getPage();
        int totalPages = pageSize <= 0 ? 0 : (int) Math.ceil((double) result.total() / pageSize);

        return new WorklogListPageView(
                result.items().stream().map(WorklogView::from).toList(),
                result.total(),
                totalPages,
                currentPage,
                pageSize,
                workItem.getId(),
                workItem.getTimeSpent(),
                workItem.getTimeRemainingEstimate()
        );
    }

    private void ensureWorkItemBelongsToProject(WorkItemEntity workItem, ProjectEntity project) {
        if (!Objects.equals(workItem.getProjectId(), project.getId())) {
            throw new ResourceNotFoundException(serp.project.pmcore.domain.shared.exception.DomainErrorCode.WORK_ITEM_NOT_FOUND);
        }
    }
}
