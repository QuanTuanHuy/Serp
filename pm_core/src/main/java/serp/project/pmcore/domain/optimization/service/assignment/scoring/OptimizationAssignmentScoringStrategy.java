/**
 * Author: QuanTuanHuy
 * Description: Part of Serp Project
 */

package serp.project.pmcore.domain.optimization.service.assignment.scoring;

public interface OptimizationAssignmentScoringStrategy {
    double requiredSkillMultiplier();

    double preferredSkillMultiplier();

    double reassignmentPenalty();

    double currentAssigneeBonus();
}
