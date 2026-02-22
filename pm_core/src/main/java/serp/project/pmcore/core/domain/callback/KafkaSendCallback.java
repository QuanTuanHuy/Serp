/**
 * Author: QuanTuanHuy
 * Description: Part of Serp Project - Kafka send callback
 */

package serp.project.pmcore.core.domain.callback;

@FunctionalInterface
public interface KafkaSendCallback {
    void onComplete(boolean success, String topic, Object payload, Throwable ex);
}
