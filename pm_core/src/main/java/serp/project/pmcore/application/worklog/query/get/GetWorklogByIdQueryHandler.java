/**
 * Author: QuanTuanHuy
 * Description: Part of Serp Project
 */

package serp.project.pmcore.application.worklog.query.get;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import serp.project.pmcore.application.shared.cqrs.query.IQueryHandler;
import serp.project.pmcore.application.worklog.WorklogDetailView;
import serp.project.pmcore.application.worklog.WorklogValidator;
import serp.project.pmcore.domain.project.dto.ProjectPermissionEvaluationContext;
import serp.project.pmcore.domain.project.entity.ProjectEntity;
import serp.project.pmcore.domain.project.service.IProjectService;
import serp.project.pmcore.domain.shared.exception.ResourceNotFoundException;
import serp.project.pmcore.domain.workitem.entity.WorkItemEntity;
import serp.project.pmcore.domain.workitem.service.IWorkItemAuthorizationSupportService;
import serp.project.pmcore.domain.workitem.service.IWorkItemService;
import serp.project.pmcore.domain.worklog.entity.WorklogEntity;
import serp.project.pmcore.domain.worklog.service.IWorklogAuthorizationService;
import serp.project.pmcore.domain.worklog.service.IWorklogService;

import java.util.Objects;

@Service
@RequiredArgsConstructor
public class GetWorklogByIdQueryHandler implements IQueryHandler<GetWorklogByIdQuery, WorklogDetailView> {

    private final WorklogValidator worklogValidator;
    private final IProjectService projectService;
    private final IWorkItemService workItemService;
    private final IWorkItemAuthorizationSupportService workItemAuthorizationSupportService;
    private final IWorklogAuthorizationService worklogAuthorizationService;
    private final IWorklogService worklogService;

    @Override
    @Transactional(readOnly = true)
    public WorklogDetailView handle(GetWorklogByIdQuery query) {
        worklogValidator.validateProjectScopedRequest(
                query.projectId(),
                query.workItemId(),
                query.tenantId(),
                query.userId()
        );
        worklogValidator.validateWorklogId(query.worklogId());

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

        WorklogEntity worklog = worklogService.getWorklogById(query.worklogId(), query.tenantId());
        ensureWorklogBelongsToWorkItem(worklog, workItem);
        return WorklogDetailView.from(worklog, workItem);
    }

    private void ensureWorkItemBelongsToProject(WorkItemEntity workItem, ProjectEntity project) {
        if (!Objects.equals(workItem.getProjectId(), project.getId())) {
            throw new ResourceNotFoundException(serp.project.pmcore.domain.shared.exception.DomainErrorCode.WORK_ITEM_NOT_FOUND);
        }
    }

    private void ensureWorklogBelongsToWorkItem(WorklogEntity worklog, WorkItemEntity workItem) {
        if (!Objects.equals(worklog.getWorkItemId(), workItem.getId())) {
            throw ResourceNotFoundException.worklog(worklog.getId());
        }
    }
}
