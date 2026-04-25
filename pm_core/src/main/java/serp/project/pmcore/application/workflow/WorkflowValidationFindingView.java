/**
 * Author: QuanTuanHuy
 * Description: Part of Serp Project
 */

package serp.project.pmcore.application.workflow;

import serp.project.pmcore.domain.workflow.dto.WorkflowValidationFinding;

public record WorkflowValidationFindingView(
        String ruleKey,
        String severity,
        String message
) {
    public static WorkflowValidationFindingView from(WorkflowValidationFinding finding) {
        return new WorkflowValidationFindingView(
                finding.ruleKey(),
                finding.severity().name(),
                finding.message()
        );
    }
}
