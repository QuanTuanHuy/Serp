package serp.project.second_mile.exception;

import lombok.Getter;
import org.springframework.http.HttpStatus;
import org.springframework.http.HttpStatusCode;

@Getter
public enum ErrorCode {

    // Hub errors (1xxx)
    HUB_NOT_FOUND(1001, "error.1001", HttpStatus.NOT_FOUND),
    HUB_CODE_EXISTED(1002, "error.1002", HttpStatus.CONFLICT),
    HUB_POST_OFFICE_CODE_INVALID(1003, "error.1003", HttpStatus.BAD_REQUEST),

    // Vehicle errors (11xx)
    VEHICLE_NOT_FOUND(1101, "error.1101", HttpStatus.NOT_FOUND),
    VEHICLE_LICENSE_PLATE_EXISTED(1102, "error.1102", HttpStatus.CONFLICT),

    // Bag errors (12xx)
    BAG_NOT_FOUND(1201, "error.1201", HttpStatus.NOT_FOUND),
    BAG_CODE_EXISTED(1202, "error.1202", HttpStatus.CONFLICT),
    BAG_DESTINATION_INVALID(1203, "error.1203", HttpStatus.BAD_REQUEST),
    BAG_ORDER_NOT_FOUND(1204, "error.1204", HttpStatus.NOT_FOUND),
    BAG_ORDER_ALREADY_ASSIGNED(1205, "error.1205", HttpStatus.CONFLICT),
    BAG_ORDER_ALREADY_IN_BAG(1206, "error.1206", HttpStatus.CONFLICT),
    BAG_ORDER_MAPPING_NOT_FOUND(1207, "error.1207", HttpStatus.NOT_FOUND),
    BAG_VEHICLE_INVALID(1208, "error.1208", HttpStatus.BAD_REQUEST),
    BAG_HUB_INVALID(1209, "error.1209", HttpStatus.BAD_REQUEST),
    BAG_POST_OFFICE_INVALID(1210, "error.1210", HttpStatus.BAD_REQUEST),
    BAG_STATUS_INVALID(1211, "error.1211", HttpStatus.BAD_REQUEST),

    // Route errors (13xx)
    ROUTE_NOT_FOUND(1301, "error.1301", HttpStatus.NOT_FOUND),
    ROUTE_CODE_EXISTED(1302, "error.1302", HttpStatus.CONFLICT),
    ROUTE_DEFINITION_INVALID(1303, "error.1303", HttpStatus.BAD_REQUEST),
    ROUTE_HUB_INVALID(1304, "error.1304", HttpStatus.BAD_REQUEST),
    ROUTE_POST_OFFICE_INVALID(1305, "error.1305", HttpStatus.BAD_REQUEST),
    ROUTE_VEHICLE_INVALID(1306, "error.1306", HttpStatus.BAD_REQUEST),

    RATE_LIMIT_EXCEEDED(9429, "error.9429", HttpStatus.TOO_MANY_REQUESTS),

    STORAGE_CONFIGURATION_INVALID(9430, "error.9430", HttpStatus.INTERNAL_SERVER_ERROR),
    FILE_SIZE_EXCEEDED(9432, "error.9432", HttpStatus.PAYLOAD_TOO_LARGE),
    FILE_UPLOAD_EMPTY(9431, "error.9431", HttpStatus.BAD_REQUEST),
    FILE_UPLOAD_FAILED(9433, "error.9433", HttpStatus.INTERNAL_SERVER_ERROR),
    FILE_IMAGE_TYPE_INVALID(9434, "error.9434", HttpStatus.BAD_REQUEST),
    // Lỗi khác (99xx)
    UNCATEGORIZED_EXCEPTION(9999, "error.9999", HttpStatus.INTERNAL_SERVER_ERROR),
    INVALID_KEY(9998, "error.9998", HttpStatus.BAD_REQUEST),
    DATA_INTEGRITY_VIOLATION(9997, "error.9997", HttpStatus.CONFLICT),
    INVALID_REQUEST(9996, "error.9996", HttpStatus.BAD_REQUEST),
    UNAUTHORIZED(9995, "error.9995", HttpStatus.FORBIDDEN)
    ;
    ErrorCode(int code, String messageKey, HttpStatusCode statusCode) {
        this.code = code;
        this.messageKey = messageKey;
        this.statusCode = statusCode;
    }

    private final int code;
    private final String messageKey;
    private final HttpStatusCode statusCode;
}
