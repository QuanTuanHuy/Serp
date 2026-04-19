/**
 * Author: QuanTuanHuy
 * Description: Part of Serp Project
 */

package serp.project.pmcore.application.workflow.command.removetransition;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import serp.project.pmcore.application.shared.cqrs.command.ICommandHandler;
import serp.project.pmcore.domain.workflow.entity.WorkflowTransitionEntity;
import serp.project.pmcore.domain.workflow.service.IWorkflowService;

@Service
@RequiredArgsConstructor
public class RemoveWorkflowTransitionCommandHandler
        implements ICommandHandler<RemoveWorkflowTransitionCommand, DeleteWorkflowTransitionResult> {

    private final IWorkflowService workflowService;

    @Override
    @Transactional(rollbackFor = Exception.class)
    public DeleteWorkflowTransitionResult handle(RemoveWorkflowTransitionCommand command) {
        WorkflowTransitionEntity deleted = workflowService.removeWorkflowTransition(
                command.workflowId(),
                command.transitionId(),
                command.tenantId(),
                command.userId()
        );
        return DeleteWorkflowTransitionResult.from(deleted);
    }
}
