/**
 * Author: QuanTuanHuy
 * Description: Part of Serp Project
 */

package serp.project.pmcore.domain.optimization.service.assignment.scoring;

import org.springframework.stereotype.Service;
import serp.project.pmcore.domain.optimization.constant.OptimizationConstants;

/**
 * Skill-first scoring strategy for optimization assignments.
 *
 * This strategy prioritizes candidate skills alignment by amplifying the multiplier
 * for both required and preferred skill matches, while applying standard reassignment penalties.
 */
@Service
public class SkillFirstOptimizationAssignmentScoringStrategy implements OptimizationAssignmentScoringStrategy {
    
    /**
     * {@inheritDoc}
     * Returns an amplified multiplier of 1.5.
     */
    @Override
    public double requiredSkillMultiplier() {
        return 1.5D;
    }

    /**
     * {@inheritDoc}
     * Returns an amplified multiplier of 1.25.
     */
    @Override
    public double preferredSkillMultiplier() {
        return 1.25D;
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
