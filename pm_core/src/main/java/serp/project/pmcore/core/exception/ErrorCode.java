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

    // Project errors
    PROJECT_NOT_FOUND("Project not found", HttpStatus.NOT_FOUND),
    PROJECT_KEY_ALREADY_EXISTS("Project with this key already exists", HttpStatus.CONFLICT),
    PROJECT_KEY_INVALID_FORMAT("Project key must be 2-10 uppercase alphanumeric characters starting with a letter", HttpStatus.BAD_REQUEST),
    PROJECT_ARCHIVED("Cannot modify an archived project", HttpStatus.BAD_REQUEST),
    PROJECT_ALREADY_ARCHIVED("Project is already archived", HttpStatus.BAD_REQUEST),
    PROJECT_NOT_ARCHIVED("Project is not archived", HttpStatus.BAD_REQUEST),

    // Category errors
    CATEGORY_NOT_FOUND("Project category not found", HttpStatus.NOT_FOUND),
    CATEGORY_NAME_ALREADY_EXISTS("Project category with this name already exists", HttpStatus.CONFLICT),
    CATEGORY_IN_USE("Cannot delete category that has projects assigned", HttpStatus.BAD_REQUEST),

    // Blueprint errors
    BLUEPRINT_NOT_FOUND("Project blueprint not found", HttpStatus.NOT_FOUND),
    BLUEPRINT_IS_SYSTEM("Cannot modify system blueprint", HttpStatus.BAD_REQUEST),

    // Scheme errors
    SCHEME_NOT_FOUND("Scheme not found", HttpStatus.NOT_FOUND),
    SCHEME_INCOMPATIBLE("Scheme is not compatible", HttpStatus.UNPROCESSABLE_ENTITY),
    SCHEME_PROVISIONING_FAILED("Failed to provision project schemes", HttpStatus.INTERNAL_SERVER_ERROR),

    // User errors
    USER_NOT_FOUND("User not found", HttpStatus.NOT_FOUND),

    ;

    private final String message;
    private final HttpStatus httpStatus;
}
