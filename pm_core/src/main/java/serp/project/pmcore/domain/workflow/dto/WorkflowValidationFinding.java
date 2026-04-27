/**
 * Author: QuanTuanHuy
 * Description: Part of Serp Project
 */

package serp.project.pmcore.domain.workflow.dto;

public record WorkflowValidationFinding(
        String ruleKey,
        WorkflowValidationSeverity severity,
        String message
) {
}
