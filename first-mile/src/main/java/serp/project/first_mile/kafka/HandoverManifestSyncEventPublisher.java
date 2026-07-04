/*
Author: Nguyen The Anh
Description: Part of Serp Project
*/

package serp.project.first_mile.kafka;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;
import serp.project.first_mile.dto.message.HandoverManifestSyncEvent;
import serp.project.first_mile.kernel.utils.JsonUtils;
import serp.project.first_mile.service.OutboxEventService;

@Component
@RequiredArgsConstructor
@Slf4j
public class HandoverManifestSyncEventPublisher {
    private static final String AGGREGATE_TYPE_HANDOVER_MANIFEST = "HANDOVER_MANIFEST";

    private final JsonUtils jsonUtils;
    private final OutboxEventService outboxEventService;

    @Value("${app.kafka.topics.sync-handover-manifest:HANDOVER_MANIFEST_SYNC}")
    private String syncHandoverManifestTopic;

    public void publish(HandoverManifestSyncEvent event) {
        if (event == null || event.getManifestCode() == null || event.getManifestCode().isBlank()) {
            log.warn("Skip handover manifest sync publish: invalid event {}", event);
            return;
        }
        String key = event.getManifestCode();
        outboxEventService.enqueue(
                AGGREGATE_TYPE_HANDOVER_MANIFEST,
                key,
                "handover-manifest." + event.getEventType(),
                syncHandoverManifestTopic,
                key,
                jsonUtils.toJson(event),
                event.getTenantId()
        );
        log.info("Enqueued handover manifest sync outbox event manifestCode={} topic={}",
                key,
                syncHandoverManifestTopic);
    }
}
