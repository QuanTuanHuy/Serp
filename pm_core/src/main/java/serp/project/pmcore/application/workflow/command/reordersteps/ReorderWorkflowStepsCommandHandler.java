/**
 * Author: QuanTuanHuy
 * Description: Part of Serp Project
 */

package serp.project.pmcore.application.workflow.command.reordersteps;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import serp.project.pmcore.application.shared.cqrs.command.ICommandHandler;
import serp.project.pmcore.application.workflow.WorkflowStepView;
import serp.project.pmcore.domain.workflow.service.IWorkflowService;

import java.util.List;

@Service
@RequiredArgsConstructor
public class ReorderWorkflowStepsCommandHandler implements ICommandHandler<ReorderWorkflowStepsCommand, List<WorkflowStepView>> {

    private final IWorkflowService workflowService;

    @Override
    @Transactional(rollbackFor = Exception.class)
    public List<WorkflowStepView> handle(ReorderWorkflowStepsCommand command) {
        return workflowService.reorderWorkflowSteps(
                        command.workflowId(),
                        command.stepIds(),
                        command.tenantId(),
                        command.userId()
                ).stream()
                .map(WorkflowStepView::from)
                .toList();
    }
}
