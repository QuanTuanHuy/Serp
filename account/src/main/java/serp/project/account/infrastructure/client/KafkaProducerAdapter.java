/**
 * Author: QuanTuanHuy
 * Description: Part of Serp Project
 */

package serp.project.account.infrastructure.client;

import java.util.concurrent.CompletableFuture;
import java.util.concurrent.TimeUnit;

import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.kafka.support.SendResult;
import org.springframework.stereotype.Component;

import lombok.extern.slf4j.Slf4j;
import serp.project.account.core.domain.callback.KafkaSendCallback;
import serp.project.account.core.port.client.IKafkaProducer;
import serp.project.account.kernel.utils.JsonUtils;

@Component
@Slf4j
public class KafkaProducerAdapter implements IKafkaProducer {
    private final KafkaTemplate<String, String> kafkaTemplate;
    private final JsonUtils jsonUtils;

    public KafkaProducerAdapter(KafkaTemplate<String, String> kafkaTemplate, JsonUtils jsonUtils) {
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

    @Override
    public <T> void sendMessageAsync(String key, T message, String topic) {
        sendMessageAsync(key, message, topic, null);
    }

    @Override
    public <T> void sendMessageSync(String key, T message, String topic) {
        String jsonMessage;
        try {
            jsonMessage = serializeMessage(message);
        } catch (Exception e) {
            log.error("Error serializing message to JSON for topic {}: {}", topic, e.getMessage(), e);
            throw new RuntimeException("Failed to serialize message to JSON", e);
        }

        try {
            SendResult<String, String> result = kafkaTemplate.send(topic, key, jsonMessage)
                    .get(10, TimeUnit.SECONDS);
            var metadata = result.getRecordMetadata();
            log.info("Message sent successfully to Kafka topic {} partition {} offset {} with key {}",
                    metadata.topic(), metadata.partition(), metadata.offset(), key);
        } catch (Exception ex) {
            log.error("Failed to send message to Kafka topic {} with key {}: {}",
                    topic, key, ex.getMessage(), ex);
            throw new RuntimeException("Failed to send message to Kafka", ex);
        }
    }

    private <T> String serializeMessage(T message) {
        if (message instanceof String rawMessage) {
            return rawMessage;
        }
        return jsonUtils.toJson(message);
    }
}
