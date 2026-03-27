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
