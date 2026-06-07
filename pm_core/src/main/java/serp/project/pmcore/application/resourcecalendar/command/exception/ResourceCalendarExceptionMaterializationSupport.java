/**
 * Author: QuanTuanHuy
 * Description: Part of Serp Project
 */

package serp.project.pmcore.application.resourcecalendar.command.exception;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
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

import java.time.LocalDate;
import java.time.ZoneOffset;
import java.util.List;
import java.util.Objects;

@Service
@RequiredArgsConstructor
class ResourceCalendarExceptionMaterializationSupport {
    private final IResourceCalendarAssignmentPort assignmentPort;
    private final IResourceCalendarProfilePort profilePort;
    private final IResourceCalendarProfileBlockPort blockPort;
    private final IResourceCalendarExceptionPort exceptionPort;
    private final IResourceCalendarMaterializationService materializationService;
    private final IResourceCalendarSlotWritePort slotWritePort;

    void rematerializeUser(Long tenantId, Long userId) {
        List<ResourceCalendarAssignmentEntity> assignments = assignmentPort.listActiveAssignments(tenantId).stream()
                .filter(assignment -> Objects.equals(assignment.getUserId(), userId))
                .toList();
        for (ResourceCalendarAssignmentEntity assignment : assignments) {
            profilePort.getProfileById(tenantId, assignment.getProfileId())
                    .ifPresent(profile -> rematerializeProfile(tenantId, profile, List.of(userId)));
        }
    }

    private void rematerializeProfile(Long tenantId, ResourceCalendarProfileEntity profile, List<Long> userIds) {
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
