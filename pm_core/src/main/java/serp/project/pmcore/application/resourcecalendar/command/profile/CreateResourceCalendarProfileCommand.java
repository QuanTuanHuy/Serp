/**
 * Author: QuanTuanHuy
 * Description: Part of Serp Project
 */

package serp.project.pmcore.application.resourcecalendar.command.profile;

import serp.project.pmcore.application.resourcecalendar.settings.ResourceCalendarSettingsOverviewView;
import serp.project.pmcore.application.shared.cqrs.command.ICommand;

public record CreateResourceCalendarProfileCommand(
        Long tenantId,
        Long userId,
        String name,
        String description,
        String timezone,
        Boolean isDefault
) implements ICommand<ResourceCalendarSettingsOverviewView.ProfileView> {
}
