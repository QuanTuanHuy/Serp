/**
 * Author: QuanTuanHuy
 * Description: Part of Serp Project
 */

package serp.project.pmcore.application.resourcecalendar.command.profile;

import serp.project.pmcore.application.resourcecalendar.ResourceCalendarDeleteResult;
import serp.project.pmcore.application.shared.cqrs.command.ICommand;

public record DeleteResourceCalendarProfileCommand(
        Long tenantId,
        Long profileId
) implements ICommand<ResourceCalendarDeleteResult> {
}
