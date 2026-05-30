/**
 * Author: QuanTuanHuy
 * Description: Part of Serp Project
 */

package serp.project.pmcore.domain.optimization.service.assignment.scoring;

import org.springframework.stereotype.Service;

@Service
public class SkillFirstOptimizationAssignmentScoringStrategy implements OptimizationAssignmentScoringStrategy {
    @Override
    public double requiredSkillMultiplier() {
        return 1.5D;
    }

    @Override
    public double preferredSkillMultiplier() {
        return 1.25D;
    }
}
