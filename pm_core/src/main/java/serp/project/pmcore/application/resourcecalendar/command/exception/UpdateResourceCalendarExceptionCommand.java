/**
 * Author: QuanTuanHuy
 * Description: Part of Serp Project
 */

package serp.project.pmcore.application.resourcecalendar.command.exception;

import serp.project.pmcore.application.resourcecalendar.settings.ResourceCalendarSettingsOverviewView;
import serp.project.pmcore.application.shared.cqrs.command.ICommand;
import serp.project.pmcore.domain.resourcecalendar.enums.ResourceCalendarExceptionType;

import java.math.BigDecimal;
import java.time.LocalDateTime;

public record UpdateResourceCalendarExceptionCommand(
        Long tenantId,
        Long exceptionId,
        Long userId,
        ResourceCalendarExceptionType exceptionType,
        LocalDateTime startAt,
        LocalDateTime endAt,
        BigDecimal capacityFactor,
        String reason
) implements ICommand<ResourceCalendarSettingsOverviewView.ExceptionView> {
}
