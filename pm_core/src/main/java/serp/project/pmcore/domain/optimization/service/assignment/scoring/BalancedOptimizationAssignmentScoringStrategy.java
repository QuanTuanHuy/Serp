/**
 * Author: QuanTuanHuy
 * Description: Part of Serp Project
 */

package serp.project.pmcore.domain.optimization.service.assignment.scoring;

import org.springframework.stereotype.Service;
import serp.project.pmcore.domain.optimization.constant.OptimizationConstants;

@Service
public class BalancedOptimizationAssignmentScoringStrategy implements OptimizationAssignmentScoringStrategy {
    @Override
    public double requiredSkillMultiplier() {
        return 1D;
    }

    @Override
    public double preferredSkillMultiplier() {
        return 1D;
    }

    @Override
    public double reassignmentPenalty() {
        return OptimizationConstants.STANDARD_REASSIGNMENT_PENALTY;
    }

    @Override
    public double currentAssigneeBonus() {
        return 0D;
    }
}
