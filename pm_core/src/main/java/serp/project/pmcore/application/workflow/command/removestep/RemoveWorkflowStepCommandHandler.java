/**
 * Author: QuanTuanHuy
 * Description: Part of Serp Project
 */

package serp.project.pmcore.application.workflow.command.removestep;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import serp.project.pmcore.application.shared.cqrs.command.ICommandHandler;
import serp.project.pmcore.domain.workflow.entity.WorkflowStepEntity;
import serp.project.pmcore.domain.workflow.service.IWorkflowService;

@Service
@RequiredArgsConstructor
public class RemoveWorkflowStepCommandHandler implements ICommandHandler<RemoveWorkflowStepCommand, DeleteWorkflowStepResult> {

    private final IWorkflowService workflowService;

    @Override
    @Transactional(rollbackFor = Exception.class)
    public DeleteWorkflowStepResult handle(RemoveWorkflowStepCommand command) {
        WorkflowStepEntity deleted = workflowService.removeWorkflowStep(
                command.workflowId(),
                command.stepId(),
                command.tenantId(),
                command.userId()
        );
        return DeleteWorkflowStepResult.from(deleted);
    }
}
