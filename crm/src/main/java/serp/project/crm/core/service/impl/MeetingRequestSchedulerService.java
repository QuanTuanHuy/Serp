/**
 * Author: QuanTuanHuy
 * Description: Part of Serp Project
 */

package serp.project.crm.core.service.impl;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;
import org.springframework.transaction.support.TransactionTemplate;
import org.springframework.util.StringUtils;
import serp.project.crm.core.domain.constant.ErrorMessage;
import serp.project.crm.core.domain.constant.WorkingHoursDefaults;
import serp.project.crm.core.domain.entity.ActivityEntity;
import serp.project.crm.core.domain.entity.MeetingRequestEntity;
import serp.project.crm.core.domain.entity.OpportunityEntity;
import serp.project.crm.core.domain.entity.TeamMemberEntity;
import serp.project.crm.core.domain.entity.WorkingHoursEntity;
import serp.project.crm.core.domain.enums.ActivityStatus;
import serp.project.crm.core.domain.enums.ActivityType;
import serp.project.crm.core.domain.enums.MeetingRequestStatus;
import serp.project.crm.core.domain.enums.PreferredTimeSlot;
import serp.project.crm.core.exception.AppException;
import serp.project.crm.core.port.store.IAccountPort;
import serp.project.crm.core.port.store.IMeetingRequestPort;
import serp.project.crm.core.port.store.IOpportunityPort;
import serp.project.crm.core.port.store.IRepTimeBlockPort;
import serp.project.crm.core.service.IActivityService;
import serp.project.crm.core.service.ITeamMemberService;

import java.time.DayOfWeek;
import java.time.Duration;
import java.time.Instant;
import java.time.LocalTime;
import java.time.ZoneId;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;
import java.util.Optional;

@Service
@RequiredArgsConstructor
@Slf4j
public class MeetingRequestSchedulerService {

    private static final ZoneId BUSINESS_TIMEZONE = ZoneId.of("Asia/Ho_Chi_Minh");
    private static final int SLOT_STEP_MINUTES = 30;
    private static final int MAX_ATTEMPTS = 3;
    private static final int BATCH_SIZE = 100;

    private static final String REASON_NO_ACTIVE_TEAM_MEMBER = "NO_ACTIVE_TEAM_MEMBER";
    private static final String REASON_NO_AVAILABLE_SLOT = "NO_AVAILABLE_SLOT";
    private static final String REASON_REQUEST_EXPIRED = "REQUEST_EXPIRED";

    private final IMeetingRequestPort meetingRequestPort;
    private final ITeamMemberService teamMemberService;
    private final IRepTimeBlockPort repTimeBlockPort;
    private final IActivityService activityService;
    private final IAccountPort accountPort;
    private final IOpportunityPort opportunityPort;
    private final MeetingRequestPriorityService meetingRequestPriorityService;
    private final TransactionTemplate transactionTemplate;

    @Scheduled(fixedDelayString = "${app.scheduler.meeting.interval-ms:300000}")
    public void schedulePendingRequests() {
        long now = System.currentTimeMillis();
        List<MeetingRequestEntity> pendingRequests = meetingRequestPort.findPendingRequests(now, BATCH_SIZE);
        if (pendingRequests.isEmpty()) {
            return;
        }

        log.info("[MeetingRequestScheduler] Processing {} pending meeting requests", pendingRequests.size());

        for (MeetingRequestEntity request : pendingRequests) {
            transactionTemplate.executeWithoutResult(status -> processSingleRequest(request.getId(), request.getTenantId()));
        }
    }

    private void processSingleRequest(Long id, Long tenantId) {
        MeetingRequestEntity request = meetingRequestPort.findById(id, tenantId).orElse(null);
        if (request == null || !MeetingRequestStatus.PENDING.equals(request.getStatus())) {
            return;
        }

        if (request.isExpired()) {
            markFailed(request, REASON_REQUEST_EXPIRED);
            return;
        }

        var account = accountPort.findById(request.getAccountId(), tenantId)
                .orElseThrow(() -> new AppException(ErrorMessage.ACCOUNT_NOT_FOUND));
        OpportunityEntity opportunity = request.getOpportunityId() != null
                ? opportunityPort.findById(request.getOpportunityId(), tenantId).orElse(null)
                : null;

        request.setPriorityScore(meetingRequestPriorityService.calculate(request, account, opportunity));
        meetingRequestPort.save(request);

        List<TeamMemberEntity> candidates = teamMemberService.getActiveMembersByTeamWithWorkingHours(request.getTeamId(), tenantId)
                .stream()
                .sorted(buildMemberComparator(request, tenantId))
                .toList();

        if (candidates.isEmpty()) {
            handleRetry(request, REASON_NO_ACTIVE_TEAM_MEMBER);
            return;
        }

        Optional<ScheduledAssignment> assignment = findAssignment(request, candidates, tenantId);
        if (assignment.isEmpty()) {
            handleRetry(request, REASON_NO_AVAILABLE_SLOT);
            return;
        }

        commitSchedule(request, assignment.get(), tenantId);
    }

    private Optional<ScheduledAssignment> findAssignment(MeetingRequestEntity request, List<TeamMemberEntity> candidates,
            Long tenantId) {
        List<TimeSlot> slots = generateCandidateSlots(request);
        for (TeamMemberEntity candidate : candidates) {
            for (TimeSlot slot : slots) {
                if (!isWithinWorkingHours(candidate, slot, request.getPreferredTimeSlot())) {
                    continue;
                }
                if (isAvailable(candidate.getId(), tenantId, slot)) {
                    return Optional.of(new ScheduledAssignment(candidate, slot));
                }
            }
        }
        return Optional.empty();
    }

