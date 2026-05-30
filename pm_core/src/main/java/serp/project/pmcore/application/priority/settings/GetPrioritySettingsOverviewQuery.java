/**
 * Author: QuanTuanHuy
 * Description: Part of Serp Project
 */

package serp.project.pmcore.application.priority.settings;

import serp.project.pmcore.application.shared.cqrs.query.IQuery;

public record GetPrioritySettingsOverviewQuery(
        Long tenantId
) implements IQuery<PrioritySettingsOverviewView> {
}
