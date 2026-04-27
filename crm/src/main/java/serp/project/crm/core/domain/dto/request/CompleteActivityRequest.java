/**
 * Author: QuanTuanHuy
 * Description: Part of Serp Project
 */

package serp.project.crm.core.domain.dto.request;

import jakarta.validation.constraints.Size;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import serp.project.crm.core.domain.enums.ActivityOutcome;

@NoArgsConstructor
@AllArgsConstructor
@Data
@Builder
public class CompleteActivityRequest {
    private ActivityOutcome outcome;

    @Size(max = 1000, message = "Notes must not exceed 1000 characters")
    private String notes;
}
