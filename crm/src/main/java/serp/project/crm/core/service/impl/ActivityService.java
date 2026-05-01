/**
 * Author: QuanTuanHuy
 * Description: Part of Serp Project
 */

package serp.project.crm.core.service.impl;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.util.Pair;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.StringUtils;
import serp.project.crm.core.domain.constant.Constants;
import serp.project.crm.core.domain.constant.ErrorMessage;
import serp.project.crm.core.domain.dto.PageRequest;
import serp.project.crm.core.domain.dto.request.ActivityFilterRequest;
import serp.project.crm.core.domain.entity.ActivityEntity;
import serp.project.crm.core.domain.enums.ActivityOutcome;
import serp.project.crm.core.domain.enums.ActivityStatus;
import serp.project.crm.core.domain.enums.ActivityType;
import serp.project.crm.core.domain.enums.TaskPriority;
import serp.project.crm.core.domain.enums.TeamMemberStatus;
import serp.project.crm.core.exception.AppException;
import serp.project.crm.core.port.store.IActivityPort;
import serp.project.crm.core.port.store.IContactPort;
import serp.project.crm.core.port.store.IAccountPort;
import serp.project.crm.core.port.store.ILeadPort;
import serp.project.crm.core.port.store.IOpportunityPort;
import serp.project.crm.core.port.store.ITeamMemberPort;
import serp.project.crm.core.service.IActivityService;

import java.time.LocalDateTime;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.Set;

@Service
@RequiredArgsConstructor
@Slf4j
public class ActivityService implements IActivityService {

    private static final int DEFAULT_MEETING_DURATION_MINUTES = 60;
    private static final int DEFAULT_CALL_DURATION_MINUTES = 15;

    private final IActivityPort activityPort;
    private final ILeadPort leadPort;
    private final IOpportunityPort opportunityPort;
    private final IAccountPort AccountPort;
    private final ITeamMemberPort teamMemberPort;

    private final IContactPort contactPort;

    @Override
    @Transactional
    public ActivityEntity createActivity(ActivityEntity activity, Long userId, Long tenantId) {
        activity.setTenantId(tenantId);
        activity.setCreatedBy(userId);
        activity.setUpdatedBy(userId);
        activity.setDefaults();
        applyTypeDefaults(activity);
        applyAssignDefault(activity, userId);
        normalizeTypeSpecificFields(activity);
        validateBusinessRules(activity, tenantId);

        ActivityEntity saved = activityPort.save(activity);

        publishActivityCreatedEvent(saved);

        return saved;
    }

    @Override
    @Transactional
    public ActivityEntity updateActivity(Long id, ActivityEntity updates, Long userId, Long tenantId) {
        ActivityEntity existing = activityPort.findById(id, tenantId)
                .orElseThrow(() -> new AppException(ErrorMessage.ACTIVITY_NOT_FOUND));

        updates.setUpdatedBy(userId);
        try {
            existing.updateFrom(updates);
        } catch (IllegalStateException e) {
            throw new AppException(e.getMessage());
        }
        applyTypeDefaults(existing);
        normalizeTypeSpecificFields(existing);
        validateBusinessRules(existing, tenantId);

        ActivityEntity updated = activityPort.save(existing);

        publishActivityUpdatedEvent(updated);

        return updated;
    }

    @Override
    @Transactional(readOnly = true)
    public Optional<ActivityEntity> getActivityById(Long id, Long tenantId) {
        return activityPort.findById(id, tenantId);
    }

    @Override
    @Transactional(readOnly = true)
    public Pair<List<ActivityEntity>, Long> getAllActivities(Long tenantId, PageRequest pageRequest) {
        pageRequest.validate();
        return activityPort.findAll(tenantId, pageRequest);
    }

