/**
 * Author: QuanTuanHuy
 * Description: Part of Serp Project
 */

package serp.project.crm.core.service.impl;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.util.StringUtils;
import serp.project.crm.core.domain.constant.Constants;
import serp.project.crm.core.domain.dto.message.NotificationCreateRequestData;
import serp.project.crm.core.domain.dto.message.NotificationKafkaMessage;
import serp.project.crm.core.domain.entity.ActivityEntity;
import serp.project.crm.core.domain.entity.LeadEntity;
import serp.project.crm.core.domain.entity.MeetingRequestEntity;
import serp.project.crm.core.domain.entity.OpportunityEntity;
import serp.project.crm.core.domain.enums.ActivityType;
import serp.project.crm.core.port.client.IKafkaPublisher;
import serp.project.crm.core.service.INotificationPublisher;

import java.util.List;
import java.util.HashMap;
import java.util.Map;
import java.util.Objects;
import java.util.UUID;

@Service
@RequiredArgsConstructor
@Slf4j
public class NotificationPublisher implements INotificationPublisher {

    private static final String VERSION = "1";
    private static final String CATEGORY_MEETING = "CRM_MEETING";
    private static final String CATEGORY_LEAD = "CRM_LEAD";
    private static final String CATEGORY_OPPORTUNITY = "CRM_OPPORTUNITY";
    private static final String TYPE_INFO = "INFO";
    private static final String TYPE_WARNING = "WARNING";
    private static final String PRIORITY_MEDIUM = "MEDIUM";
    private static final String PRIORITY_HIGH = "HIGH";
    private static final String ACTION_TYPE_VIEW = "VIEW";
    private static final String ENTITY_TYPE_ACTIVITY = "ACTIVITY";
    private static final String ENTITY_TYPE_MEETING_REQUEST = "MEETING_REQUEST";
    private static final String ENTITY_TYPE_LEAD = "LEAD";
    private static final String ENTITY_TYPE_OPPORTUNITY = "OPPORTUNITY";
    private static final String ACTIVITY_ACTION_URL_PREFIX = "/crm/activities/";
    private static final String MEETING_REQUEST_ACTION_URL_PREFIX = "/crm/meeting-requests/";
    private static final String LEAD_ACTION_URL_PREFIX = "/crm/leads/";
    private static final String OPPORTUNITY_ACTION_URL_PREFIX = "/crm/opportunities/";

    private final IKafkaPublisher kafkaPublisher;

    @Override
    public void publishMeetingAssigned(ActivityEntity activity, Long tenantId) {
        if (!isMeetingWithAssignee(activity)) {
            return;
        }

        publishToUser(activity.getAssignedTo(), tenantId,
                titleForActivity(activity, "New meeting assigned"),
                messageForActivity(activity, "You have new meeting assignment"),
                TYPE_INFO,
                PRIORITY_HIGH,
                activityActionUrl(activity.getId()),
                ENTITY_TYPE_ACTIVITY,
                activity.getId(),
                CATEGORY_MEETING,
                Map.of(
                        "event", "meeting_assigned",
                        "activityId", activity.getId(),
                        "activityDate", activity.getActivityDate(),
                        "accountId", activity.getAccountId()));
    }

    @Override
    public void publishMeetingUpdated(ActivityEntity activity, Long tenantId) {
        if (!isMeetingWithAssignee(activity)) {
            return;
        }

        publishToUser(activity.getAssignedTo(), tenantId,
                titleForActivity(activity, "Meeting updated"),
                messageForActivity(activity, "Your meeting schedule was updated"),
                TYPE_INFO,
                PRIORITY_MEDIUM,
                activityActionUrl(activity.getId()),
                ENTITY_TYPE_ACTIVITY,
                activity.getId(),
                CATEGORY_MEETING,
                metadataOf(
                        "event", "meeting_updated",
                        "activityId", activity.getId(),
                        "activityDate", activity.getActivityDate(),
                        "status", activity.getStatus() != null ? activity.getStatus().name() : null));
    }

    @Override
    public void publishMeetingCompleted(ActivityEntity activity, Long tenantId) {
        if (!isMeetingWithAssignee(activity)) {
            return;
        }

        publishToUser(activity.getAssignedTo(), tenantId,
                titleForActivity(activity, "Meeting completed"),
                messageForActivity(activity, "Meeting marked as completed"),
                TYPE_INFO,
                PRIORITY_MEDIUM,
                activityActionUrl(activity.getId()),
                ENTITY_TYPE_ACTIVITY,
                activity.getId(),
                CATEGORY_MEETING,
                metadataOf(
                        "event", "meeting_completed",
                        "activityId", activity.getId(),
                        "status", activity.getStatus() != null ? activity.getStatus().name() : null));
    }

