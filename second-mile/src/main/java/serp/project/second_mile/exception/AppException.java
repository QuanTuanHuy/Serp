package serp.project.second_mile.exception;

import lombok.Getter;

@Getter
public class AppException extends RuntimeException {

    private final ErrorCode errorCode;
    private final String detail;

    public AppException(ErrorCode errorCode) {
        this(errorCode, null);
    }

    public AppException(ErrorCode errorCode, String detail) {
        super(errorCode.getMessageKey());
        this.errorCode = errorCode;
        this.detail = detail;
    }
}
