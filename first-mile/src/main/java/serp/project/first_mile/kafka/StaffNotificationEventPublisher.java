/*
Author: Nguyen The Anh
Description: Part of Serp Project
*/

package serp.project.first_mile.kafka;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;
import serp.project.first_mile.domain.PostOfficeStaff;
import serp.project.first_mile.domain.Trip;
import serp.project.first_mile.service.OutboxEventService;

import java.time.Instant;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.UUID;

@Slf4j
@Component
@RequiredArgsConstructor
public class StaffNotificationEventPublisher {
    private static final String EVENT_TYPE_NOTIFICATION_CREATE_REQUESTED = "notification.create.requested";
    private static final String SOURCE_SERVICE = "first-mile";
    private static final String EVENT_VERSION = "1";
    private static final String CATEGORY_TMS = "TMS";
    private static final String TYPE_INFO = "INFO";
    private static final String PRIORITY_HIGH = "HIGH";
    private static final String DELIVERY_CHANNEL_IN_APP = "IN_APP";
    private static final String ACTION_VIEW_TRIP = "VIEW_TRIP";
    private static final String ENTITY_TYPE_TRIP = "FIRST_MILE_TRIP";
    private static final String AGGREGATE_TYPE_TRIP = "FIRST_MILE_TRIP";

    private final ObjectMapper objectMapper;
    private final OutboxEventService outboxEventService;

    @Value("${app.kafka.topics.user-notification:serp.notification.user.events}")
    private String userNotificationTopic;

    public void publishPickupTripAssigned(PostOfficeStaff courier, Trip trip, Long tenantId) {
        publishTripAssigned(
                courier,
                trip,
                tenantId,
                "pickup-trip-assigned",
                "Bạn có chuyến gom hàng mới",
                "Bạn đã được phân công chuyến gom hàng %s với %d đơn.",
                "/first-mile/pickup"
        );
    }

    public void publishDeliveryTripAssigned(PostOfficeStaff courier, Trip trip, Long tenantId) {
        publishTripAssigned(
                courier,
                trip,
                tenantId,
                "delivery-trip-assigned",
                "Bạn có chuyến giao hàng mới",
                "Bạn đã được phân công chuyến giao hàng %s với %d đơn.",
                "/first-mile/delivery"
        );
    }

    private void publishTripAssigned(
            PostOfficeStaff courier,
            Trip trip,
            Long tenantId,
            String eventName,
            String title,
            String messageTemplate,
            String actionUrl
    ) {
        if (courier == null || courier.getUserId() == null || trip == null || trip.getId() == null) {
            return;
        }

        Long resolvedTenantId = tenantId == null ? trip.getTenantId() : tenantId;
        if (resolvedTenantId == null) {
            log.warn("Skip staff notification because tenantId is null tripId={} staffId={}",
                    trip.getId(), courier.getId());
            return;
        }

        String tripCode = hasText(trip.getTripCode()) ? trip.getTripCode() : String.valueOf(trip.getId());
        String eventId = SOURCE_SERVICE + "." + eventName + "." + trip.getId() + "." + courier.getUserId();
        Map<String, Object> payload = new HashMap<>();
        payload.put("meta", buildMeta(eventId));
        payload.put("data", buildData(
                courier,
                trip,
                resolvedTenantId,
                title,
                String.format(messageTemplate, tripCode, safeInt(trip.getTotalOrders())),
                actionUrl,
                eventId,
                eventName
        ));

        try {
            outboxEventService.enqueue(
                    AGGREGATE_TYPE_TRIP,
                    String.valueOf(trip.getId()),
                    EVENT_TYPE_NOTIFICATION_CREATE_REQUESTED,
                    userNotificationTopic,
                    String.valueOf(courier.getUserId()),
                    objectMapper.writeValueAsString(payload),
                    resolvedTenantId
            );
            log.info("Enqueued staff assignment notification eventId={} topic={}", eventId, userNotificationTopic);
        } catch (JsonProcessingException exception) {
            log.error("Failed to serialize staff assignment notification eventId={}", eventId, exception);
        }
    }

    private Map<String, Object> buildMeta(String eventId) {
        Map<String, Object> meta = new HashMap<>();
        meta.put("id", eventId);
        meta.put("type", EVENT_TYPE_NOTIFICATION_CREATE_REQUESTED);
        meta.put("source", SOURCE_SERVICE);
        meta.put("v", EVENT_VERSION);
        meta.put("ts", Instant.now().toEpochMilli());
        meta.put("traceId", UUID.randomUUID().toString());
        return meta;
    }

    private Map<String, Object> buildData(
            PostOfficeStaff courier,
            Trip trip,
            Long tenantId,
            String title,
            String message,
            String actionUrl,
            String eventId,
            String eventName
    ) {
        Map<String, Object> data = new HashMap<>();
        data.put("userId", courier.getUserId());
        data.put("tenantId", tenantId);
        data.put("title", title);
        data.put("message", message);
        data.put("type", TYPE_INFO);
        data.put("category", CATEGORY_TMS);
        data.put("priority", PRIORITY_HIGH);
        data.put("sourceService", SOURCE_SERVICE);
        data.put("sourceEventId", eventId);
        data.put("actionUrl", actionUrl);
        data.put("actionType", ACTION_VIEW_TRIP);
        data.put("entityType", ENTITY_TYPE_TRIP);
        data.put("entityId", trip.getId());
        data.put("deliveryChannels", List.of(DELIVERY_CHANNEL_IN_APP));
        data.put("metadata", buildMetadata(courier, trip, eventName));
        return data;
    }

    private Map<String, Object> buildMetadata(PostOfficeStaff courier, Trip trip, String eventName) {
        Map<String, Object> metadata = new HashMap<>();
        metadata.put("eventName", eventName);
        metadata.put("tripId", trip.getId());
        metadata.put("tripCode", trip.getTripCode());
        metadata.put("tripType", trip.getTripType());
        metadata.put("tripDate", trip.getTripDate());
        metadata.put("totalOrders", trip.getTotalOrders());
        metadata.put("staffId", courier.getId());
        metadata.put("staffCode", courier.getCode());
        metadata.put("staffName", courier.getFullName());
        return metadata;
    }

    private boolean hasText(String value) {
        return value != null && !value.trim().isEmpty();
    }

    private int safeInt(Integer value) {
        return value == null ? 0 : value;
    }
}
