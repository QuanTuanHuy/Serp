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
    
    @Size(max = 1000, message = "Qualification notes must not exceed 1000 characters")
    private String qualificationNotes;
}
