/*
Author: Nguyen The Anh
Description: Part of Serp Project
*/

package serp.project.second_mile.kafka;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;
import serp.project.second_mile.domain.BagDistributionManifest;
import serp.project.second_mile.domain.HubStaff;
import serp.project.second_mile.service.OutboxEventService;

import java.time.Instant;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.UUID;

@Slf4j
@Component
@RequiredArgsConstructor
public class DriverNotificationEventPublisher {
    private static final String EVENT_TYPE_NOTIFICATION_CREATE_REQUESTED = "notification.create.requested";
    private static final String SOURCE_SERVICE = "second-mile";
    private static final String EVENT_VERSION = "1";
    private static final String CATEGORY_TMS = "TMS";
    private static final String TYPE_INFO = "INFO";
    private static final String PRIORITY_HIGH = "HIGH";
    private static final String DELIVERY_CHANNEL_IN_APP = "IN_APP";
    private static final String ACTION_VIEW_MANIFEST = "VIEW_BAG_DISTRIBUTION_MANIFEST";
    private static final String ENTITY_TYPE_MANIFEST = "BAG_DISTRIBUTION_MANIFEST";

    private final ObjectMapper objectMapper;
    private final OutboxEventService outboxEventService;

    @Value("${app.kafka.topics.user-notification:serp.notification.user.events}")
    private String userNotificationTopic;

    public void publishBagDistributionAssigned(HubStaff driver, BagDistributionManifest manifest, Long tenantId) {
        if (driver == null || driver.getUserId() == null || manifest == null || manifest.getId() == null) {
            return;
        }

        Long resolvedTenantId = tenantId == null ? manifest.getTenantId() : tenantId;
        if (resolvedTenantId == null) {
            log.warn("Skip driver notification because tenantId is null manifestId={} driverId={}",
                    manifest.getId(), driver.getId());
            return;
        }

        String manifestCode = hasText(manifest.getManifestCode())
                ? manifest.getManifestCode()
                : String.valueOf(manifest.getId());
        String eventId = SOURCE_SERVICE + ".bag-distribution-assigned." + manifest.getId() + "." + driver.getUserId();
        Map<String, Object> payload = new HashMap<>();
        payload.put("meta", buildMeta(eventId));
        payload.put("data", buildData(
                driver,
                manifest,
                resolvedTenantId,
                eventId,
                manifestCode
        ));

        try {
            outboxEventService.enqueue(
                    ENTITY_TYPE_MANIFEST,
                    String.valueOf(manifest.getId()),
                    EVENT_TYPE_NOTIFICATION_CREATE_REQUESTED,
                    userNotificationTopic,
                    String.valueOf(driver.getUserId()),
                    objectMapper.writeValueAsString(payload),
                    resolvedTenantId
            );
            log.info("Enqueued driver assignment notification eventId={} topic={}", eventId, userNotificationTopic);
        } catch (JsonProcessingException exception) {
            log.error("Failed to serialize driver assignment notification eventId={}", eventId, exception);
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
            HubStaff driver,
            BagDistributionManifest manifest,
            Long tenantId,
            String eventId,
            String manifestCode
    ) {
        Map<String, Object> data = new HashMap<>();
        data.put("userId", driver.getUserId());
        data.put("tenantId", tenantId);
        data.put("title", "Bạn có chuyến trung chuyển mới");
        data.put("message", String.format("Bạn đã được phân công manifest %s.", manifestCode));
        data.put("type", TYPE_INFO);
        data.put("category", CATEGORY_TMS);
        data.put("priority", PRIORITY_HIGH);
        data.put("sourceService", SOURCE_SERVICE);
        data.put("sourceEventId", eventId);
        data.put("actionUrl", "/first-mile/bag-distribution-manifests");
        data.put("actionType", ACTION_VIEW_MANIFEST);
        data.put("entityType", ENTITY_TYPE_MANIFEST);
        data.put("entityId", manifest.getId());
        data.put("deliveryChannels", List.of(DELIVERY_CHANNEL_IN_APP));
        data.put("metadata", buildMetadata(driver, manifest));
        return data;
    }

    private Map<String, Object> buildMetadata(HubStaff driver, BagDistributionManifest manifest) {
        Map<String, Object> metadata = new HashMap<>();
        metadata.put("eventName", "bag-distribution-assigned");
        metadata.put("manifestId", manifest.getId());
        metadata.put("manifestCode", manifest.getManifestCode());
        metadata.put("originHubId", manifest.getOriginHubId());
        metadata.put("destinationHubId", manifest.getDestinationHubId());
        metadata.put("destinationPostOfficeCode", manifest.getDestinationPostOfficeCode());
        metadata.put("plannedDepartureAt", manifest.getPlannedDepartureAt());
        metadata.put("plannedArrivalAt", manifest.getPlannedArrivalAt());
        metadata.put("driverId", driver.getId());
        metadata.put("driverCode", driver.getCode());
        metadata.put("driverName", driver.getFullName());
        return metadata;
    }

    private boolean hasText(String value) {
        return value != null && !value.trim().isEmpty();
    }
}
