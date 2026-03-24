package serp.project.first_mile.exception;

import lombok.Getter;
import org.springframework.http.HttpStatus;
import org.springframework.http.HttpStatusCode;

@Getter
public enum ErrorCode {
    // Lỗi khác (99xx)
    UNCATEGORIZED_EXCEPTION(9999, "error.9999", HttpStatus.INTERNAL_SERVER_ERROR),
    INVALID_KEY(9998, "error.9998", HttpStatus.BAD_REQUEST),
    DATA_INTEGRITY_VIOLATION(9997, "error.9997", HttpStatus.CONFLICT),
    INVALID_REQUEST(9996, "error.9996", HttpStatus.BAD_REQUEST),
    UNAUTHORIZED(9995, "error.9995", HttpStatus.FORBIDDEN)
    ;
    ErrorCode(int code, String messageKey, HttpStatusCode statusCode) {
        this.code = code;
        this.messageKey = messageKey;
        this.statusCode = statusCode;
    }

    private final int code;
    private final String messageKey;
    private final HttpStatusCode statusCode;
}
