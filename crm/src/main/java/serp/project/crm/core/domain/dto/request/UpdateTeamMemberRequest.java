/**
 * Author: QuanTuanHuy
 * Description: Part of Serp Project
 */

package serp.project.crm.core.domain.dto.request;

import jakarta.validation.Valid;
import jakarta.validation.constraints.*;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import serp.project.crm.core.domain.enums.ExperienceLevel;
import serp.project.crm.core.domain.enums.TeamMemberStatus;

import java.util.List;

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

    private List<@Size(max = 100, message = "Skill must not exceed 100 characters") String> skills;

    private List<@Size(max = 50, message = "Language must not exceed 50 characters") String> languages;

    private ExperienceLevel experienceLevel;

    @Min(value = 1, message = "Capacity must be at least 1")
    @Max(value = 100, message = "Capacity must not exceed 100")
    private Integer capacity;

    @Min(value = 1, message = "Max meetings must be at least 1")
    @Max(value = 50, message = "Max meetings must not exceed 50")
    private Integer maxMeetings;

    @Valid
    private List<WorkingHoursRequest> workingHours;
}
