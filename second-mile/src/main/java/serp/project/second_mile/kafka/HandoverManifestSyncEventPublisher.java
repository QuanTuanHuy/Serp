/*
Author: QuanTuanHuy
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
import serp.project.second_mile.kafka.event.HandoverManifestSyncEvent;

import java.util.concurrent.CompletableFuture;

@Component
@RequiredArgsConstructor
@Slf4j
public class HandoverManifestSyncEventPublisher {
    private final KafkaTemplate<String, String> kafkaTemplate;
    private final ObjectMapper objectMapper;

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

        CompletableFuture<SendResult<String, String>> future = kafkaTemplate.send(topic, key, json);
        future.whenComplete((result, ex) -> {
            if (ex != null) {
                log.error("Failed to send handover manifest sync topic={} key={}: {}", topic, key, ex.getMessage(), ex);
            } else if (result != null && result.getRecordMetadata() != null) {
                log.info("Handover manifest sync sent topic={} partition={} offset={} key={} status={}",
                        result.getRecordMetadata().topic(),
                        result.getRecordMetadata().partition(),
                        result.getRecordMetadata().offset(),
                        key,
                        event.getStatus());
            }
        });
    }
}
