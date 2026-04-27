/**
 * Author: QuanTuanHuy
 * Description: Part of Serp Project
 */

package serp.project.crm.core.domain.dto.request;

import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import serp.project.crm.core.domain.enums.OpportunityStage;

@NoArgsConstructor
@AllArgsConstructor
@Data
@Builder
public class ChangeOpportunityStageRequest {
    @NotNull(message = "Stage is required")
    private OpportunityStage stage;

    @Size(max = 1000, message = "Loss reason must not exceed 1000 characters")
    private String lossReason;

    @Size(max = 1000, message = "Notes must not exceed 1000 characters")
    private String notes;
}
