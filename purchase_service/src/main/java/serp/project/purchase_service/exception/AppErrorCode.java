package serp.project.purchase_service.exception;

import lombok.Getter;
import org.springframework.http.HttpStatus;

@Getter
public enum AppErrorCode {
    UNIMPLEMENTED("PURCHASE-APP-001", "Feature not implemented", HttpStatus.NOT_IMPLEMENTED),
    UNEXPECTED_EXCEPTION("PURCHASE-APP-002", "An unexpected error occurred", HttpStatus.INTERNAL_SERVER_ERROR),
    NOT_FOUND("PURCHASE-BIZ-001", "Entity not found or access denied", HttpStatus.NOT_FOUND),
    INVALID_STATUS_TRANSITION("PURCHASE-BIZ-002", "Invalid status transition", HttpStatus.BAD_REQUEST),
    UNKNOWN_ENUM_VALUE("PURCHASE-VAL-001", "Unknown enum value provided", HttpStatus.BAD_REQUEST),
    ORDER_NOT_READY_FOR_DELIVERY("PURCHASE-BIZ-003", "Order is not ready for delivery", HttpStatus.BAD_REQUEST),
    ORDER_NOT_APPROVED_YET("PURCHASE-BIZ-004", "Order has not been approved yet", HttpStatus.BAD_REQUEST),
    CANNOT_UPDATE_ORDER_IN_CURRENT_STATUS("PURCHASE-BIZ-005", "Cannot update order in its current status", HttpStatus.BAD_REQUEST),
    CANNOT_ACCESS("PURCHASE-SEC-002", "Cannot access the requested resource", HttpStatus.FORBIDDEN),
    UNAUTHORIZED("PURCHASE-SEC-001", "Unauthorized access", HttpStatus.UNAUTHORIZED);

    private final String code;
    private final String message;
    private final HttpStatus status;

    AppErrorCode(String code, String message, HttpStatus status) {
        this.code = code;
        this.message = message;
        this.status = status;
    }

}
