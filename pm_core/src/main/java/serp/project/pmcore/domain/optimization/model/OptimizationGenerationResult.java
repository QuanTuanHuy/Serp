/**
 * Author: QuanTuanHuy
 * Description: Part of Serp Project
 */

package serp.project.pmcore.domain.optimization.model;

import java.util.List;
import java.util.Map;

public record OptimizationGenerationResult(
        Map<Long, OptimizationAssignmentSuggestion> assignmentSuggestions,
        Map<Long, OptimizationScheduleSuggestion> scheduleSuggestions,
        List<OptimizationConstraintViolation> warnings,
        OptimizationRunSummary summary
) {
}