    @Override
    @Transactional(readOnly = true)
    public Pair<List<ActivityEntity>, Long> getActivitiesByType(ActivityType type, Long tenantId,
            PageRequest pageRequest) {
        pageRequest.validate();
        return activityPort.findByActivityType(type, tenantId, pageRequest);
    }

    @Override
    @Transactional(readOnly = true)
    public Pair<List<ActivityEntity>, Long> getActivitiesByStatus(ActivityStatus status, Long tenantId,
            PageRequest pageRequest) {
        pageRequest.validate();
        return activityPort.findByStatus(status, tenantId, pageRequest);
    }

    @Override
    @Transactional(readOnly = true)
    public Pair<List<ActivityEntity>, Long> getActivitiesByAssignee(Long userId, Long tenantId,
            PageRequest pageRequest) {
        pageRequest.validate();
        return activityPort.findByAssignedTo(userId, tenantId, pageRequest);
    }

    @Override
    @Transactional(readOnly = true)
    public Pair<List<ActivityEntity>, Long> getActivitiesByLead(Long leadId, Long tenantId, PageRequest pageRequest) {
        pageRequest.validate();
        return activityPort.findByLeadId(leadId, tenantId, pageRequest);
    }

    @Override
    @Transactional(readOnly = true)
    public Pair<List<ActivityEntity>, Long> getActivitiesByAccount(Long accountId, Long tenantId,
            PageRequest pageRequest) {
        pageRequest.validate();
        return activityPort.findByAccountId(accountId, tenantId, pageRequest);
    }

    @Override
    @Transactional(readOnly = true)
    public Pair<List<ActivityEntity>, Long> getActivitiesByOpportunity(Long opportunityId, Long tenantId,
            PageRequest pageRequest) {
        pageRequest.validate();
        return activityPort.findByOpportunityId(opportunityId, tenantId, pageRequest);
    }

    @Override
    @Transactional(readOnly = true)
    public Pair<List<ActivityEntity>, Long> getActivitiesByContact(Long contactId, Long tenantId,
            PageRequest pageRequest) {
        pageRequest.validate();
        return activityPort.findByContactId(contactId, tenantId, pageRequest);
    }

    @Override
    @Transactional(readOnly = true)
    public List<ActivityEntity> getOverdueActivities(Long tenantId) {
        return activityPort.findOverdueActivities(tenantId);
    }

    @Override
    @Transactional(readOnly = true)
    public List<ActivityEntity> getUpcomingActivities(LocalDateTime startDate, LocalDateTime endDate, Long tenantId) {
        // Implement later
        List<ActivityEntity> allUpcoming = activityPort.findUpcomingActivities(tenantId);

        Long startTimestamp = startDate.atZone(java.time.ZoneId.systemDefault()).toEpochSecond();
        Long endTimestamp = endDate.atZone(java.time.ZoneId.systemDefault()).toEpochSecond();

        return allUpcoming.stream()
                .filter(activity -> activity.getDueDate() != null
                        && activity.getDueDate() >= startTimestamp
                        && activity.getDueDate() <= endTimestamp)
                .toList();
    }

    @Override
    @Transactional
    public ActivityEntity completeActivity(Long id, ActivityOutcome outcome, String notes, Long userId, Long tenantId) {
        ActivityEntity activity = activityPort.findById(id, tenantId)
                .orElseThrow(() -> new AppException(ErrorMessage.ACTIVITY_NOT_FOUND));

        if (activity.isCompleted()) {
            throw new AppException(ErrorMessage.ACTIVITY_ALREADY_COMPLETED);
        }
        if (activity.isCancelled()) {
            throw new AppException(ErrorMessage.ACTIVITY_ALREADY_CANCELLED);
        }

        validateOutcomeForType(activity.getActivityType(), outcome, true);

        try {
            activity.markAsCompleted(outcome, notes, userId);
        } catch (IllegalStateException e) {
            throw new AppException(e.getMessage());
        }

        ActivityEntity completed = activityPort.save(activity);

        publishActivityCompletedEvent(completed);

        return completed;
    }

