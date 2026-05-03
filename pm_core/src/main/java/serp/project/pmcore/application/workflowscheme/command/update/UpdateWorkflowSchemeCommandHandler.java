/**
 * Author: QuanTuanHuy
 * Description: Part of Serp Project
 */

package serp.project.pmcore.application.workflowscheme.command.update;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import serp.project.pmcore.application.shared.cqrs.command.ICommandHandler;
import serp.project.pmcore.application.workflowscheme.WorkflowSchemeView;
import serp.project.pmcore.domain.workflow.entity.WorkflowSchemeEntity;
import serp.project.pmcore.domain.workflow.service.IWorkflowSchemeService;

@Service
@RequiredArgsConstructor
public class UpdateWorkflowSchemeCommandHandler implements ICommandHandler<UpdateWorkflowSchemeCommand, WorkflowSchemeView> {

    private final IWorkflowSchemeService workflowSchemeService;

    @Override
    @Transactional(rollbackFor = Exception.class)
    public WorkflowSchemeView handle(UpdateWorkflowSchemeCommand command) {
        WorkflowSchemeEntity updated = workflowSchemeService.updateWorkflowScheme(
                command.schemeId(),
                command.data(),
                command.tenantId(),
                command.userId()
        );
        return WorkflowSchemeView.from(updated, false);
    }
}
