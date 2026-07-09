/**
 * Author: QuanTuanHuy
 * Description: Part of Serp Project
 */

package serp.project.pmcore.domain.optimization.service.schedule.priority;

import serp.project.pmcore.domain.optimization.model.OptimizationWorkItem;

import java.util.Comparator;
import java.util.Map;

/**
 * Defines the contract for scheduling priority strategies within the project optimization domain.
 * A scheduling priority strategy provides a comparator to order ready work items
 * in the scheduling queue, dictating the order in which they are assigned to resource capacities.
 */
public interface OptimizationSchedulingPriorityStrategy {
    /**
     * Returns a comparator to sort and prioritize work items in the ready queue.
     *
     * @param itemById a lookup map of work items by ID for retrieving work item metadata
     * @return a comparator for work item IDs
     */
    Comparator<Long> readyWorkItemComparator(Map<Long, OptimizationWorkItem> itemById);
}
