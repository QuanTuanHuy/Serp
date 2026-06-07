/**
 * Author: QuanTuanHuy
 * Description: Part of Serp Project
 */

package serp.project.pmcore.application.resourcecalendar.command.profile;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import serp.project.pmcore.application.resourcecalendar.ResourceCalendarDeleteResult;
import serp.project.pmcore.application.shared.cqrs.command.ICommandHandler;
import serp.project.pmcore.domain.resourcecalendar.port.IResourceCalendarAssignmentPort;
import serp.project.pmcore.domain.resourcecalendar.port.IResourceCalendarProfilePort;
import serp.project.pmcore.domain.shared.exception.BusinessRuleViolationException;
import serp.project.pmcore.domain.shared.exception.DomainErrorCode;

@Service
@RequiredArgsConstructor
public class DeleteResourceCalendarProfileCommandHandler
        implements ICommandHandler<DeleteResourceCalendarProfileCommand, ResourceCalendarDeleteResult> {
    private final IResourceCalendarProfilePort profilePort;
    private final IResourceCalendarAssignmentPort assignmentPort;

    @Override
    @Transactional(rollbackFor = Exception.class)
    public ResourceCalendarDeleteResult handle(DeleteResourceCalendarProfileCommand command) {
        if (!assignmentPort.listByProfileId(command.tenantId(), command.profileId()).isEmpty()) {
            throw new BusinessRuleViolationException(DomainErrorCode.CONFLICT,
                    "Cannot delete resource calendar profile while users are assigned");
        }
        profilePort.deleteProfile(command.tenantId(), command.profileId());
        return new ResourceCalendarDeleteResult(true);
    }
}