    @Override
    @Transactional
    public ActivityEntity cancelActivity(Long id, Long userId, Long tenantId) {
        ActivityEntity activity = activityPort.findById(id, tenantId)
                .orElseThrow(() -> new AppException(ErrorMessage.ACTIVITY_NOT_FOUND));

        if (activity.isCompleted()) {
            throw new AppException(ErrorMessage.ACTIVITY_ALREADY_COMPLETED);
        }
        if (activity.isCancelled()) {
            log.info("Activity already cancelled: {}", id);
            return activity;
        }

        activity.markAsCancelled(userId);

        ActivityEntity cancelled = activityPort.save(activity);

        publishActivityCancelledEvent(cancelled);

        return cancelled;
    }

    @Override
    @Transactional
    public ActivityEntity rescheduleActivity(Long id, Long dueDate, Long reminderDate, Long userId, Long tenantId) {
        ActivityEntity activity = activityPort.findById(id, tenantId)
                .orElseThrow(() -> new AppException(ErrorMessage.ACTIVITY_NOT_FOUND));

        try {
            activity.reschedule(dueDate, userId);
        } catch (IllegalStateException e) {
            throw new AppException(e.getMessage());
        }
        activity.setReminderDate(reminderDate);
        validateBusinessRules(activity, tenantId);

        ActivityEntity rescheduled = activityPort.save(activity);
        publishActivityUpdatedEvent(rescheduled);

        return rescheduled;
    }

    @Override
    @Transactional
    public void deleteActivity(Long id, Long tenantId) {
        ActivityEntity activity = activityPort.findById(id, tenantId)
                .orElseThrow(() -> new AppException(ErrorMessage.ACTIVITY_NOT_FOUND));

        activityPort.deleteById(id, tenantId);

        publishActivityDeletedEvent(activity);

    }

    @Override
    @Transactional(readOnly = true)
    public void validateRelations(ActivityEntity activity, Long tenantId) {
        if (activity.getLeadId() != null && leadPort.findById(activity.getLeadId(), tenantId).isEmpty()) {
            throw new AppException(ErrorMessage.LEAD_NOT_FOUND);
        }
        if (activity.getOpportunityId() != null
                && opportunityPort.findById(activity.getOpportunityId(), tenantId).isEmpty()) {
            throw new AppException(ErrorMessage.OPPORTUNITY_NOT_FOUND);
        }
        if (activity.getAccountId() != null
                && AccountPort.findById(activity.getAccountId(), tenantId).isEmpty()) {
            throw new AppException(ErrorMessage.ACCOUNT_NOT_FOUND);
        }
        if (activity.getContactId() != null
                && contactPort.findById(activity.getContactId(), tenantId).isEmpty()) {
            throw new AppException(ErrorMessage.CONTACT_NOT_FOUND);
        }
    }

    @Override
    @Transactional(readOnly = true)
    public Pair<List<ActivityEntity>, Long> filterActivities(ActivityFilterRequest filterRequest, Long tenantId) {
        return activityPort.filter(filterRequest, tenantId);
    }

    @Override
    @Transactional(readOnly = true)
    public Map<String, Long> getActivityStats(Long tenantId) {
        Map<String, Long> stats = new HashMap<>();
        stats.put("total", activityPort.countByTenantId(tenantId));
        stats.put("overdue", (long) activityPort.findOverdueActivities(tenantId).size());
        stats.put("upcoming", (long) activityPort.findUpcomingActivities(tenantId).size());

        for (ActivityStatus status : ActivityStatus.values()) {
            stats.put("status_" + status.name().toLowerCase(), activityPort.countByStatus(status, tenantId));
        }
        for (ActivityType type : ActivityType.values()) {
            stats.put("type_" + type.name().toLowerCase(), activityPort.countByActivityType(type, tenantId));
        }
        for (TaskPriority priority : TaskPriority.values()) {
            stats.put("priority_" + priority.name().toLowerCase(), activityPort.countByPriority(priority, tenantId));
        }
        return stats;
    }

