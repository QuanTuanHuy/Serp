/**
 * Author: QuanTuanHuy
 * Description: Part of Serp Project
 */

package serp.project.account.core.service.impl;

import java.util.List;
import java.util.Map;
import java.util.Objects;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import lombok.extern.slf4j.Slf4j;
import serp.project.account.core.domain.constant.Constants;
import serp.project.account.core.domain.constant.KafkaConstants;
import serp.project.account.core.domain.dto.message.BaseKafkaMessage;
import serp.project.account.core.domain.dto.message.CreateNotificationEvent;
import serp.project.account.core.domain.dto.message.SendEmailRequest;
import serp.project.account.core.domain.entity.OutboxEventEntity;
import serp.project.account.core.service.INotificationService;
import serp.project.account.core.service.IOutboxEventService;
import serp.project.account.kernel.utils.JsonUtils;

@Service
@Slf4j
public class NotificationService implements INotificationService {
    private static final String NOTIFICATION_AGGREGATE_TYPE = "USER_NOTIFICATION";
    private static final String DEFAULT_EMAIL_AGGREGATE_TYPE = "EMAIL_REQUEST";
    private static final long DEFAULT_AGGREGATE_ID = 0L;

    private final IOutboxEventService outboxEventService;
    private final JsonUtils jsonUtils;

    public NotificationService(IOutboxEventService outboxEventService, JsonUtils jsonUtils) {
        this.outboxEventService = outboxEventService;
        this.jsonUtils = jsonUtils;
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public void sendNotification(CreateNotificationEvent event) {
        Objects.requireNonNull(event, "Notification event is required");
        Map<String, Object> eventData = toMap(event);
        Long tenantId = requireLong(eventData.get("tenantId"), "Notification event tenantId is required");
        Long userId = optionalLong(eventData.get("userId"));

        applyDefault(eventData, "type", "INFO");
        applyDefault(eventData, "category", "SYSTEM");
        applyDefault(eventData, "priority", "MEDIUM");
        applyDefault(eventData, "sourceService", Constants.SERVICE_NAME);
        if (!(eventData.get("deliveryChannels") instanceof List<?> deliveryChannels) || deliveryChannels.isEmpty()) {
            eventData.put("deliveryChannels", List.of("IN_APP"));
        }

        Long aggregateId = resolveAggregateId(userId, tenantId);

        BaseKafkaMessage<Map<String, Object>> kafkaMessage = BaseKafkaMessage.of(
                Constants.SERVICE_NAME,
                KafkaConstants.Notification.EVENT_CREATE_REQUESTED,
                tenantId,
                null,
                NOTIFICATION_AGGREGATE_TYPE,
                String.valueOf(aggregateId),
                eventData);

        saveOutboxEvent(
                tenantId,
                NOTIFICATION_AGGREGATE_TYPE,
                aggregateId,
                KafkaConstants.Notification.EVENT_CREATE_REQUESTED,
                KafkaConstants.Notification.USER_NOTIFICATION_TOPIC,
                resolvePartitionKey(userId, tenantId),
                kafkaMessage);

        log.info("Queued notification outbox event for user: {}", userId);
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public void sendEmail(Long actorId, Long tenantId, String aggregateType, Long aggregateId, SendEmailRequest request) {
        Objects.requireNonNull(request, "Email request is required");
        Objects.requireNonNull(tenantId, "Email event tenantId is required");

        String resolvedAggregateType = aggregateType != null && !aggregateType.isBlank()
                ? aggregateType
                : DEFAULT_EMAIL_AGGREGATE_TYPE;
        Long resolvedAggregateId = aggregateId != null ? aggregateId : tenantId;

        BaseKafkaMessage<SendEmailRequest> kafkaMessage = BaseKafkaMessage.of(
                Constants.SERVICE_NAME,
                KafkaConstants.Email.EVENT_SEND_REQUESTED,
                tenantId,
                actorId,
                resolvedAggregateType,
                String.valueOf(resolvedAggregateId),
                request);

        saveOutboxEvent(
                tenantId,
                resolvedAggregateType,
                resolvedAggregateId,
                KafkaConstants.Email.EVENT_SEND_REQUESTED,
                KafkaConstants.Email.EMAIL_SENDING_TOPIC,
                resolvePartitionKey(resolvedAggregateId, tenantId),
                kafkaMessage);

        log.info("Queued email outbox event for actor: {}, aggregateType: {}, aggregateId: {}",
                actorId,
                resolvedAggregateType,
                resolvedAggregateId);
    }

    private <T> void saveOutboxEvent(Long tenantId, String aggregateType, Long aggregateId,
            String eventType, String topic, String partitionKey, BaseKafkaMessage<T> message) {
        outboxEventService.saveEvent(
                OutboxEventEntity.createNew(
                        tenantId,
                        aggregateType,
                        aggregateId,
                        eventType,
                        topic,
                        partitionKey,
                        jsonUtils.toJson(message)));
    }

    private Long resolveAggregateId(Long preferredId, Long fallbackId) {
        if (preferredId != null) {
            return preferredId;
        }
        if (fallbackId != null) {
            return fallbackId;
        }
        return DEFAULT_AGGREGATE_ID;
    }

    private String resolvePartitionKey(Long preferredId, Long fallbackId) {
        if (preferredId != null && preferredId > 0) {
            return preferredId.toString();
        }
        if (fallbackId != null && fallbackId > 0) {
            return fallbackId.toString();
        }
        return String.valueOf(DEFAULT_AGGREGATE_ID);
    }

    private Map<String, Object> toMap(Object value) {
        @SuppressWarnings("unchecked")
        Map<String, Object> map = jsonUtils.fromJson(jsonUtils.toJson(value), Map.class);
        return map;
    }

    private void applyDefault(Map<String, Object> map, String key, String defaultValue) {
        Object value = map.get(key);
        if (!(value instanceof String stringValue) || stringValue.isBlank()) {
            map.put(key, defaultValue);
        }
    }

    private Long requireLong(Object value, String message) {
        Long result = optionalLong(value);
        if (result == null) {
            throw new NullPointerException(message);
        }
        return result;
    }

    private Long optionalLong(Object value) {
        if (value == null) {
            return null;
        }
        if (value instanceof Number number) {
            return number.longValue();
        }
        if (value instanceof String stringValue && !stringValue.isBlank()) {
            return Long.parseLong(stringValue.trim());
        }
        return null;
    }
}
