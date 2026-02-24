/**
 * Author: QuanTuanHuy
 * Description: Part of Serp Project
 */

package serp.project.pmcore.core.exception;

import org.springframework.http.HttpStatus;

import lombok.Getter;
import lombok.RequiredArgsConstructor;

@Getter
@RequiredArgsConstructor
public enum ErrorCode {
    // Success
    SUCCESS("Success", HttpStatus.OK),

    // General errors
    UNAUTHORIZED("Unauthorized", HttpStatus.UNAUTHORIZED),
    FORBIDDEN("You do not have permission to perform this action", HttpStatus.FORBIDDEN),
    REQUEST_TIMEOUT("Request timeout", HttpStatus.REQUEST_TIMEOUT),
    BAD_REQUEST("Bad request", HttpStatus.BAD_REQUEST),
    NOT_FOUND("Resource not found", HttpStatus.NOT_FOUND),
    INTERNAL_SERVER_ERROR("Internal server error", HttpStatus.INTERNAL_SERVER_ERROR),
    SERVICE_UNAVAILABLE("Service is currently unavailable", HttpStatus.SERVICE_UNAVAILABLE),
    CONFLICT("Conflict occurred", HttpStatus.CONFLICT),
    TOO_MANY_REQUESTS("Too many requests", HttpStatus.TOO_MANY_REQUESTS),
    INVALID_TOKEN("Invalid or expired token", HttpStatus.UNAUTHORIZED),
    
    ;

    private final String message;
    private final HttpStatus httpStatus;
}
