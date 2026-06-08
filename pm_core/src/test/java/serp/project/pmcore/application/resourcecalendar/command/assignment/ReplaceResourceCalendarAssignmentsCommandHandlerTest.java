/**
 * Author: QuanTuanHuy
 * Description: Part of Serp Project
 */

package serp.project.pmcore.application.resourcecalendar.command.assignment;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import serp.project.pmcore.domain.resourcecalendar.entity.ResourceCalendarAssignmentEntity;
import serp.project.pmcore.domain.resourcecalendar.entity.ResourceCalendarProfileEntity;
import serp.project.pmcore.domain.resourcecalendar.port.IResourceCalendarAssignmentPort;
import serp.project.pmcore.domain.resourcecalendar.port.IResourceCalendarExceptionPort;
import serp.project.pmcore.domain.resourcecalendar.port.IResourceCalendarProfileBlockPort;
import serp.project.pmcore.domain.resourcecalendar.port.IResourceCalendarProfilePort;
import serp.project.pmcore.domain.resourcecalendar.port.IResourceCalendarSlotWritePort;
import serp.project.pmcore.domain.resourcecalendar.service.IResourceCalendarMaterializationService;
import serp.project.pmcore.domain.resourcecalendar.service.IResourceCalendarSettingsService;

import java.time.LocalDate;
import java.util.List;
import java.util.Optional;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class ReplaceResourceCalendarAssignmentsCommandHandlerTest {
    @Mock
    private IResourceCalendarAssignmentPort assignmentPort;
    @Mock
    private IResourceCalendarProfilePort profilePort;
    @Mock
    private IResourceCalendarProfileBlockPort blockPort;
    @Mock
    private IResourceCalendarExceptionPort exceptionPort;
    @Mock
    private IResourceCalendarMaterializationService materializationService;
    @Mock
    private IResourceCalendarSlotWritePort slotWritePort;
    @Mock
    private IResourceCalendarSettingsService settingsService;
    @InjectMocks
    private ReplaceResourceCalendarAssignmentsCommandHandler handler;

    @Test
    void handleShouldReplaceAssignmentsAndMaterializeAffectedUsers() {
        ReplaceResourceCalendarAssignmentsCommand command = new ReplaceResourceCalendarAssignmentsCommand(
                10L,
                1L,
                List.of(new ReplaceResourceCalendarAssignmentsCommand.Assignment(20L, LocalDate.of(2026, 6, 7), null))
        );
        when(profilePort.getProfileById(10L, 1L))
                .thenReturn(Optional.of(ResourceCalendarProfileEntity.builder()
                        .id(1L)
                        .tenantId(10L)
                        .timezone("Asia/Ho_Chi_Minh")
                        .build()));
        when(assignmentPort.replaceProfileAssignments(eq(10L), eq(1L), any()))
                .thenReturn(List.of(ResourceCalendarAssignmentEntity.builder()
                        .tenantId(10L)
                        .userId(20L)
                        .profileId(1L)
                        .effectiveFrom(LocalDate.of(2026, 6, 7))
                        .build()));
        when(blockPort.listByProfileId(1L)).thenReturn(List.of());
        when(exceptionPort.listExceptions(eq(10L), eq(List.of(20L)), any(), any())).thenReturn(List.of());
        when(materializationService.materialize(any())).thenReturn(List.of());

        handler.handle(command);

        verify(slotWritePort).replaceGeneratedSlots(eq(10L), eq(List.of(20L)), any(), any(), eq(List.of()));
    }
}
