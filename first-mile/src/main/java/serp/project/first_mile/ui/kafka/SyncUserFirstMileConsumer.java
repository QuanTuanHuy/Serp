/*
Author: Nguyen The Anh
Description: Part of Serp Project
*/

package serp.project.first_mile.ui.kafka;

import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.kafka.support.KafkaHeaders;
import org.springframework.messaging.handler.annotation.Header;
import org.springframework.stereotype.Component;
import serp.project.first_mile.dto.message.SyncUserFirstMileEvent;
import serp.project.first_mile.service.KafkaDlqService;
import serp.project.first_mile.service.PostOfficeStaffSyncService;

@Component
@RequiredArgsConstructor
@Slf4j
public class SyncUserFirstMileConsumer {
    private final ObjectMapper objectMapper;
    private final PostOfficeStaffSyncService postOfficeStaffSyncService;
    private final KafkaDlqService kafkaDlqService;

    @KafkaListener(
            topics = "${app.kafka.topics.sync-user-first-mile:SYNC_USER_FIRST_MILE}",
            groupId = "${spring.kafka.consumer.group-id:first-mile-sync-user}"
    )
    public void consume(
            String payload,
            @Header(KafkaHeaders.RECEIVED_TOPIC) String topic,
            @Header(value = KafkaHeaders.RECEIVED_KEY, required = false) String key
    ) {
        try {
            SyncUserFirstMileEvent event = objectMapper.readValue(payload, SyncUserFirstMileEvent.class);
            postOfficeStaffSyncService.syncUser(event);
            log.info("Consumed sync-user-first-mile event: topic={}, key={}, userId={}, role={}",
                    topic,
                    key,
                    event.getUserId(),
                    event.getRoleName());
        } catch (Exception exception) {
            Long tenantId = extractTenantId(payload);
            kafkaDlqService.saveFailedMessage(topic, key, payload, exception.getMessage(), tenantId);
            log.error(
                    "Failed to consume sync-user-first-mile event and moved to DLQ: topic={}, key={}, tenantId={}, payload={}",
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
            SyncUserFirstMileEvent event = objectMapper.readValue(payload, SyncUserFirstMileEvent.class);
            if (event.getTenantId() != null) {
                return event.getTenantId();
            }
            return event.getOrganizationId();
        } catch (Exception exception) {
            return null;
        }
    }
}
