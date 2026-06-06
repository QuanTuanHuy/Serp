/**
 * Author: QuanTuanHuy
 * Description: Part of Serp Project
 */

package serp.project.pmcore.application.project.query.settings;

import java.util.Set;

public record GetProjectSettingsOverviewQuery(
        Long projectId,
        Long tenantId,
        Long userId,
        Set<String> groupKeys
) {
}
