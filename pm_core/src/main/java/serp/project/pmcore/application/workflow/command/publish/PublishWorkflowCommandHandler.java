/**
 * Author: QuanTuanHuy
 * Description: Part of Serp Project
 */

package serp.project.pmcore.application.workflow.command.publish;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import serp.project.pmcore.application.shared.cqrs.command.ICommandHandler;
import serp.project.pmcore.application.workflow.WorkflowView;
import serp.project.pmcore.domain.workflow.entity.WorkflowEntity;
import serp.project.pmcore.domain.workflow.service.IWorkflowService;

@Service
@RequiredArgsConstructor
public class PublishWorkflowCommandHandler implements ICommandHandler<PublishWorkflowCommand, WorkflowView> {

    private final IWorkflowService workflowService;

    @Override
    @Transactional(rollbackFor = Exception.class)
    public WorkflowView handle(PublishWorkflowCommand command) {
        WorkflowEntity published = workflowService.publishWorkflow(
                command.workflowId(),
                command.tenantId(),
                command.userId()
        );
        return WorkflowView.from(published, false);
    }
}
