package serp.project.first_mile.exception;

import lombok.Getter;
import org.springframework.http.HttpStatus;
import org.springframework.http.HttpStatusCode;

@Getter
public enum ErrorCode {
    // Lỗi liên quan đến PostOffice (10xx)
    POST_OFFICE_OVERLOADED(1001, "error.1001", HttpStatus.BAD_REQUEST),

    // Lỗi dữ liệu PostOffice/Location (94xx)
    PROVINCE_NOT_FOUND(9404, "error.9404", HttpStatus.NOT_FOUND),
    POST_OFFICE_NOT_FOUND(9405, "error.9405", HttpStatus.NOT_FOUND),
    POST_OFFICE_CODE_EXISTED(9406, "error.9406", HttpStatus.CONFLICT),
    WARD_NOT_FOUND(9407, "error.9407", HttpStatus.NOT_FOUND),
    PRODUCT_TYPE_NOT_FOUND(9408, "error.9408", HttpStatus.NOT_FOUND),
    PRODUCT_TYPE_CODE_EXISTED(9409, "error.9409", HttpStatus.CONFLICT),
    GEOCODE_NOT_FOUND(9410, "error.9410", HttpStatus.NOT_FOUND),
    POST_OFFICE_STAFF_NOT_FOUND(9411, "error.9411", HttpStatus.NOT_FOUND),
    VEHICLE_NOT_FOUND(9412, "error.9412", HttpStatus.NOT_FOUND),
    VEHICLE_LICENSE_PLATE_EXISTED(9413, "error.9413", HttpStatus.CONFLICT),
    VEHICLE_OWNER_MUST_BE_COURIER(9414, "error.9414", HttpStatus.BAD_REQUEST),
    VEHICLE_OWNER_STAFF_NOT_ACTIVE(9415, "error.9415", HttpStatus.BAD_REQUEST),
    ORDER_NOT_FOUND(9416, "error.9416", HttpStatus.NOT_FOUND),
    ORDER_NOT_ASSIGNABLE(9417, "error.9417", HttpStatus.BAD_REQUEST),
    ORDER_ALREADY_ASSIGNED_TO_PICKUP_TRIP(9418, "error.9418", HttpStatus.CONFLICT),
    COURIER_NOT_ASSIGNED_TO_POST_OFFICE(9419, "error.9419", HttpStatus.BAD_REQUEST),
    STORAGE_CONFIGURATION_INVALID(9430, "error.9430", HttpStatus.INTERNAL_SERVER_ERROR),
    FILE_UPLOAD_EMPTY(9431, "error.9431", HttpStatus.BAD_REQUEST),
    FILE_SIZE_EXCEEDED(9432, "error.9432", HttpStatus.PAYLOAD_TOO_LARGE),
    FILE_UPLOAD_FAILED(9433, "error.9433", HttpStatus.INTERNAL_SERVER_ERROR),
    FILE_IMAGE_TYPE_INVALID(9434, "error.9434", HttpStatus.BAD_REQUEST),
    RATE_LIMIT_EXCEEDED(9429, "error.9429", HttpStatus.TOO_MANY_REQUESTS),

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
