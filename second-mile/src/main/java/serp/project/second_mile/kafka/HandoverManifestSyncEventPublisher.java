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
import serp.project.second_mile.kafka.event.HandoverManifestSyncEvent;
import serp.project.second_mile.service.OutboxEventService;

@Component
@RequiredArgsConstructor
@Slf4j
public class HandoverManifestSyncEventPublisher {
    private static final String AGGREGATE_TYPE_HANDOVER_MANIFEST = "HANDOVER_MANIFEST";

    private final ObjectMapper objectMapper;
    private final OutboxEventService outboxEventService;

    @Value("${app.kafka.topics.sync-handover-manifest:HANDOVER_MANIFEST_SYNC}")
    private String topic;

    public void publish(HandoverManifestSyncEvent event) {
        if (event == null || event.getManifestCode() == null || event.getManifestCode().isBlank()) {
            log.warn("Skip handover manifest sync publish: invalid event {}", event);
            return;
        }
        final String key = event.getManifestCode();
        final String json;
        try {
            json = objectMapper.writeValueAsString(event);
        } catch (JsonProcessingException e) {
            log.error("Failed to serialize HandoverManifestSyncEvent manifestCode={}: {}", key, e.getMessage(), e);
            return;
        }

        outboxEventService.enqueue(
                AGGREGATE_TYPE_HANDOVER_MANIFEST,
                key,
                "handover-manifest." + event.getEventType(),
                topic,
                key,
                json,
                event.getTenantId()
        );
        log.info("Enqueued handover manifest sync outbox event key={} topic={} status={}",
                key,
                topic,
                event.getStatus());
    }
}
