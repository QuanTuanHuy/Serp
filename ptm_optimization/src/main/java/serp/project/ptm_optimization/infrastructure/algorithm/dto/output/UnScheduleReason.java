package serp.project.ptm_optimization.infrastructure.algorithm.dto.output;

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
