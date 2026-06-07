/**
 * Author: QuanTuanHuy
 * Description: Part of Serp Project
 */

package serp.project.pmcore.application.resourcecalendar.command.assignment;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import serp.project.pmcore.application.resourcecalendar.settings.ResourceCalendarSettingsOverviewView;
import serp.project.pmcore.application.shared.cqrs.command.ICommandHandler;
import serp.project.pmcore.domain.resourcecalendar.entity.ResourceCalendarAssignmentEntity;
import serp.project.pmcore.domain.resourcecalendar.entity.ResourceCalendarExceptionEntity;
import serp.project.pmcore.domain.resourcecalendar.entity.ResourceCalendarProfileBlockEntity;
import serp.project.pmcore.domain.resourcecalendar.entity.ResourceCalendarProfileEntity;
import serp.project.pmcore.domain.resourcecalendar.model.GeneratedResourceCalendarSlot;
import serp.project.pmcore.domain.resourcecalendar.model.ResourceCalendarMaterializationInput;
import serp.project.pmcore.domain.resourcecalendar.port.IResourceCalendarAssignmentPort;
import serp.project.pmcore.domain.resourcecalendar.port.IResourceCalendarExceptionPort;
import serp.project.pmcore.domain.resourcecalendar.port.IResourceCalendarProfileBlockPort;
import serp.project.pmcore.domain.resourcecalendar.port.IResourceCalendarProfilePort;
import serp.project.pmcore.domain.resourcecalendar.port.IResourceCalendarSlotWritePort;
import serp.project.pmcore.domain.resourcecalendar.service.IResourceCalendarMaterializationService;
import serp.project.pmcore.domain.resourcecalendar.service.IResourceCalendarSettingsService;
import serp.project.pmcore.domain.shared.exception.DomainErrorCode;
import serp.project.pmcore.domain.shared.exception.ResourceNotFoundException;

import java.time.LocalDate;
import java.time.ZoneOffset;
import java.util.List;

@Service
@RequiredArgsConstructor
public class ReplaceResourceCalendarAssignmentsCommandHandler
        implements ICommandHandler<ReplaceResourceCalendarAssignmentsCommand, List<ResourceCalendarSettingsOverviewView.AssignmentView>> {
    private final IResourceCalendarAssignmentPort assignmentPort;
    private final IResourceCalendarProfilePort profilePort;
    private final IResourceCalendarProfileBlockPort blockPort;
    private final IResourceCalendarExceptionPort exceptionPort;
    private final IResourceCalendarMaterializationService materializationService;
    private final IResourceCalendarSlotWritePort slotWritePort;
    private final IResourceCalendarSettingsService settingsService;

    @Override
    @Transactional(rollbackFor = Exception.class)
    public List<ResourceCalendarSettingsOverviewView.AssignmentView> handle(ReplaceResourceCalendarAssignmentsCommand command) {
        ResourceCalendarProfileEntity profile = requireProfile(command.tenantId(), command.profileId());
        List<ReplaceResourceCalendarAssignmentsCommand.Assignment> assignmentItems =
                command.assignments() == null ? List.of() : command.assignments();
        List<ResourceCalendarAssignmentEntity> assignments = assignmentItems.stream()
                .map(item -> {
                    ResourceCalendarAssignmentEntity assignment = ResourceCalendarAssignmentEntity.builder()
                            .tenantId(command.tenantId())
                            .userId(item.userId())
                            .profileId(command.profileId())
                            .effectiveFrom(item.effectiveFrom())
                            .effectiveTo(item.effectiveTo())
                            .build();
                    return assignment;
                })
                .toList();
        settingsService.validateAssignments(assignments);
        List<ResourceCalendarAssignmentEntity> saved = assignmentPort.replaceProfileAssignments(
                command.tenantId(),
                command.profileId(),
                assignments
        );
        rematerialize(command.tenantId(), profile, saved.stream().map(ResourceCalendarAssignmentEntity::getUserId).distinct().toList());
        return saved.stream().map(ResourceCalendarSettingsOverviewView.AssignmentView::from).toList();
    }

    private ResourceCalendarProfileEntity requireProfile(Long tenantId, Long profileId) {
        return profilePort.getProfileById(tenantId, profileId)
                .orElseThrow(() -> new ResourceNotFoundException(DomainErrorCode.NOT_FOUND,
                        "Resource calendar profile not found: id=" + profileId));
    }

    private void rematerialize(Long tenantId, ResourceCalendarProfileEntity profile, List<Long> userIds) {
        if (userIds == null || userIds.isEmpty()) {
            return;
        }
        LocalDate windowStart = LocalDate.now(ZoneOffset.UTC);
        LocalDate windowEnd = windowStart.plusDays(90);
        Long windowStartMillis = windowStart.atStartOfDay(ZoneOffset.UTC).toInstant().toEpochMilli();
        Long windowEndMillis = windowEnd.plusDays(1).atStartOfDay(ZoneOffset.UTC).toInstant().toEpochMilli();
        List<ResourceCalendarProfileBlockEntity> blocks = blockPort.listByProfileId(profile.getId());
        List<ResourceCalendarExceptionEntity> exceptions = exceptionPort.listExceptions(tenantId, userIds, windowStartMillis, windowEndMillis);
        List<GeneratedResourceCalendarSlot> slots = materializationService.materialize(new ResourceCalendarMaterializationInput(
                tenantId,
                userIds,
                profile.getTimezone(),
                windowStart,
                windowEnd,
                blocks,
                exceptions
        ));
        slotWritePort.replaceGeneratedSlots(tenantId, userIds, windowStartMillis, windowEndMillis, slots);
    }
}
