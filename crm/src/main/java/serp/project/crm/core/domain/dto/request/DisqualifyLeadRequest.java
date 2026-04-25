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
public class DisqualifyLeadRequest {
    @NotBlank(message = "Disqualification notes are required")
    @Size(max = 1000, message = "Disqualification notes must not exceed 1000 characters")
    private String notes;
}
