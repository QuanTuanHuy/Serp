/**
 * Author: QuanTuanHuy
 * Description: Part of Serp Project - Additional parameters for CP-SAT
 */

package serp.project.ptm_optimization.kernel.algorithm.heuristic.dto.input;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.Duration;

/**
 * Extended parameters for optimization algorithms.
 * Add fields for CP-SAT configuration.
 * 
 * NOTE: This is a reference design. If Params class already exists,
 * add these fields to the existing class instead.
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class ParamsExtension {

    // Existing fields (from original Params if any)
    private Integer slotMin; // Time slot granularity in minutes (e.g., 15, 30)
    private Duration timeBudgetLS; // Local search time budget

    // CP-SAT specific parameters
    private Integer maxTimeSec; // Maximum solver time in seconds (default: 30)
    private Integer numWorkers; // Number of parallel workers (default: 4)
    private Boolean logSearchProgress; // Log solver progress (default: false)
    private Double optimizationStep; // Minimum improvement threshold (default: 0.01)

    // MILP specific parameters
    private Boolean useWarmStart; // Use heuristic solution as warm-start (default: true)

    // General parameters
    private Boolean enableCaching; // Cache optimization results (default: true)
    private Integer cacheTtlSeconds; // Cache TTL (default: 300)
}
