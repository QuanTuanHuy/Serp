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
        PLAN_IN_OPTIMIZATION("LOGISTICS2-BIZ-007", "Delivery plan is currently being optimized",
                        HttpStatus.BAD_REQUEST),
        DELIVERY_SLIP_ALREADY_ASSIGNED("LOGISTICS2-BIZ-008", "Delivery slip is already assigned",
                        HttpStatus.BAD_REQUEST),
        VEHICLE_SHIPPER_NOT_AVAILABLE("LOGISTICS2-BIZ-009",
                        "Vehicle-shipper assignment is not available for the delivery date",
                        HttpStatus.BAD_REQUEST),
        CANNOT_DELETE_DELIVERY_PLAN("LOGISTICS2-BIZ-010",
                        "Cannot delete delivery plan with optimization status OPTIMIZING or above",
                        HttpStatus.BAD_REQUEST),
        ROUTE_ALREADY_SELECTED("LOGISTICS2-BIZ-011", "Route has already been selected for delivery",
                        HttpStatus.BAD_REQUEST),
        PREVIOUS_STOP_NOT_ARRIVED("LOGISTICS2-BIZ-012", "Previous stop has not been arrived yet",
                        HttpStatus.BAD_REQUEST),
        ANOTHER_ROUTE_IN_PROGRESS("LOGISTICS2-BIZ-013",
                        "Another route is currently in progress for the same vehicle-shipper. Please complete that route before selecting a new one.",
                        HttpStatus.BAD_REQUEST),
        DELIVERY_SLIP_NOT_AVAILABLE("LOGISTICS2-BIZ-014",
                        "Delivery slip is not available for assignment", HttpStatus.BAD_REQUEST),
        VEHICLE_SHIPPER_NOT_FOUND("LOGISTICS2-BIZ-015",
                        "Vehicle-shipper assignment not found or access denied", HttpStatus.NOT_FOUND),
        INSUFFICIENT_QUANTITY("LOGISTICS2-BIZ-016", "Insufficient quantity available", HttpStatus.BAD_REQUEST),
    VEHICLE_ALREADY_ASSIGNED("LOGISTICS2-BIZ-017", "Vehicle is already assigned for the given date", HttpStatus.BAD_REQUEST),

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
