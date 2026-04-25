/**
 * Author: QuanTuanHuy
 * Description: Part of Serp Project
 */

package serp.project.crm.core.domain.dto.request;

import jakarta.validation.constraints.*;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@NoArgsConstructor
@AllArgsConstructor
@Data
@Builder
public class QualifyLeadRequest {
    private Long leadId;

    @NotBlank(message = "Qualification notes are required")
    @Size(max = 1000, message = "Qualification notes must not exceed 1000 characters")
    private String notes;

    private Boolean budgetConfirmed;
    private Boolean hasAuthority;
    private Boolean needIdentified;
    private Boolean timelineEstablished;
}
