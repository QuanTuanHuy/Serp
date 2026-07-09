/**
 * Author: QuanTuanHuy
 * Description: Part of Serp Project
 */

package serp.project.pmcore.domain.optimization.service.assignment;

import serp.project.pmcore.domain.optimization.model.OptimizationAlgorithmOptions;
import serp.project.pmcore.domain.optimization.model.OptimizationAssignmentSuggestion;
import serp.project.pmcore.domain.optimization.model.OptimizationConstraintViolation;
import serp.project.pmcore.domain.optimization.model.OptimizationProjectModel;

import java.util.List;
import java.util.Map;

/**
 * Defines the contract for assignment policies within the project optimization domain.
 * An assignment policy is responsible for deciding resource/assignee assignments
 * for work items based on candidate skills, project constraints, capacity limits, and cost functions.
 */
public interface OptimizationAssignmentPolicy {
    /**
     * Generates assignment suggestions for all work items in a project model.
     *
     * @param projectModel the overall model containing work items, candidate assignees, and resource capacities
     * @param options algorithm options specifying scoring strategies and assignment scope
     * @param warnings a list to collect any constraint violations or warnings encountered during assignment optimization
     * @return a map of work item ID to its corresponding assignment suggestion
     */
    Map<Long, OptimizationAssignmentSuggestion> generateAssignments(
            OptimizationProjectModel projectModel,
            OptimizationAlgorithmOptions options,
            List<OptimizationConstraintViolation> warnings
    );
}
