/**
 * Author: QuanTuanHuy
 * Description: Part of Serp Project
 */

package serp.project.crm.core.domain.dto.request;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@NoArgsConstructor
@AllArgsConstructor
@Data
@Builder
public class CloseOpportunityLostRequest {
    @NotBlank(message = "Loss reason is required")
    @Size(max = 1000, message = "Loss reason must not exceed 1000 characters")
    private String lossReason;
}
