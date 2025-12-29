/**
 * Author: QuanTuanHuy
 * Description: Part of Serp Project - Strategy Pattern Interface
 */

package serp.project.ptm_optimization.core.port.strategy;

import serp.project.ptm_optimization.kernel.algorithm.heuristic.dto.input.Params;
import serp.project.ptm_optimization.kernel.algorithm.heuristic.dto.input.TaskInput;
import serp.project.ptm_optimization.kernel.algorithm.heuristic.dto.input.Weights;
import serp.project.ptm_optimization.kernel.algorithm.heuristic.dto.input.Window;
import serp.project.ptm_optimization.kernel.algorithm.heuristic.dto.output.PlanResult;

import java.util.List;

/**
 * Strategy Pattern Interface for scheduling algorithms.
 * Each implementation provides a different approach to solving the scheduling problem.
 */
public interface ISchedulingStrategy {

    /**
     * Schedule tasks into available windows using this strategy's algorithm.
     *
     * @param tasks   List of tasks to schedule
     * @param windows Available time windows
     * @param weights Optimization weights (priority, deadline, context-switch, etc.)
     * @param params  Algorithm-specific parameters (time budget, slot size, etc.)
     * @return PlanResult containing scheduled assignments and unscheduled tasks
     */
    PlanResult schedule(List<TaskInput> tasks, List<Window> windows, Weights weights, Params params);

    /**
     * Get the name of this scheduling strategy.
     *
     * @return Strategy name (e.g., "HEURISTIC", "MILP", "CPSAT")
     */
    String getName();

    /**
     * Check if this strategy is available (e.g., solver library loaded).
     *
     * @return true if strategy can be used
     */
    boolean isAvailable();

    /**
     * Get recommended maximum number of tasks for this strategy.
     *
     * @return Maximum task count, or Integer.MAX_VALUE if unlimited
     */
    int getMaxRecommendedTasks();

    /**
     * Get recommended maximum number of time slots for this strategy.
     *
     * @return Maximum slot count, or Integer.MAX_VALUE if unlimited
     */
    int getMaxRecommendedSlots();

    /**
     * Check if this strategy supports the given problem size.
     *
     * @param taskCount Number of tasks
     * @param slotCount Number of time slots
     * @return true if strategy can handle this size
     */
    default boolean canHandle(int taskCount, int slotCount) {
        return taskCount <= getMaxRecommendedTasks() && slotCount <= getMaxRecommendedSlots();
    }
}
