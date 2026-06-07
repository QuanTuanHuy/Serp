/**
 * Author: QuanTuanHuy
 * Description: Part of Serp Project
 */

package serp.project.pmcore.application.resourcecalendar.command.exception;

import serp.project.pmcore.application.resourcecalendar.ResourceCalendarDeleteResult;
import serp.project.pmcore.application.shared.cqrs.command.ICommand;

public record DeleteResourceCalendarExceptionCommand(
        Long tenantId,
        Long exceptionId
) implements ICommand<ResourceCalendarDeleteResult> {
}
