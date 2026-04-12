package serp.project.logistics2.exception;

import lombok.Getter;
import org.springframework.http.HttpStatus;

@Getter
public enum AppErrorCode {
    UNIMPLEMENTED("LOGISTICS2-APP-001", "Feature not implemented", HttpStatus.NOT_IMPLEMENTED),
    UNEXPECTED_EXCEPTION("LOGISTICS2-APP-002", "An unexpected error occurred", HttpStatus.INTERNAL_SERVER_ERROR),

    NOT_FOUND("LOGISTICS2-BIZ-001", "Entity not found or access denied", HttpStatus.NOT_FOUND),
    INVALID_STATUS_TRANSITION("LOGISTICS2-BIZ-002", "Invalid status transition", HttpStatus.BAD_REQUEST),
    ORDER_NOT_APPROVED_YET("LOGISTICS2-BIZ-004", "Order has not been approved yet", HttpStatus.BAD_REQUEST),
    EXCEED_REMAINING_QUANTITY("LOGISTICS2-BIZ-005", "Quantity exceeds remaining allowable amount",
            HttpStatus.BAD_REQUEST),
    DATA_INTEGRITY_VIOLATION("LOGISTICS2-BIZ-006", "Data integrity violation", HttpStatus.BAD_REQUEST),
    PLAN_IN_OPTIMIZATION("LOGISTICS2-BIZ-007", "Delivery plan is currently being optimized", HttpStatus.BAD_REQUEST),
    DELIVERY_SLIP_ALREADY_ASSIGNED("LOGISTICS2-BIZ-008", "Delivery slip is already assigned",
            HttpStatus.BAD_REQUEST),

    UNKNOWN_ENUM_VALUE("LOGISTICS2-VAL-001", "Unknown enum value provided", HttpStatus.BAD_REQUEST),
    REQUEST_VALIDATION_FAILED("LOGISTICS2-VAL-002", "Request validation failed", HttpStatus.BAD_REQUEST),

    UNAUTHORIZED("LOGISTICS2-SEC-001", "Unauthorized access", HttpStatus.UNAUTHORIZED),
    CANNOT_ACCESS("LOGISTICS2-SEC-002", "Cannot access the requested resource", HttpStatus.FORBIDDEN),
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
