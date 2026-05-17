/**
 * Author: QuanTuanHuy
 * Description: Part of Serp Project
 */

package serp.project.pmcore.domain.optimization.model;

import java.util.List;

public record OptimizationAssignmentSuggestion(
        Long workItemId,
        Long suggestedAssigneeId,
        double cost,
        List<String> reasons,
        List<OptimizationConstraintViolation> violations
) {
}
