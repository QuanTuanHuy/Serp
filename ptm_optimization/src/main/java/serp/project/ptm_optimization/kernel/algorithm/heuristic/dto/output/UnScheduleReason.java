package serp.project.ptm_optimization.kernel.algorithm.heuristic.dto.output;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@NoArgsConstructor
@AllArgsConstructor
@Data
@Builder
public class UnScheduleReason {
    private Long taskId;
    private String reason;
}
