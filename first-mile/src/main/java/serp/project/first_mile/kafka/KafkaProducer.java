/**
 * Author: Nguyen The Anh
 * Description: Part of Serp Project
 */

package serp.project.first_mile.kafka;

public interface KafkaProducer {
    <T> void sendMessageAsync(String key, T message, String topic, KafkaSendCallback callback);
}
