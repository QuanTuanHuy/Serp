/**
 * Author: QuanTuanHuy
 * Description: Part of Serp Project - Kafka producer adapter implementation
 */

package serp.project.discuss_service.infrastructure.client;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.kafka.support.SendResult;
import org.springframework.stereotype.Component;
import serp.project.discuss_service.core.domain.callback.KafkaSendCallback;
import serp.project.discuss_service.core.port.client.IKafkaProducerPort;
import serp.project.discuss_service.kernel.utils.JsonUtils;

import java.util.concurrent.CompletableFuture;

/**
 * Kafka implementation of the IKafkaProducerPort.
 * Provides async message publishing to Kafka topics.
 */
@Component
@RequiredArgsConstructor
@Slf4j
public class KafkaProducerAdapter implements IKafkaProducerPort {

    private final KafkaTemplate<String, String> kafkaTemplate;
    private final JsonUtils jsonUtils;

    @Override
    public <T> void sendMessageAsync(String key, T message, String topic, KafkaSendCallback callback) {
        String jsonMessage;
        try {
            jsonMessage = jsonUtils.toJson(message);
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
}
