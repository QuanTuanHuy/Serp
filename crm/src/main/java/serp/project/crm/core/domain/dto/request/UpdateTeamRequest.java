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
import serp.project.crm.core.domain.enums.TeamStatus;

@NoArgsConstructor
@AllArgsConstructor
@Data
@Builder
public class UpdateTeamRequest {
    @Size(max = 255, message = "Name must not exceed 255 characters")
    private String name;
    
    @Size(max = 1000, message = "Description must not exceed 1000 characters")
    private String description;
    
    private Long managerUserId;

    private TeamStatus status;
    
    @Size(max = 1000, message = "Notes must not exceed 1000 characters")
    private String notes;
}
