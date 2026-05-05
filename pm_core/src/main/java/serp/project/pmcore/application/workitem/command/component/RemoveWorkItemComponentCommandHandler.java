/**
 * Author: QuanTuanHuy
 * Description: Part of Serp Project
 */

package serp.project.pmcore.application.workitem.command.component;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import serp.project.pmcore.application.shared.cqrs.command.ICommandHandler;
import serp.project.pmcore.application.workitem.support.WorkItemComponentAccessHelper;
import serp.project.pmcore.domain.project.service.IProjectComponentService;
import serp.project.pmcore.domain.shared.exception.DomainErrorCode;
import serp.project.pmcore.domain.shared.exception.ResourceNotFoundException;
import serp.project.pmcore.domain.workitem.port.write.IWorkItemWritePort;

@Service
@RequiredArgsConstructor
public class RemoveWorkItemComponentCommandHandler
        implements ICommandHandler<RemoveWorkItemComponentCommand, RemoveWorkItemComponentResult> {

    private final WorkItemComponentAccessHelper accessHelper;
    private final IProjectComponentService projectComponentService;
    private final IWorkItemWritePort workItemWritePort;

    @Override
    @Transactional(rollbackFor = Exception.class)
    public RemoveWorkItemComponentResult handle(RemoveWorkItemComponentCommand command) {
        accessHelper.requireEditableWorkItem(
                command.projectId(),
                command.workItemId(),
                command.tenantId(),
                command.userId(),
                command.groupKeys()
        );
        projectComponentService.getComponentById(command.componentId(), command.projectId(), command.tenantId());

        long removedAt = System.currentTimeMillis();
        boolean removed = workItemWritePort.removeWorkItemComponent(
                command.workItemId(),
                command.componentId(),
                command.tenantId(),
                command.userId(),
                removedAt
        );
        if (!removed) {
            throw new ResourceNotFoundException(
                    DomainErrorCode.COMPONENT_NOT_FOUND,
                    "Work item component link not found: workItemId=" + command.workItemId()
                            + ", componentId=" + command.componentId()
            );
        }

        return new RemoveWorkItemComponentResult(
                command.workItemId(),
                command.componentId(),
                true,
                removedAt
        );
    }
}
