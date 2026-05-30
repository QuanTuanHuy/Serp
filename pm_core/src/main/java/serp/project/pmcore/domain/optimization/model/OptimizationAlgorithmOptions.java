/**
 * Author: QuanTuanHuy
 * Description: Part of Serp Project
 */

package serp.project.pmcore.domain.optimization.model;

import serp.project.pmcore.domain.optimization.service.assignment.scoring.OptimizationAssignmentScoringStrategy;

import java.util.Objects;

public record OptimizationAlgorithmOptions(
        OptimizationRunIntent intent,
        OptimizationAssignmentScoringStrategy assignmentScoringStrategy
) {
    public OptimizationAlgorithmOptions {
        intent = Objects.requireNonNull(intent, "intent");
        assignmentScoringStrategy = Objects.requireNonNull(assignmentScoringStrategy, "assignmentScoringStrategy");
    }
}
