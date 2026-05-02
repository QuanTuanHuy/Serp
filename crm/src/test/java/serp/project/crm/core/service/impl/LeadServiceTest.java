/**
 * Author: QuanTuanHuy
 * Description: Part of Serp Project
 */

package serp.project.crm.core.service.impl;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import serp.project.crm.core.domain.constant.Constants;
import serp.project.crm.core.domain.entity.LeadEntity;
import serp.project.crm.core.domain.enums.LeadStatus;
import serp.project.crm.core.exception.AppException;
import serp.project.crm.core.port.store.ILeadPort;
import serp.project.crm.core.service.ILeadScoringService;
import serp.project.crm.core.service.INotificationPublisher;
import serp.project.crm.core.service.ITeamMemberService;
import serp.project.crm.core.service.ITeamRoutingService;

import java.util.Optional;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyLong;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class LeadServiceTest {

    @Mock
    private ILeadPort leadPort;

    @Mock
    private ILeadScoringService leadScoringService;

    @Mock
    private ITeamRoutingService teamRoutingService;

    @Mock
    private ITeamMemberService teamMemberService;

    @Mock
    private INotificationPublisher notificationPublisher;

    @InjectMocks
    private LeadService leadService;

    @Test
    void updateLeadStatusShouldPersistValidWorkingStageTransition() {
        Long leadId = 10L;
        Long tenantId = 99L;
        Long userId = 7L;
        LeadEntity lead = LeadEntity.builder()
                .id(leadId)
                .tenantId(tenantId)
                .leadStatus(LeadStatus.NEW)
                .build();

        when(leadPort.findById(leadId, tenantId)).thenReturn(Optional.of(lead));
        when(leadPort.save(lead)).thenReturn(lead);

        LeadEntity updated = leadService.updateLeadStatus(
                leadId,
                LeadStatus.NEW,
                LeadStatus.CONTACTED,
                "Reached customer by phone",
                userId,
                tenantId);

        assertEquals(LeadStatus.CONTACTED, updated.getLeadStatus());
        assertEquals("Reached customer by phone", updated.getNotes());
        assertEquals(userId, updated.getUpdatedBy());
        verify(leadPort).save(lead);
    }

    @Test
    void updateLeadStatusShouldRejectStaleFromStatus() {
        Long leadId = 10L;
        Long tenantId = 99L;
        LeadEntity lead = LeadEntity.builder()
                .id(leadId)
                .tenantId(tenantId)
                .leadStatus(LeadStatus.CONTACTED)
                .build();

        when(leadPort.findById(leadId, tenantId)).thenReturn(Optional.of(lead));

        AppException exception = assertThrows(AppException.class,
                () -> leadService.updateLeadStatus(
                        leadId,
                        LeadStatus.NEW,
                        LeadStatus.NURTURING,
                        null,
                        7L,
                        tenantId));

        assertEquals(Constants.HttpStatusCode.CONFLICT, exception.getCode());
        assertEquals("Lead status changed by another action. Please refresh and try again.", exception.getMessage());
        verify(leadPort, never()).save(lead);
    }

    @Test
    void updateLeadStatusShouldRejectQualifiedTarget() {
        Long leadId = 10L;
        Long tenantId = 99L;
        LeadEntity lead = LeadEntity.builder()
                .id(leadId)
                .tenantId(tenantId)
                .leadStatus(LeadStatus.CONTACTED)
                .build();

        when(leadPort.findById(leadId, tenantId)).thenReturn(Optional.of(lead));

        AppException exception = assertThrows(AppException.class,
                () -> leadService.updateLeadStatus(
                        leadId,
                        LeadStatus.CONTACTED,
                        LeadStatus.QUALIFIED,
                        null,
                        7L,
                        tenantId));

        assertEquals("Use qualify endpoint to move lead to QUALIFIED", exception.getMessage());
        verify(leadPort, never()).save(lead);
    }

    @Test
    void updateLeadWhenAssignedToChangesShouldPublishLeadAssigned() {
        Long tenantId = 10L;
        LeadEntity existing = LeadEntity.builder()
                .id(1L)
                .tenantId(tenantId)
                .email("a@b.com")
                .assignedTo(100L)
                .leadStatus(LeadStatus.NEW)
                .build();
        LeadEntity updates = LeadEntity.builder().assignedTo(200L).updatedBy(50L).build();

        when(leadPort.findById(1L, tenantId)).thenReturn(Optional.of(existing));
        when(leadPort.save(existing)).thenAnswer(invocation -> invocation.getArgument(0));

        leadService.updateLead(1L, updates, tenantId);

        verify(notificationPublisher).publishLeadAssigned(existing, tenantId, 100L);
        assertEquals(200L, existing.getAssignedTo());
    }

    @Test
    void updateLeadWhenOnlyOtherFieldsChangeShouldNotPublishLeadAssigned() {
        Long tenantId = 10L;
        LeadEntity existing = LeadEntity.builder()
                .id(1L)
                .tenantId(tenantId)
                .email("a@b.com")
                .assignedTo(100L)
                .leadStatus(LeadStatus.NEW)
                .build();
        LeadEntity updates = LeadEntity.builder().name("Only name").updatedBy(50L).build();

        when(leadPort.findById(1L, tenantId)).thenReturn(Optional.of(existing));
        when(leadPort.save(existing)).thenAnswer(invocation -> invocation.getArgument(0));

        leadService.updateLead(1L, updates, tenantId);

        verify(notificationPublisher, never()).publishLeadAssigned(any(), anyLong(), any());
    }

    @Test
    void assignLeadShouldPublishLeadAssignedWhenAssigneeChanges() {
        Long tenantId = 10L;
        LeadEntity lead = LeadEntity.builder()
                .id(1L)
                .tenantId(tenantId)
                .email("a@b.com")
                .assignedTo(100L)
                .leadStatus(LeadStatus.NEW)
                .build();

        when(leadPort.findById(1L, tenantId)).thenReturn(Optional.of(lead));
        when(teamMemberService.getTeamMemberByUserId(200L, tenantId)).thenReturn(Optional.of(
                serp.project.crm.core.domain.entity.TeamMemberEntity.builder()
                        .status(serp.project.crm.core.domain.enums.TeamMemberStatus.ACTIVE)
                        .build()));
        when(leadPort.save(lead)).thenAnswer(invocation -> invocation.getArgument(0));

        leadService.assignLead(1L, 200L, 50L, tenantId);

        verify(notificationPublisher).publishLeadAssigned(lead, tenantId, 100L);
    }

    @Test
    void assignLeadWhenAssigneeUnchangedShouldNotPublishLeadAssigned() {
        Long tenantId = 10L;
        LeadEntity lead = LeadEntity.builder()
                .id(1L)
                .tenantId(tenantId)
                .email("a@b.com")
                .assignedTo(100L)
                .leadStatus(LeadStatus.NEW)
                .build();

        when(leadPort.findById(1L, tenantId)).thenReturn(Optional.of(lead));
        when(teamMemberService.getTeamMemberByUserId(100L, tenantId)).thenReturn(Optional.of(
                serp.project.crm.core.domain.entity.TeamMemberEntity.builder()
                        .status(serp.project.crm.core.domain.enums.TeamMemberStatus.ACTIVE)
                        .build()));
        when(leadPort.save(lead)).thenAnswer(invocation -> invocation.getArgument(0));

        leadService.assignLead(1L, 100L, 50L, tenantId);

        verify(notificationPublisher, never()).publishLeadAssigned(any(), anyLong(), any());
    }

    @Test
    void createLeadWithAssigneeShouldPublishLeadAssignedWithNullPrevious() {
        Long tenantId = 10L;
        LeadEntity lead = LeadEntity.builder()
                .email("x@y.com")
                .assignedTo(77L)
                .leadStatus(LeadStatus.NEW)
                .build();

        when(leadPort.existsByEmail("x@y.com", tenantId)).thenReturn(false);
        when(leadPort.save(any(LeadEntity.class))).thenAnswer(invocation -> {
            LeadEntity saved = invocation.getArgument(0);
            saved.setId(99L);
            return saved;
        });

        LeadEntity saved = leadService.createLead(lead, tenantId);

        assertEquals(99L, saved.getId());
        verify(notificationPublisher).publishLeadAssigned(saved, tenantId, null);
    }

    @Test
    void createLeadWithoutAssigneeShouldNotPublishLeadAssigned() {
        Long tenantId = 10L;
        LeadEntity lead = LeadEntity.builder()
                .email("x@y.com")
                .leadStatus(LeadStatus.NEW)
                .build();

        when(leadPort.existsByEmail("x@y.com", tenantId)).thenReturn(false);
        when(leadPort.save(any(LeadEntity.class))).thenAnswer(invocation -> {
            LeadEntity saved = invocation.getArgument(0);
            saved.setId(99L);
            return saved;
        });

        leadService.createLead(lead, tenantId);

        verify(notificationPublisher, never()).publishLeadAssigned(any(), anyLong(), any());
    }
}
