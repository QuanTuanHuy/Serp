package serp.project.school_bus_service.shared.exception;

import lombok.Getter;

@Getter
public class AppException extends RuntimeException {
    private final AppErrorCode.ErrorInfo errorCode;

    public AppException(AppErrorCode.ErrorInfo errorCode) {
        super(errorCode.defaultMessage());
        this.errorCode = errorCode;
    }

    public AppException(AppErrorCode.ErrorInfo errorCode, String message) {
        super(message);
        this.errorCode = errorCode;
    }
}
