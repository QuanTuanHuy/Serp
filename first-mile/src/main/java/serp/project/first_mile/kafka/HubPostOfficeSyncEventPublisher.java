/*
Author: Nguyen The Anh
Description: Part of Serp Project
*/

package serp.project.first_mile.kafka;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;
import serp.project.first_mile.dto.message.HubPostOfficeSyncEvent;
import serp.project.first_mile.kernel.utils.JsonUtils;
import serp.project.first_mile.service.OutboxEventService;

@Component
@RequiredArgsConstructor
@Slf4j
public class HubPostOfficeSyncEventPublisher {
    private static final String AGGREGATE_TYPE_HUB_POST_OFFICE = "HUB_POST_OFFICE";

    private final JsonUtils jsonUtils;
    private final OutboxEventService outboxEventService;

    @Value("${app.kafka.topics.sync-hub-post-office:HUB_POST_OFFICE_SYNC}")
    private String topic;

    public void publish(HubPostOfficeSyncEvent event) {
        if (event == null || event.getTenantId() == null || event.getPostOfficeCode() == null
                || event.getPostOfficeCode().isBlank()) {
            log.warn("Skip HubPostOffice sync publish: invalid event {}", event);
            return;
        }
        String key = event.getTenantId() + ":" + event.getPostOfficeCode().trim();
        outboxEventService.enqueue(
                AGGREGATE_TYPE_HUB_POST_OFFICE,
                event.getPostOfficeCode().trim(),
                "hub-post-office." + event.getEventType(),
                topic,
                key,
                jsonUtils.toJson(event),
                event.getTenantId()
        );
        log.info("Enqueued HubPostOffice sync outbox event key={} topic={}", key, topic);
    }
}
