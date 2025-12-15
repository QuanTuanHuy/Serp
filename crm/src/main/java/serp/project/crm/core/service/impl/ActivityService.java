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
import serp.project.crm.core.domain.entity.ActivityEntity;
import serp.project.crm.core.domain.enums.ActivityStatus;
import serp.project.crm.core.domain.enums.ActivityType;
import serp.project.crm.core.exception.AppException;
import serp.project.crm.core.port.store.IActivityPort;
import serp.project.crm.core.port.store.IContactPort;
import serp.project.crm.core.port.store.ICustomerPort;
import serp.project.crm.core.port.store.ILeadPort;
import serp.project.crm.core.port.store.IOpportunityPort;
import serp.project.crm.core.port.store.ITeamMemberPort;
import serp.project.crm.core.service.IActivityService;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

@Service
@RequiredArgsConstructor
@Slf4j
public class ActivityService implements IActivityService {

    private static final int DEFAULT_MEETING_DURATION_MINUTES = 60;
    private static final int DEFAULT_CALL_DURATION_MINUTES = 15;

    private final IActivityPort activityPort;
    private final ILeadPort leadPort;
    private final IOpportunityPort opportunityPort;
    private final ICustomerPort customerPort;
    private final ITeamMemberPort teamMemberPort;

    private final IContactPort contactPort;

    @Override
    @Transactional
    public ActivityEntity createActivity(ActivityEntity activity, Long userId, Long tenantId) {
        activity.setTenantId(tenantId);
        activity.setDefaults();
        applyTypeDefaults(activity);
        applyAssignDefault(activity, userId);
        validateBusinessRules(activity, tenantId);

        ActivityEntity saved = activityPort.save(activity);

        publishActivityCreatedEvent(saved);

        return saved;
    }

    @Override
    @Transactional
    public ActivityEntity updateActivity(Long id, ActivityEntity updates, Long tenantId) {
        ActivityEntity existing = activityPort.findById(id, tenantId)
                .orElseThrow(() -> new AppException(ErrorMessage.ACTIVITY_NOT_FOUND));

        existing.updateFrom(updates);
        applyTypeDefaults(existing);
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
    public Pair<List<ActivityEntity>, Long> getActivitiesByCustomer(Long customerId, Long tenantId,
            PageRequest pageRequest) {
        pageRequest.validate();
        return activityPort.findByCustomerId(customerId, tenantId, pageRequest);
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
    public ActivityEntity completeActivity(Long id, Long tenantId) {
        ActivityEntity activity = activityPort.findById(id, tenantId)
                .orElseThrow(() -> new AppException(ErrorMessage.ACTIVITY_NOT_FOUND));

        if (activity.isCompleted()) {
            throw new AppException(ErrorMessage.ACTIVITY_ALREADY_COMPLETED);
        }
        if (activity.isCancelled()) {
            throw new AppException(ErrorMessage.ACTIVITY_ALREADY_CANCELLED);
        }

        activity.markAsCompleted(tenantId);

        ActivityEntity completed = activityPort.save(activity);

        publishActivityCompletedEvent(completed);

        return completed;
    }

    @Override
    @Transactional
    public ActivityEntity cancelActivity(Long id, Long tenantId) {
        ActivityEntity activity = activityPort.findById(id, tenantId)
                .orElseThrow(() -> new AppException(ErrorMessage.ACTIVITY_NOT_FOUND));

        if (activity.isCompleted()) {
            throw new AppException(ErrorMessage.ACTIVITY_ALREADY_COMPLETED);
        }
        if (activity.isCancelled()) {
            log.info("Activity already cancelled: {}", id);
            return activity;
        }

        activity.markAsCancelled(tenantId);

        ActivityEntity cancelled = activityPort.save(activity);

        publishActivityCancelledEvent(cancelled);

        return cancelled;
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
        if (activity.getCustomerId() != null
                && customerPort.findById(activity.getCustomerId(), tenantId).isEmpty()) {
            throw new AppException(ErrorMessage.CUSTOMER_NOT_FOUND);
        }
        if (activity.getContactId() != null
                && contactPort.findById(activity.getContactId(), tenantId).isEmpty()) {
            throw new AppException(ErrorMessage.CONTACT_NOT_FOUND);
        }
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
            var member = teamMemberPort.findByUserId(userId, activity.getTenantId()).orElse(null);
            if (member != null) {
                activity.setAssignedTo(member.getId());
            }
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

        if (activity.isTask() && activity.getDueDate() == null) {
            throw new AppException(ErrorMessage.ACTIVITY_DUE_DATE_REQUIRED_FOR_TASK);
        }

        long now = System.currentTimeMillis();
        if (activity.getDueDate() != null && activity.getDueDate() < now) {
            throw new AppException(ErrorMessage.ACTIVITY_DUE_DATE_PAST);
        }

        if (activity.isMeeting()
                || activity.isCall()
                && activity.getActivityDate() != null && activity.getActivityDate() < now) {
            log.warn("Activity date is in the past for {} activity {}", activity.getActivityType(), activity.getId());
        }

        validateRelations(activity, tenantId);
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
