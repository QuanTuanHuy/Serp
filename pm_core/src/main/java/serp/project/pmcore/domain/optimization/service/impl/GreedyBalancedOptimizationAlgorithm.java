/**
 * Author: QuanTuanHuy
 * Description: Part of Serp Project
 */

package serp.project.pmcore.domain.optimization.service.impl;

import org.springframework.stereotype.Service;
import serp.project.pmcore.domain.optimization.constant.OptimizationAlgorithmKeys;
import serp.project.pmcore.domain.optimization.enums.OptimizationCapability;
import serp.project.pmcore.domain.optimization.model.OptimizationAlgorithmDescriptor;
import serp.project.pmcore.domain.optimization.model.OptimizationAlgorithmOptions;
import serp.project.pmcore.domain.optimization.model.OptimizationProblem;
import serp.project.pmcore.domain.optimization.model.OptimizationSolution;
import serp.project.pmcore.domain.optimization.service.IOptimizationAlgorithm;

import java.util.Set;

@Service
public class GreedyBalancedOptimizationAlgorithm implements IOptimizationAlgorithm {
    private final GreedyOptimizationRunGenerator greedyOptimizationRunGenerator;

    public GreedyBalancedOptimizationAlgorithm(GreedyOptimizationRunGenerator greedyOptimizationRunGenerator) {
        this.greedyOptimizationRunGenerator = greedyOptimizationRunGenerator;
    }

    @Override
    public OptimizationAlgorithmDescriptor descriptor() {
        return new OptimizationAlgorithmDescriptor(
                OptimizationAlgorithmKeys.GREEDY_BALANCED,
                OptimizationAlgorithmKeys.DEFAULT_VERSION,
                Set.of(
                        OptimizationCapability.ASSIGNMENT,
                        OptimizationCapability.SCHEDULING,
                        OptimizationCapability.CAPACITY_AWARE,
                        OptimizationCapability.SKILL_AWARE,
                        OptimizationCapability.DEPENDENCY_AWARE
                )
        );
    }

    @Override
    public OptimizationSolution solve(OptimizationProblem problem, OptimizationAlgorithmOptions options) {
        return OptimizationSolution.fromGenerationResult(
                greedyOptimizationRunGenerator.generate(problem.projectModel(), problem.input()),
                descriptor()
        );
    }
}
