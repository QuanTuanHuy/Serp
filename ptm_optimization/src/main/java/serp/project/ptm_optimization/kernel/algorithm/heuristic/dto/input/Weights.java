
package serp.project.ptm_optimization.kernel.algorithm.heuristic.dto.input;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@NoArgsConstructor
@AllArgsConstructor
@Data
@Builder
public class Weights {
    private Double wPriority;
    private Double wDeadline;
    private Double wSwitch;
    private Double wFatigue;
    private Double wEnjoy;
}