    private void commitSchedule(MeetingRequestEntity request, ScheduledAssignment assignment, Long tenantId) {
        ActivityEntity activity = buildActivity(request, assignment.teamMember());
        activity.setActivityDate(assignment.slot().startTime());

        Long actorId = request.getCreatedBy() != null ? request.getCreatedBy() : request.getUpdatedBy();
        ActivityEntity created = activityService.createActivity(activity, actorId, tenantId);

        request.setStatus(MeetingRequestStatus.SCHEDULED);
        request.setFailureReason(null);
        request.setScheduledActivityId(created.getId());
        request.setAssignedTeamMemberId(assignment.teamMember().getId());
        request.setAssignedUserId(assignment.teamMember().getUserId());
        request.setScheduledStartTime(assignment.slot().startTime());
        meetingRequestPort.save(request);
    }

    private void handleRetry(MeetingRequestEntity request, String reason) {
        request.incrementAttempts();
        request.setFailureReason(reason);
        if (request.isExpired() || request.getSchedulingAttempts() >= MAX_ATTEMPTS) {
            request.setStatus(MeetingRequestStatus.FAILED);
        }
        meetingRequestPort.save(request);
    }

    private void markFailed(MeetingRequestEntity request, String reason) {
        request.incrementAttempts();
        request.setStatus(MeetingRequestStatus.FAILED);
        request.setFailureReason(reason);
        meetingRequestPort.save(request);
    }

    private Comparator<TeamMemberEntity> buildMemberComparator(MeetingRequestEntity request, Long tenantId) {
        return Comparator
                .comparing((TeamMemberEntity member) -> request.getPreferredUserId() == null
                        || !member.getUserId().equals(request.getPreferredUserId()))
                .thenComparingInt(member -> plannedLoad(member.getId(), tenantId))
                .thenComparing(TeamMemberEntity::getUserId);
    }

    private int plannedLoad(Long teamMemberId, Long tenantId) {
        long now = System.currentTimeMillis();
        return repTimeBlockPort.findUpcomingByTeamMemberId(teamMemberId, tenantId, now).size();
    }

    private List<TimeSlot> generateCandidateSlots(MeetingRequestEntity request) {
        List<TimeSlot> slots = new ArrayList<>();
        int durationMinutes = request.getEffectiveDurationMinutes();
        Instant cursor = Instant.ofEpochMilli(request.getEarliestStart());
        Instant latestStart = Instant.ofEpochMilli(request.getLatestStart());

        while (!cursor.isAfter(latestStart)) {
            slots.add(new TimeSlot(cursor.toEpochMilli(), cursor.plus(Duration.ofMinutes(durationMinutes)).toEpochMilli()));
            cursor = cursor.plus(Duration.ofMinutes(SLOT_STEP_MINUTES));
        }

        return slots;
    }

    private boolean isWithinWorkingHours(TeamMemberEntity teamMember, TimeSlot slot, PreferredTimeSlot preferredTimeSlot) {
        var start = Instant.ofEpochMilli(slot.startTime()).atZone(BUSINESS_TIMEZONE);
        var end = Instant.ofEpochMilli(slot.endTime()).atZone(BUSINESS_TIMEZONE);

        if (start.toLocalDate().isBefore(end.toLocalDate())) {
            return false;
        }

        LocalTime startTime = start.toLocalTime();
        LocalTime endTime = end.toLocalTime();
        DayOfWeek dayOfWeek = start.getDayOfWeek();
        WorkingHoursEntity workingHours = effectiveWorkingHours(teamMember).stream()
                .filter(item -> dayOfWeek.equals(item.getDayOfWeek()))
                .findFirst()
                .orElse(null);
        if (workingHours == null || !workingHours.covers(startTime, endTime)) {
            return false;
        }

        if (preferredTimeSlot == null) {
            return true;
        }
        return preferredTimeSlot.contains(startTime.getHour());
    }

    private boolean isAvailable(Long teamMemberId, Long tenantId, TimeSlot slot) {
        return repTimeBlockPort.countConflicts(teamMemberId, tenantId, slot.startTime(), slot.endTime()) == 0;
    }

    private List<WorkingHoursEntity> effectiveWorkingHours(TeamMemberEntity teamMember) {
        if (teamMember.getWorkingHours() != null && !teamMember.getWorkingHours().isEmpty()) {
            return teamMember.getWorkingHours();
        }

        return WorkingHoursDefaults.createDefaultWeek();
    }

    private ActivityEntity buildActivity(MeetingRequestEntity request, TeamMemberEntity teamMember) {
        String subject = StringUtils.hasText(request.getSubject())
                ? request.getSubject()
                : "[" + request.getMeetingType().name() + "] Meeting request #" + request.getId();

        return ActivityEntity.builder()
                .accountId(request.getAccountId())
                .opportunityId(request.getOpportunityId())
                .contactId(request.getContactId())
                .activityType(ActivityType.MEETING)
                .status(ActivityStatus.PLANNED)
                .subject(subject)
                .description(request.getDescription())
                .location(request.getLocation())
                .assignedTo(teamMember.getUserId())
                .durationMinutes(request.getEffectiveDurationMinutes())
                .build();
    }

    private record TimeSlot(long startTime, long endTime) {
    }

    private record ScheduledAssignment(TeamMemberEntity teamMember, TimeSlot slot) {
    }
}
