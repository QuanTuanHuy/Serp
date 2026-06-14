/**
 * Author: QuanTuanHuy
 * Description: Part of Serp Project
 */

package serp.project.pmcore.application.resourcecalendar.command.profile;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import serp.project.pmcore.application.resourcecalendar.settings.ResourceCalendarSettingsOverviewView;
import serp.project.pmcore.application.shared.cqrs.command.ICommandHandler;
import serp.project.pmcore.domain.resourcecalendar.entity.ResourceCalendarProfileEntity;
import serp.project.pmcore.domain.resourcecalendar.port.IResourceCalendarProfilePort;

import java.util.List;

@Service
@RequiredArgsConstructor
public class CreateResourceCalendarProfileCommandHandler
        implements ICommandHandler<CreateResourceCalendarProfileCommand, ResourceCalendarSettingsOverviewView.ProfileView> {
    private final IResourceCalendarProfilePort profilePort;

    @Override
    @Transactional(rollbackFor = Exception.class)
    public ResourceCalendarSettingsOverviewView.ProfileView handle(CreateResourceCalendarProfileCommand command) {
        ResourceCalendarProfileEntity saved = profilePort.saveProfile(ResourceCalendarProfileEntity.builder()
                .tenantId(command.tenantId())
                .name(command.name())
                .description(command.description())
                .timezone(command.timezone())
                .isDefault(Boolean.TRUE.equals(command.isDefault()))
                .build());
        return ResourceCalendarSettingsOverviewView.ProfileView.from(saved, List.of(), 0);
    }
}
