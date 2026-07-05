/**
 * Author: QuanTuanHuy
 * Description: Part of Serp Project
 */

package serp.project.crm.core.service.impl;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import serp.project.crm.core.domain.entity.ActivityEntity;
import serp.project.crm.core.domain.entity.RepTimeBlockEntity;
import serp.project.crm.core.domain.entity.TeamMemberEntity;
import serp.project.crm.core.domain.enums.ActivityStatus;
import serp.project.crm.core.domain.enums.ActivityType;
import serp.project.crm.core.domain.enums.RepTimeBlockType;
import serp.project.crm.core.domain.enums.TeamMemberStatus;
import serp.project.crm.core.port.store.IRepTimeBlockPort;
import serp.project.crm.core.port.store.ITeamMemberPort;

import java.time.Duration;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class RepTimeBlockServiceTest {

    @Mock
    private IRepTimeBlockPort repTimeBlockPort;

    @Mock
    private ITeamMemberPort teamMemberPort;

    @InjectMocks
    private RepTimeBlockService repTimeBlockService;

    @Test
    void syncFromActivityShouldUpdateExistingBlockInsteadOfDeletingAndRecreating() {
        ActivityEntity activity = ActivityEntity.builder()
                .id(12L)
                .tenantId(1L)
                .activityType(ActivityType.MEETING)
                .status(ActivityStatus.PLANNED)
                .assignedTo(99L)
                .activityDate(1_800_000L)
                .durationMinutes(45)
                .createdBy(5L)
                .updatedBy(7L)
                .build();
        TeamMemberEntity teamMember = TeamMemberEntity.builder()
                .id(21L)
                .userId(99L)
                .status(TeamMemberStatus.ACTIVE)
                .build();
        RepTimeBlockEntity existingBlock = RepTimeBlockEntity.builder()
                .id(40L)
                .tenantId(1L)
                .teamMemberId(20L)
                .activityId(12L)
                .startTime(1_000_000L)
                .endTime(1_060_000L)
                .blockType(RepTimeBlockType.MEETING)
                .version(3L)
                .createdBy(5L)
                .updatedBy(5L)
                .build();

        when(teamMemberPort.findByUserId(99L, 1L)).thenReturn(Optional.of(teamMember));
        when(repTimeBlockPort.findByActivityId(12L, 1L)).thenReturn(Optional.of(existingBlock));
        when(repTimeBlockPort.save(any(RepTimeBlockEntity.class))).thenAnswer(invocation -> invocation.getArgument(0));

        repTimeBlockService.syncFromActivity(activity, 1L);

        ArgumentCaptor<RepTimeBlockEntity> blockCaptor = ArgumentCaptor.forClass(RepTimeBlockEntity.class);
        verify(repTimeBlockPort, never()).deleteByActivityId(12L, 1L);
        verify(repTimeBlockPort).save(blockCaptor.capture());

        RepTimeBlockEntity savedBlock = blockCaptor.getValue();
        assertEquals(40L, savedBlock.getId());
        assertEquals(1L, savedBlock.getTenantId());
        assertEquals(21L, savedBlock.getTeamMemberId());
        assertEquals(12L, savedBlock.getActivityId());
        assertEquals(1_800_000L, savedBlock.getStartTime());
        assertEquals(1_800_000L + Duration.ofMinutes(45).toMillis(), savedBlock.getEndTime());
        assertEquals(RepTimeBlockType.MEETING, savedBlock.getBlockType());
        assertEquals(3L, savedBlock.getVersion());
        assertEquals(5L, savedBlock.getCreatedBy());
        assertEquals(7L, savedBlock.getUpdatedBy());
    }
}
