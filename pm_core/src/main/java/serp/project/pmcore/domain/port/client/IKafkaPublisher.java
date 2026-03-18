/**
 * Author: QuanTuanHuy
 * Description: Part of Serp Project
 */

package serp.project.pmcore.domain.port.client;

public interface IKafkaPublisher {
    <T> void sendMessageAsync(String key, T message, String topic, KafkaSendCallback callback);

    <T> void sendMessageAsync(String key, T message, String topic);

    <T> void sendMessageSync(String key, T message, String topic) throws Exception;

    @FunctionalInterface
    interface KafkaSendCallback {
        void onComplete(boolean success, String topic, Object payload, Throwable ex);
    }
}
