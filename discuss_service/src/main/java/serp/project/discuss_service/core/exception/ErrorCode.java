/**
 * Author: QuanTuanHuy
 * Description: Part of Serp Project - Error codes for Discuss Service
 */

package serp.project.discuss_service.core.exception;

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
    
    // Channel errors (400, 403, 404)
    CHANNEL_NOT_FOUND("Channel not found", HttpStatus.NOT_FOUND),
    CHANNEL_ARCHIVED("Cannot perform action on archived channel", HttpStatus.BAD_REQUEST),
    CHANNEL_UPDATE_FORBIDDEN("You do not have permission to update this channel", HttpStatus.FORBIDDEN),
    CHANNEL_ARCHIVE_FORBIDDEN("You do not have permission to archive this channel", HttpStatus.FORBIDDEN),
    CHANNEL_DELETE_FORBIDDEN("You do not have permission to delete this channel", HttpStatus.FORBIDDEN),
    CHANNEL_NAME_REQUIRED("Channel name is required", HttpStatus.BAD_REQUEST),
    CANNOT_UPDATE_DIRECT_CHANNEL("Cannot update name of direct message channel", HttpStatus.BAD_REQUEST),
    CANNOT_ARCHIVE_DIRECT_CHANNEL("Cannot archive direct message channel", HttpStatus.BAD_REQUEST),
    CHANNEL_ALREADY_ARCHIVED("Channel is already archived", HttpStatus.BAD_REQUEST),
    
    // Member errors (400, 403, 404)
    NOT_CHANNEL_MEMBER("You are not a member of this channel", HttpStatus.FORBIDDEN),
    MEMBER_NOT_FOUND("Member not found", HttpStatus.NOT_FOUND),
    CANNOT_ADD_MEMBERS("You do not have permission to add members", HttpStatus.FORBIDDEN),
    CANNOT_REMOVE_MEMBERS("You do not have permission to remove members", HttpStatus.FORBIDDEN),
    CANNOT_SEND_MESSAGES("You cannot send messages in this channel", HttpStatus.FORBIDDEN),
    ALREADY_CHANNEL_MEMBER("User is already a member of this channel", HttpStatus.BAD_REQUEST),
    CANNOT_REMOVE_OWNER("Cannot remove channel owner", HttpStatus.BAD_REQUEST),
    OWNER_CANNOT_LEAVE("Owner cannot leave channel. Transfer ownership first", HttpStatus.BAD_REQUEST),
    ALREADY_LEFT_CHANNEL("Already left the channel", HttpStatus.BAD_REQUEST),
    MEMBER_NOT_ACTIVE("Member is not active in channel", HttpStatus.BAD_REQUEST),
    
    // Message errors (400, 403, 404)
    MESSAGE_NOT_FOUND("Message not found", HttpStatus.NOT_FOUND),
    MESSAGE_DELETED("Cannot perform action on deleted message", HttpStatus.BAD_REQUEST),
    CANNOT_EDIT_MESSAGE("Only the sender can edit this message", HttpStatus.FORBIDDEN),
    CANNOT_DELETE_MESSAGE("Only the sender or admin can delete this message", HttpStatus.FORBIDDEN),
    CANNOT_EDIT_SYSTEM_MESSAGE("Cannot edit system messages", HttpStatus.BAD_REQUEST),
    PARENT_MESSAGE_NOT_IN_CHANNEL("Parent message is not in the specified channel", HttpStatus.BAD_REQUEST),
    MESSAGE_CONTENT_REQUIRED("Message content is required", HttpStatus.BAD_REQUEST),
    
    // Validation errors (400)
    INVALID_REQUEST("Invalid request", HttpStatus.BAD_REQUEST),
    VALIDATION_ERROR("Validation error", HttpStatus.BAD_REQUEST),
    TENANT_ID_REQUIRED("Tenant ID is required", HttpStatus.BAD_REQUEST),
    USER_ID_REQUIRED("User ID is required", HttpStatus.BAD_REQUEST),
    
    // Server errors (500)
    DATABASE_ERROR("Database error occurred", HttpStatus.INTERNAL_SERVER_ERROR),
    CACHE_ERROR("Cache error occurred", HttpStatus.INTERNAL_SERVER_ERROR),
    EVENT_PUBLISH_ERROR("Failed to publish event", HttpStatus.INTERNAL_SERVER_ERROR),

    // File/Attachment errors (400, 404, 413)
    ATTACHMENT_NOT_FOUND("Attachment not found", HttpStatus.NOT_FOUND),
    ATTACHMENT_NOT_AVAILABLE("Attachment is not available for download", HttpStatus.BAD_REQUEST),
    FILE_REQUIRED("File is required", HttpStatus.BAD_REQUEST),
    FILE_TOO_LARGE("File size exceeds maximum allowed size", HttpStatus.PAYLOAD_TOO_LARGE),
    FILE_TYPE_NOT_ALLOWED("File type is not allowed", HttpStatus.BAD_REQUEST),
    FILE_UPLOAD_FAILED("Failed to upload file", HttpStatus.INTERNAL_SERVER_ERROR),
    TOO_MANY_FILES("Too many files. Maximum allowed files per message exceeded", HttpStatus.BAD_REQUEST),

    // Presence errors
    PRESENCE_NOT_FOUND("Presence not found", HttpStatus.NOT_FOUND),
    ;

    private final String message;
    private final HttpStatus httpStatus;
}
