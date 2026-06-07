package serp.project.school_bus_service.shared.exception;

import lombok.Getter;

@Getter
public class AppException extends RuntimeException {
    private final AppErrorCode.ErrorInfo errorCode;
    private final Object data;

    public AppException(AppErrorCode.ErrorInfo errorCode) {
        super(errorCode.defaultMessage());
        this.errorCode = errorCode;
        this.data = null;
    }

    public AppException(AppErrorCode.ErrorInfo errorCode, String message) {
        super(message);
        this.errorCode = errorCode;
        this.data = null;
    }

    public AppException(AppErrorCode.ErrorInfo errorCode, String message, Object data) {
        super(message);
        this.errorCode = errorCode;
        this.data = data;
    }
}
