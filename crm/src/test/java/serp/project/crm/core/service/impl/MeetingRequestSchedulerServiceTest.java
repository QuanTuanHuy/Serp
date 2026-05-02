package serp.project.crm.core.service.impl;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.transaction.TransactionStatus;
import org.springframework.transaction.support.SimpleTransactionStatus;
import org.springframework.transaction.support.TransactionTemplate;
import serp.project.crm.core.domain.entity.AccountEntity;
import serp.project.crm.core.domain.entity.ActivityEntity;
import serp.project.crm.core.domain.entity.MeetingRequestEntity;
import serp.project.crm.core.domain.entity.TeamMemberEntity;
import serp.project.crm.core.domain.entity.WorkingHoursEntity;
import serp.project.crm.core.domain.enums.AccountType;
import serp.project.crm.core.domain.enums.ActivityStatus;
import serp.project.crm.core.domain.enums.MeetingRequestStatus;
import serp.project.crm.core.domain.enums.MeetingRequestType;
import serp.project.crm.core.domain.enums.TeamMemberStatus;
import serp.project.crm.core.port.concurrency.IRepCalendarLockPort;
import serp.project.crm.core.port.store.IAccountPort;
import serp.project.crm.core.port.store.IMeetingRequestPort;
import serp.project.crm.core.port.store.IOpportunityPort;
import serp.project.crm.core.port.store.IRepTimeBlockPort;
import serp.project.crm.core.service.IActivityService;
import serp.project.crm.core.service.ITeamMemberService;

