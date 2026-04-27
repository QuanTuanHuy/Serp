/**
 * Author: QuanTuanHuy
 * Description: Part of Serp Project
 */

package serp.project.pmcore.application.workflow.command.addtransition;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import serp.project.pmcore.application.shared.cqrs.command.ICommandHandler;
import serp.project.pmcore.application.workflow.WorkflowTransitionView;
import serp.project.pmcore.domain.workflow.entity.WorkflowTransitionEntity;
import serp.project.pmcore.domain.workflow.service.IWorkflowService;

@Service
@RequiredArgsConstructor
public class AddWorkflowTransitionCommandHandler
        implements ICommandHandler<AddWorkflowTransitionCommand, WorkflowTransitionView> {

    private final IWorkflowService workflowService;

    @Override
    @Transactional(rollbackFor = Exception.class)
    public WorkflowTransitionView handle(AddWorkflowTransitionCommand command) {
        WorkflowTransitionEntity created = workflowService.addWorkflowTransition(
                command.workflowId(),
                command.name(),
                command.fromStepId(),
                command.toStepId(),
                command.screenId(),
                command.sequence(),
                command.tenantId(),
                command.userId()
        );
        return WorkflowTransitionView.from(created);
    }
}
