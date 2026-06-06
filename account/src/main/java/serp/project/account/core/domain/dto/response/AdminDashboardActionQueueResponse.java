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
public class AdminDashboardActionQueueResponse {
    private Long pendingSubscriptions;
    private Long subscriptionsEndingSoon;
    private Long trialsEndingSoon;
    private Long suspendedOrganizations;
    private Long expiredOrganizations;
}