    @Override
    @Transactional
    public Map<String, Integer> bulkCompleteActivities(Set<Long> activityIds, Long userId, Long tenantId) {
        Map<String, Integer> result = new HashMap<>();
        int success = 0;
        int failed = 0;
        List<ActivityEntity> activities = activityPort.findByIds(activityIds, tenantId);
        for (ActivityEntity activity : activities) {
            try {
                if (activity.isCompleted() || activity.isCancelled()) {
                    failed++;
                    continue;
                }
                activity.markAsCompleted(null, null, userId);
                activityPort.save(activity);
                success++;
            } catch (Exception e) {
                failed++;
            }
        }
        result.put("success", success);
        result.put("failed", failed);
        return result;
    }

    @Override
    @Transactional
    public Map<String, Integer> bulkCancelActivities(Set<Long> activityIds, Long userId, Long tenantId) {
        Map<String, Integer> result = new HashMap<>();
        int success = 0;
        int failed = 0;
        List<ActivityEntity> activities = activityPort.findByIds(activityIds, tenantId);
        for (ActivityEntity activity : activities) {
            try {
                if (activity.isCompleted() || activity.isCancelled()) {
                    failed++;
                    continue;
                }
                activity.markAsCancelled(userId);
                activityPort.save(activity);
                success++;
            } catch (Exception e) {
                failed++;
            }
        }
        result.put("success", success);
        result.put("failed", failed);
        return result;
    }

    @Override
    @Transactional
    public Map<String, Integer> bulkDeleteActivities(Set<Long> activityIds, Long tenantId) {
        Map<String, Integer> result = new HashMap<>();
        int success = 0;
        int failed = 0;
        List<ActivityEntity> activities = activityPort.findByIds(activityIds, tenantId);
        for (ActivityEntity activity : activities) {
            try {
                activityPort.deleteById(activity.getId(), tenantId);
                success++;
            } catch (Exception e) {
                failed++;
            }
        }
        result.put("success", success);
        result.put("failed", failed);
        return result;
    }

    @Override
    @Transactional
    public Map<String, Integer> bulkReassignActivities(Set<Long> activityIds, Long newAssigneeId, Long userId,
            Long tenantId) {
        if (newAssigneeId == null) {
            throw new AppException(ErrorMessage.TEAM_MEMBER_NOT_FOUND);
        }
        Map<String, Integer> result = new HashMap<>();
        int success = 0;
        int failed = 0;
        List<ActivityEntity> activities = activityPort.findByIds(activityIds, tenantId);
        for (ActivityEntity activity : activities) {
            try {
                activity.setAssignedTo(newAssigneeId);
                activity.setUpdatedBy(userId);
                validateAssignedUser(activity, tenantId);
                activityPort.save(activity);
                success++;
            } catch (Exception e) {
                failed++;
            }
        }
        result.put("success", success);
        result.put("failed", failed);
        return result;
    }

    private void applyTypeDefaults(ActivityEntity activity) {
        if (activity.isMeeting() && activity.getDurationMinutes() == null) {
            activity.setDurationMinutes(DEFAULT_MEETING_DURATION_MINUTES);
        }
        if (activity.isCall() && activity.getDurationMinutes() == null) {
            activity.setDurationMinutes(DEFAULT_CALL_DURATION_MINUTES);
        }
    }

    private void applyAssignDefault(ActivityEntity activity, Long userId) {
        if (activity.getAssignedTo() == null) {
            activity.setAssignedTo(userId);
        }
    }

    private void normalizeTypeSpecificFields(ActivityEntity activity) {
        if (!activity.isTask()) {
            activity.setProgressPercent(null);
        } else if (activity.getProgressPercent() == null) {
            activity.setProgressPercent(0);
        }
    }

