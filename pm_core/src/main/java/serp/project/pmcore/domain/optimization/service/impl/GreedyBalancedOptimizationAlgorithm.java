/**
 * Author: QuanTuanHuy
 * Description: Part of Serp Project
 */

package serp.project.pmcore.domain.optimization.service.impl;

import org.springframework.stereotype.Service;
import serp.project.pmcore.domain.optimization.constant.OptimizationAlgorithmKeys;
import serp.project.pmcore.domain.optimization.service.assignment.scoring.BalancedOptimizationAssignmentScoringStrategy;
import serp.project.pmcore.domain.optimization.service.schedule.priority.BalancedOptimizationSchedulingPriorityStrategy;

@Service
public class GreedyBalancedOptimizationAlgorithm extends AbstractGreedyOptimizationAlgorithm {
    public GreedyBalancedOptimizationAlgorithm(GreedyOptimizationRunGenerator greedyOptimizationRunGenerator,
                                               BalancedOptimizationAssignmentScoringStrategy assignmentScoringStrategy,
                                               BalancedOptimizationSchedulingPriorityStrategy schedulingPriorityStrategy) {
        super(greedyOptimizationRunGenerator, assignmentScoringStrategy, schedulingPriorityStrategy);
    }

    @Override
    protected String algorithmKey() {
        return OptimizationAlgorithmKeys.GREEDY_BALANCED;
    }
}
