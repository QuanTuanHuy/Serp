/**
 * Author: QuanTuanHuy
 * Description: Part of Serp Project - Kafka producer adapter implementation
 */

package serp.project.discuss_service.infrastructure.client;

import io.github.serp.platform.kafka.publisher.SerpKafkaPublisher;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;
import serp.project.discuss_service.core.domain.callback.KafkaSendCallback;
import serp.project.discuss_service.core.port.client.IKafkaProducerPort;
import serp.project.discuss_service.kernel.utils.JsonUtils;

@Component
@RequiredArgsConstructor
@Slf4j
public class KafkaProducerAdapter implements IKafkaProducerPort {

    private final SerpKafkaPublisher kafkaPublisher;
    private final JsonUtils jsonUtils;

    @Override
    public <T> void sendMessageAsync(String key, T message, String topic, KafkaSendCallback callback) {
        String jsonMessage;
        try {
            jsonMessage = jsonUtils.toJson(message);
        } catch (Exception ex) {
            log.error("Error serializing message to JSON for topic {}: {}", topic, ex.getMessage(), ex);
            if (callback != null) {
                callback.onComplete(false, topic, message, ex);
            }
            return;
        }

        try {
            kafkaPublisher.publish(topic, key, jsonMessage);
            log.debug("Message published to Kafka topic {} with key {}", topic, key);
            if (callback != null) {
                callback.onComplete(true, topic, message, null);
            }
        } catch (Exception ex) {
            log.error("Failed to publish message to Kafka topic {} with key {}: {}", topic, key, ex.getMessage(), ex);
            if (callback != null) {
                callback.onComplete(false, topic, message, ex);
            }
        }
    }

    @Override
    public <T> void sendMessageAsync(String key, T message, String topic) {
        sendMessageAsync(key, message, topic, null);
    }
}
