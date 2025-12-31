/**
 * Author: QuanTuanHuy
 * Description: Part of Serp Project - CP-SAT Strategy Implementation
 */

package serp.project.ptm_optimization.infrastructure.algorithm.strategy;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;
import serp.project.ptm_optimization.core.port.strategy.ISchedulingStrategy;
import serp.project.ptm_optimization.infrastructure.algorithm.cpsat.CpSatScheduler;
import serp.project.ptm_optimization.infrastructure.algorithm.dto.input.Params;
import serp.project.ptm_optimization.infrastructure.algorithm.dto.input.TaskInput;
import serp.project.ptm_optimization.infrastructure.algorithm.dto.input.Weights;
import serp.project.ptm_optimization.infrastructure.algorithm.dto.input.Window;
import serp.project.ptm_optimization.infrastructure.algorithm.dto.output.PlanResult;

import java.util.List;

/**
 * Constraint Programming SAT-based strategy using Google OR-Tools CP-SAT solver.
 * Best for complex scheduling problems with many constraints (50-100+ tasks).
 * Native support for interval variables and no-overlap constraints.
 */
@Component
@RequiredArgsConstructor
@Slf4j
public class CpSatStrategy implements ISchedulingStrategy {

    private final CpSatScheduler cpSatScheduler;

    private static final int MAX_TASKS = 100;
    private static final int MAX_SLOTS = 1000;

    @Override
    public PlanResult schedule(List<TaskInput> tasks, List<Window> windows, Weights weights, Params params) {
        log.info("Running CP-SAT strategy: tasks={}, windows={}", tasks.size(), windows.size());

        long startTime = System.currentTimeMillis();
        PlanResult result = cpSatScheduler.schedule(tasks, windows, weights, params);
        long duration = System.currentTimeMillis() - startTime;

        log.info("CP-SAT completed in {}ms: scheduled={}, unscheduled={}",
                duration, result.getAssignments().size(), result.getUnScheduled().size());

        return result;
    }

    @Override
    public String getName() {
        return "CP-SAT";
    }

    @Override
    public boolean isAvailable() {
        return CpSatScheduler.isAvailable();
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