    @Override
    public void publishMeetingCancelled(ActivityEntity activity, Long tenantId) {
        if (!isMeetingWithAssignee(activity)) {
            return;
        }

        publishToUser(activity.getAssignedTo(), tenantId,
                titleForActivity(activity, "Meeting cancelled"),
                messageForActivity(activity, "Meeting was cancelled"),
                TYPE_WARNING,
                PRIORITY_HIGH,
                activityActionUrl(activity.getId()),
                ENTITY_TYPE_ACTIVITY,
                activity.getId(),
                CATEGORY_MEETING,
                metadataOf(
                        "event", "meeting_cancelled",
                        "activityId", activity.getId(),
                        "status", activity.getStatus() != null ? activity.getStatus().name() : null));
    }

    @Override
    public void publishMeetingRequestScheduled(MeetingRequestEntity request, Long tenantId) {
        if (request == null || request.getAssignedUserId() == null) {
            return;
        }

        publishToUser(request.getAssignedUserId(), tenantId,
                titleForRequest(request, "Meeting request scheduled"),
                messageForRequest(request, "Meeting request has been scheduled for you"),
                TYPE_INFO,
                PRIORITY_HIGH,
                activityActionUrl(request.getScheduledActivityId()),
                ENTITY_TYPE_ACTIVITY,
                request.getScheduledActivityId() != null ? request.getScheduledActivityId() : request.getId(),
                CATEGORY_MEETING,
                metadataOf(
                        "event", "meeting_request_scheduled",
                        "meetingRequestId", request.getId(),
                        "scheduledActivityId", request.getScheduledActivityId(),
                        "scheduledStartTime", request.getScheduledStartTime()));
    }

    @Override
    public void publishMeetingRequestFailed(MeetingRequestEntity request, Long tenantId) {
        if (request == null || request.getCreatedBy() == null) {
            return;
        }

        publishToUser(request.getCreatedBy(), tenantId,
                titleForRequest(request, "Meeting request failed"),
                messageForRequest(request, "Meeting request could not be scheduled"),
                TYPE_WARNING,
                PRIORITY_HIGH,
                meetingRequestActionUrl(request.getId()),
                ENTITY_TYPE_MEETING_REQUEST,
                request.getId(),
                CATEGORY_MEETING,
                metadataOf(
                        "event", "meeting_request_failed",
                        "meetingRequestId", request.getId(),
                        "failureReason", request.getFailureReason(),
                        "attempts", request.getSchedulingAttempts()));
    }

    @Override
    public void publishLeadAssigned(LeadEntity lead, Long tenantId, Long previousAssignedTo) {
        if (lead == null || tenantId == null || lead.getAssignedTo() == null) {
            return;
        }
        if (Objects.equals(previousAssignedTo, lead.getAssignedTo())) {
            return;
        }

        Map<String, Object> metadata = metadataOf(
                "event", "lead_assigned",
                "leadId", lead.getId(),
                "assignedBy", lead.getUpdatedBy());
        if (previousAssignedTo != null) {
            metadata.put("previousAssignee", previousAssignedTo);
        }

        publishToUser(lead.getAssignedTo(), tenantId,
                titleForLead(lead, "New lead assigned"),
                "A lead has been assigned to you",
                TYPE_INFO,
                PRIORITY_HIGH,
                leadActionUrl(lead.getId()),
                ENTITY_TYPE_LEAD,
                lead.getId(),
                CATEGORY_LEAD,
                metadata);
    }

    @Override
    public void publishOpportunityAssigned(OpportunityEntity opportunity, Long tenantId, Long previousAssignedTo) {
        if (opportunity == null || tenantId == null || opportunity.getAssignedTo() == null) {
            return;
        }
        if (Objects.equals(previousAssignedTo, opportunity.getAssignedTo())) {
            return;
        }

        Map<String, Object> metadata = metadataOf(
                "event", "opportunity_assigned",
                "opportunityId", opportunity.getId(),
                "accountId", opportunity.getAccountId(),
                "assignedBy", opportunity.getUpdatedBy());
        if (previousAssignedTo != null) {
            metadata.put("previousAssignee", previousAssignedTo);
        }

        publishToUser(opportunity.getAssignedTo(), tenantId,
                titleForOpportunity(opportunity, "New opportunity assigned"),
                "An opportunity has been assigned to you",
                TYPE_INFO,
                PRIORITY_HIGH,
                opportunityActionUrl(opportunity.getId()),
                ENTITY_TYPE_OPPORTUNITY,
                opportunity.getId(),
                CATEGORY_OPPORTUNITY,
                metadata);
    }

