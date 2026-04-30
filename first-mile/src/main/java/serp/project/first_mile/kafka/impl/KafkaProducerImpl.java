/**
 * Author: Nguyen The Anh
 * Description: Part of Serp Project
 */

package serp.project.first_mile.kafka.impl;

import lombok.extern.slf4j.Slf4j;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.kafka.support.SendResult;
import org.springframework.stereotype.Component;
import serp.project.first_mile.kafka.KafkaProducer;
import serp.project.first_mile.kafka.KafkaSendCallback;
import serp.project.first_mile.kernel.utils.JsonUtils;

import java.util.concurrent.CompletableFuture;

@Component
@Slf4j
public class KafkaProducerImpl implements KafkaProducer {
    private final KafkaTemplate<String, String> kafkaTemplate;
    private final JsonUtils jsonUtils;

    public KafkaProducerImpl(KafkaTemplate<String, String> kafkaTemplate, JsonUtils jsonUtils) {
        this.kafkaTemplate = kafkaTemplate;
        this.jsonUtils = jsonUtils;
    }

    @Override
    public <T> void sendMessageAsync(String key, T message, String topic, KafkaSendCallback callback) {
        String jsonMessage;
        try {
            jsonMessage = serializeMessage(message);
        } catch (Exception e) {
            log.error("Error serializing message to JSON for topic {}: {}", topic, e.getMessage(), e);
            if (callback != null) {
                callback.onComplete(false, topic, message, e);
            }
            return;
        }

        log.debug("Sending message to Kafka topic {} with key {}: {}", topic, key, jsonMessage);

        CompletableFuture<SendResult<String, String>> future = kafkaTemplate.send(topic, key, jsonMessage);

        future.whenComplete((result, ex) -> {
            if (ex != null) {
                log.error("Failed to send message to Kafka topic {} with key {}: {}",
                        topic, key, ex.getMessage(), ex);
                if (callback != null) {
                    callback.onComplete(false, topic, message, ex);
                }
            } else {
                var metadata = result.getRecordMetadata();
                log.info("Message sent successfully to Kafka topic {} partition {} offset {} with key {}",
                        metadata.topic(), metadata.partition(), metadata.offset(), key);
                if (callback != null) {
                    callback.onComplete(true, topic, message, null);
                }
            }
        });
    }

    private <T> String serializeMessage(T message) {
        if (message instanceof String rawMessage) {
            return rawMessage;
        }
        return jsonUtils.toJson(message);
    }
}
