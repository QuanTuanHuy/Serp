package serp.project.tms_payment_service.exception;

import lombok.Getter;
import org.springframework.http.HttpStatus;
import org.springframework.http.HttpStatusCode;

@Getter
public enum ErrorCode {
    UNCATEGORIZED_EXCEPTION(9999, "Uncategorized error", HttpStatus.INTERNAL_SERVER_ERROR),
    INVALID_KEY(1001, "Uncategorized error", HttpStatus.BAD_REQUEST),
    UNAUTHENTICATED(1006, "Unauthenticated", HttpStatus.UNAUTHORIZED),
    UNAUTHORIZED(1007, "You do not have permission", HttpStatus.FORBIDDEN),
    INVALID_REQUEST(1008, "Invalid request", HttpStatus.BAD_REQUEST),
    
    // Payment specific errors
    PAYMENT_NOT_FOUND(2001, "Payment not found", HttpStatus.NOT_FOUND),
    PAYMENT_ALREADY_PROCESSED(2002, "Payment already processed", HttpStatus.BAD_REQUEST),
    PAYMENT_EXPIRED(2003, "Payment expired", HttpStatus.BAD_REQUEST),
    PAYMENT_FAILED(2004, "Payment failed", HttpStatus.BAD_REQUEST),
    INVALID_PAYMENT_DATA(2005, "Invalid payment data", HttpStatus.BAD_REQUEST),
    ZALOPAY_API_ERROR(2006, "ZaloPay API error", HttpStatus.INTERNAL_SERVER_ERROR),
    INVALID_CALLBACK(2007, "Invalid callback data", HttpStatus.BAD_REQUEST),
    ;

    ErrorCode(int code, String message, HttpStatusCode statusCode) {
        this.code = code;
        this.message = message;
        this.statusCode = statusCode;
    }

    private final int code;
    private final String message;
    private final HttpStatusCode statusCode;
}
