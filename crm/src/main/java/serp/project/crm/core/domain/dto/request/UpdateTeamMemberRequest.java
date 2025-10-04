/**
 * Author: QuanTuanHuy
 * Description: Part of Serp Project
 */

package serp.project.crm.core.domain.dto.request;

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
    private String name;
    private String email;
    private String phone;
    private String role;
    private TeamMemberStatus status;
}
