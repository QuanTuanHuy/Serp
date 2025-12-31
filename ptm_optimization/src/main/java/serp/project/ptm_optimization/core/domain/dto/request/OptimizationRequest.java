package serp.project.ptm_optimization.core.domain.dto.request;

import java.util.List;

import lombok.Data;
import lombok.NoArgsConstructor;
import serp.project.ptm_optimization.infrastructure.algorithm.dto.input.Params;
import serp.project.ptm_optimization.infrastructure.algorithm.dto.input.TaskInput;
import serp.project.ptm_optimization.infrastructure.algorithm.dto.input.Weights;
import serp.project.ptm_optimization.infrastructure.algorithm.dto.input.Window;

@Data
@NoArgsConstructor
public class OptimizationRequest {
    private List<TaskInput> tasks;
    private List<Window> windows;
    private Weights weights;
    private Params params;

}