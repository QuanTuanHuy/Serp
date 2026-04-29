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
import serp.project.second_mile.kafka.event.UserSyncEvent;
import serp.project.second_mile.service.KafkaDlqService;
import serp.project.second_mile.service.UserSyncService;

@Component
@RequiredArgsConstructor
@Slf4j
public class UserSyncConsumer {
    private final ObjectMapper objectMapper;
    private final KafkaDlqService kafkaDlqService;
    private final UserSyncService userSyncService;

    @KafkaListener(
            topics = "${app.kafka.topics.sync-user:SYNC_USER}",
            groupId = "${spring.kafka.consumer.group-id:second-mile-sync-user}"
    )
    public void consume(
            String payload,
            @Header(KafkaHeaders.RECEIVED_TOPIC) String topic,
            @Header(value = KafkaHeaders.RECEIVED_KEY, required = false) String key
    ) {
        try {
            UserSyncEvent event = objectMapper.readValue(payload, UserSyncEvent.class);
            userSyncService.syncUser(event);
            log.info("Consumed sync-user event: topic={}, key={}, userId={}, roles={}",
                    topic,
                    key,
                    event.getUserId(),
                    event.getRoleNames());
        } catch (Exception exception) {
            Long tenantId = extractTenantId(payload);
            kafkaDlqService.saveFailedMessage(topic, key, payload, exception.getMessage(), tenantId);
            log.error(
                    "Failed to consume sync-user event and moved to DLQ: topic={}, key={}, tenantId={}, payload={}",
                    topic,
                    key,
                    tenantId,
                    payload,
                    exception
            );
        }
    }

    private Long extractTenantId(String payload) {
        try {
            UserSyncEvent event = objectMapper.readValue(payload, UserSyncEvent.class);
            if (event.getTenantId() != null) {
                return event.getTenantId();
            }
            return event.getOrganizationId();
        } catch (Exception exception) {
            return null;
        }
    }
}
