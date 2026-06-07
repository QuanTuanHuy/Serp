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

@Service
@RequiredArgsConstructor
public class CreateResourceCalendarExceptionCommandHandler
        implements ICommandHandler<CreateResourceCalendarExceptionCommand, ResourceCalendarSettingsOverviewView.ExceptionView> {
    private final IResourceCalendarExceptionPort exceptionPort;
    private final IResourceCalendarSettingsService settingsService;
    private final ResourceCalendarExceptionMaterializationSupport materializationSupport;

    @Override
    @Transactional(rollbackFor = Exception.class)
    public ResourceCalendarSettingsOverviewView.ExceptionView handle(CreateResourceCalendarExceptionCommand command) {
        ResourceCalendarExceptionEntity exception = ResourceCalendarExceptionEntity.builder()
                .tenantId(command.tenantId())
                .userId(command.userId())
                .exceptionType(command.exceptionType())
                .startAt(command.startAt())
                .endAt(command.endAt())
                .capacityFactor(command.capacityFactor())
                .reason(command.reason())
                .build();
        settingsService.validateException(exception);
        ResourceCalendarExceptionEntity saved = exceptionPort.saveException(exception);
        materializationSupport.rematerializeUser(command.tenantId(), saved.getUserId());
        return ResourceCalendarSettingsOverviewView.ExceptionView.from(saved);
    }
}
