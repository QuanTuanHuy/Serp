package serp.project.ptm_optimization.kernel.algorithm.heuristic.dto.output;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@NoArgsConstructor
@AllArgsConstructor
@Data
@Builder
public class UtilityBreakdown {
    private Double priority;
    private Double deadline;
    private Double uSwitch;
    private Double fatigue;
    private Double enjoy;
}
