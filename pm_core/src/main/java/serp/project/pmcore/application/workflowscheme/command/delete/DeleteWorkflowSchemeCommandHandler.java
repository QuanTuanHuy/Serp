/**
 * Author: QuanTuanHuy
 * Description: Part of Serp Project
 */

package serp.project.pmcore.application.workflowscheme.command.delete;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import serp.project.pmcore.application.shared.cqrs.command.ICommandHandler;
import serp.project.pmcore.domain.workflow.entity.WorkflowSchemeEntity;
import serp.project.pmcore.domain.workflow.service.IWorkflowSchemeService;

@Service
@RequiredArgsConstructor
public class DeleteWorkflowSchemeCommandHandler implements ICommandHandler<DeleteWorkflowSchemeCommand, DeleteWorkflowSchemeResult> {

    private final IWorkflowSchemeService workflowSchemeService;

    @Override
    @Transactional(rollbackFor = Exception.class)
    public DeleteWorkflowSchemeResult handle(DeleteWorkflowSchemeCommand command) {
        WorkflowSchemeEntity deleted = workflowSchemeService.deleteWorkflowScheme(
                command.schemeId(),
                command.tenantId(),
                command.userId()
        );
        return DeleteWorkflowSchemeResult.from(deleted);
    }
}
