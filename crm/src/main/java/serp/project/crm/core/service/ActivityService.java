/**
 * Author: QuanTuanHuy
 * Description: Part of Serp Project
 */

package serp.project.crm.core.service;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.util.Pair;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import serp.project.crm.core.domain.constant.Constants;
import serp.project.crm.core.domain.dto.PageRequest;
import serp.project.crm.core.domain.entity.ActivityEntity;
import serp.project.crm.core.domain.enums.ActivityStatus;
import serp.project.crm.core.domain.enums.ActivityType;
import serp.project.crm.core.port.client.IKafkaPublisher;
import serp.project.crm.core.port.store.IActivityPort;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

/**
 * Activity Service - Business logic for activity and task management
 */
@Service
@RequiredArgsConstructor
@Slf4j
public class ActivityService implements IActivityService {

    private final IActivityPort activityPort;
    private final IKafkaPublisher kafkaPublisher;

    @Override
    @Transactional
    public ActivityEntity createActivity(ActivityEntity activity, Long tenantId) {
        log.info("Creating activity {} for tenant {}", activity.getSubject(), tenantId);

        // Validation: Due date cannot be in the past for new activities
        Long now = System.currentTimeMillis() / 1000;
        if (activity.getDueDate() != null && activity.getDueDate() < now) {
            throw new IllegalArgumentException("Due date cannot be in the past");
        }

        // Set defaults
        activity.setTenantId(tenantId);
        if (activity.getStatus() == null) {
            activity.setStatus(ActivityStatus.PLANNED);
        }
        if (activity.getProgressPercent() == null) {
            activity.setProgressPercent(0);
        }

        // Save
        ActivityEntity saved = activityPort.save(activity);

        // Publish event
        publishActivityCreatedEvent(saved);

        log.info("Activity created successfully with ID {}", saved.getId());
        return saved;
    }

