/**
 * Author: QuanTuanHuy
 * Description: Part of Serp Project
 */

package serp.project.pmcore.domain.optimization.model;

import serp.project.pmcore.domain.optimization.enums.OptimizationSolverStatus;

import java.math.BigDecimal;
import java.util.List;
import java.util.Map;

public record OptimizationSolution(
        Map<Long, OptimizationAssignmentSuggestion> assignmentSuggestions,
        Map<Long, OptimizationScheduleSuggestion> scheduleSuggestions,
        List<OptimizationConstraintViolation> warnings,
        OptimizationRunSummary summary,
        OptimizationAlgorithmDescriptor algorithm,
        OptimizationSolverStatus solverStatus,
        BigDecimal objectiveScore
) {
    public static OptimizationSolution fromGenerationResult(OptimizationGenerationResult result,
                                                            OptimizationAlgorithmDescriptor algorithm) {
        return new OptimizationSolution(
                result.assignmentSuggestions(),
                result.scheduleSuggestions(),
                result.warnings(),
                result.summary(),
                algorithm,
                OptimizationSolverStatus.FEASIBLE,
                null
        );
    }
}
