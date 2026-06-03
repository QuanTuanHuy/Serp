/*
Author: Nguyen The Anh
Description: Part of Serp Project
*/

package serp.project.tms_order.exception;

import lombok.Getter;
import org.springframework.http.HttpStatus;
import org.springframework.http.HttpStatusCode;

@Getter
public enum ErrorCode {
    // Lỗi liên quan đến PostOffice (10xx)
    POST_OFFICE_OVERLOADED(1001, "error.1001", HttpStatus.BAD_REQUEST),

    POST_OFFICE_NOT_FOUND(9405, "error.9405", HttpStatus.NOT_FOUND),
    PRODUCT_TYPE_NOT_FOUND(9408, "error.9408", HttpStatus.NOT_FOUND),
    PRODUCT_TYPE_CODE_EXISTED(9409, "error.9409", HttpStatus.CONFLICT),
    ORDER_NOT_FOUND(9416, "error.9416", HttpStatus.NOT_FOUND),
    ORDER_NOT_ASSIGNABLE(9417, "error.9417", HttpStatus.BAD_REQUEST),
    NO_SUITABLE_ORIGIN_POST_OFFICE(9421, "error.9421", HttpStatus.BAD_REQUEST),
    ORDER_CUSTOMER_CODE_EXISTED(9422, "error.9422", HttpStatus.CONFLICT),
    ORDER_NOT_EDITABLE(9423, "error.9423", HttpStatus.BAD_REQUEST),

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
