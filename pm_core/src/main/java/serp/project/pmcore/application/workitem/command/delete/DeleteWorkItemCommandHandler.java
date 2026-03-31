/**
 * Author: QuanTuanHuy
 * Description: Part of Serp Project
 */

package serp.project.pmcore.application.workitem.command.delete;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import serp.project.pmcore.application.shared.cqrs.command.ICommandHandler;
import serp.project.pmcore.domain.project.dto.ProjectPermissionEvaluationContext;
import serp.project.pmcore.domain.project.entity.ProjectEntity;
import serp.project.pmcore.domain.shared.exception.DomainErrorCode;
import serp.project.pmcore.domain.shared.exception.ResourceNotFoundException;
import serp.project.pmcore.domain.workitem.dto.WorkItemDeleteExecutionResult;
import serp.project.pmcore.domain.workitem.entity.WorkItemEntity;
import serp.project.pmcore.domain.workitem.service.IWorkItemDeleteAuthorizationService;
import serp.project.pmcore.domain.workitem.service.IWorkItemService;

@Service
@RequiredArgsConstructor
@Slf4j
public class DeleteWorkItemCommandHandler
        implements ICommandHandler<DeleteWorkItemCommand, DeleteWorkItemResult> {

    private final IWorkItemService workItemService;
    private final IWorkItemDeleteAuthorizationService workItemDeleteAuthorizationService;

    private final DeleteWorkItemValidator deleteWorkItemValidator;

    @Override
    public DeleteWorkItemResult handle(DeleteWorkItemCommand command) {
        deleteWorkItemValidator.validateCommand(command);

        ProjectEntity project = deleteWorkItemValidator.validateWritableProject(command.projectId(), command.tenantId());

        WorkItemEntity workItem = workItemService.getWorkItemById(command.workItemId(), command.tenantId());
        if (!workItem.getProjectId().equals(project.getId())) {
            log.warn("[DeleteWorkItemCommandHandler] Work item {} does not belong to project {}",
                    command.workItemId(), project.getId());
            throw new ResourceNotFoundException(DomainErrorCode.WORK_ITEM_NOT_FOUND);
        }

        var baseActorContext = ProjectPermissionEvaluationContext.builder()
                .userId(command.userId())
                .groupKeys(command.groupKeys())
                .build();
        workItemDeleteAuthorizationService.checkDeletePermission(project, baseActorContext);

        workItemDeleteAuthorizationService.checkDeleteSecurityAccess(project, workItem, baseActorContext);

        long now = System.currentTimeMillis();
        WorkItemDeleteExecutionResult result = workItemService.softDeleteWorkItem(
                command.workItemId(),
                command.projectId(),
                command.tenantId(),
                command.userId(),
                now
        );

        return DeleteWorkItemResult.from(workItem.getId(), result, now);
    }
}
