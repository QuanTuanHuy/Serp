/**
 * Author: QuanTuanHuy
 * Description: Part of Serp Project
 */

package serp.project.pmcore.application.worklog.command.delete;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import serp.project.pmcore.application.shared.cqrs.command.ICommandHandler;
import serp.project.pmcore.application.worklog.WorklogValidator;
import serp.project.pmcore.application.worklog.command.WorklogEventPayload;
import serp.project.pmcore.application.worklog.command.WorklogOutboxPublisher;
import serp.project.pmcore.domain.project.dto.ProjectPermissionEvaluationContext;
import serp.project.pmcore.domain.project.entity.ProjectEntity;
import serp.project.pmcore.domain.project.service.IProjectService;
import serp.project.pmcore.domain.shared.exception.BusinessRuleViolationException;
import serp.project.pmcore.domain.shared.exception.DomainErrorCode;
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
@Slf4j
public class DeleteWorklogCommandHandler implements ICommandHandler<DeleteWorklogCommand, DeleteWorklogResult> {

    private final WorklogValidator worklogValidator;
    private final IProjectService projectService;
    private final IWorkItemService workItemService;
    private final IWorkItemAuthorizationSupportService workItemAuthorizationSupportService;
    private final IWorklogAuthorizationService worklogAuthorizationService;
    private final IWorklogService worklogService;
    private final WorklogOutboxPublisher worklogOutboxPublisher;

    @Override
    @Transactional(rollbackFor = Exception.class)
    public DeleteWorklogResult handle(DeleteWorklogCommand command) {
        worklogValidator.validateProjectScopedRequest(
                command.projectId(),
                command.workItemId(),
                command.tenantId(),
                command.userId()
        );
        worklogValidator.validateWorklogId(command.worklogId());

        ProjectEntity project = projectService.getProjectById(command.projectId(), command.tenantId());
        ensureProjectWritable(project);

        WorkItemEntity workItem = workItemService.getWorkItemById(command.workItemId(), command.tenantId());
        ensureWorkItemBelongsToProject(workItem, project);

        WorklogEntity worklog = worklogService.getWorklogById(command.worklogId(), command.tenantId());
        ensureWorklogBelongsToWorkItem(worklog, workItem);

        ProjectPermissionEvaluationContext actorContext = workItemAuthorizationSupportService.buildActorContext(
                command.userId(),
                command.groupKeys(),
                workItem.getReporterId(),
                workItem.getAssigneeId()
        );
        worklogAuthorizationService.checkDeleteAccess(project, workItem, worklog, actorContext);

        long deletedAt = System.currentTimeMillis();
        WorklogEntity deletedWorklog = worklogService.softDeleteWorklog(worklog, command.userId(), deletedAt);
        WorkItemEntity refreshedWorkItem = worklogService.refreshWorkItemTimeTracking(workItem, command.userId());
        worklogOutboxPublisher.publishWorklogDeleted(
                command.tenantId(),
                WorklogEventPayload.from(
                        deletedWorklog,
                        refreshedWorkItem,
                        command.userId(),
                        deletedAt,
                        deletedAt
                )
        );

        log.info("Deleted worklog id={} workItemId={} projectId={}",
                deletedWorklog.getId(), deletedWorklog.getWorkItemId(), project.getId());

        return DeleteWorklogResult.from(deletedWorklog, refreshedWorkItem, command.userId(), deletedAt);
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

    private void ensureWorklogBelongsToWorkItem(WorklogEntity worklog, WorkItemEntity workItem) {
        if (!Objects.equals(worklog.getWorkItemId(), workItem.getId())) {
            throw ResourceNotFoundException.worklog(worklog.getId());
        }
    }
}
