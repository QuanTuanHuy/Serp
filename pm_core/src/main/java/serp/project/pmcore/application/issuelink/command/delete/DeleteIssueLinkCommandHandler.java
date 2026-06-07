/**
 * Author: QuanTuanHuy
 * Description: Part of Serp Project
 */

package serp.project.pmcore.application.issuelink.command.delete;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import serp.project.pmcore.application.issuelink.IssueLinkValidator;
import serp.project.pmcore.application.issuelink.command.IssueLinkEventPayload;
import serp.project.pmcore.application.issuelink.command.IssueLinkOutboxPublisher;
import serp.project.pmcore.application.shared.cqrs.command.ICommandHandler;
import serp.project.pmcore.domain.issuelink.entity.IssueLinkEntity;
import serp.project.pmcore.domain.issuelink.service.IIssueLinkAuthorizationService;
import serp.project.pmcore.domain.issuelink.service.IIssueLinkService;
import serp.project.pmcore.domain.project.dto.ProjectPermissionEvaluationContext;
import serp.project.pmcore.domain.project.entity.ProjectEntity;
import serp.project.pmcore.domain.project.service.IProjectService;
import serp.project.pmcore.domain.shared.exception.ResourceNotFoundException;
import serp.project.pmcore.domain.workitem.entity.WorkItemEntity;
import serp.project.pmcore.domain.workitem.service.IWorkItemAuthorizationSupportService;
import serp.project.pmcore.domain.workitem.service.IWorkItemService;

import java.util.Objects;

@Service
@RequiredArgsConstructor
public class DeleteIssueLinkCommandHandler implements ICommandHandler<DeleteIssueLinkCommand, DeleteIssueLinkResult> {

    private final IssueLinkValidator issueLinkValidator;
    private final IProjectService projectService;
    private final IWorkItemService workItemService;
    private final IWorkItemAuthorizationSupportService workItemAuthorizationSupportService;
    private final IIssueLinkAuthorizationService issueLinkAuthorizationService;
    private final IIssueLinkService issueLinkService;
    private final IssueLinkOutboxPublisher issueLinkOutboxPublisher;

    @Override
    @Transactional(rollbackFor = Exception.class)
    public DeleteIssueLinkResult handle(DeleteIssueLinkCommand command) {
        issueLinkValidator.validateDeleteRequest(
                command.projectId(),
                command.workItemId(),
                command.linkId(),
                command.tenantId(),
                command.userId()
        );

        ProjectEntity project = projectService.getProjectById(command.projectId(), command.tenantId());
        WorkItemEntity ownerWorkItem = workItemService.getWorkItemById(command.workItemId(), command.tenantId());
        ensureWorkItemBelongsToProject(ownerWorkItem, project);

        ProjectPermissionEvaluationContext actorContext = workItemAuthorizationSupportService.buildActorContext(
                command.userId(),
                command.groupKeys(),
                ownerWorkItem.getReporterId(),
                ownerWorkItem.getAssigneeId()
        );
        issueLinkAuthorizationService.checkWriteAccess(project, ownerWorkItem, actorContext);

        IssueLinkEntity issueLink = issueLinkService.getById(command.linkId(), command.tenantId());
        ensureWorkItemOwnsLink(issueLink, ownerWorkItem.getId());

        long deletedAt = System.currentTimeMillis();
        IssueLinkEntity deleted = issueLinkService.delete(issueLink);
        issueLinkOutboxPublisher.publishIssueLinkDeleted(
                command.tenantId(),
                IssueLinkEventPayload.from(deleted, command.userId(), deletedAt)
        );
        return DeleteIssueLinkResult.from(deleted, command.userId(), deletedAt);
    }

    private void ensureWorkItemBelongsToProject(WorkItemEntity workItem, ProjectEntity project) {
        if (!Objects.equals(workItem.getProjectId(), project.getId())) {
            throw ResourceNotFoundException.workItem(workItem.getId());
        }
    }

    private void ensureWorkItemOwnsLink(IssueLinkEntity issueLink, Long ownerWorkItemId) {
        if (!Objects.equals(issueLink.getSourceId(), ownerWorkItemId)
                && !Objects.equals(issueLink.getTargetId(), ownerWorkItemId)) {
            throw ResourceNotFoundException.issueLink(issueLink.getId());
        }
    }
}
