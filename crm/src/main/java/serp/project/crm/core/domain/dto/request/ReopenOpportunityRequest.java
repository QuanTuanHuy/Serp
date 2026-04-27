/**
 * Author: QuanTuanHuy
 * Description: Part of Serp Project
 */

package serp.project.crm.core.domain.dto.request;

import jakarta.validation.constraints.NotBlank;
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
public class ReopenOpportunityRequest {
    @NotNull(message = "Stage is required")
    private OpportunityStage stage;

    @NotBlank(message = "Reopen reason is required when reopening a closed lost opportunity")
    @Size(max = 1000, message = "Reopen reason must not exceed 1000 characters")
    private String reopenReason;
}
