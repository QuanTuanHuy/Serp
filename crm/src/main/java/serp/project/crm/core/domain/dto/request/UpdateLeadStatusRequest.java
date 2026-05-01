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
import serp.project.crm.core.domain.enums.LeadStatus;

@NoArgsConstructor
@AllArgsConstructor
@Data
@Builder
public class UpdateLeadStatusRequest {

    private LeadStatus fromStatus;

    @NotNull(message = "Target lead status is required")
    private LeadStatus toStatus;

    @Size(max = 1000, message = "Status update notes must not exceed 1000 characters")
    private String notes;
}
