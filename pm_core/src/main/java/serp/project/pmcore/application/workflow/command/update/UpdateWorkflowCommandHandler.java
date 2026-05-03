/**
 * Author: QuanTuanHuy
 * Description: Part of Serp Project
 */

package serp.project.pmcore.application.workflow.command.update;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import serp.project.pmcore.application.shared.cqrs.command.ICommandHandler;
import serp.project.pmcore.application.workflow.WorkflowView;
import serp.project.pmcore.domain.workflow.entity.WorkflowEntity;
import serp.project.pmcore.domain.workflow.service.IWorkflowService;

@Service
@RequiredArgsConstructor
public class UpdateWorkflowCommandHandler implements ICommandHandler<UpdateWorkflowCommand, WorkflowView> {

    private final IWorkflowService workflowService;

    @Override
    @Transactional(rollbackFor = Exception.class)
    public WorkflowView handle(UpdateWorkflowCommand command) {
        WorkflowEntity updated = workflowService.updateWorkflow(
                command.workflowId(),
                command.name(),
                command.description(),
                command.tenantId(),
                command.userId()
        );
        return WorkflowView.from(updated, false);
    }
}
