/**
 * Author: QuanTuanHuy
 * Description: Part of Serp Project
 */

package serp.project.pmcore.domain.optimization.service.schedule;

import serp.project.pmcore.domain.optimization.model.OptimizationAlgorithmOptions;
import serp.project.pmcore.domain.optimization.model.OptimizationAssignmentSuggestion;
import serp.project.pmcore.domain.optimization.model.OptimizationConstraintViolation;
import serp.project.pmcore.domain.optimization.model.OptimizationProjectModel;
import serp.project.pmcore.domain.optimization.model.OptimizationScheduleSuggestion;

import java.util.List;
import java.util.Map;

/**
 * Defines the contract for scheduling policies within the project optimization domain.
 * A scheduling policy is responsible for determining the planned start and end times
 * of work items based on their dependencies, resource availability, and assignment decisions.
 */
public interface OptimizationSchedulingPolicy {
    /**
     * Generates schedule suggestions for all work items in a project model.
     *
     * @param projectModel the overall model containing work items, dependencies, and capacity slots
     * @param options algorithm options specifying prioritization strategy and scheduling scope
     * @param assignments current resource assignment suggestions (mapping work item ID to assignment details)
     * @param warnings a list to collect any constraint violations or warnings encountered during scheduling
     * @return a map of work item ID to its corresponding schedule suggestion
     */
    Map<Long, OptimizationScheduleSuggestion> generateSchedules(
            OptimizationProjectModel projectModel,
            OptimizationAlgorithmOptions options,
            Map<Long, OptimizationAssignmentSuggestion> assignments,
            List<OptimizationConstraintViolation> warnings
    );
}
