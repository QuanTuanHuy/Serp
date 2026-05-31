/**
 * Author: QuanTuanHuy
 * Description: Part of Serp Project
 */

package serp.project.pmcore.domain.optimization.service.impl;

import serp.project.pmcore.domain.optimization.constant.OptimizationAlgorithmKeys;
import serp.project.pmcore.domain.optimization.enums.OptimizationCapability;
import serp.project.pmcore.domain.optimization.model.OptimizationAlgorithmDescriptor;
import serp.project.pmcore.domain.optimization.model.OptimizationAlgorithmOptions;
import serp.project.pmcore.domain.optimization.model.OptimizationProblem;
import serp.project.pmcore.domain.optimization.model.OptimizationSolution;
import serp.project.pmcore.domain.optimization.service.IOptimizationAlgorithm;
import serp.project.pmcore.domain.optimization.service.assignment.scoring.OptimizationAssignmentScoringStrategy;
import serp.project.pmcore.domain.optimization.service.schedule.priority.OptimizationSchedulingPriorityStrategy;

import java.util.Set;

public abstract class AbstractGreedyOptimizationAlgorithm implements IOptimizationAlgorithm {
    private final GreedyOptimizationRunGenerator greedyOptimizationRunGenerator;
    private final OptimizationAssignmentScoringStrategy assignmentScoringStrategy;
    private final OptimizationSchedulingPriorityStrategy schedulingPriorityStrategy;

    protected AbstractGreedyOptimizationAlgorithm(GreedyOptimizationRunGenerator greedyOptimizationRunGenerator,
                                                  OptimizationAssignmentScoringStrategy assignmentScoringStrategy,
                                                  OptimizationSchedulingPriorityStrategy schedulingPriorityStrategy) {
        this.greedyOptimizationRunGenerator = greedyOptimizationRunGenerator;
        this.assignmentScoringStrategy = assignmentScoringStrategy;
        this.schedulingPriorityStrategy = schedulingPriorityStrategy;
    }

    @Override
    public final OptimizationAlgorithmDescriptor descriptor() {
        return new OptimizationAlgorithmDescriptor(
                algorithmKey(),
                OptimizationAlgorithmKeys.DEFAULT_VERSION,
                capabilities()
        );
    }

    @Override
    public final OptimizationSolution solve(OptimizationProblem problem) {
        OptimizationAlgorithmOptions options = new OptimizationAlgorithmOptions(
                problem.input().intent(),
                assignmentScoringStrategy,
                schedulingPriorityStrategy
        );
        return OptimizationSolution.fromGenerationResult(
                greedyOptimizationRunGenerator.generate(problem.projectModel(), options),
                descriptor()
        );
    }

    protected Set<OptimizationCapability> capabilities() {
        return Set.of(
                OptimizationCapability.ASSIGNMENT,
                OptimizationCapability.SCHEDULING,
                OptimizationCapability.CAPACITY_AWARE,
                OptimizationCapability.SKILL_AWARE,
                OptimizationCapability.DEPENDENCY_AWARE
        );
    }

    protected abstract String algorithmKey();
}
