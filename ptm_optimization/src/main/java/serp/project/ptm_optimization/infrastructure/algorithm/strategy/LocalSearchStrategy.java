/*
Author: QuanTuanHuy
Description: Part of Serp Project - Local Search Strategy Implementation
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
import serp.project.ptm_optimization.infrastructure.algorithm.localsearch.LocalSearchScheduler;

import java.util.List;

/**
 * Local Search strategy using Simulated Annealing.
 * 
 * Workflow:
 * 1. Generate initial solution with GapBasedScheduler (fast)
 * 2. Refine with local search moves (SWAP, SHIFT)
 * 3. Accept worse solutions probabilistically to escape local optima
 * 
 * Best for:
 * - Medium to large problems (100-1000 tasks)
 * - When GapBased quality not sufficient
 * - Post-optimization of existing schedules
 * - Rescheduling scenarios (user edits)
 */
@Component
@RequiredArgsConstructor
@Slf4j
public class LocalSearchStrategy implements ISchedulingStrategy {

    private final LocalSearchScheduler localSearchScheduler;

    @Override
    public PlanResult schedule(List<TaskInput> tasks, List<Window> windows, Weights weights, Params params) {
        log.info("Running LOCAL_SEARCH (Simulated Annealing) strategy: tasks={}, windows={}", 
            tasks.size(), windows.size());
        long startTime = System.currentTimeMillis();

        PlanResult result = localSearchScheduler.schedule(tasks, windows, weights, params);

        long duration = System.currentTimeMillis() - startTime;
        log.info("LOCAL_SEARCH completed in {}ms: scheduled={}, unscheduled={}",
                duration, result.getAssignments().size(), result.getUnScheduled().size());

        return result;
    }

    @Override
    public String getName() {
        return "LOCAL_SEARCH";
    }

    @Override
    public boolean isAvailable() {
        return true;
    }

    @Override
    public int getMaxRecommendedTasks() {
        return 1000;
    }

    @Override
    public int getMaxRecommendedSlots() {
        return Integer.MAX_VALUE;
    }
}
