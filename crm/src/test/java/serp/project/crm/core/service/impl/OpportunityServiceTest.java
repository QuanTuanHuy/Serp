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
import serp.project.crm.core.domain.entity.OpportunityEntity;
import serp.project.crm.core.domain.entity.TeamMemberEntity;
import serp.project.crm.core.domain.enums.OpportunityStage;
import serp.project.crm.core.domain.enums.TeamMemberStatus;
import serp.project.crm.core.port.store.IOpportunityPort;
import serp.project.crm.core.port.store.ITeamMemberPort;
import serp.project.crm.core.service.INotificationPublisher;

import java.util.Optional;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyLong;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class OpportunityServiceTest {

    @Mock
    private IOpportunityPort opportunityPort;

    @Mock
    private ITeamMemberPort teamMemberPort;

    @Mock
    private INotificationPublisher notificationPublisher;

    @InjectMocks
    private OpportunityService opportunityService;

    @Test
    void updateOpportunityWhenAssignedToChangesShouldPublishOpportunityAssigned() {
        Long tenantId = 20L;
        OpportunityEntity existing = OpportunityEntity.builder()
                .id(1L)
                .tenantId(tenantId)
                .accountId(5L)
                .name("O1")
                .stage(OpportunityStage.PROSPECTING)
                .assignedTo(10L)
                .build();
        OpportunityEntity updates = OpportunityEntity.builder().assignedTo(30L).updatedBy(99L).build();

        when(opportunityPort.findById(1L, tenantId)).thenReturn(Optional.of(existing));
        when(teamMemberPort.findByUserId(30L, tenantId)).thenReturn(Optional.of(TeamMemberEntity.builder()
                .status(TeamMemberStatus.ACTIVE)
                .build()));
        when(opportunityPort.save(existing)).thenAnswer(invocation -> invocation.getArgument(0));

        opportunityService.updateOpportunity(1L, updates, tenantId);

        verify(notificationPublisher).publishOpportunityAssigned(existing, tenantId, 10L);
    }

    @Test
    void updateOpportunityWhenOnlyNameChangesShouldNotPublishOpportunityAssigned() {
        Long tenantId = 20L;
        OpportunityEntity existing = OpportunityEntity.builder()
                .id(1L)
                .tenantId(tenantId)
                .accountId(5L)
                .name("O1")
                .stage(OpportunityStage.PROSPECTING)
                .assignedTo(10L)
                .build();
        OpportunityEntity updates = OpportunityEntity.builder().name("Renamed").updatedBy(1L).build();

        when(opportunityPort.findById(1L, tenantId)).thenReturn(Optional.of(existing));
        when(opportunityPort.save(existing)).thenAnswer(invocation -> invocation.getArgument(0));

        opportunityService.updateOpportunity(1L, updates, tenantId);

        verify(notificationPublisher, never()).publishOpportunityAssigned(any(), anyLong(), any());
    }

    @Test
    void assignOpportunityWhenAssigneeChangesShouldPublishOpportunityAssigned() {
        Long tenantId = 20L;
        OpportunityEntity opportunity = OpportunityEntity.builder()
                .id(1L)
                .tenantId(tenantId)
                .accountId(5L)
                .name("O1")
                .stage(OpportunityStage.PROSPECTING)
                .assignedTo(10L)
                .build();

        when(opportunityPort.findById(1L, tenantId)).thenReturn(Optional.of(opportunity));
        when(teamMemberPort.findByUserId(40L, tenantId)).thenReturn(Optional.of(TeamMemberEntity.builder()
                .status(TeamMemberStatus.ACTIVE)
                .build()));
        when(opportunityPort.save(opportunity)).thenAnswer(invocation -> invocation.getArgument(0));

        opportunityService.assignOpportunity(1L, 40L, 7L, tenantId);

        verify(notificationPublisher).publishOpportunityAssigned(opportunity, tenantId, 10L);
    }

    @Test
    void assignOpportunityWhenAssigneeUnchangedShouldNotPublishOpportunityAssigned() {
        Long tenantId = 20L;
        OpportunityEntity opportunity = OpportunityEntity.builder()
                .id(1L)
                .tenantId(tenantId)
                .accountId(5L)
                .name("O1")
                .stage(OpportunityStage.PROSPECTING)
                .assignedTo(10L)
                .build();

        when(opportunityPort.findById(1L, tenantId)).thenReturn(Optional.of(opportunity));
        when(teamMemberPort.findByUserId(10L, tenantId)).thenReturn(Optional.of(TeamMemberEntity.builder()
                .status(TeamMemberStatus.ACTIVE)
                .build()));
        when(opportunityPort.save(opportunity)).thenAnswer(invocation -> invocation.getArgument(0));

        opportunityService.assignOpportunity(1L, 10L, 7L, tenantId);

        verify(notificationPublisher, never()).publishOpportunityAssigned(any(), anyLong(), any());
    }
}
