/**
 * Author: QuanTuanHuy
 * Description: Part of Serp Project
 */

package serp.project.pmcore.application.workflowscheme.command.create;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import serp.project.pmcore.application.shared.cqrs.command.ICommandHandler;
import serp.project.pmcore.application.workflowscheme.WorkflowSchemeView;
import serp.project.pmcore.domain.workflow.entity.WorkflowSchemeEntity;
import serp.project.pmcore.domain.workflow.service.IWorkflowSchemeService;

@Service
@RequiredArgsConstructor
public class CreateWorkflowSchemeCommandHandler implements ICommandHandler<CreateWorkflowSchemeCommand, WorkflowSchemeView> {

    private final IWorkflowSchemeService workflowSchemeService;

    @Override
    @Transactional(rollbackFor = Exception.class)
    public WorkflowSchemeView handle(CreateWorkflowSchemeCommand command) {
        WorkflowSchemeEntity created = workflowSchemeService.createWorkflowScheme(
                WorkflowSchemeEntity.builder()
                        .name(command.name())
                        .description(command.description())
                        .defaultWorkflowId(command.defaultWorkflowId())
                        .build(),
                command.tenantId(),
                command.userId()
        );
        return WorkflowSchemeView.from(created, false);
    }
}
