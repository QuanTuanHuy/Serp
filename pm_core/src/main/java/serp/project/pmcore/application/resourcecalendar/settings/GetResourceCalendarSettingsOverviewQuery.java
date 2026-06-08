/**
 * Author: QuanTuanHuy
 * Description: Part of Serp Project
 */

package serp.project.pmcore.application.resourcecalendar.settings;

import serp.project.pmcore.application.shared.cqrs.query.IQuery;

public record GetResourceCalendarSettingsOverviewQuery(Long tenantId)
        implements IQuery<ResourceCalendarSettingsOverviewView> {
}
