/**
 * Author: QuanTuanHuy
 * Description: Part of Serp Project
 */

package serp.project.pmcore.domain.notification.service.impl;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import serp.project.pmcore.domain.notification.dto.WorkItemStatusChangeNotificationContext;
import serp.project.pmcore.domain.notification.entity.NotificationEventEntity;
import serp.project.pmcore.domain.notification.entity.NotificationSchemeEntity;
import serp.project.pmcore.domain.notification.entity.NotificationSchemeEntryEntity;
import serp.project.pmcore.domain.notification.port.INotificationEventPort;
import serp.project.pmcore.domain.notification.port.INotificationSchemeEntryPort;
import serp.project.pmcore.domain.notification.port.INotificationSchemePort;
import serp.project.pmcore.domain.notification.service.IWorkItemNotificationOutboxPublisher;
import serp.project.pmcore.domain.project.entity.ProjectComponentEntity;
import serp.project.pmcore.domain.project.entity.ProjectEntity;
import serp.project.pmcore.domain.project.entity.ProjectRoleActorEntity;
import serp.project.pmcore.domain.project.entity.ProjectRoleEntity;
import serp.project.pmcore.domain.project.service.IProjectRoleActorService;
import serp.project.pmcore.domain.project.service.IProjectRoleService;
import serp.project.pmcore.domain.shared.constant.EventConstants;
import serp.project.pmcore.domain.shared.constant.NotificationKafkaConstants;
import serp.project.pmcore.domain.shared.dto.message.BaseKafkaMessage;
import serp.project.pmcore.domain.shared.dto.message.NotificationCreateRequest;
import serp.project.pmcore.domain.shared.entity.OutboxEventEntity;
import serp.project.pmcore.domain.shared.enums.OutboxEventStatus;
import serp.project.pmcore.domain.shared.service.IOutboxEventService;
import serp.project.pmcore.domain.workitem.entity.WorkItemEntity;
import serp.project.pmcore.domain.workitem.port.read.IWorkItemReadPort;
import serp.project.pmcore.kernel.utils.JsonUtils;

import java.util.Collection;
import java.util.Collections;
import java.util.LinkedHashMap;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.Optional;

@Service
@RequiredArgsConstructor
@Slf4j
public class WorkItemNotificationOutboxPublisher implements IWorkItemNotificationOutboxPublisher {

    private final INotificationSchemePort notificationSchemePort;
    private final INotificationSchemeEntryPort notificationSchemeEntryPort;
    private final INotificationEventPort notificationEventPort;
    private final IProjectRoleService projectRoleService;
    private final IProjectRoleActorService projectRoleActorService;
    private final IWorkItemReadPort workItemReadPort;
    private final IOutboxEventService outboxEventService;
    private final JsonUtils jsonUtils;

    public void publishWorkItemCreatedNotifications(ProjectEntity project,
                                                     WorkItemEntity workItem,
                                                     Long tenantId,
                                                     Long actorId,
                                                     Long sourceEventId) {
        publishNotifications(project, workItem, tenantId, actorId, sourceEventId,
                NotificationKafkaConstants.WORK_ITEM_CREATED_EVENT_KEY,
                "Created",
                Collections.emptyMap());
    }

    public void publishWorkItemAssignedNotifications(ProjectEntity project,
                                                     WorkItemEntity workItem,
                                                     Long tenantId,
                                                     Long actorId,
                                                     Long sourceEventId) {
        publishNotifications(project, workItem, tenantId, actorId, sourceEventId,
                NotificationKafkaConstants.WORK_ITEM_ASSIGNED_EVENT_KEY,
                "Assigned",
                Collections.emptyMap());
    }

    public void publishWorkItemStatusChangedNotifications(ProjectEntity project,
                                                          WorkItemEntity workItem,
                                                          Long tenantId,
                                                          Long actorId,
                                                          Long sourceEventId,
                                                          WorkItemStatusChangeNotificationContext context) {
        publishNotifications(project, workItem, tenantId, actorId, sourceEventId,
                context == null ? null : resolveStatusChangeNotificationEventKey(context),
                "Status changed",
                context == null ? Collections.emptyMap() : buildStatusChangeNotificationMetadata(context, actorId));
    }

