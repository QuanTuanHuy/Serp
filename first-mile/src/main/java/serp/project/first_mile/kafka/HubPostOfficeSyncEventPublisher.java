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

@Component
@RequiredArgsConstructor
@Slf4j
public class HubPostOfficeSyncEventPublisher {

    private final KafkaProducer kafkaProducer;

    @Value("${app.kafka.topics.sync-hub-post-office:HUB_POST_OFFICE_SYNC}")
    private String topic;

    public void publish(HubPostOfficeSyncEvent event) {
        if (event == null || event.getTenantId() == null || event.getPostOfficeCode() == null
                || event.getPostOfficeCode().isBlank()) {
            log.warn("Skip HubPostOffice sync publish: invalid event {}", event);
            return;
        }
        String key = event.getTenantId() + ":" + event.getPostOfficeCode().trim();
        kafkaProducer.sendMessageAsync(key, event, topic, null);
    }
}
