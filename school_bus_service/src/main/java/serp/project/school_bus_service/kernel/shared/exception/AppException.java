package serp.project.school_bus_service.kernel.shared.exception;

import lombok.Getter;

@Getter
public class AppException extends RuntimeException {
    private final AppErrorCode errorCode;

    public AppException(AppErrorCode errorCode) {
        super(errorCode.getMessage());
        this.errorCode = errorCode;
    }
}