    private void publishNotifications(ProjectEntity project,
                                       WorkItemEntity workItem,
                                       Long tenantId,
                                       Long actorId,
                                       Long sourceEventId,
                                       String notificationEventKey,
                                       String actionLabel,
                                       Map<String, Object> extraMetadata) {
        if (project == null || workItem == null || tenantId == null || sourceEventId == null) {
            return;
        }

        Long notificationSchemeId = project.getNotificationSchemeId();
        if (notificationSchemeId == null) {
            return;
        }

        Optional<NotificationSchemeEntity> schemeOpt = notificationSchemePort
                .getNotificationSchemeByIdIncludingSystem(notificationSchemeId, tenantId);
        Optional<NotificationEventEntity> eventOpt = notificationEventPort
                .getNotificationEventByEventKeyIncludingSystem(notificationEventKey, tenantId);
        if (schemeOpt.isEmpty() || eventOpt.isEmpty()) {
            return;
        }

        NotificationSchemeEntity scheme = schemeOpt.get();
        NotificationEventEntity event = eventOpt.get();
        List<NotificationSchemeEntryEntity> entries = notificationSchemeEntryPort
                .getNotificationSchemeEntriesBySchemeIdIncludingSystem(scheme.getId(), tenantId)
                .stream()
                .filter(entry -> Boolean.TRUE.equals(entry.getIsEnabled()))
                .filter(entry -> event.getId().equals(entry.getEventId()))
                .toList();
        if (entries.isEmpty()) {
            return;
        }

        LinkedHashSet<Long> recipientIds = new LinkedHashSet<>();
        for (NotificationSchemeEntryEntity entry : entries) {
            recipientIds.addAll(resolveRecipients(entry, project, workItem, tenantId, actorId));
        }

        if (recipientIds.isEmpty()) {
            return;
        }

        for (Long recipientId : recipientIds) {
            NotificationCreateRequest request = buildRequest(
                    project,
                    workItem,
                    tenantId,
                    actorId,
                    sourceEventId,
                    recipientId,
                    notificationEventKey,
                    actionLabel,
                    extraMetadata
            );
            BaseKafkaMessage<NotificationCreateRequest> message = BaseKafkaMessage.of(
                    NotificationKafkaConstants.SOURCE,
                    NotificationKafkaConstants.EVENT_NOTIFICATION_CREATE_REQUESTED,
                    tenantId,
                    actorId,
                    EventConstants.WorkItem.AGGREGATE,
                    String.valueOf(workItem.getId()),
                    request
            );
            OutboxEventEntity outboxEvent = OutboxEventEntity.builder()
                    .tenantId(tenantId)
                    .aggregateType(EventConstants.WorkItem.AGGREGATE)
                    .aggregateId(workItem.getId())
                    .eventType(NotificationKafkaConstants.EVENT_NOTIFICATION_CREATE_REQUESTED)
                    .topic(NotificationKafkaConstants.TOPIC)
                    .partitionKey(String.valueOf(recipientId))
                    .payload(jsonUtils.toJson(message))
                    .status(OutboxEventStatus.PENDING)
                    .createdAt(System.currentTimeMillis())
                    .updatedAt(System.currentTimeMillis())
                    .build();
            outboxEventService.saveEvent(outboxEvent);
        }
    }

