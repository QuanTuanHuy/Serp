package serp.project.logistics.exception;

import lombok.Getter;
import org.springframework.http.HttpStatus;

@Getter
public enum AppErrorCode {
    UNIMPLEMENTED("LOGISTICS-APP-001", "Feature not implemented", HttpStatus.NOT_IMPLEMENTED),
    UNEXPECTED_EXCEPTION("LOGISTICS-APP-002", "An unexpected error occurred", HttpStatus.INTERNAL_SERVER_ERROR),

    NOT_FOUND("LOGISTICS-BIZ-001", "Entity not found or access denied", HttpStatus.NOT_FOUND),
    INVALID_STATUS_TRANSITION("LOGISTICS-BIZ-002", "Invalid status transition", HttpStatus.BAD_REQUEST),
    ORDER_NOT_APPROVED_YET("LOGISTICS-BIZ-004", "Order has not been approved yet", HttpStatus.BAD_REQUEST),
    EXCEED_REMAINING_QUANTITY("LOGISTICS-BIZ-005", "Quantity exceeds remaining allowable amount",
            HttpStatus.BAD_REQUEST),
    DATA_INTEGRITY_VIOLATION("LOGISTICS-BIZ-006", "Data integrity violation", HttpStatus.BAD_REQUEST),

    UNKNOWN_ENUM_VALUE("LOGISTICS-VAL-001", "Unknown enum value provided", HttpStatus.BAD_REQUEST),
    REQUEST_VALIDATION_FAILED("LOGISTICS-VAL-002", "Request validation failed", HttpStatus.BAD_REQUEST),

    UNAUTHORIZED("LOGISTICS-SEC-001", "Unauthorized access", HttpStatus.UNAUTHORIZED),
    CANNOT_ACCESS("LOGISTICS-SEC-002", "Cannot access the requested resource", HttpStatus.FORBIDDEN),
    ;

    private final String code;
    private final String message;
    private final HttpStatus status;

    AppErrorCode(String code, String message, HttpStatus status) {
        this.code = code;
        this.message = message;
        this.status = status;
    }

}
