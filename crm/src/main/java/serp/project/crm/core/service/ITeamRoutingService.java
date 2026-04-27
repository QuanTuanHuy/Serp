/**
 * Author: QuanTuanHuy
 * Description: Part of Serp Project
 */

package serp.project.crm.core.service;

import java.util.Optional;

public interface ITeamRoutingService {
    Optional<Long> routeLeadAssignee(String territoryCode, Long tenantId);
}
