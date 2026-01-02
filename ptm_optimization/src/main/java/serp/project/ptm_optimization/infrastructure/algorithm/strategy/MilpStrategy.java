/**
 * Author: QuanTuanHuy
 * Description: Part of Serp Project - MILP Strategy Implementation
 */

package serp.project.ptm_optimization.infrastructure.algorithm.strategy;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;
import serp.project.ptm_optimization.core.port.strategy.ISchedulingStrategy;
import serp.project.ptm_optimization.infrastructure.algorithm.dto.input.Params;
import serp.project.ptm_optimization.infrastructure.algorithm.dto.input.TaskInput;
import serp.project.ptm_optimization.infrastructure.algorithm.dto.input.Weights;
import serp.project.ptm_optimization.infrastructure.algorithm.dto.input.Window;
import serp.project.ptm_optimization.infrastructure.algorithm.dto.output.PlanResult;
import serp.project.ptm_optimization.infrastructure.algorithm.milp.MilpScheduler;

import java.util.List;

/**
 * Mixed Integer Linear Programming strategy using Google OR-Tools.
 * Optimal solutions for small-medium problems (<30 tasks).
 */
@Component
@RequiredArgsConstructor
@Slf4j
public class MilpStrategy implements ISchedulingStrategy {

    private final MilpScheduler milpScheduler;

    private static final int MAX_TASKS = 30;
    private static final int MAX_SLOTS = 500;

    @Override
    public PlanResult schedule(List<TaskInput> tasks, List<Window> windows, Weights weights, Params params) {
        log.info("Running MILP strategy: tasks={}, windows={}", tasks.size(), windows.size());

        int slotMin = params != null && params.getSlotMin() != null ? params.getSlotMin() : 15;

        long startTime = System.currentTimeMillis();
        PlanResult result = milpScheduler.schedule(tasks, windows, weights, slotMin);
        long duration = System.currentTimeMillis() - startTime;

        log.info("MILP completed in {}ms: scheduled={}, unscheduled={}",
                duration, result.getAssignments().size(), result.getUnScheduled().size());

        return result;
    }

    @Override
    public String getName() {
        return "MILP";
    }

    @Override
    public boolean isAvailable() {
        return MilpScheduler.isAvailable();
    }

    @Override
    public int getMaxRecommendedTasks() {
        return MAX_TASKS;
    }

    @Override
    public int getMaxRecommendedSlots() {
        return MAX_SLOTS;
    }
}
