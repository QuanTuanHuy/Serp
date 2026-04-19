/**
 * Author: QuanTuanHuy
 * Description: Part of Serp Project
 */

package serp.project.pmcore.application.workflow.command.addstep;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import serp.project.pmcore.application.shared.cqrs.command.ICommandHandler;
import serp.project.pmcore.application.workflow.WorkflowStepView;
import serp.project.pmcore.domain.workflow.entity.WorkflowStepEntity;
import serp.project.pmcore.domain.workflow.service.IWorkflowService;

@Service
@RequiredArgsConstructor
public class AddWorkflowStepCommandHandler implements ICommandHandler<AddWorkflowStepCommand, WorkflowStepView> {

    private final IWorkflowService workflowService;

    @Override
    @Transactional(rollbackFor = Exception.class)
    public WorkflowStepView handle(AddWorkflowStepCommand command) {
        WorkflowStepEntity created = workflowService.addWorkflowStep(
                command.workflowId(),
                command.statusId(),
                command.isInitial(),
                command.isTerminal(),
                command.tenantId(),
                command.userId()
        );
        return WorkflowStepView.from(created);
    }
}
