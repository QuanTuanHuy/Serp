/**
 * Author: QuanTuanHuy
 * Description: Part of Serp Project
 */

package serp.project.pmcore.domain.optimization.service.impl;

import org.springframework.stereotype.Service;
import serp.project.pmcore.domain.optimization.constant.OptimizationAlgorithmKeys;
import serp.project.pmcore.domain.optimization.service.assignment.scoring.BalancedOptimizationAssignmentScoringStrategy;
import serp.project.pmcore.domain.optimization.service.schedule.priority.DeadlineFirstOptimizationSchedulingPriorityStrategy;

@Service
public class GreedyDeadlineFirstOptimizationAlgorithm extends AbstractGreedyOptimizationAlgorithm {
    public GreedyDeadlineFirstOptimizationAlgorithm(
            GreedyOptimizationRunGenerator greedyOptimizationRunGenerator,
            BalancedOptimizationAssignmentScoringStrategy assignmentScoringStrategy,
            DeadlineFirstOptimizationSchedulingPriorityStrategy schedulingPriorityStrategy) {
        super(greedyOptimizationRunGenerator, assignmentScoringStrategy, schedulingPriorityStrategy);
    }

    @Override
    protected String algorithmKey() {
        return OptimizationAlgorithmKeys.GREEDY_DEADLINE_FIRST;
    }
}
