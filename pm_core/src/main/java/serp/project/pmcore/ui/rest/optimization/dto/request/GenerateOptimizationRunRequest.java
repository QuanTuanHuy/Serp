/**
 * Author: QuanTuanHuy
 * Description: Part of Serp Project
 */

package serp.project.pmcore.ui.rest.optimization.dto.request;

import jakarta.validation.constraints.AssertTrue;
import jakarta.validation.constraints.NotEmpty;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Positive;
import jakarta.validation.constraints.Size;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import serp.project.pmcore.domain.optimization.constant.OptimizationAlgorithmKeys;
import serp.project.pmcore.domain.optimization.constant.OptimizationConstants;
import serp.project.pmcore.domain.optimization.enums.OptimizationChangeScope;
import serp.project.pmcore.domain.optimization.enums.OptimizationObjective;

import java.util.List;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class GenerateOptimizationRunRequest {
    @Builder.Default
    private String scope = OptimizationConstants.DEFAULT_SCOPE;

    @Builder.Default
    private String algorithmKey = OptimizationAlgorithmKeys.GREEDY_BALANCED;

    @NotNull(message = "Optimization objective is required")
    private OptimizationObjective objective;

    @NotNull(message = "Optimization change scope is required")
    private OptimizationChangeScope changeScope;

    @NotNull(message = "Planning start is required")
    private Long planningStart;

    @NotNull(message = "Planning end is required")
    private Long planningEnd;

    @NotEmpty(message = "Selected work item ids are required")
    @Size(
            max = OptimizationConstants.MAX_SELECTED_WORK_ITEM_IDS,
            message = "Selected work item ids must not exceed "
                    + OptimizationConstants.MAX_SELECTED_WORK_ITEM_IDS
                    + " items"
    )
    private List<@NotNull @Positive Long> selectedWorkItemIds;

    @AssertTrue(message = "Planning start must be before planning end")
    public boolean isPlanningRangeValid() {
        return planningStart == null || planningEnd == null || planningStart < planningEnd;
    }
}
