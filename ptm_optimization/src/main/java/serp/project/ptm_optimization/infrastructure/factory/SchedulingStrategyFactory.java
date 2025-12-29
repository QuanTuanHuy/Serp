/**
 * Author: QuanTuanHuy
 * Description: Part of Serp Project - Strategy Factory
 */

package serp.project.ptm_optimization.infrastructure.factory;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;
import serp.project.ptm_optimization.core.domain.enums.StrategyType;
import serp.project.ptm_optimization.core.port.factory.ISchedulingStrategyFactory;
import serp.project.ptm_optimization.core.port.strategy.ISchedulingStrategy;
import serp.project.ptm_optimization.kernel.algorithm.strategy.CpSatStrategy;
import serp.project.ptm_optimization.kernel.algorithm.strategy.HeuristicStrategy;
import serp.project.ptm_optimization.kernel.algorithm.strategy.MilpStrategy;

import java.util.List;

/**
 * Factory Pattern implementation for creating scheduling strategies.
 * Provides centralized strategy selection with auto-detection based on problem size.
 */
@Component
@RequiredArgsConstructor
@Slf4j
public class SchedulingStrategyFactory implements ISchedulingStrategyFactory {

    private final HeuristicStrategy heuristicStrategy;
    private final MilpStrategy milpStrategy;
    private final CpSatStrategy cpSatStrategy;

    /**
     * Create a strategy by type.
     *
     * @param type Strategy type enum
     * @return Strategy implementation
     */
    public ISchedulingStrategy create(StrategyType type) {
        return switch (type) {
            case HEURISTIC -> heuristicStrategy;
            case MILP -> {
                if (!milpStrategy.isAvailable()) {
                    log.warn("MILP not available, falling back to HEURISTIC");
                    yield heuristicStrategy;
                }
                yield milpStrategy;
            }
            case CPSAT -> {
                if (!cpSatStrategy.isAvailable()) {
                    log.warn("CP-SAT not available, falling back to MILP");
                    yield milpStrategy.isAvailable() ? milpStrategy : heuristicStrategy;
                }
                yield cpSatStrategy;
            }
            case AUTO -> createBestFor(0, 0); // Will be set by caller
            default -> {
                log.warn("Unknown strategy type: {}, using HEURISTIC", type);
                yield heuristicStrategy;
            }
        };
    }

    /**
     * Auto-select best strategy based on problem size.
     * Selection logic:
     * - <20 tasks: HEURISTIC (fast, good enough)
     * - 20-30 tasks: MILP (optimal, reasonable time)
     * - 30-100 tasks: CP-SAT (handles complexity)
     * - >100 tasks: HEURISTIC (only scalable option)
     *
     * @param taskCount Number of tasks
     * @param slotCount Number of time slots
     * @return Best strategy for this problem size
     */
    public ISchedulingStrategy createBestFor(int taskCount, int slotCount) {
        log.info("Auto-selecting strategy for: tasks={}, slots={}", taskCount, slotCount);

        if (taskCount > 100) {
            log.info("Selected HEURISTIC (task count > 100)");
            return heuristicStrategy;
        }

        if (taskCount > 30 && cpSatStrategy.isAvailable() && 
            cpSatStrategy.canHandle(taskCount, slotCount)) {
            log.info("Selected CP-SAT (30 < tasks <= 100)");
            return cpSatStrategy;
        }

        if (taskCount >= 20 && taskCount <= 30 && milpStrategy.isAvailable() && 
            milpStrategy.canHandle(taskCount, slotCount)) {
            log.info("Selected MILP (20 <= tasks <= 30)");
            return milpStrategy;
        }

        log.info("Selected HEURISTIC (tasks < 20 or fallback)");
        return heuristicStrategy;
    }

    /**
     * Get all available strategies in priority order for fallback chain.
     *
     * @return List of strategies (CP-SAT -> MILP -> Heuristic)
     */
    public List<ISchedulingStrategy> getFallbackChain() {
        return List.of(
                cpSatStrategy.isAvailable() ? cpSatStrategy : null,
                milpStrategy.isAvailable() ? milpStrategy : null,
                heuristicStrategy
        ).stream().filter(s -> s != null).toList();
    }
}
