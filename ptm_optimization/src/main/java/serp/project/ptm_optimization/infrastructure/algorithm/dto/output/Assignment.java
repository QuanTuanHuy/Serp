package serp.project.ptm_optimization.infrastructure.algorithm.dto.output;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@NoArgsConstructor
@AllArgsConstructor
@Data
@Builder
public class Assignment {
    private Long taskId;
    private Long dateMs;
    private Integer startMin;
    private Integer endMin;
    private Double utility;
    // private UtilityBreakdown rationale;
}
