/**
 * Author: QuanTuanHuy
 * Description: Part of Serp Project - Generic Kafka producer port
 */

package serp.project.discuss_service.core.port.client;

import serp.project.discuss_service.core.domain.callback.KafkaSendCallback;

/**
 * Generic port for publishing messages to Kafka.
 * This is a low-level port that handles the actual Kafka communication.
 * For discuss-specific event publishing, use IDiscussEventPublisher service.
 */
public interface IKafkaProducerPort {

    /**
     * Send message to Kafka asynchronously with callback
     *
     * @param key     Message key for partitioning
     * @param message Message payload (will be serialized to JSON)
     * @param topic   Kafka topic name
     * @param callback Callback to be invoked on completion
     * @param <T>     Type of message
     */
    <T> void sendMessageAsync(String key, T message, String topic, KafkaSendCallback callback);

    /**
     * Send message to Kafka asynchronously without callback
     *
     * @param key     Message key for partitioning
     * @param message Message payload (will be serialized to JSON)
     * @param topic   Kafka topic name
     * @param <T>     Type of message
     */
    <T> void sendMessageAsync(String key, T message, String topic);
}
