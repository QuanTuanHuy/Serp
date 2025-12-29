package serp.project.ptm_optimization.core.port.factory;

import java.util.List;

import serp.project.ptm_optimization.core.domain.enums.StrategyType;
import serp.project.ptm_optimization.core.port.strategy.ISchedulingStrategy;

public interface ISchedulingStrategyFactory {
    /**
     * Create a strategy by type.
     *
     * @param type Strategy type enum
     * @return Strategy implementation
     */
    ISchedulingStrategy create(StrategyType type);

    /**
     * Auto-select best strategy based on problem size.
     * @param taskCount Number of tasks
     * @param slotCount Number of time slots
     * @return Best strategy for this problem size
     */
    ISchedulingStrategy createBestFor(int taskCount, int slotCount);

    /**
     * Get all available strategies in priority order for fallback chain.
     *
     * @return List of strategies
     */
    public List<ISchedulingStrategy> getFallbackChain();
}
