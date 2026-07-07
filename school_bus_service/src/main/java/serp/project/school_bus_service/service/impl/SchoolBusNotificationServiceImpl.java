package serp.project.school_bus_service.service.impl;

import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.kafka.support.SendResult;
import org.springframework.stereotype.Service;
import org.springframework.transaction.support.TransactionSynchronization;
import org.springframework.transaction.support.TransactionSynchronizationManager;
import org.springframework.util.StringUtils;
import serp.project.school_bus_service.dto.message.NotificationBulkCreateMessage;
import serp.project.school_bus_service.dto.message.NotificationCreateMessage;
import serp.project.school_bus_service.dto.message.NotificationEventMessage;
import serp.project.school_bus_service.dto.message.NotificationEventMetadata;
import serp.project.school_bus_service.dto.request.BaseNotificationCommand;
import serp.project.school_bus_service.dto.request.BulkNotificationSendCommand;
import serp.project.school_bus_service.dto.request.NotificationSendCommand;
import serp.project.school_bus_service.enums.NotificationCategory;
import serp.project.school_bus_service.enums.NotificationDeliveryChannel;
import serp.project.school_bus_service.enums.NotificationPriority;
import serp.project.school_bus_service.enums.NotificationType;
import serp.project.school_bus_service.service.ISchoolBusNotificationService;
import serp.project.school_bus_service.shared.exception.AppErrorCode;
import serp.project.school_bus_service.shared.exception.AppException;
import serp.project.school_bus_service.shared.i18n.MessageCommon;
import tools.jackson.databind.ObjectMapper;

import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Map;
import java.util.UUID;
import java.util.concurrent.CompletableFuture;

@Service
@Slf4j
public class SchoolBusNotificationServiceImpl implements ISchoolBusNotificationService {

    private static final String CREATE_EVENT_TYPE = "notification.create.requested";
    private static final String BULK_CREATE_EVENT_TYPE = "notification.bulk_create.requested";
    private static final String EVENT_VERSION = "1.0";
    private static final int MAX_TITLE_LENGTH = 200;
    private static final int MAX_MESSAGE_LENGTH = 1000;

    private final KafkaTemplate<String, String> kafkaTemplate;
    private final ObjectMapper objectMapper;
    private final MessageCommon messageCommon;
    private final String notificationTopic;
    private final String sourceService;

    public SchoolBusNotificationServiceImpl(
            KafkaTemplate<String, String> kafkaTemplate,
            ObjectMapper objectMapper,
            MessageCommon messageCommon,
            @Value("${school-bus.kafka.topics.user-notifications:serp.notification.user.events}")
            String notificationTopic,
            @Value("${school-bus.notification.source-service:school-bus-service}")
            String sourceService) {
        this.kafkaTemplate = kafkaTemplate;
        this.objectMapper = objectMapper;
        this.messageCommon = messageCommon;
        this.notificationTopic = notificationTopic;
        this.sourceService = sourceService;
    }

    @Override
    public String sendNotification(NotificationSendCommand command) {
        validateBaseCommand(command);
        requirePositive(command.getUserId(), "notification.userId.required");

        String eventId = UUID.randomUUID().toString();
        NotificationCreateMessage data = toCreateMessage(command, eventId);
        NotificationEventMessage<NotificationCreateMessage> event =
                createEvent(eventId, CREATE_EVENT_TYPE, data);

        publishAfterCommit(String.valueOf(command.getUserId()), eventId, CREATE_EVENT_TYPE, event);
        return eventId;
    }

    @Override
    public String sendBulkNotification(BulkNotificationSendCommand command) {
        validateBaseCommand(command);
        List<Long> userIds = normalizeUserIds(command.getUserIds());
        if (userIds.isEmpty()) {
            throw validationException("notification.userIds.required");
        }

        String eventId = UUID.randomUUID().toString();
        NotificationBulkCreateMessage data = toBulkCreateMessage(command, userIds);
        NotificationEventMessage<NotificationBulkCreateMessage> event =
                createEvent(eventId, BULK_CREATE_EVENT_TYPE, data);

        publishAfterCommit(String.valueOf(command.getTenantId()), eventId, BULK_CREATE_EVENT_TYPE, event);
        return eventId;
    }

