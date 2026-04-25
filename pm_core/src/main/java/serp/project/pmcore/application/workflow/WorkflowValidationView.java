/**
 * Author: QuanTuanHuy
 * Description: Part of Serp Project
 */

package serp.project.pmcore.application.workflow;

import serp.project.pmcore.domain.workflow.dto.WorkflowValidationResult;

import java.util.List;

public record WorkflowValidationView(
        boolean valid,
        List<WorkflowValidationFindingView> errors,
        List<WorkflowValidationFindingView> warnings
) {
    public static WorkflowValidationView from(WorkflowValidationResult result) {
        return new WorkflowValidationView(
                result.isValid(),
                result.errors().stream().map(WorkflowValidationFindingView::from).toList(),
                result.warnings().stream().map(WorkflowValidationFindingView::from).toList()
        );
    }
}
