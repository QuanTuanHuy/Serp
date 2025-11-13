/**
 * Author: QuanTuanHuy
 * Description: Part of Serp Project
 */

package serp.project.mailservice.core.domain.callback;

@FunctionalInterface
public interface KafkaSendCallback {
    void onComplete(boolean success, String topic, Object payload, Throwable ex);
}