    private void validateBaseCommand(BaseNotificationCommand command) {
        if (command == null) {
            throw validationException("notification.command.required");
        }

        requirePositive(command.getTenantId(), "notification.tenantId.required");
        requireText(command.getTitle(), MAX_TITLE_LENGTH, "notification.title.required", "notification.title.maxLength");
        requireText(command.getMessage(), MAX_MESSAGE_LENGTH, "notification.message.required", "notification.message.maxLength");

    }

    private NotificationCreateMessage toCreateMessage(
            NotificationSendCommand command,
            String eventId) {
        NotificationCreateMessage data = new NotificationCreateMessage();
        data.setUserId(command.getUserId());
        data.setTenantId(command.getTenantId());
        applyCommonData(data, command);
        data.setSourceEventId(StringUtils.hasText(command.getSourceEventId())
                ? command.getSourceEventId().trim()
                : eventId);
        data.setActionType(trimToNull(command.getActionType()));
        data.setEntityType(trimToNull(command.getEntityType()));
        data.setEntityId(command.getEntityId());
        data.setDeliveryChannels(normalizeDeliveryChannels(command.getDeliveryChannels()));
        data.setExpiresAt(command.getExpiresAt());
        return data;
    }

    private NotificationBulkCreateMessage toBulkCreateMessage(
            BulkNotificationSendCommand command,
            List<Long> userIds) {
        NotificationBulkCreateMessage data = new NotificationBulkCreateMessage();
        data.setUserIds(userIds);
        data.setTenantId(command.getTenantId());
        data.setTitle(command.getTitle().trim());
        data.setMessage(command.getMessage().trim());
        data.setType(resolveType(command).name());
        data.setCategory(resolveCategory(command).name());
        data.setPriority(resolvePriority(command).name());
        data.setSourceService(sourceService);
        data.setActionUrl(trimToNull(command.getActionUrl()));
        data.setMetadata(copyMetadata(command.getMetadata()));
        return data;
    }

    private void applyCommonData(
            NotificationCreateMessage data,
            BaseNotificationCommand command) {
        data.setTitle(command.getTitle().trim());
        data.setMessage(command.getMessage().trim());
        data.setType(resolveType(command).name());
        data.setCategory(resolveCategory(command).name());
        data.setPriority(resolvePriority(command).name());
        data.setSourceService(sourceService);
        data.setActionUrl(trimToNull(command.getActionUrl()));
        data.setMetadata(copyMetadata(command.getMetadata()));
    }

    private <T> NotificationEventMessage<T> createEvent(
            String eventId,
            String eventType,
            T data) {
        NotificationEventMetadata metadata = new NotificationEventMetadata();
        metadata.setId(eventId);
        metadata.setType(eventType);
        metadata.setSource(sourceService);
        metadata.setV(EVENT_VERSION);
        metadata.setTs(System.currentTimeMillis());
        metadata.setTraceId(eventId);

        NotificationEventMessage<T> event = new NotificationEventMessage<>();
        event.setMeta(metadata);
        event.setData(data);
        return event;
    }

    private void publishAfterCommit(
            String key,
            String eventId,
            String eventType,
            NotificationEventMessage<?> event) {
        String payload = serialize(event);
        Runnable publishAction = () -> publish(key, eventId, eventType, payload);

        if (TransactionSynchronizationManager.isActualTransactionActive()
                && TransactionSynchronizationManager.isSynchronizationActive()) {
            TransactionSynchronizationManager.registerSynchronization(new TransactionSynchronization() {
                @Override
                public void afterCommit() {
                    publishAction.run();
                }
            });
            log.debug(
                    "Notification event queued until transaction commit: eventId={}, eventType={}, key={}",
                    eventId,
                    eventType,
                    key);
            return;
        }

        publishAction.run();
    }

