/**
 * Author: QuanTuanHuy
 * Description: Part of Serp Project
 */

package serp.project.account.core.domain.dto.response;

import com.fasterxml.jackson.annotation.JsonInclude;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@NoArgsConstructor
@AllArgsConstructor
@Data
@Builder
@JsonInclude(JsonInclude.Include.NON_NULL)
public class UserStatsResponse {
    private Integer totalUsers;
    private Integer activeUsers;
    private Integer inactiveUsers;
    private Integer suspendedUsers;
    private Integer invitedUsers;
    private Integer adminUsers;
    private Integer newUsersThisMonth;
    private Integer newUsersLastMonth;
}
