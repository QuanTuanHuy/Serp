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
public class AdminDashboardRecentOrganizationResponse {
    private Long id;
    private String name;
    private String code;
    private String status;
    private Long userCount;
    private String subscriptionStatus;
    private Long createdAt;
}
