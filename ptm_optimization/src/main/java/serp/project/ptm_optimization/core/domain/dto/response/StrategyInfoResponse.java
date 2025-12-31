package serp.project.ptm_optimization.core.domain.dto.response;

import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
public class StrategyInfoResponse {
    private String name;
    private String description;
    private boolean available;
    private int maxTasks;
    private int maxSlots;
}
