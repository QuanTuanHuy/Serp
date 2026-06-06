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
public class AdminDashboardConfigurationCoverageResponse {
    private Long totalPlans;
    private Long activePlans;
    private Long inactivePlans;
    private Long totalModules;
    private Long availableModules;
    private Long unavailableModules;
    private Long totalMenuDisplays;
    private Long visibleMenuDisplays;
    private Long hiddenMenuDisplays;
}
