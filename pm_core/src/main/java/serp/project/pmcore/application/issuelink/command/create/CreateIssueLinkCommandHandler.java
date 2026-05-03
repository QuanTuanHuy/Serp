/**
 * Author: QuanTuanHuy
 * Description: Part of Serp Project
 */

package serp.project.pmcore.application.issuelink.command.create;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import serp.project.pmcore.application.issuelink.IssueLinkValidator;
import serp.project.pmcore.application.issuelink.IssueLinkView;
import serp.project.pmcore.application.issuelink.command.IssueLinkEventPayload;
import serp.project.pmcore.application.issuelink.command.IssueLinkOutboxPublisher;
import serp.project.pmcore.application.shared.cqrs.command.ICommandHandler;
import serp.project.pmcore.domain.issuelink.entity.IssueLinkEntity;
import serp.project.pmcore.domain.issuelink.service.IIssueLinkAuthorizationService;
import serp.project.pmcore.domain.issuelink.service.IIssueLinkService;
import serp.project.pmcore.domain.issuelink.service.IIssueLinkTypeService;
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
public class CreateIssueLinkCommandHandler implements ICommandHandler<CreateIssueLinkCommand, IssueLinkView> {

    private final IssueLinkValidator issueLinkValidator;
    private final IProjectService projectService;
    private final IWorkItemService workItemService;
    private final IWorkItemAuthorizationSupportService workItemAuthorizationSupportService;
    private final IIssueLinkAuthorizationService issueLinkAuthorizationService;
    private final IIssueLinkTypeService issueLinkTypeService;
    private final IIssueLinkService issueLinkService;
    private final IssueLinkOutboxPublisher issueLinkOutboxPublisher;

    @Override
    @Transactional(rollbackFor = Exception.class)
    public IssueLinkView handle(CreateIssueLinkCommand command) {
        issueLinkValidator.validateCreateRequest(
                command.projectId(),
                command.workItemId(),
                command.targetId(),
                command.linkTypeId(),
                command.tenantId(),
                command.userId()
        );

        ProjectEntity project = projectService.getProjectById(command.projectId(), command.tenantId());
        WorkItemEntity sourceWorkItem = workItemService.getWorkItemById(command.workItemId(), command.tenantId());
        ensureWorkItemBelongsToProject(sourceWorkItem, project);

        WorkItemEntity targetWorkItem = workItemService.getWorkItemById(command.targetId(), command.tenantId());
        issueLinkTypeService.getVisibleById(command.linkTypeId(), command.tenantId());

        ProjectPermissionEvaluationContext actorContext = workItemAuthorizationSupportService.buildActorContext(
                command.userId(),
                command.groupKeys(),
                sourceWorkItem.getReporterId(),
                sourceWorkItem.getAssigneeId()
        );
        issueLinkAuthorizationService.checkWriteAccess(project, sourceWorkItem, actorContext);

        IssueLinkEntity created = issueLinkService.create(
                IssueLinkEntity.builder()
                        .sourceId(sourceWorkItem.getId())
                        .targetId(targetWorkItem.getId())
                        .linkTypeId(command.linkTypeId())
                        .build(),
                command.tenantId(),
                command.userId()
        );
        issueLinkOutboxPublisher.publishIssueLinkCreated(
                command.tenantId(),
                IssueLinkEventPayload.from(created, command.userId(), null)
        );
        return IssueLinkView.from(created);
    }

    private void ensureWorkItemBelongsToProject(WorkItemEntity workItem, ProjectEntity project) {
        if (!Objects.equals(workItem.getProjectId(), project.getId())) {
            throw ResourceNotFoundException.workItem(workItem.getId());
        }
    }
}
