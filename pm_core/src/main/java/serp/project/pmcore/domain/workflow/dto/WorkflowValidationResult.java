/**
 * Author: QuanTuanHuy
 * Description: Part of Serp Project
 */

package serp.project.pmcore.domain.workflow.dto;

import java.util.List;

public record WorkflowValidationResult(
        List<WorkflowValidationFinding> errors,
        List<WorkflowValidationFinding> warnings
) {
    public WorkflowValidationResult {
        errors = List.copyOf(errors == null ? List.of() : errors);
        warnings = List.copyOf(warnings == null ? List.of() : warnings);
    }

    public boolean isValid() {
        return errors.isEmpty();
    }
}
