package serp.project.second_mile.exception;

import lombok.Getter;
import org.springframework.http.HttpStatus;
import org.springframework.http.HttpStatusCode;

@Getter
public enum ErrorCode {

    // Hub errors (1xxx)
    HUB_NOT_FOUND(1001, "error.1001", HttpStatus.NOT_FOUND),
    HUB_CODE_EXISTED(1002, "error.1002", HttpStatus.CONFLICT),
    HUB_POST_OFFICE_CODE_INVALID(1003, "error.1003", HttpStatus.BAD_REQUEST),

    RATE_LIMIT_EXCEEDED(9429, "error.9429", HttpStatus.TOO_MANY_REQUESTS),

    STORAGE_CONFIGURATION_INVALID(9430, "error.9430", HttpStatus.INTERNAL_SERVER_ERROR),
    FILE_SIZE_EXCEEDED(9432, "error.9432", HttpStatus.PAYLOAD_TOO_LARGE),
    FILE_UPLOAD_EMPTY(9431, "error.9431", HttpStatus.BAD_REQUEST),
    FILE_UPLOAD_FAILED(9433, "error.9433", HttpStatus.INTERNAL_SERVER_ERROR),
    FILE_IMAGE_TYPE_INVALID(9434, "error.9434", HttpStatus.BAD_REQUEST),
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
