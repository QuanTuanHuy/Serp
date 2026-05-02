/**
 * Author: QuanTuanHuy
 * Description: Part of Serp Project
 */

package serp.project.crm.core.domain.dto.response;

import com.fasterxml.jackson.annotation.JsonInclude;

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
@JsonInclude(JsonInclude.Include.NON_NULL)
public class TeamMemberResponse {
    private Long id;
    private String name;
    private String email;
    private String phone;
    private Long teamId;
    private Long userId;
    private String role;
    private TeamMemberStatus status;
    private List<String> skills;
    private List<String> languages;
    private ExperienceLevel experienceLevel;
    private Integer capacity;
    private Integer maxMeetings;
    private List<WorkingHoursResponse> workingHours;
    
    // Metadata
    private Long tenantId;
    private Long createdAt;
    private Long updatedAt;
    private Long createdBy;
    private Long updatedBy;
}
