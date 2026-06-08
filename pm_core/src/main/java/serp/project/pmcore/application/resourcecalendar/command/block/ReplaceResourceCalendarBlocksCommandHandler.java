/**
 * Author: QuanTuanHuy
 * Description: Part of Serp Project
 */

package serp.project.pmcore.application.resourcecalendar.command.block;

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
public class ReplaceResourceCalendarBlocksCommandHandler
        implements ICommandHandler<ReplaceResourceCalendarBlocksCommand, List<ResourceCalendarSettingsOverviewView.BlockView>> {
    private final IResourceCalendarProfilePort profilePort;
    private final IResourceCalendarProfileBlockPort blockPort;
    private final IResourceCalendarAssignmentPort assignmentPort;
    private final IResourceCalendarExceptionPort exceptionPort;
    private final IResourceCalendarMaterializationService materializationService;
    private final IResourceCalendarSlotWritePort slotWritePort;
    private final IResourceCalendarSettingsService settingsService;

    @Override
    @Transactional(rollbackFor = Exception.class)
    public List<ResourceCalendarSettingsOverviewView.BlockView> handle(ReplaceResourceCalendarBlocksCommand command) {
        ResourceCalendarProfileEntity profile = profilePort.getProfileById(command.tenantId(), command.profileId())
                .orElseThrow(() -> new ResourceNotFoundException(DomainErrorCode.NOT_FOUND,
                        "Resource calendar profile not found: id=" + command.profileId()));
        List<ReplaceResourceCalendarBlocksCommand.Block> blockItems = command.blocks() == null ? List.of() : command.blocks();
        List<ResourceCalendarProfileBlockEntity> blocks = blockItems.stream()
                .map(item -> {
                    ResourceCalendarProfileBlockEntity block = ResourceCalendarProfileBlockEntity.builder()
                            .profileId(command.profileId())
                            .dayOfWeek(item.dayOfWeek())
                            .startTime(item.startTime())
                            .endTime(item.endTime())
                            .capacityFactor(item.capacityFactor())
                            .build();
                    return block;
                })
                .toList();
        settingsService.validateBlocks(blocks);
        List<ResourceCalendarProfileBlockEntity> saved = blockPort.replaceBlocks(command.profileId(), blocks);
        List<Long> userIds = assignmentPort.listByProfileId(command.tenantId(), command.profileId()).stream()
                .map(ResourceCalendarAssignmentEntity::getUserId)
                .distinct()
                .toList();
        rematerialize(command.tenantId(), profile, userIds);
        return saved.stream().map(ResourceCalendarSettingsOverviewView.BlockView::from).toList();
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