    private boolean isMeetingWithAssignee(ActivityEntity activity) {
        return activity != null
                && ActivityType.MEETING.equals(activity.getActivityType())
                && activity.getAssignedTo() != null;
    }

    private void publishToUser(Long userId, Long tenantId, String title, String message, String type,
            String priority, String actionUrl, String entityType, Long entityId, String category,
            Map<String, Object> metadata) {
        if (userId == null || tenantId == null) {
            return;
        }

        String eventId = UUID.randomUUID().toString();
        NotificationCreateRequestData data = NotificationCreateRequestData.builder()
                .userId(userId)
                .tenantId(tenantId)
                .title(title)
                .message(message)
                .type(type)
                .category(category)
                .priority(priority)
                .sourceService(Constants.SERVICE_NAME)
                .sourceEventId(eventId)
                .actionUrl(actionUrl)
                .actionType(ACTION_TYPE_VIEW)
                .entityType(entityType)
                .entityId(entityId)
                .deliveryChannels(List.of("IN_APP"))
                .metadata(metadata)
                .build();

        NotificationKafkaMessage payload = NotificationKafkaMessage.builder()
                .meta(NotificationKafkaMessage.NotificationMessageMetadata.builder()
                        .id(eventId)
                        .type(Constants.KafkaCommand.NOTIFICATION_CREATE_REQUESTED)
                        .source(Constants.SERVICE_NAME)
                        .v(VERSION)
                        .ts(System.currentTimeMillis())
                        .traceId(eventId)
                        .build())
                .data(data)
                .build();

        kafkaPublisher.sendMessageAsync(String.valueOf(userId), payload, Constants.KafkaTopic.USER_NOTIFICATION);
        log.debug("Notification event published: userId={}, tenantId={}, title={}, topic={}",
                userId, tenantId, title, Constants.KafkaTopic.USER_NOTIFICATION);
    }

    private String titleForActivity(ActivityEntity activity, String fallback) {
        return StringUtils.hasText(activity.getSubject()) ? activity.getSubject() : fallback;
    }

    private String titleForRequest(MeetingRequestEntity request, String fallback) {
        return StringUtils.hasText(request.getSubject()) ? request.getSubject() : fallback;
    }

    private String messageForActivity(ActivityEntity activity, String fallback) {
        if (StringUtils.hasText(activity.getDescription())) {
            return activity.getDescription();
        }
        return fallback;
    }

    private String messageForRequest(MeetingRequestEntity request, String fallback) {
        if (StringUtils.hasText(request.getDescription())) {
            return request.getDescription();
        }
        return fallback;
    }

    private Map<String, Object> metadataOf(Object... pairs) {
        Map<String, Object> metadata = new HashMap<>();
        for (int i = 0; i + 1 < pairs.length; i += 2) {
            Object key = pairs[i];
            Object value = pairs[i + 1];
            if (key instanceof String stringKey && value != null) {
                metadata.put(stringKey, value);
            }
        }
        return metadata;
    }

    private String activityActionUrl(Long activityId) {
        if (activityId == null) {
            return null;
        }
        return ACTIVITY_ACTION_URL_PREFIX + activityId;
    }

    private String meetingRequestActionUrl(Long meetingRequestId) {
        if (meetingRequestId == null) {
            return null;
        }
        return MEETING_REQUEST_ACTION_URL_PREFIX + meetingRequestId;
    }

    private String titleForLead(LeadEntity lead, String fallback) {
        if (StringUtils.hasText(lead.getCompany())) {
            return lead.getCompany();
        }
        if (StringUtils.hasText(lead.getName())) {
            return lead.getName();
        }
        return fallback;
    }

    private String titleForOpportunity(OpportunityEntity opportunity, String fallback) {
        return StringUtils.hasText(opportunity.getName()) ? opportunity.getName() : fallback;
    }

    private String leadActionUrl(Long leadId) {
        if (leadId == null) {
            return null;
        }
        return LEAD_ACTION_URL_PREFIX + leadId;
    }

    private String opportunityActionUrl(Long opportunityId) {
        if (opportunityId == null) {
            return null;
        }
        return OPPORTUNITY_ACTION_URL_PREFIX + opportunityId;
    }
}
