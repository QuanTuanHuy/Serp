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
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.kafka.support.SendResult;
import org.springframework.stereotype.Component;
import serp.project.second_mile.kafka.event.HubPostOfficeSyncEvent;

import java.util.concurrent.CompletableFuture;

@Component
@RequiredArgsConstructor
@Slf4j
public class HubPostOfficeSyncEventPublisher {

    private final KafkaTemplate<String, String> kafkaTemplate;
    private final ObjectMapper objectMapper;

    @Value("${app.kafka.topics.sync-hub-post-office:HUB_POST_OFFICE_SYNC}")
    private String topic;

    public void publish(HubPostOfficeSyncEvent event) {
        if (event == null || event.getTenantId() == null || event.getPostOfficeCode() == null
                || event.getPostOfficeCode().isBlank()) {
            log.warn("Skip HubPostOffice sync publish: invalid event {}", event);
            return;
        }
        final String key = event.getTenantId() + ":" + event.getPostOfficeCode().trim();
        final String json;
        try {
            json = objectMapper.writeValueAsString(event);
        } catch (JsonProcessingException e) {
            log.error("Failed to serialize HubPostOfficeSyncEvent: {}", e.getMessage(), e);
            return;
        }
        CompletableFuture<SendResult<String, String>> future = kafkaTemplate.send(topic, key, json);
        future.whenComplete((result, ex) -> {
            if (ex != null) {
                log.error("Failed to send HubPostOffice sync to topic {} key {}: {}", topic, key, ex.getMessage(), ex);
            } else if (result != null && result.getRecordMetadata() != null) {
                log.info("HubPostOffice sync sent topic={} partition={} offset={} key={}",
                        result.getRecordMetadata().topic(),
                        result.getRecordMetadata().partition(),
                        result.getRecordMetadata().offset(),
                        key);
            }
        });
    }
}
