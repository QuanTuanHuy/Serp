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
import serp.project.pmcore.domain.resourcecalendar.entity.ResourceCalendarProfileBlockEntity;
import serp.project.pmcore.domain.resourcecalendar.entity.ResourceCalendarProfileEntity;
import serp.project.pmcore.domain.resourcecalendar.port.IResourceCalendarAssignmentPort;
import serp.project.pmcore.domain.resourcecalendar.port.IResourceCalendarProfileBlockPort;
import serp.project.pmcore.domain.resourcecalendar.port.IResourceCalendarProfilePort;
import serp.project.pmcore.domain.shared.exception.DomainErrorCode;
import serp.project.pmcore.domain.shared.exception.ResourceNotFoundException;

import java.util.List;

@Service
@RequiredArgsConstructor
public class UpdateResourceCalendarProfileCommandHandler
        implements ICommandHandler<UpdateResourceCalendarProfileCommand, ResourceCalendarSettingsOverviewView.ProfileView> {
    private final IResourceCalendarProfilePort profilePort;
    private final IResourceCalendarProfileBlockPort blockPort;
    private final IResourceCalendarAssignmentPort assignmentPort;

    @Override
    @Transactional(rollbackFor = Exception.class)
    public ResourceCalendarSettingsOverviewView.ProfileView handle(UpdateResourceCalendarProfileCommand command) {
        ResourceCalendarProfileEntity existing = profilePort.getProfileById(command.tenantId(), command.profileId())
                .orElseThrow(() -> new ResourceNotFoundException(DomainErrorCode.NOT_FOUND,
                        "Resource calendar profile not found: id=" + command.profileId()));
        existing.setName(command.name());
        existing.setDescription(command.description());
        existing.setTimezone(command.timezone());
        existing.setIsDefault(Boolean.TRUE.equals(command.isDefault()));
        ResourceCalendarProfileEntity saved = profilePort.saveProfile(existing);
        List<ResourceCalendarProfileBlockEntity> blocks = blockPort.listByProfileId(saved.getId());
        int assignmentCount = assignmentPort.listByProfileId(command.tenantId(), saved.getId()).size();
        return ResourceCalendarSettingsOverviewView.ProfileView.from(saved, blocks, assignmentCount);
    }
}
