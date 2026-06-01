/*
Author: QuanTuanHuy
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
import serp.project.first_mile.dto.message.HandoverManifestSyncEvent;
import serp.project.first_mile.service.KafkaDlqService;
import serp.project.first_mile.service.PostOfficeHandoverManifestService;

@Component
@RequiredArgsConstructor
@Slf4j
public class HandoverManifestSyncConsumer {
    private final ObjectMapper objectMapper;
    private final PostOfficeHandoverManifestService postOfficeHandoverManifestService;
    private final KafkaDlqService kafkaDlqService;

    @KafkaListener(
            topics = "${app.kafka.topics.sync-handover-manifest:HANDOVER_MANIFEST_SYNC}",
            groupId = "${app.kafka.handover-manifest-sync.consumer-group-id:first-mile-sync-handover-manifest}"
    )
    public void consume(
            String payload,
            @Header(KafkaHeaders.RECEIVED_TOPIC) String topic,
            @Header(value = KafkaHeaders.RECEIVED_KEY, required = false) String key
    ) {
        try {
            HandoverManifestSyncEvent event = objectMapper.readValue(payload, HandoverManifestSyncEvent.class);
            postOfficeHandoverManifestService.applyInboundSync(event);
            log.info("Consumed handover manifest sync: topic={}, key={}, manifestCode={}, origin={}",
                    topic,
                    key,
                    event.getManifestCode(),
                    event.getOrigin());
        } catch (Exception exception) {
            Long tenantId = extractTenantId(payload);
            kafkaDlqService.saveFailedMessage(topic, key, payload, exception.getMessage(), tenantId);
            log.error(
                    "Failed handover manifest sync consumer, moved to DLQ: topic={}, key={}, tenantId={}, payload={}",
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
            HandoverManifestSyncEvent event = objectMapper.readValue(payload, HandoverManifestSyncEvent.class);
            return event.getTenantId();
        } catch (Exception exception) {
            return null;
        }
    }
}
