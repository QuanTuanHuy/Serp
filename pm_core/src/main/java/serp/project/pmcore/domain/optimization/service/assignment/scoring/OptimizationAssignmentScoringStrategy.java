/**
 * Author: QuanTuanHuy
 * Description: Part of Serp Project
 */

package serp.project.pmcore.domain.optimization.service.assignment.scoring;

/**
 * Defines the contract for assignment scoring strategies within the project optimization domain.
 * Scoring strategies provide multipliers and penalties used to compute the effective cost
 * of assigning candidate resources to work items, allowing different trade-offs
 * between skill match alignment and reassignment disruption.
 */
public interface OptimizationAssignmentScoringStrategy {
    /**
     * Returns the multiplier applied to required skill match bonuses.
     *
     * @return the required skill multiplier
     */
    double requiredSkillMultiplier();

    /**
     * Returns the multiplier applied to preferred skill match bonuses.
     *
     * @return the preferred skill multiplier
     */
    double preferredSkillMultiplier();

    /**
     * Returns the penalty cost added when a work item is reassigned to a different resource.
     *
     * @return the reassignment penalty cost
     */
    double reassignmentPenalty();

    /**
     * Returns the bonus score subtracted when the current resource assignment is retained.
     *
     * @return the current assignee bonus score
     */
    double currentAssigneeBonus();
}
