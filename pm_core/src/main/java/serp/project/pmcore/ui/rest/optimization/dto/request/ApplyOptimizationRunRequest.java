/**
 * Author: QuanTuanHuy
 * Description: Part of Serp Project
 */

package serp.project.pmcore.ui.rest.optimization.dto.request;

import jakarta.validation.constraints.NotEmpty;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Positive;
import jakarta.validation.constraints.Size;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.util.List;

@Getter
@Setter
@NoArgsConstructor
public class ApplyOptimizationRunRequest {
    @NotNull
    private Boolean applyAssignment;

    @NotNull
    private Boolean applySchedule;

    @NotEmpty
    @Size(max = 50)
    private List<@NotNull @Positive Long> workItemIds;
}
