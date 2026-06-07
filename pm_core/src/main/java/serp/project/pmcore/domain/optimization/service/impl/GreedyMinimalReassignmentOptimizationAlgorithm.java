/**
 * Author: QuanTuanHuy
 * Description: Part of Serp Project
 */

package serp.project.pmcore.domain.optimization.service.impl;

import org.springframework.stereotype.Service;
import serp.project.pmcore.domain.optimization.constant.OptimizationAlgorithmKeys;
import serp.project.pmcore.domain.optimization.service.assignment.scoring.MinimalReassignmentOptimizationAssignmentScoringStrategy;
import serp.project.pmcore.domain.optimization.service.schedule.priority.BalancedOptimizationSchedulingPriorityStrategy;

@Service
public class GreedyMinimalReassignmentOptimizationAlgorithm extends AbstractGreedyOptimizationAlgorithm {
    public GreedyMinimalReassignmentOptimizationAlgorithm(
            GreedyOptimizationRunGenerator greedyOptimizationRunGenerator,
            MinimalReassignmentOptimizationAssignmentScoringStrategy assignmentScoringStrategy,
            BalancedOptimizationSchedulingPriorityStrategy schedulingPriorityStrategy) {
        super(greedyOptimizationRunGenerator, assignmentScoringStrategy, schedulingPriorityStrategy);
    }

    @Override
    protected String algorithmKey() {
        return OptimizationAlgorithmKeys.GREEDY_MINIMAL_REASSIGNMENT;
    }
}
