/**
 * Author: QuanTuanHuy
 * Description: Part of Serp Project
 */

package serp.project.pmcore.application.workitem.query.transition;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import serp.project.pmcore.application.shared.cqrs.query.IQueryHandler;
import serp.project.pmcore.application.workitem.command.transition.internal.TransitionSubjectContext;
import serp.project.pmcore.application.workitem.command.transition.support.TransitionConfigurationResolver;
import serp.project.pmcore.domain.project.dto.ProjectPermissionEvaluationContext;
import serp.project.pmcore.domain.project.entity.ProjectEntity;
import serp.project.pmcore.domain.project.service.IProjectService;
import serp.project.pmcore.domain.shared.exception.BusinessRuleViolationException;
import serp.project.pmcore.domain.shared.exception.DomainErrorCode;
import serp.project.pmcore.domain.shared.exception.ResourceNotFoundException;
import serp.project.pmcore.domain.workitem.entity.WorkItemEntity;
import serp.project.pmcore.domain.workitem.service.IWorkItemAuthorizationSupportService;
import serp.project.pmcore.domain.workitem.service.IWorkItemService;
import serp.project.pmcore.domain.workitem.service.IWorkItemTransitionAuthorizationService;

import java.util.List;
import java.util.Objects;

@Service
@RequiredArgsConstructor
public class ListWorkItemTransitionsQueryHandler
        implements IQueryHandler<ListWorkItemTransitionsQuery, List<WorkItemTransitionView>> {

    private final IProjectService projectService;
    private final IWorkItemService workItemService;
    private final IWorkItemAuthorizationSupportService workItemAuthorizationSupportService;
    private final IWorkItemTransitionAuthorizationService workItemTransitionAuthorizationService;
    private final TransitionConfigurationResolver transitionConfigurationResolver;

    @Override
    @Transactional(readOnly = true)
    public List<WorkItemTransitionView> handle(ListWorkItemTransitionsQuery query) {
        ProjectEntity project = projectService.getProjectById(query.projectId(), query.tenantId());
        ensureProjectWritable(project);

        WorkItemEntity workItem = workItemService.getWorkItemById(query.workItemId(), query.tenantId());
        ensureWorkItemBelongsToProject(workItem, project);

        ProjectPermissionEvaluationContext actorContext = workItemAuthorizationSupportService.buildActorContext(
                query.userId(),
                query.groupKeys(),
                workItem.getReporterId(),
                workItem.getAssigneeId()
        );
        workItemTransitionAuthorizationService.checkTransitionPermissions(project, actorContext);
        workItemTransitionAuthorizationService.checkIssueSecurityAccessIfNeeded(
                project,
                workItem,
                actorContext,
                query.tenantId()
        );

        return transitionConfigurationResolver.listAvailableTransitions(
                        new TransitionSubjectContext(
                                project.getId(),
                                project.getWorkflowSchemeId(),
                                workItem.getId(),
                                workItem.getIssueTypeId(),
                                workItem.getWorkflowStepId(),
                                workItem.getStatusId()
                        ),
                        query.tenantId()
                )
                .stream()
                .map(WorkItemTransitionView::from)
                .toList();
    }

    private void ensureProjectWritable(ProjectEntity project) {
        if (Boolean.TRUE.equals(project.getIsArchived())) {
            throw new BusinessRuleViolationException(DomainErrorCode.PROJECT_ARCHIVED);
        }
    }

    private void ensureWorkItemBelongsToProject(WorkItemEntity workItem, ProjectEntity project) {
        if (!Objects.equals(workItem.getProjectId(), project.getId())) {
            throw new ResourceNotFoundException(DomainErrorCode.WORK_ITEM_NOT_FOUND);
        }
    }
}
