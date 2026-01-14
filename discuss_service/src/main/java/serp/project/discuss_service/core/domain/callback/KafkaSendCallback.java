/**
 * Author: QuanTuanHuy
 * Description: Part of Serp Project - Kafka send callback
 */

package serp.project.discuss_service.core.domain.callback;

/**
 * Callback interface for Kafka message send operations.
 * Provides feedback on success/failure of async message publishing.
 */
@FunctionalInterface
public interface KafkaSendCallback {

    /**
     * Called when the send operation completes
     *
     * @param success Whether the send was successful
     * @param topic   The topic the message was sent to
     * @param payload The message payload that was sent
     * @param ex      Exception if failed, null if successful
     */
    void onComplete(boolean success, String topic, Object payload, Throwable ex);
}
