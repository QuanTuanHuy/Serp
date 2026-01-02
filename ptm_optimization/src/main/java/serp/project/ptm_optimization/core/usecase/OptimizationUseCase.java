/**
 * Author: QuanTuanHuy
 * Description: Part of Serp Project - Optimization Use Case
 */

package serp.project.ptm_optimization.core.usecase;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import serp.project.ptm_optimization.core.domain.enums.StrategyType;
import serp.project.ptm_optimization.core.port.factory.ISchedulingStrategyFactory;
import serp.project.ptm_optimization.core.port.strategy.ISchedulingStrategy;
import serp.project.ptm_optimization.infrastructure.algorithm.dto.input.Params;
import serp.project.ptm_optimization.infrastructure.algorithm.dto.input.TaskInput;
import serp.project.ptm_optimization.infrastructure.algorithm.dto.input.Weights;
import serp.project.ptm_optimization.infrastructure.algorithm.dto.input.Window;
import serp.project.ptm_optimization.infrastructure.algorithm.dto.output.PlanResult;

import java.util.List;

/**
 * Use Case for running scheduling optimization.
 * Orchestrates strategy selection and execution.
 */
@Service
@RequiredArgsConstructor
@Slf4j
public class OptimizationUseCase {

    private final ISchedulingStrategyFactory strategyFactory;

    /**
     * Run optimization with specified strategy.
     */
    @Transactional
    public PlanResult optimize(
            List<TaskInput> tasks,
            List<Window> windows,
            Weights weights,
            Params params,
            StrategyType strategyType
    ) {
        log.info("Starting optimization: tasks={}, windows={}, strategy={}", 
                tasks.size(), windows.size(), strategyType);

        ISchedulingStrategy strategy;
        if (strategyType == StrategyType.AUTO) {
            int slotMin = params != null && params.getSlotMin() != null ? params.getSlotMin() : 15;
            int slotCount = calculateSlotCount(windows, slotMin);
            strategy = strategyFactory.createBestFor(tasks.size(), slotCount);
        } else {
            strategy = strategyFactory.create(strategyType);
        }

        log.info("Selected strategy: {}", strategy.getName());

        long startTime = System.currentTimeMillis();
        PlanResult result = strategy.schedule(tasks, windows, weights, params);
        long duration = System.currentTimeMillis() - startTime;

        log.info("Optimization completed in {}ms: scheduled={}/{}, unscheduled={}",
                duration, result.getAssignments().size(), tasks.size(), 
                result.getUnScheduled().size());

        return result;
    }

    /**
     * Run optimization with fallback chain (CP-SAT -> MILP -> Heuristic).
     */
    @Transactional
    public PlanResult optimizeWithFallback(
            List<TaskInput> tasks,
            List<Window> windows,
            Weights weights,
            Params params
    ) {
        List<ISchedulingStrategy> chain = strategyFactory.getFallbackChain();
        
        PlanResult bestResult = null;
        Exception lastException = null;

        for (ISchedulingStrategy strategy : chain) {
            try {
                log.info("Trying strategy: {}", strategy.getName());
                
                PlanResult result = strategy.schedule(tasks, windows, weights, params);
                
                // Accept if all tasks scheduled, or if it's the last strategy
                if (result.getUnScheduled().isEmpty() || 
                    strategy == chain.get(chain.size() - 1)) {
                    log.info("Strategy {} succeeded", strategy.getName());
                    return result;
                }
                
                // Keep best partial result
                if (bestResult == null || 
                    result.getAssignments().size() > bestResult.getAssignments().size()) {
                    bestResult = result;
                }
                
            } catch (Exception e) {
                log.warn("Strategy {} failed: {}", strategy.getName(), e.getMessage());
                lastException = e;
            }
        }

        // All strategies failed, return best partial result or throw
        if (bestResult != null) {
            log.warn("All strategies had unscheduled tasks, returning best partial result");
            return bestResult;
        }

        throw new RuntimeException("All optimization strategies failed", lastException);
    }

    private int calculateSlotCount(List<Window> windows, int slotMin) {
        int total = 0;
        for (Window w : windows) {
            int start = w.getStartMin() != null ? w.getStartMin() : 0;
            int end = w.getEndMin() != null ? w.getEndMin() : 1440;
            total += (end - start) / slotMin;
        }
        return total;
    }
}
