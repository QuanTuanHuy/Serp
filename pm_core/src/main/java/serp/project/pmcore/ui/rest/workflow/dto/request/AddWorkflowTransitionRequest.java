/**
 * Author: QuanTuanHuy
 * Description: Part of Serp Project
 */

package serp.project.pmcore.ui.rest.workflow.dto.request;

import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class AddWorkflowTransitionRequest {

    @NotBlank(message = "name is required")
    private String name;

    private Long fromStepId;

    @NotNull(message = "toStepId is required")
    private Long toStepId;

    private Long screenId;

    @Min(value = 0, message = "sequence must be greater than or equal to 0")
    private Integer sequence;
}
