package serp.project.sales.exception;

import lombok.Getter;
import org.springframework.http.HttpStatus;

@Getter
public enum AppErrorCode {
        UNIMPLEMENTED("SALES-APP-001", "Feature not implemented", HttpStatus.NOT_IMPLEMENTED),
        UNEXPECTED_EXCEPTION("SALES-APP-002", "An unexpected error occurred", HttpStatus.INTERNAL_SERVER_ERROR),

        NOT_FOUND("SALES-BIZ-001", "Entity not found or access denied", HttpStatus.NOT_FOUND),
        INVALID_STATUS_TRANSITION("SALES-BIZ-002", "Invalid status transition", HttpStatus.BAD_REQUEST),
        CANNOT_UPDATE_ORDER_IN_CURRENT_STATUS("SALES-BIZ-003", "Cannot update order in its current status",
                        HttpStatus.BAD_REQUEST),
        ORDER_NOT_APPROVED_YET("SALES-BIZ-004", "Order has not been approved yet", HttpStatus.BAD_REQUEST),
        INSUFFICIENT_PRODUCT_QUANTITY("SALES-BIZ-005", "Insufficient product quantity available",
                        HttpStatus.BAD_REQUEST),
        DATA_INTEGRITY_VIOLATION("SALES-BIZ-006", "Data integrity violation", HttpStatus.BAD_REQUEST),
        CANNOT_DELETE_ORDER_IN_CURRENT_STATUS("SALES-BIZ-007", "Cannot delete order in its current status",
                        HttpStatus.BAD_REQUEST),
        INSUFFICIENT_INVENTORY_TO_ALLOCATE("SALES-BIZ-008", "Insufficient inventory to allocate for the order",
                        HttpStatus.BAD_REQUEST),
    NEED_TO_BE_RESERVED_OR_DELIVERED("SALES-BIZ-009", "There are still items that need to be reserved or delivered", HttpStatus.BAD_REQUEST),

        UNKNOWN_ENUM_VALUE("SALES-VAL-001", "Unknown enum value provided", HttpStatus.BAD_REQUEST),
        REQUEST_VALIDATION_FAILED("SALES-VAL-002", "Request validation failed", HttpStatus.BAD_REQUEST),

        UNAUTHORIZED("SALES-SEC-001", "Unauthorized access", HttpStatus.UNAUTHORIZED),
        CANNOT_ACCESS("SALES-SEC-002", "Cannot access the requested resource", HttpStatus.FORBIDDEN),
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