    @Override
    @Transactional
    public ActivityEntity updateActivity(Long id, ActivityEntity updates, Long tenantId) {
        log.info("Updating activity {} for tenant {}", id, tenantId);

        ActivityEntity existing = activityPort.findById(id, tenantId)
                .orElseThrow(() -> new IllegalArgumentException("Activity not found"));

        // Validation: Cannot update completed or cancelled activities
        if (existing.isCompleted() || ActivityStatus.CANCELLED.equals(existing.getStatus())) {
            throw new IllegalStateException("Cannot update completed or cancelled activities");
        }

        // Update fields
        if (updates.getSubject() != null) existing.setSubject(updates.getSubject());
        if (updates.getDescription() != null) existing.setDescription(updates.getDescription());
        if (updates.getActivityType() != null) existing.setActivityType(updates.getActivityType());
        if (updates.getStatus() != null) existing.setStatus(updates.getStatus());
        if (updates.getLocation() != null) existing.setLocation(updates.getLocation());
        if (updates.getAssignedTo() != null) existing.setAssignedTo(updates.getAssignedTo());
        if (updates.getActivityDate() != null) existing.setActivityDate(updates.getActivityDate());
        if (updates.getDueDate() != null) existing.setDueDate(updates.getDueDate());
        if (updates.getReminderDate() != null) existing.setReminderDate(updates.getReminderDate());
        if (updates.getDurationMinutes() != null) existing.setDurationMinutes(updates.getDurationMinutes());
        if (updates.getPriority() != null) existing.setPriority(updates.getPriority());
        if (updates.getProgressPercent() != null) existing.setProgressPercent(updates.getProgressPercent());

        // Save
        ActivityEntity updated = activityPort.save(existing);

        // Publish event
        publishActivityUpdatedEvent(updated);

        log.info("Activity {} updated successfully", id);
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
    public Pair<List<ActivityEntity>, Long> getActivitiesByType(ActivityType type, Long tenantId, PageRequest pageRequest) {
        pageRequest.validate();
        return activityPort.findByActivityType(type, tenantId, pageRequest);
    }

    @Override
    @Transactional(readOnly = true)
    public Pair<List<ActivityEntity>, Long> getActivitiesByStatus(ActivityStatus status, Long tenantId, PageRequest pageRequest) {
        pageRequest.validate();
        return activityPort.findByStatus(status, tenantId, pageRequest);
    }

    @Override
    @Transactional(readOnly = true)
    public Pair<List<ActivityEntity>, Long> getActivitiesByAssignee(Long userId, Long tenantId, PageRequest pageRequest) {
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
    public Pair<List<ActivityEntity>, Long> getActivitiesByCustomer(Long customerId, Long tenantId, PageRequest pageRequest) {
        pageRequest.validate();
        return activityPort.findByCustomerId(customerId, tenantId, pageRequest);
    }

    @Override
    @Transactional(readOnly = true)
    public Pair<List<ActivityEntity>, Long> getActivitiesByOpportunity(Long opportunityId, Long tenantId, PageRequest pageRequest) {
        pageRequest.validate();
        return activityPort.findByOpportunityId(opportunityId, tenantId, pageRequest);
    }

    @Override
    @Transactional(readOnly = true)
    public Pair<List<ActivityEntity>, Long> getActivitiesByContact(Long contactId, Long tenantId, PageRequest pageRequest) {
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
        // Port only accepts tenantId, so we filter manually
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
        log.info("Completing activity {} for tenant {}", id, tenantId);

        ActivityEntity activity = activityPort.findById(id, tenantId)
                .orElseThrow(() -> new IllegalArgumentException("Activity not found"));

        // Validation: Cannot complete cancelled activities
        if (ActivityStatus.CANCELLED.equals(activity.getStatus())) {
            throw new IllegalStateException("Cannot complete cancelled activities");
        }

        // Update status
        activity.setStatus(ActivityStatus.COMPLETED);
        activity.setProgressPercent(100);

        ActivityEntity completed = activityPort.save(activity);

        // Publish event
        publishActivityCompletedEvent(completed);

        log.info("Activity {} completed successfully", id);
        return completed;
    }

    @Override
    @Transactional
    public ActivityEntity cancelActivity(Long id, Long tenantId) {
        log.info("Cancelling activity {} for tenant {}", id, tenantId);

        ActivityEntity activity = activityPort.findById(id, tenantId)
                .orElseThrow(() -> new IllegalArgumentException("Activity not found"));

        // Validation: Cannot cancel completed activities
        if (activity.isCompleted()) {
            throw new IllegalStateException("Cannot cancel completed activities");
        }

        // Update status
        activity.setStatus(ActivityStatus.CANCELLED);

        ActivityEntity cancelled = activityPort.save(activity);

        // Publish event
        publishActivityCancelledEvent(cancelled);

        log.info("Activity {} cancelled successfully", id);
        return cancelled;
    }

    @Override
    @Transactional
    public void deleteActivity(Long id, Long tenantId) {
        log.info("Deleting activity {} for tenant {}", id, tenantId);

        ActivityEntity activity = activityPort.findById(id, tenantId)
                .orElseThrow(() -> new IllegalArgumentException("Activity not found"));

        activityPort.deleteById(id, tenantId);

        // Publish event
        publishActivityDeletedEvent(activity);

        log.info("Activity {} deleted successfully", id);
    }

    // ========== Event Publishing ==========

    private void publishActivityCreatedEvent(ActivityEntity activity) {
        // TODO: Implement event publishing
        log.debug("Event: Activity created - ID: {}, Topic: {}", activity.getId(), Constants.KafkaTopic.ACTIVITY);
    }

    private void publishActivityUpdatedEvent(ActivityEntity activity) {
        // TODO: Implement event publishing
        log.debug("Event: Activity updated - ID: {}, Topic: {}", activity.getId(), Constants.KafkaTopic.ACTIVITY);
    }

    private void publishActivityCompletedEvent(ActivityEntity activity) {
        // TODO: Implement event publishing
        log.debug("Event: Activity completed - ID: {}, Topic: {}", activity.getId(), Constants.KafkaTopic.ACTIVITY);
    }

    private void publishActivityCancelledEvent(ActivityEntity activity) {
        // TODO: Implement event publishing
        log.debug("Event: Activity cancelled - ID: {}, Topic: {}", activity.getId(), Constants.KafkaTopic.ACTIVITY);
    }

    private void publishActivityDeletedEvent(ActivityEntity activity) {
        // TODO: Implement event publishing
        log.debug("Event: Activity deleted - ID: {}, Topic: {}", activity.getId(), Constants.KafkaTopic.ACTIVITY);
    }
}
