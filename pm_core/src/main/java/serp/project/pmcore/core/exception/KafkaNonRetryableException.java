/**
 * Author: QuanTuanHuy
 * Description: Part of Serp Project
 */

package serp.project.pmcore.core.exception;

public class KafkaNonRetryableException extends RuntimeException {
    public KafkaNonRetryableException(String message) {
        super(message);
    }

    public KafkaNonRetryableException(String message, Throwable cause) {
        super(message, cause);
    }
}
