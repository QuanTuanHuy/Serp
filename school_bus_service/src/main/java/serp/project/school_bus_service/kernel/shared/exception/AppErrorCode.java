package serp.project.school_bus_service.kernel.shared.exception;

import lombok.Getter;
import org.springframework.http.HttpStatus;

@Getter
public enum AppErrorCode {
    UNEXPECTED_EXCEPTION("SCHOOLBUS-APP-001", "An unexpected error occurred", HttpStatus.INTERNAL_SERVER_ERROR),
    NOT_FOUND("SCHOOLBUS-BIZ-001", "Entity not found or access denied", HttpStatus.NOT_FOUND),
    INVALID_REQUEST("SCHOOLBUS-BIZ-002", "Invalid request payload or business rule", HttpStatus.BAD_REQUEST),
    INVALID_STATE("SCHOOLBUS-BIZ-003", "Invalid workflow state transition", HttpStatus.BAD_REQUEST),
    CONFLICT("SCHOOLBUS-BIZ-004", "Resource conflict detected", HttpStatus.CONFLICT),
    DATA_INTEGRITY_VIOLATION("SCHOOLBUS-BIZ-005", "Data integrity violation", HttpStatus.BAD_REQUEST),
    UNAUTHORIZED("SCHOOLBUS-SEC-001", "Unauthorized access", HttpStatus.UNAUTHORIZED),
    FORBIDDEN("SCHOOLBUS-SEC-002", "Forbidden resource access", HttpStatus.FORBIDDEN),
    REQUEST_VALIDATION_FAILED("SCHOOLBUS-VAL-001", "Request validation failed", HttpStatus.BAD_REQUEST);

    private final String code;
    private final String message;
    private final HttpStatus status;

    AppErrorCode(String code, String message, HttpStatus status) {
        this.code = code;
        this.message = message;
        this.status = status;
    }
}
