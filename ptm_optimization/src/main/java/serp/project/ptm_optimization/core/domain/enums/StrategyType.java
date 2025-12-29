/**
 * Author: QuanTuanHuy
 * Description: Part of Serp Project - Strategy Type Enum
 */

package serp.project.ptm_optimization.core.domain.enums;

import lombok.Getter;
import lombok.RequiredArgsConstructor;

/**
 * Enum for scheduling strategy types.
 * Used to select which algorithm to use for optimization.
 */
@Getter
@RequiredArgsConstructor
public enum StrategyType {
    /**
     * Auto-select best strategy based on problem size.
     */
    AUTO("Auto-select based on problem size"),

    /**
     * Heuristic greedy + local search.
     * Fast, scalable, always finds solution.
     */
    HEURISTIC("Greedy + Local Search"),

    /**
     * Mixed Integer Linear Programming.
     * Optimal for small problems (<30 tasks).
     */
    MILP("Mixed Integer Linear Programming"),

    /**
     * Constraint Programming SAT-based solver.
     * Best for medium-large scheduling problems (30-100 tasks).
     */
    CPSAT("Constraint Programming SAT"),

    /**
     * Hybrid: Heuristic warm-start + CPSAT/MILP refinement.
     */
    HYBRID("Hybrid (Heuristic + CPSAT/MILP)");

    private final String description;
}
