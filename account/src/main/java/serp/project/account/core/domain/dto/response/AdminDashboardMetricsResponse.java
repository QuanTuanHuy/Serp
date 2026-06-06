/**
 * Author: QuanTuanHuy
 * Description: Part of Serp Project
 */

package serp.project.account.core.domain.dto.response;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@NoArgsConstructor
@AllArgsConstructor
@Getter
@Setter
@Builder
public class AdminDashboardMetricsResponse {
    private Long totalOrganizations;
    private Long activeOrganizations;
    private Long suspendedOrganizations;
    private Long expiredOrganizations;
    private Long totalUsers;
    private Long activeUsers;
    private Long suspendedUsers;
    private Long totalSubscriptions;
    private Long activeSubscriptions;
    private Long trialSubscriptions;
    private Long pendingSubscriptions;
    private Long expiredSubscriptions;
}
