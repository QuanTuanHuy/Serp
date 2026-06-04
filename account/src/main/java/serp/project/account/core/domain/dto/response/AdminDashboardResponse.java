/**
 * Author: QuanTuanHuy
 * Description: Part of Serp Project
 */

package serp.project.account.core.domain.dto.response;

import java.util.List;

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
public class AdminDashboardResponse {
    private AdminDashboardMetricsResponse metrics;
    private AdminDashboardActionQueueResponse actionQueue;
    private List<AdminDashboardRecentOrganizationResponse> recentOrganizations;
    private List<AdminDashboardStatusCountResponse> organizationStatuses;
    private List<AdminDashboardStatusCountResponse> subscriptionStatuses;
    private AdminDashboardConfigurationCoverageResponse configurationCoverage;
    private Long generatedAt;
}
