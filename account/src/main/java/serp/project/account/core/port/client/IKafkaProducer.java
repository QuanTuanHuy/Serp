/**
 * Author: QuanTuanHuy
 * Description: Part of Serp Project
 */

package serp.project.account.core.port.client;

import serp.project.account.core.domain.callback.KafkaSendCallback;

public interface IKafkaProducer {
    <T> void sendMessageAsync(String key, T message, String topic, KafkaSendCallback callback);
    <T> void sendMessageAsync(String key, T message, String topic);
}
