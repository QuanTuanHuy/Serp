/*
Author: Nguyen The Anh
Description: Part of Serp Project
*/

package serp.project.second_mile.kafka.consumer;

import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.kafka.support.KafkaHeaders;
import org.springframework.messaging.handler.annotation.Header;
import org.springframework.stereotype.Component;
import serp.project.second_mile.kafka.event.HubPostOfficeSyncEvent;
import serp.project.second_mile.service.HubPostOfficeInboundSyncService;
import serp.project.second_mile.service.KafkaDlqService;

@Component
@RequiredArgsConstructor
@Slf4j
public class HubPostOfficeSyncConsumer {

    private final ObjectMapper objectMapper;
    private final HubPostOfficeInboundSyncService hubPostOfficeInboundSyncService;
    private final KafkaDlqService kafkaDlqService;

    @KafkaListener(
            topics = "${app.kafka.topics.sync-hub-post-office:HUB_POST_OFFICE_SYNC}",
            groupId = "${app.kafka.hub-post-office-sync.consumer-group-id:second-mile-sync-hub-post-office}"
    )
    public void consume(
            String payload,
            @Header(KafkaHeaders.RECEIVED_TOPIC) String receivedTopic,
            @Header(value = KafkaHeaders.RECEIVED_KEY, required = false) String key
    ) {
        try {
            HubPostOfficeSyncEvent event = objectMapper.readValue(payload, HubPostOfficeSyncEvent.class);
            hubPostOfficeInboundSyncService.applyFirstMileKafkaEvent(event);
            log.info("Consumed hub-post-office sync: topic={}, key={}, type={}, origin={}",
                    receivedTopic,
                    key,
                    event.getEventType(),
                    event.getOrigin());
        } catch (Exception exception) {
            Long tenantId = extractTenantId(payload);
            kafkaDlqService.saveFailedMessage(receivedTopic, key, payload, exception.getMessage(), tenantId);
            log.error(
                    "Failed hub-post-office sync consumer, moved to DLQ: topic={}, key={}, tenantId={}, payload={}",
                    receivedTopic,
                    key,
                    tenantId,
                    payload,
                    exception
            );
        }
    }

    private Long extractTenantId(String payload) {
        try {
            HubPostOfficeSyncEvent event = objectMapper.readValue(payload, HubPostOfficeSyncEvent.class);
            return event.getTenantId();
        } catch (Exception exception) {
            return null;
        }
    }
}