    private NotificationCreateRequest buildRequest(ProjectEntity project,
                                                   WorkItemEntity workItem,
                                                   Long tenantId,
                                                   Long actorId,
                                                   Long sourceEventId,
                                                   Long recipientId,
                                                   String notificationEventKey,
                                                   String actionLabel,
                                                   Map<String, Object> extraMetadata) {
        Map<String, Object> metadata = new LinkedHashMap<>();
        metadata.put("notificationEventKey", notificationEventKey);
        metadata.put("recipientUserId", recipientId);
        metadata.put("projectId", project.getId());
        metadata.put("projectKey", project.getKey());
        metadata.put("projectName", project.getName());
        metadata.put("workItemId", workItem.getId());
        metadata.put("workItemKey", workItem.getKey());
        metadata.put("workItemSummary", workItem.getSummary());
        metadata.put("sourceEventId", String.valueOf(sourceEventId));
        if (actorId != null) {
            metadata.put("actorId", actorId);
        }
        if (workItem.getAssigneeId() != null) {
            metadata.put("assigneeId", workItem.getAssigneeId());
        }
        if (workItem.getReporterId() != null) {
            metadata.put("reporterId", workItem.getReporterId());
        }
        if (extraMetadata != null && !extraMetadata.isEmpty()) {
            metadata.putAll(extraMetadata);
        }

        return new NotificationCreateRequest(
                recipientId,
                tenantId,
                buildTitle(workItem, actionLabel),
                buildMessage(workItem, actionLabel),
                NotificationKafkaConstants.DEFAULT_TYPE,
                NotificationKafkaConstants.DEFAULT_CATEGORY,
                NotificationKafkaConstants.DEFAULT_PRIORITY,
                NotificationKafkaConstants.SOURCE,
                String.valueOf(sourceEventId),
                null,
                null,
                "WORK_ITEM",
                workItem.getId(),
                List.of(NotificationKafkaConstants.DEFAULT_DELIVERY_CHANNEL),
                null,
                metadata
        );
    }

    private String buildTitle(WorkItemEntity workItem, String actionLabel) {
        String workItemKey = workItem.getKey() == null ? String.valueOf(workItem.getId()) : workItem.getKey();
        return workItemKey + " " + actionLabel;
    }

    private String buildMessage(WorkItemEntity workItem, String actionLabel) {
        String summary = workItem.getSummary();
        if (summary == null || summary.isBlank()) {
            return actionLabel + " work item";
        }
        return actionLabel + ": " + summary;
    }

    private String resolveStatusChangeNotificationEventKey(WorkItemStatusChangeNotificationContext context) {
        String transitionName = context.transitionName() == null ? "" : context.transitionName();
        String targetStatusKey = context.targetStatusKey() == null ? "" : context.targetStatusKey();
        String targetCategoryKey = context.targetStatusCategoryKey() == null ? "" : context.targetStatusCategoryKey();
        String normalized = (transitionName + " " + targetStatusKey).toLowerCase(Locale.ROOT);
        if (normalized.contains("reopen")) {
            return NotificationKafkaConstants.WORK_ITEM_REOPENED_EVENT_KEY;
        }
        if (normalized.contains("close")) {
            return NotificationKafkaConstants.WORK_ITEM_CLOSED_EVENT_KEY;
        }
        if (normalized.contains("resolve") || normalized.contains("complete") || "done".equalsIgnoreCase(targetCategoryKey)) {
            return NotificationKafkaConstants.WORK_ITEM_RESOLVED_EVENT_KEY;
        }
        return NotificationKafkaConstants.WORK_ITEM_UPDATED_EVENT_KEY;
    }

    private Map<String, Object> buildStatusChangeNotificationMetadata(WorkItemStatusChangeNotificationContext context,
                                                                      Long userId) {
        Map<String, Object> metadata = new LinkedHashMap<>();
        if (context.transitionId() != null) {
            metadata.put("transitionId", context.transitionId());
        }
        if (context.transitionName() != null) {
            metadata.put("transitionName", context.transitionName());
        }
        if (context.currentStepId() != null) {
            metadata.put("fromStepId", context.currentStepId());
        }
        if (context.targetStepId() != null) {
            metadata.put("toStepId", context.targetStepId());
        }
        if (context.targetStatusId() != null) {
            metadata.put("targetStatusId", context.targetStatusId());
        }
        if (context.targetStatusKey() != null) {
            metadata.put("targetStatusKey", context.targetStatusKey());
        }
        if (context.targetStatusName() != null) {
            metadata.put("targetStatusName", context.targetStatusName());
        }
        if (context.targetStatusCategoryKey() != null) {
            metadata.put("targetStatusCategoryKey", context.targetStatusCategoryKey());
        }
        if (context.targetStatusCategoryName() != null) {
            metadata.put("targetStatusCategoryName", context.targetStatusCategoryName());
        }
        if (context.resolutionId() != null) {
            metadata.put("resolutionId", context.resolutionId());
        }
        metadata.put("transitionedBy", userId);
        return metadata;
    }

