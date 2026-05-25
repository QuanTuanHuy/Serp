/**
 * Author: Nguyen The Anh
 * Description: Part of Serp Project
 */

package serp.project.first_mile.kafka;

@FunctionalInterface
public interface KafkaSendCallback {
    void onComplete(boolean success, String topic, Object payload, Throwable ex);
}
