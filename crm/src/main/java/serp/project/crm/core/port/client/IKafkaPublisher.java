/**
 * Author: QuanTuanHuy
 * Description: Part of Serp Project
 */

package serp.project.crm.core.port.client;

import serp.project.crm.core.domain.callback.KafkaSendCallback;

public interface IKafkaPublisher {
    <T> void sendMessageAsync(String key, T message, String topic, KafkaSendCallback callback);

    <T> void sendMessageAsync(String key, T message, String topic);
}
