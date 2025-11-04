package serp.project.ptm_optimization.kernel.algorithm.heuristic.dto.output;

import java.util.List;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@NoArgsConstructor
@AllArgsConstructor
@Data
@Builder
public class PlanResult {
    private List<Assignment> assignments;
    private List<UnScheduleReason> unScheduled;
}
