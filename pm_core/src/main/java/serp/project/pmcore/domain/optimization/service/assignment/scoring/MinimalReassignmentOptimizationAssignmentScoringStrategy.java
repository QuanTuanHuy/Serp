/**
 * Author: QuanTuanHuy
 * Description: Part of Serp Project
 */

package serp.project.pmcore.domain.optimization.service.assignment.scoring;

import org.springframework.stereotype.Service;
import serp.project.pmcore.domain.optimization.constant.OptimizationConstants;

/**
 * Minimal reassignment scoring strategy for optimization assignments.
 *
 * This strategy strongly discourages changes to existing assignments by lowering the
 * reassignment penalty slightly but adding a significant bonus for retaining the current assignee.
 */
@Service
public class MinimalReassignmentOptimizationAssignmentScoringStrategy implements OptimizationAssignmentScoringStrategy {
    
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
     * Returns the lower penalty cost specific to minimal reassignment setups.
     */
    @Override
    public double reassignmentPenalty() {
        return OptimizationConstants.MINIMAL_REASSIGNMENT_PENALTY;
    }

    /**
     * {@inheritDoc}
     * Returns a high bonus score to encourage keeping the current assignee.
     */
    @Override
    public double currentAssigneeBonus() {
        return OptimizationConstants.MINIMAL_REASSIGNMENT_CURRENT_ASSIGNEE_BONUS;
    }
}
