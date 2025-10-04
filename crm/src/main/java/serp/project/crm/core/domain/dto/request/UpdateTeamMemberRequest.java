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
import serp.project.crm.core.domain.enums.TeamMemberStatus;

@NoArgsConstructor
@AllArgsConstructor
@Data
@Builder
public class UpdateTeamMemberRequest {
    @Size(max = 255, message = "Name must not exceed 255 characters")
    private String name;
    
    @Email(message = "Email must be valid")
    @Size(max = 255, message = "Email must not exceed 255 characters")
    private String email;
    
    @Size(max = 20, message = "Phone must not exceed 20 characters")
    private String phone;
    
    @Size(max = 100, message = "Role must not exceed 100 characters")
    private String role;
    
    private TeamMemberStatus status;
}
