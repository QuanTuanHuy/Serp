/**
 * Author: QuanTuanHuy
 * Description: Part of Serp Project
 */

package serp.project.pmcore.application.workflow.command.updatetransition;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import serp.project.pmcore.application.shared.cqrs.command.ICommandHandler;
import serp.project.pmcore.application.workflow.WorkflowTransitionView;
import serp.project.pmcore.domain.workflow.entity.WorkflowTransitionEntity;
import serp.project.pmcore.domain.workflow.service.IWorkflowService;

@Service
@RequiredArgsConstructor
public class UpdateWorkflowTransitionCommandHandler
        implements ICommandHandler<UpdateWorkflowTransitionCommand, WorkflowTransitionView> {

    private final IWorkflowService workflowService;

    @Override
    @Transactional(rollbackFor = Exception.class)
    public WorkflowTransitionView handle(UpdateWorkflowTransitionCommand command) {
        WorkflowTransitionEntity updated = workflowService.updateWorkflowTransition(
                command.workflowId(),
                command.transitionId(),
                command.name(),
                command.screenId(),
                command.sequence(),
                command.tenantId(),
                command.userId()
        );
        return WorkflowTransitionView.from(updated);
    }
}
