package serp.project.mailservice.core.exception;

import lombok.Getter;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;

@Getter
@RequiredArgsConstructor
public enum ErrorCode {
    // Success
    SUCCESS("Success", HttpStatus.OK),

    // General errors
    UNAUTHORIZED("Unauthorized", HttpStatus.UNAUTHORIZED),
    FORBIDDEN("You do not have permission to perform this action", HttpStatus.FORBIDDEN),
    BAD_REQUEST("Bad request", HttpStatus.BAD_REQUEST),
    NOT_FOUND("Resource not found", HttpStatus.NOT_FOUND),
    INTERNAL_SERVER_ERROR("Internal server error", HttpStatus.INTERNAL_SERVER_ERROR),
    CONFLICT("Conflict occurred", HttpStatus.CONFLICT),
    TOO_MANY_REQUESTS("Too many requests", HttpStatus.TOO_MANY_REQUESTS),

    // Authentication errors
    TENANT_ID_REQUIRED("Tenant ID is required", HttpStatus.BAD_REQUEST),
    USER_ID_REQUIRED("User ID is required", HttpStatus.BAD_REQUEST),
    USER_EMAIL_REQUIRED("User email is required", HttpStatus.BAD_REQUEST),

    // Email errors
    EMAIL_NOT_FOUND("Email not found", HttpStatus.NOT_FOUND),
    EMAIL_NOT_RETRYABLE("Email is not retryable", HttpStatus.BAD_REQUEST),
    EMAIL_RESEND_FAILED("Failed to resend email", HttpStatus.INTERNAL_SERVER_ERROR),
    RATE_LIMIT_EXCEEDED("Rate limit exceeded", HttpStatus.TOO_MANY_REQUESTS),

    // Template errors
    TEMPLATE_NOT_FOUND("Template not found", HttpStatus.NOT_FOUND),
    TEMPLATE_CODE_ALREADY_EXISTS("Template code already exists", HttpStatus.CONFLICT),
    INVALID_TEMPLATE_SYNTAX("Invalid template syntax", HttpStatus.BAD_REQUEST),
    TEMPLATE_NOT_BELONG_TO_TENANT("Template does not belong to tenant", HttpStatus.FORBIDDEN),

    // Provider errors
    NO_HEALTHY_PROVIDER("No healthy email provider available", HttpStatus.SERVICE_UNAVAILABLE),
    ;

    private final String message;
    private final HttpStatus httpStatus;
}
