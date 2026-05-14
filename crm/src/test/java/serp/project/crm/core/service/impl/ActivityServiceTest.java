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
import serp.project.crm.core.domain.constant.ErrorMessage;
import serp.project.crm.core.domain.entity.ActivityEntity;
import serp.project.crm.core.domain.entity.TeamMemberEntity;
import serp.project.crm.core.domain.enums.ActivityStatus;
import serp.project.crm.core.domain.enums.ActivityType;
import serp.project.crm.core.domain.enums.TeamMemberStatus;
import serp.project.crm.core.exception.AppException;
import serp.project.crm.core.port.store.IAccountPort;
import serp.project.crm.core.port.store.IActivityPort;
import serp.project.crm.core.port.store.IContactPort;
import serp.project.crm.core.port.store.ILeadPort;
import serp.project.crm.core.port.store.IOpportunityPort;
import serp.project.crm.core.port.store.ITeamMemberPort;
import serp.project.crm.core.service.IRepTimeBlockService;
import serp.project.crm.core.service.INotificationPublisher;

import java.time.LocalDateTime;
import java.time.ZoneId;
import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertSame;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyNoMoreInteractions;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class ActivityServiceTest {

    @Mock
    private IActivityPort activityPort;

    @Mock
    private ILeadPort leadPort;

    @Mock
    private IOpportunityPort opportunityPort;

    @Mock
    private IAccountPort accountPort;

    @Mock
    private ITeamMemberPort teamMemberPort;

    @Mock
    private IRepTimeBlockService repTimeBlockService;

    @Mock
    private IContactPort contactPort;

    @Mock
    private INotificationPublisher notificationPublisher;

    @InjectMocks
    private ActivityService activityService;

    @Test
    void getUpcomingActivitiesShouldQueryByRequestedRangeInEpochMillis() {
        LocalDateTime startDate = LocalDateTime.of(2026, 5, 1, 8, 0);
        LocalDateTime endDate = LocalDateTime.of(2026, 5, 10, 18, 30);
        Long tenantId = 99L;
        Long expectedStart = startDate.atZone(ZoneId.systemDefault()).toInstant().toEpochMilli();
        Long expectedEnd = endDate.atZone(ZoneId.systemDefault()).toInstant().toEpochMilli();
        List<ActivityEntity> expectedActivities = List.of(ActivityEntity.builder().id(1L).build());

        when(activityPort.findUpcomingActivities(tenantId, expectedStart, expectedEnd)).thenReturn(expectedActivities);

        List<ActivityEntity> result = activityService.getUpcomingActivities(startDate, endDate, tenantId);

        assertSame(expectedActivities, result);
        verify(activityPort).findUpcomingActivities(tenantId, expectedStart, expectedEnd);
        verifyNoMoreInteractions(activityPort);
    }

    @Test
    void getUpcomingActivitiesShouldRejectInvalidDateRange() {
        LocalDateTime startDate = LocalDateTime.of(2026, 5, 10, 18, 30);
        LocalDateTime endDate = LocalDateTime.of(2026, 5, 1, 8, 0);

        AppException exception = assertThrows(AppException.class,
                () -> activityService.getUpcomingActivities(startDate, endDate, 99L));

        assertEquals(ErrorMessage.INVALID_DATE_RANGE, exception.getMessage());
    }

    @Test
    void createMeetingActivityShouldSyncRepTimeBlock() {
        ActivityEntity activity = ActivityEntity.builder()
                .activityType(ActivityType.MEETING)
                .subject("Demo meeting")
                .accountId(10L)
                .assignedTo(20L)
                .activityDate(System.currentTimeMillis() + 3600000)
                .durationMinutes(60)
                .build();

        TeamMemberEntity teamMember = TeamMemberEntity.builder()
                .id(99L)
                .userId(20L)
                .status(TeamMemberStatus.ACTIVE)
                .build();

        when(teamMemberPort.findByUserId(20L, 30L)).thenReturn(Optional.of(teamMember));
        when(accountPort.findById(10L, 30L)).thenReturn(Optional.of(serp.project.crm.core.domain.entity.AccountEntity.builder().id(10L).build()));
        when(activityPort.save(activity)).thenAnswer(invocation -> {
            ActivityEntity saved = invocation.getArgument(0);
            saved.setId(101L);
            return saved;
        });

        ActivityEntity created = activityService.createActivity(activity, 40L, 30L);

        assertEquals(101L, created.getId());
        verify(repTimeBlockService).syncFromActivity(created, 30L);
        verify(notificationPublisher).publishMeetingAssigned(created, 30L);
    }

    @Test
    void cancelMeetingActivityShouldRemoveRepTimeBlockThroughSync() {
        ActivityEntity activity = ActivityEntity.builder()
                .id(11L)
                .tenantId(30L)
                .activityType(ActivityType.MEETING)
                .status(ActivityStatus.PLANNED)
                .subject("Demo")
                .accountId(1L)
                .assignedTo(2L)
                .activityDate(System.currentTimeMillis() + 3600000)
                .durationMinutes(60)
                .build();

        when(activityPort.findById(11L, 30L)).thenReturn(Optional.of(activity));
        when(activityPort.save(activity)).thenReturn(activity);

        activityService.cancelActivity(11L, 50L, 30L);

        verify(repTimeBlockService).syncFromActivity(activity, 30L);
        verify(notificationPublisher).publishMeetingCancelled(activity, 30L);
    }
}
