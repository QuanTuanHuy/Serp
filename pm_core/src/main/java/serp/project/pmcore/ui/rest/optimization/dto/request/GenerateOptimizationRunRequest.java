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
import serp.project.pmcore.domain.optimization.enums.OptimizationMode;

import java.util.List;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class GenerateOptimizationRunRequest {
    @Builder.Default
    private String scope = "SELECTED_WORK_ITEMS";

    @NotNull(message = "Optimization mode is required")
    private OptimizationMode mode;

    @NotNull(message = "Planning start is required")
    private Long planningStart;

    @NotNull(message = "Planning end is required")
    private Long planningEnd;

    @NotNull(message = "Allow reassignment flag is required")
    private Boolean allowReassignment;

    @NotNull(message = "Allow schedule changes flag is required")
    private Boolean allowScheduleChanges;

    @NotEmpty(message = "Selected work item ids are required")
    @Size(max = 50, message = "Selected work item ids must not exceed 50 items")
    private List<@NotNull @Positive Long> selectedWorkItemIds;

    @AssertTrue(message = "Planning start must be before planning end")
    public boolean isPlanningRangeValid() {
        return planningStart == null || planningEnd == null || planningStart < planningEnd;
    }
}
