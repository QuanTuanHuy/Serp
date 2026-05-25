/**
 * Author: QuanTuanHuy
 * Description: Part of Serp Project
 */

package serp.project.pmcore.application.workflowscheme.command.manageitems;

import serp.project.pmcore.application.shared.cqrs.command.ICommand;
import serp.project.pmcore.application.workflowscheme.WorkflowSchemeDetailView;

import java.util.List;

public record ManageWorkflowSchemeItemsCommand(
        Long schemeId,
        List<WorkflowSchemeItemInput> items,
        Long tenantId,
        Long userId
) implements ICommand<WorkflowSchemeDetailView> {
    public record WorkflowSchemeItemInput(
            Long issueTypeId,
            Long workflowId
    ) {
    }
}
