/**
 * Author: QuanTuanHuy
 * Description: Part of Serp Project
 */

package serp.project.pmcore.domain.optimization.service.assignment.scoring;

import org.springframework.stereotype.Service;
import serp.project.pmcore.domain.optimization.constant.OptimizationConstants;

@Service
public class MinimalReassignmentOptimizationAssignmentScoringStrategy implements OptimizationAssignmentScoringStrategy {
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
        return OptimizationConstants.MINIMAL_REASSIGNMENT_PENALTY;
    }

    @Override
    public double currentAssigneeBonus() {
        return OptimizationConstants.MINIMAL_REASSIGNMENT_CURRENT_ASSIGNEE_BONUS;
    }
}
