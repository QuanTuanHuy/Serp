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

@Component
@RequiredArgsConstructor
@Slf4j
public class HandoverManifestSyncEventPublisher {
    private final KafkaProducer kafkaProducer;

    @Value("${app.kafka.topics.sync-handover-manifest:HANDOVER_MANIFEST_SYNC}")
    private String syncHandoverManifestTopic;

    public void publish(HandoverManifestSyncEvent event) {
        if (event == null || event.getManifestCode() == null || event.getManifestCode().isBlank()) {
            log.warn("Skip handover manifest sync publish: invalid event {}", event);
            return;
        }
        String key = event.getManifestCode();
        kafkaProducer.sendMessageAsync(key, event, syncHandoverManifestTopic, (success, sentTopic, payload, ex) -> {
            if (success) {
                log.info("Published handover manifest sync event: manifestCode={}, topic={}", key, sentTopic);
            } else {
                log.error("Failed to publish handover manifest sync event: manifestCode={}, topic={}",
                        key,
                        sentTopic,
                        ex);
            }
        });
    }
}