import java.time.DayOfWeek;
import java.time.Instant;
import java.time.LocalDate;
import java.time.ZoneId;
import java.time.temporal.ChronoUnit;
import java.time.temporal.TemporalAdjusters;
import java.util.Collections;
import java.util.List;
import java.util.Optional;
import java.util.function.Consumer;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyInt;
import static org.mockito.ArgumentMatchers.anyLong;
import static org.mockito.Mockito.atLeastOnce;
import static org.mockito.Mockito.doAnswer;
import static org.mockito.Mockito.lenient;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class MeetingRequestSchedulerServiceTest {

    @Mock
    private IMeetingRequestPort meetingRequestPort;
    @Mock
    private ITeamMemberService teamMemberService;
    @Mock
    private IActivityService activityService;
    @Mock
    private IAccountPort accountPort;
    @Mock
    private IOpportunityPort opportunityPort;
    @Mock
    private IRepTimeBlockPort repTimeBlockPort;
    @Mock
    private IRepCalendarLockPort repCalendarLockPort;
    @Mock
    private MeetingPriorityCalculator meetingPriorityCalculator;
    @Mock
    private RepCompatibilityMatcher repCompatibilityMatcher;
    @Mock
    private TransactionTemplate transactionTemplate;

    private MeetingRequestSchedulerService schedulerService;

    @BeforeEach
    void setUp() {
        schedulerService = new MeetingRequestSchedulerService(
                meetingRequestPort,
                teamMemberService,
                repTimeBlockPort,
                repCalendarLockPort,
                activityService,
                accountPort,
                opportunityPort,
                meetingPriorityCalculator,
                repCompatibilityMatcher,
                transactionTemplate);

        doAnswer(invocation -> {
            @SuppressWarnings("unchecked")
            Consumer<TransactionStatus> consumer = invocation.getArgument(0);
            consumer.accept(new SimpleTransactionStatus());
            return null;
        }).when(transactionTemplate).executeWithoutResult(any());

        when(meetingRequestPort.save(any(MeetingRequestEntity.class)))
                .thenAnswer(invocation -> invocation.getArgument(0));
        lenient().when(repTimeBlockPort.findUpcomingByTeamMemberId(anyLong(), anyLong(), anyLong()))
                .thenReturn(Collections.emptyList());
    }

    @Test
    void schedulePendingRequests_createsActivityWhenSlotIsAvailable() {
        MeetingRequestEntity request = buildPendingRequest();
        TeamMemberEntity member = TeamMemberEntity.builder()
                .id(11L)
                .userId(21L)
                .status(TeamMemberStatus.ACTIVE)
                .workingHours(List.of(defaultWorkingHours(DayOfWeek.MONDAY)))
                .build();

        when(meetingRequestPort.findPendingRequests(anyLong(), anyInt())).thenReturn(List.of(request));
        when(meetingRequestPort.findById(request.getId(), request.getTenantId())).thenReturn(Optional.of(request));
        when(accountPort.findById(request.getAccountId(), request.getTenantId()))
                .thenReturn(Optional.of(AccountEntity.builder().accountType(AccountType.CUSTOMER).build()));
        when(teamMemberService.getActiveMembersByTeamWithWorkingHours(request.getTeamId(), request.getTenantId())).thenReturn(List.of(member));
        when(repCompatibilityMatcher.canTakeMoreMeetings(any(), anyInt())).thenReturn(true);
        when(repTimeBlockPort.countConflicts(member.getId(), request.getTenantId(), request.getEarliestStart(), request.getEarliestStart() + ChronoUnit.HOURS.getDuration().toMillis())).thenReturn(0L);
        when(meetingPriorityCalculator.calculate(any(), any(), any())).thenReturn(80);
        when(activityService.createActivity(any(ActivityEntity.class), anyLong(), anyLong()))
                .thenAnswer(invocation -> {
                    ActivityEntity activity = invocation.getArgument(0);
                    activity.setId(999L);
                    activity.setStatus(ActivityStatus.PLANNED);
                    return activity;
                });

        schedulerService.schedulePendingRequests();

        verify(activityService).createActivity(any(ActivityEntity.class), anyLong(), anyLong());
        ArgumentCaptor<MeetingRequestEntity> captor = ArgumentCaptor.forClass(MeetingRequestEntity.class);
        verify(meetingRequestPort, org.mockito.Mockito.atLeastOnce()).save(captor.capture());
        MeetingRequestEntity saved = captor.getAllValues().get(captor.getAllValues().size() - 1);

        assertThat(saved.getStatus()).isEqualTo(MeetingRequestStatus.SCHEDULED);
        assertThat(saved.getAssignedUserId()).isEqualTo(member.getUserId());
        assertThat(saved.getScheduledActivityId()).isEqualTo(999L);
    }

    @Test
    void schedulePendingRequests_marksExpiredRequestAsFailed() {
        MeetingRequestEntity request = buildPendingRequest();
        request.setLatestStart(Instant.now().minus(1, ChronoUnit.HOURS).toEpochMilli());

        when(meetingRequestPort.findPendingRequests(anyLong(), anyInt())).thenReturn(List.of(request));
        when(meetingRequestPort.findById(request.getId(), request.getTenantId())).thenReturn(Optional.of(request));

        schedulerService.schedulePendingRequests();

        verify(activityService, never()).createActivity(any(ActivityEntity.class), anyLong(), anyLong());
        ArgumentCaptor<MeetingRequestEntity> captor = ArgumentCaptor.forClass(MeetingRequestEntity.class);
        verify(meetingRequestPort).save(captor.capture());
        assertThat(captor.getValue().getStatus()).isEqualTo(MeetingRequestStatus.FAILED);
    }

    @Test
    void schedulePendingRequests_skipsBlockedSlot() {
        MeetingRequestEntity request = buildPendingRequest();
        TeamMemberEntity member = TeamMemberEntity.builder()
                .id(11L)
                .userId(21L)
                .status(TeamMemberStatus.ACTIVE)
                .workingHours(List.of(defaultWorkingHours(DayOfWeek.MONDAY)))
                .build();

        when(meetingRequestPort.findPendingRequests(anyLong(), anyInt())).thenReturn(List.of(request));
        when(meetingRequestPort.findById(request.getId(), request.getTenantId())).thenReturn(Optional.of(request));
        when(accountPort.findById(request.getAccountId(), request.getTenantId()))
                .thenReturn(Optional.of(AccountEntity.builder().accountType(AccountType.CUSTOMER).build()));
        when(teamMemberService.getActiveMembersByTeamWithWorkingHours(request.getTeamId(), request.getTenantId())).thenReturn(List.of(member));
        when(repCompatibilityMatcher.canTakeMoreMeetings(any(), anyInt())).thenReturn(true);
        when(repTimeBlockPort.countConflicts(member.getId(), request.getTenantId(), request.getEarliestStart(), request.getEarliestStart() + ChronoUnit.HOURS.getDuration().toMillis()))
                .thenReturn(1L);
        when(repTimeBlockPort.countConflicts(member.getId(), request.getTenantId(), request.getEarliestStart() + ChronoUnit.MINUTES.getDuration().toMillis() * 30, request.getEarliestStart() + ChronoUnit.MINUTES.getDuration().toMillis() * 90))
                .thenReturn(0L);
        when(meetingPriorityCalculator.calculate(any(), any(), any())).thenReturn(80);
        when(activityService.createActivity(any(ActivityEntity.class), anyLong(), anyLong()))
                .thenAnswer(invocation -> {
                    ActivityEntity activity = invocation.getArgument(0);
                    activity.setId(999L);
                    activity.setStatus(ActivityStatus.PLANNED);
                    return activity;
                });

        schedulerService.schedulePendingRequests();

        verify(activityService).createActivity(any(ActivityEntity.class), anyLong(), anyLong());
    }

    @Test
    void schedulePendingRequests_retriesWhenSlotConflictsAfterLock() {
        MeetingRequestEntity request = buildPendingRequest();
        request.setLatestStart(request.getEarliestStart());

        TeamMemberEntity member = TeamMemberEntity.builder()
                .id(11L)
                .userId(21L)
                .status(TeamMemberStatus.ACTIVE)
                .workingHours(List.of(defaultWorkingHours(DayOfWeek.MONDAY)))
                .build();

        long slotStart = request.getEarliestStart();
        long slotEnd = slotStart + ChronoUnit.HOURS.getDuration().toMillis();

        when(meetingRequestPort.findPendingRequests(anyLong(), anyInt())).thenReturn(List.of(request));
        when(meetingRequestPort.findById(request.getId(), request.getTenantId())).thenReturn(Optional.of(request));
        when(accountPort.findById(request.getAccountId(), request.getTenantId()))
                .thenReturn(Optional.of(AccountEntity.builder().accountType(AccountType.CUSTOMER).build()));
        when(teamMemberService.getActiveMembersByTeamWithWorkingHours(request.getTeamId(), request.getTenantId())).thenReturn(List.of(member));
        when(repCompatibilityMatcher.canTakeMoreMeetings(any(), anyInt())).thenReturn(true);
        when(repTimeBlockPort.countConflicts(member.getId(), request.getTenantId(), slotStart, slotEnd)).thenReturn(0L, 1L);
        when(meetingPriorityCalculator.calculate(any(), any(), any())).thenReturn(80);

        schedulerService.schedulePendingRequests();

        verify(repCalendarLockPort).acquireExclusiveForRep(request.getTenantId(), member.getId());
        verify(activityService, never()).createActivity(any(ActivityEntity.class), anyLong(), anyLong());
        ArgumentCaptor<MeetingRequestEntity> captor = ArgumentCaptor.forClass(MeetingRequestEntity.class);
        verify(meetingRequestPort, atLeastOnce()).save(captor.capture());
        MeetingRequestEntity lastSave = captor.getAllValues().get(captor.getAllValues().size() - 1);
        assertThat(lastSave.getFailureReason()).isEqualTo("NO_AVAILABLE_SLOT");
        assertThat(lastSave.getSchedulingAttempts()).isEqualTo(1);
    }

    private MeetingRequestEntity buildPendingRequest() {
        long nextBusinessSlot = nextMondayNineAm();
        return MeetingRequestEntity.builder()
                .id(1L)
                .tenantId(2L)
                .teamId(3L)
                .accountId(4L)
                .createdBy(5L)
                .meetingType(MeetingRequestType.DEMO)
                .status(MeetingRequestStatus.PENDING)
                .earliestStart(nextBusinessSlot)
                .latestStart(nextBusinessSlot + ChronoUnit.HOURS.getDuration().toMillis())
                .requestedDeadline(nextBusinessSlot + ChronoUnit.HOURS.getDuration().toMillis())
                .durationMinutes(60)
                .schedulingAttempts(0)
                .build();
    }

    private long nextMondayNineAm() {
        ZoneId zoneId = ZoneId.of("Asia/Ho_Chi_Minh");
        LocalDate today = LocalDate.now(zoneId);
        LocalDate nextMonday = today.with(TemporalAdjusters.nextOrSame(java.time.DayOfWeek.MONDAY));
        if (nextMonday.equals(today)) {
            nextMonday = nextMonday.plusWeeks(1);
        }
        return nextMonday.atTime(9, 0).atZone(zoneId).toInstant().toEpochMilli();
    }

    private WorkingHoursEntity defaultWorkingHours(DayOfWeek dayOfWeek) {
        return WorkingHoursEntity.builder()
                .dayOfWeek(dayOfWeek)
                .workingDay(true)
                .startMinute(8 * 60)
                .endMinute(17 * 60)
                .build();
    }
}
