/**
 * Author: QuanTuanHuy
 * Description: Part of Serp Project - Heuristic Strategy Implementation
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
import serp.project.ptm_optimization.infrastructure.algorithm.heuristic.GapBasedScheduler;

import java.util.List;

/**
 * Heuristic-based scheduling strategy.
 * Uses Gap-based scheduler for better gap utilization and less fragmentation.
 */
@Component
@RequiredArgsConstructor
@Slf4j
public class HeuristicStrategy implements ISchedulingStrategy {

    private final GapBasedScheduler gapBasedScheduler;

    @Override
    public PlanResult schedule(List<TaskInput> tasks, List<Window> windows, Weights weights, Params params) {
        log.info("Running HEURISTIC (Gap-based) strategy: tasks={}, windows={}", tasks.size(), windows.size());
        long startTime = System.currentTimeMillis();

        PlanResult result = gapBasedScheduler.schedule(tasks, windows, weights, params);

        long duration = System.currentTimeMillis() - startTime;
        log.info("HEURISTIC (Gap-based) completed in {}ms: scheduled={}, unscheduled={}",
                duration, result.getAssignments().size(), result.getUnScheduled().size());

        return result;
    }

    @Override
    public String getName() {
        return "HEURISTIC";
    }

    @Override
    public boolean isAvailable() {
        return true;
    }

    @Override
    public int getMaxRecommendedTasks() {
        return Integer.MAX_VALUE; // No hard limit
    }

    @Override
    public int getMaxRecommendedSlots() {
        return Integer.MAX_VALUE;
    }
}
