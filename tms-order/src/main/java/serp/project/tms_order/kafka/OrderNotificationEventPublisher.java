/*
Author: Nguyen The Anh
Description: Part of Serp Project
*/

package serp.project.tms_order.kafka;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;
import serp.project.tms_order.domain.Order;
import serp.project.tms_order.kafka.event.NotificationCreateRequestedEvent;
import serp.project.tms_order.service.OutboxEventService;

import java.time.Instant;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.UUID;

@Slf4j
@Component
@RequiredArgsConstructor
public class OrderNotificationEventPublisher {
    private static final String AGGREGATE_TYPE_TMS_ORDER = "TMS_ORDER";
    private static final String SOURCE_SERVICE = "tms-order";
    private static final String EVENT_TYPE_NOTIFICATION_CREATE_REQUESTED = "notification.create.requested";
    private static final String EVENT_VERSION = "1";
    private static final String CATEGORY_TMS = "TMS";
    private static final String TYPE_SUCCESS = "SUCCESS";
    private static final String TYPE_WARNING = "WARNING";
    private static final String PRIORITY_MEDIUM = "MEDIUM";
    private static final String PRIORITY_HIGH = "HIGH";
    private static final String DELIVERY_CHANNEL_IN_APP = "IN_APP";
    private static final String ACTION_VIEW_ORDER = "VIEW_ORDER";
    private static final String ENTITY_TYPE_TMS_ORDER = "TMS_ORDER";
    private static final String ORDER_ACTION_URL = "/first-mile/orders";

    private final ObjectMapper objectMapper;
    private final OutboxEventService outboxEventService;

    @Value("${app.kafka.topics.user-notification:serp.notification.user.events}")
    private String userNotificationTopic;

    public void publishOrderConfirmed(Order order) {
        publishForOrderOwner(
                order,
                "order-confirmed",
                "Order confirmed",
                String.format("Order %s has been confirmed.", resolveOrderCode(order)),
                TYPE_SUCCESS,
                PRIORITY_MEDIUM
        );
    }

    public void publishOrderPaymentSucceeded(Order order) {
        publishForOrderOwner(
                order,
                "payment-succeeded",
                "Payment successful",
                String.format("Shipping fee payment for order %s was successful.", resolveOrderCode(order)),
                TYPE_SUCCESS,
                PRIORITY_MEDIUM
        );
    }

    public void publishOrderCancelled(Order order) {
        publishForOrderOwner(
                order,
                "order-cancelled",
                "Order cancelled",
                String.format("Order %s has been cancelled.", resolveOrderCode(order)),
                TYPE_WARNING,
                PRIORITY_HIGH
        );
    }

    private void publishForOrderOwner(
            Order order,
            String eventName,
            String title,
            String message,
            String notificationType,
            String priority
    ) {
        if (order == null) {
            log.warn("Skip publishing TMS notification because order is null eventName={}", eventName);
            return;
        }

        Optional<Long> userId = resolveOwnerUserId(order);
        if (userId.isEmpty()) {
            return;
        }

        if (order.getTenantId() == null) {
            log.warn("Skip publishing TMS notification because tenantId is null orderCode={} eventName={}",
                    order.getOrderCode(), eventName);
            return;
        }

        String eventId = buildEventId(order, eventName);
        NotificationCreateRequestedEvent event = NotificationCreateRequestedEvent.builder()
                .meta(NotificationCreateRequestedEvent.NotificationEventMetadata.builder()
                        .eventId(eventId)
                        .eventType(EVENT_TYPE_NOTIFICATION_CREATE_REQUESTED)
                        .source(SOURCE_SERVICE)
                        .version(EVENT_VERSION)
                        .timestamp(Instant.now().toEpochMilli())
                        .traceId(UUID.randomUUID().toString())
                        .build())
                .data(NotificationCreateRequestedEvent.NotificationCreateRequestedData.builder()
                        .userId(userId.get())
                        .tenantId(order.getTenantId())
                        .title(title)
                        .message(message)
                        .type(notificationType)
                        .category(CATEGORY_TMS)
                        .priority(priority)
                        .sourceService(SOURCE_SERVICE)
                        .sourceEventId(eventId)
                        .actionUrl(ORDER_ACTION_URL)
                        .actionType(ACTION_VIEW_ORDER)
                        .entityType(ENTITY_TYPE_TMS_ORDER)
                        .entityId(order.getId())
                        .deliveryChannels(List.of(DELIVERY_CHANNEL_IN_APP))
                        .metadata(buildMetadata(order, eventName))
                        .build())
                .build();

        publish(event, order, resolveOrderCode(order));
    }

    private void publish(NotificationCreateRequestedEvent event, Order order, String key) {
        try {
            String payload = objectMapper.writeValueAsString(event);
            outboxEventService.enqueue(
                    AGGREGATE_TYPE_TMS_ORDER,
                    order.getId() == null ? key : String.valueOf(order.getId()),
                    EVENT_TYPE_NOTIFICATION_CREATE_REQUESTED,
                    userNotificationTopic,
                    key,
                    payload,
                    order.getTenantId()
            );
            log.info("Enqueued TMS notification outbox eventId={} topic={}",
                    event.getMeta().getEventId(), userNotificationTopic);
        } catch (JsonProcessingException exception) {
            log.error("Failed to serialize TMS notification eventId={}",
                    event.getMeta().getEventId(), exception);
        }
    }

    private Optional<Long> resolveOwnerUserId(Order order) {
        String createdBy = order.getCreatedBy();
        if (createdBy == null || createdBy.trim().isEmpty()) {
            log.warn("Skip publishing TMS notification because createdBy is empty orderCode={}", order.getOrderCode());
            return Optional.empty();
        }

        try {
            return Optional.of(Long.valueOf(createdBy.trim()));
        } catch (NumberFormatException exception) {
            log.warn("Skip publishing TMS notification because createdBy is not a user id orderCode={} createdBy={}",
                    order.getOrderCode(), createdBy);
            return Optional.empty();
        }
    }

    private String buildEventId(Order order, String eventName) {
        String orderIdentity = order.getId() == null ? resolveOrderCode(order) : String.valueOf(order.getId());
        return SOURCE_SERVICE + "." + eventName + "." + orderIdentity;
    }

    private String resolveOrderCode(Order order) {
        if (order == null || order.getOrderCode() == null || order.getOrderCode().trim().isEmpty()) {
            return "unknown";
        }
        return order.getOrderCode().trim();
    }

    private Map<String, Object> buildMetadata(Order order, String eventName) {
        Map<String, Object> metadata = new HashMap<>();
        metadata.put("eventName", eventName);
        putIfNotNull(metadata, "orderId", order.getId());
        putIfNotNull(metadata, "orderCode", order.getOrderCode());
        putIfNotNull(metadata, "customerOrderCode", order.getCustomerOrderCode());
        putIfNotNull(metadata, "status", order.getStatus());
        putIfNotNull(metadata, "paymentStatus", order.getPaymentStatus());
        putIfNotNull(metadata, "cancelReason", order.getCancelReason());
        return metadata;
    }

    private void putIfNotNull(Map<String, Object> metadata, String key, Object value) {
        if (value != null) {
            metadata.put(key, value);
        }
    }
}