    private void validateBusinessRules(ActivityEntity activity, Long tenantId) {
        if (activity.getActivityType() == null) {
            throw new AppException(ErrorMessage.ACTIVITY_TYPE_REQUIRED);
        }

        if (!StringUtils.hasText(activity.getSubject())) {
            throw new AppException(ErrorMessage.ACTIVITY_SUBJECT_REQUIRED);
        }

        if (!activity.hasAnyLink()) {
            throw new AppException(ErrorMessage.ACTIVITY_MISSING_ENTITY_REFERENCE);
        }

        if (!activity.isProgressValid()) {
            throw new AppException(ErrorMessage.ACTIVITY_PROGRESS_INVALID);
        }

        if (!activity.isDurationValid()) {
            throw new AppException(ErrorMessage.ACTIVITY_DURATION_INVALID);
        }

        validateAssignedUser(activity, tenantId);

        if (activity.isTask() && activity.getDueDate() == null) {
            throw new AppException(ErrorMessage.ACTIVITY_DUE_DATE_REQUIRED_FOR_TASK);
        }

        long now = System.currentTimeMillis();
        if ((activity.isMeeting() || activity.isCall())
                && activity.getActivityDate() != null && activity.getActivityDate() < now) {
            log.warn("Activity date is in the past for {} activity {}", activity.getActivityType(), activity.getId());
        }

        validateOutcomeForType(activity.getActivityType(), activity.getOutcome(), false);

        validateRelations(activity, tenantId);
    }

    private void validateAssignedUser(ActivityEntity activity, Long tenantId) {
        if (activity.getAssignedTo() != null && teamMemberPort.findByUserId(activity.getAssignedTo(), tenantId)
                .filter(member -> TeamMemberStatus.ACTIVE.equals(member.getStatus())).isEmpty()) {
            throw new AppException(ErrorMessage.TEAM_MEMBER_NOT_FOUND);
        }
    }

    private void validateOutcomeForType(ActivityType activityType, ActivityOutcome outcome, boolean required) {
        if (ActivityType.CALL.equals(activityType)) {
            if (outcome == null && required) {
                throw new AppException("Outcome is required when completing a call activity");
            }
            if (outcome != null && !outcome.isCallOutcome()) {
                throw new AppException("Invalid outcome for call activity");
            }
            return;
        }

        if (ActivityType.MEETING.equals(activityType)) {
            if (outcome == null && required) {
                throw new AppException("Outcome is required when completing a meeting activity");
            }
            if (outcome != null && !outcome.isMeetingOutcome()) {
                throw new AppException("Invalid outcome for meeting activity");
            }
            return;
        }

        if (outcome != null) {
            throw new AppException("Outcome is only supported for call or meeting activities");
        }
    }


    private void publishActivityCreatedEvent(ActivityEntity activity) {
        log.debug("Event: Activity created - ID: {}, Topic: {}", activity.getId(), Constants.KafkaTopic.ACTIVITY);
    }

    private void publishActivityUpdatedEvent(ActivityEntity activity) {
        log.debug("Event: Activity updated - ID: {}, Topic: {}", activity.getId(), Constants.KafkaTopic.ACTIVITY);
    }

    private void publishActivityCompletedEvent(ActivityEntity activity) {
        log.debug("Event: Activity completed - ID: {}, Topic: {}", activity.getId(), Constants.KafkaTopic.ACTIVITY);
    }

    private void publishActivityCancelledEvent(ActivityEntity activity) {
        log.debug("Event: Activity cancelled - ID: {}, Topic: {}", activity.getId(), Constants.KafkaTopic.ACTIVITY);
    }

    private void publishActivityDeletedEvent(ActivityEntity activity) {
        log.debug("Event: Activity deleted - ID: {}, Topic: {}", activity.getId(), Constants.KafkaTopic.ACTIVITY);
    }
}
