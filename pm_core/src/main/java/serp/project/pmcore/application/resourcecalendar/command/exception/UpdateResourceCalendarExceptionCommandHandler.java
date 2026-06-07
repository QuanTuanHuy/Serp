/**
 * Author: QuanTuanHuy
 * Description: Part of Serp Project
 */

package serp.project.pmcore.application.resourcecalendar.command.exception;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import serp.project.pmcore.application.resourcecalendar.settings.ResourceCalendarSettingsOverviewView;
import serp.project.pmcore.application.shared.cqrs.command.ICommandHandler;
import serp.project.pmcore.domain.resourcecalendar.entity.ResourceCalendarExceptionEntity;
import serp.project.pmcore.domain.resourcecalendar.port.IResourceCalendarExceptionPort;
import serp.project.pmcore.domain.resourcecalendar.service.IResourceCalendarSettingsService;
import serp.project.pmcore.domain.shared.exception.DomainErrorCode;
import serp.project.pmcore.domain.shared.exception.ResourceNotFoundException;

@Service
@RequiredArgsConstructor
public class UpdateResourceCalendarExceptionCommandHandler
        implements ICommandHandler<UpdateResourceCalendarExceptionCommand, ResourceCalendarSettingsOverviewView.ExceptionView> {
    private final IResourceCalendarExceptionPort exceptionPort;
    private final IResourceCalendarSettingsService settingsService;
    private final ResourceCalendarExceptionMaterializationSupport materializationSupport;

    @Override
    @Transactional(rollbackFor = Exception.class)
    public ResourceCalendarSettingsOverviewView.ExceptionView handle(UpdateResourceCalendarExceptionCommand command) {
        ResourceCalendarExceptionEntity existing = exceptionPort.getExceptionById(command.tenantId(), command.exceptionId())
                .orElseThrow(() -> new ResourceNotFoundException(DomainErrorCode.NOT_FOUND,
                        "Resource calendar exception not found: id=" + command.exceptionId()));
        existing.setUserId(command.userId());
        existing.setExceptionType(command.exceptionType());
        existing.setStartAt(command.startAt());
        existing.setEndAt(command.endAt());
        existing.setCapacityFactor(command.capacityFactor());
        existing.setReason(command.reason());
        settingsService.validateException(existing);
        ResourceCalendarExceptionEntity saved = exceptionPort.saveException(existing);
        materializationSupport.rematerializeUser(command.tenantId(), saved.getUserId());
        return ResourceCalendarSettingsOverviewView.ExceptionView.from(saved);
    }
}
