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
import serp.project.crm.core.domain.constant.Constants;
import serp.project.crm.core.domain.dto.PageRequest;
import serp.project.crm.core.domain.entity.ActivityEntity;
import serp.project.crm.core.domain.enums.ActivityStatus;
import serp.project.crm.core.domain.enums.ActivityType;
import serp.project.crm.core.port.client.IKafkaPublisher;
import serp.project.crm.core.port.store.IActivityPort;
import serp.project.crm.core.service.IActivityService;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

@Service
@RequiredArgsConstructor
@Slf4j
public class ActivityService implements IActivityService {

    private final IActivityPort activityPort;
    private final IKafkaPublisher kafkaPublisher;

    @Override
    @Transactional
    public ActivityEntity createActivity(ActivityEntity activity, Long tenantId) {
        Long now = System.currentTimeMillis();
        if (activity.getDueDate() != null && activity.getDueDate() < now) {
            throw new IllegalArgumentException("Due date cannot be in the past");
        }

        activity.setTenantId(tenantId);
        activity.setDefaults();

        ActivityEntity saved = activityPort.save(activity);

        publishActivityCreatedEvent(saved);

        return saved;
    }

    @Override
    @Transactional
    public ActivityEntity updateActivity(Long id, ActivityEntity updates, Long tenantId) {

        ActivityEntity existing = activityPort.findById(id, tenantId)
                .orElseThrow(() -> new IllegalArgumentException("Activity not found"));

        existing.updateFrom(updates);

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
                .orElseThrow(() -> new IllegalArgumentException("Activity not found"));

        activity.markAsCompleted(tenantId);

        ActivityEntity completed = activityPort.save(activity);

        publishActivityCompletedEvent(completed);

        return completed;
    }

    @Override
    @Transactional
    public ActivityEntity cancelActivity(Long id, Long tenantId) {
        ActivityEntity activity = activityPort.findById(id, tenantId)
                .orElseThrow(() -> new IllegalArgumentException("Activity not found"));

        activity.markAsCancelled(tenantId);

        ActivityEntity cancelled = activityPort.save(activity);

        publishActivityCancelledEvent(cancelled);

        return cancelled;
    }

    @Override
    @Transactional
    public void deleteActivity(Long id, Long tenantId) {
        ActivityEntity activity = activityPort.findById(id, tenantId)
                .orElseThrow(() -> new IllegalArgumentException("Activity not found"));

        activityPort.deleteById(id, tenantId);

        publishActivityDeletedEvent(activity);

    }

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
