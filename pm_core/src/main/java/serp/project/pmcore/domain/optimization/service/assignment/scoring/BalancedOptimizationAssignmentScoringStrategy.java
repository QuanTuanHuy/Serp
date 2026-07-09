/**
 * Author: QuanTuanHuy
 * Description: Part of Serp Project
 */

package serp.project.pmcore.domain.optimization.service.assignment.scoring;

import org.springframework.stereotype.Service;
import serp.project.pmcore.domain.optimization.constant.OptimizationConstants;

/**
 * Balanced scoring strategy for optimization assignments.
 *
 * This strategy maintains a neutral stance, applying standard skill multipliers (1.0)
 * and a standard reassignment penalty to prevent unnecessary shifts while keeping the
 * current assignee bonus at 0.
 */
@Service
public class BalancedOptimizationAssignmentScoringStrategy implements OptimizationAssignmentScoringStrategy {
    
    /**
     * {@inheritDoc}
     * Returns a neutral multiplier of 1.0.
     */
    @Override
    public double requiredSkillMultiplier() {
        return 1D;
    }

    /**
     * {@inheritDoc}
     * Returns a neutral multiplier of 1.0.
     */
    @Override
    public double preferredSkillMultiplier() {
        return 1D;
    }

    /**
     * {@inheritDoc}
     * Returns the standard reassignment penalty cost.
     */
    @Override
    public double reassignmentPenalty() {
        return OptimizationConstants.STANDARD_REASSIGNMENT_PENALTY;
    }

    /**
     * {@inheritDoc}
     * Returns 0.0 (no additional bonus for keeping the current assignee).
     */
    @Override
    public double currentAssigneeBonus() {
        return 0D;
    }
}