    private void publish(
            String key,
            String eventId,
            String eventType,
            String payload) {
        try {
            CompletableFuture<SendResult<String, String>> future =
                    kafkaTemplate.send(notificationTopic, key, payload);
            future.whenComplete((result, error) -> {
                if (error != null) {
                    log.error(
                            "Failed to publish notification event: topic={}, key={}, eventId={}, eventType={}",
                            notificationTopic,
                            key,
                            eventId,
                            eventType,
                            error);
                    return;
                }

                log.info(
                        "Published notification event: topic={}, partition={}, offset={}, key={}, eventId={}, eventType={}",
                        result.getRecordMetadata().topic(),
                        result.getRecordMetadata().partition(),
                        result.getRecordMetadata().offset(),
                        key,
                        eventId,
                        eventType);
            });
        } catch (Exception exception) {
            log.error(
                    "Failed to enqueue notification event: topic={}, key={}, eventId={}, eventType={}",
                    notificationTopic,
                    key,
                    eventId,
                    eventType,
                    exception);
        }
    }

    private String serialize(NotificationEventMessage<?> event) {
        try {
            return objectMapper.writeValueAsString(event);
        } catch (Exception exception) {
            log.error("Failed to serialize notification event", exception);
            throw new AppException(
                    AppErrorCode.UNEXPECTED_EXCEPTION,
                    messageCommon.getMessage("notification.serialize.failed"));
        }
    }

    private List<Long> normalizeUserIds(List<Long> userIds) {
        if (userIds == null || userIds.isEmpty()) {
            return List.of();
        }

        LinkedHashSet<Long> uniqueUserIds = new LinkedHashSet<>();
        for (Long userId : userIds) {
            requirePositive(userId, "notification.userId.positive");
            uniqueUserIds.add(userId);
        }
        return new ArrayList<>(uniqueUserIds);
    }

    private List<String> normalizeDeliveryChannels(
            List<NotificationDeliveryChannel> deliveryChannels) {
        List<NotificationDeliveryChannel> channels = deliveryChannels;
        if (channels == null || channels.isEmpty()) {
            channels = List.of(NotificationDeliveryChannel.IN_APP);
        }

        List<String> normalizedChannels = channels.stream()
                .filter(channel -> channel != null)
                .map(Enum::name)
                .distinct()
                .toList();
        return normalizedChannels.isEmpty()
                ? List.of(NotificationDeliveryChannel.IN_APP.name())
                : normalizedChannels;
    }

    private Map<String, Object> copyMetadata(Map<String, Object> metadata) {
        return metadata == null
                ? new LinkedHashMap<>()
                : new LinkedHashMap<>(metadata);
    }

    private NotificationType resolveType(BaseNotificationCommand command) {
        return command.getType() != null ? command.getType() : NotificationType.INFO;
    }

    private NotificationCategory resolveCategory(BaseNotificationCommand command) {
        return command.getCategory() != null
                ? command.getCategory()
                : NotificationCategory.SCHOOL_BUS;
    }

    private NotificationPriority resolvePriority(BaseNotificationCommand command) {
        return command.getPriority() != null
                ? command.getPriority()
                : NotificationPriority.MEDIUM;
    }

    private void requirePositive(Long value, String messageKey) {
        if (value == null || value <= 0) {
            throw validationException(messageKey);
        }
    }

    private void requireText(String value, int maxLength, String requiredKey, String maxLengthKey) {
        if (!StringUtils.hasText(value)) {
            throw validationException(requiredKey);
        }
        if (value.trim().length() > maxLength) {
            throw new AppException(
                    AppErrorCode.REQUEST_VALIDATION_FAILED,
                    messageCommon.getMessage(maxLengthKey, maxLength));
        }
    }

    private String trimToNull(String value) {
        return StringUtils.hasText(value) ? value.trim() : null;
    }

    private AppException validationException(String messageKey) {
        return new AppException(AppErrorCode.REQUEST_VALIDATION_FAILED, messageCommon.getMessage(messageKey));
    }
}
