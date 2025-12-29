package serp.project.ptm_optimization.kernel.algorithm.heuristic.dto.input;

import java.time.Duration;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@NoArgsConstructor
@AllArgsConstructor
@Data
@Builder
public class Params {
    private Integer slotMin;
    private Duration timeBudgetLS;
    private Integer maxTimeSec; // For CP-SAT solver time limit
    
    // Local Search parameters
    private Double initialTemperature; // Simulated Annealing initial temp (default: 1000.0)
    private Double coolingRate;        // Temperature decrease rate (default: 0.95)
    private Integer maxIterations;     // Max iterations for local search (default: 1000)
}