    private Collection<Long> resolveRecipients(NotificationSchemeEntryEntity entry,
                                                ProjectEntity project,
                                                WorkItemEntity workItem,
                                                Long tenantId,
                                                Long actorId) {
        String recipientType = normalize(entry.getRecipientType());
        return switch (recipientType) {
            case "ASSIGNEE" -> toRecipientSet(workItem.getAssigneeId());
            case "REPORTER" -> toRecipientSet(workItem.getReporterId());
            case "PROJECT_LEAD" -> toRecipientSet(project.getLeadUserId());
            case "CURRENT_USER" -> toRecipientSet(actorId);
            case "USER" -> parseUserRecipient(entry.getRecipientRef());
            case "COMPONENT_LEAD" -> resolveComponentLeads(workItem, tenantId);
            case "PROJECT_ROLE" -> resolveProjectRoleRecipients(project, tenantId, entry.getRecipientRef());
            case "WATCHERS", "GROUP", "USER_CUSTOM_FIELD_VALUE", "GROUP_CUSTOM_FIELD_VALUE" -> {
                log.debug("Skipping unsupported notification recipient type: type={}, eventId={}, workItemId={}",
                        recipientType, entry.getEventId(), workItem.getId());
                yield List.of();
            }
            default -> {
                log.debug("Skipping unknown notification recipient type: type={}, eventId={}, workItemId={}",
                        recipientType, entry.getEventId(), workItem.getId());
                yield List.of();
            }
        };
    }

    private Collection<Long> resolveProjectRoleRecipients(ProjectEntity project,
                                                           Long tenantId,
                                                           String roleName) {
        if (roleName == null || roleName.isBlank()) {
            return List.of();
        }

        LinkedHashSet<Long> recipientIds = new LinkedHashSet<>();
        List<ProjectRoleEntity> roles = projectRoleService.getProjectRolesByNameIncludingSystem(roleName, tenantId);
        for (ProjectRoleEntity role : roles) {
            List<ProjectRoleActorEntity> actors = projectRoleActorService.getActorsByProjectAndRole(
                    project.getId(),
                    role.getId(),
                    tenantId
            );
            for (ProjectRoleActorEntity actor : actors) {
                if (!"USER".equalsIgnoreCase(actor.getSubjectType())) {
                    continue;
                }
                parseUserId(actor.getSubjectId()).ifPresent(recipientIds::add);
            }
        }
        return recipientIds;
    }

    private Collection<Long> resolveComponentLeads(WorkItemEntity workItem, Long tenantId) {
        List<ProjectComponentEntity> components = workItemReadPort.getActiveComponentsByWorkItemId(workItem.getId(), tenantId);
        if (components == null || components.isEmpty()) {
            return List.of();
        }
        LinkedHashSet<Long> recipientIds = new LinkedHashSet<>();
        for (ProjectComponentEntity component : components) {
            if (component.getLeadUserId() != null) {
                recipientIds.add(component.getLeadUserId());
            }
        }
        return recipientIds;
    }

    private Collection<Long> parseUserRecipient(String userIdRef) {
        Optional<Long> userId = parseUserId(userIdRef);
        return userId.map(List::of).orElseGet(List::of);
    }

    private Optional<Long> parseUserId(String userIdRef) {
        if (userIdRef == null || userIdRef.isBlank()) {
            return Optional.empty();
        }
        try {
            return Optional.of(Long.parseLong(userIdRef.trim()));
        } catch (NumberFormatException ex) {
            log.warn("Skipping invalid notification recipient user id: value={}", userIdRef);
            return Optional.empty();
        }
    }

    private Collection<Long> toRecipientSet(Long userId) {
        if (userId == null) {
            return List.of();
        }
        return List.of(userId);
    }

    private String normalize(String value) {
        if (value == null) {
            return "";
        }
        return value.trim().toUpperCase(Locale.ROOT);
    }
}
