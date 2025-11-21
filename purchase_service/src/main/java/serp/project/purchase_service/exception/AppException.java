package serp.project.purchase_service.exception;

import lombok.Getter;

@Getter
public class AppException extends RuntimeException {
    private final AppErrorCode errorCode;

    public AppException(AppErrorCode errorCode) {
        super(errorCode.getMessage());
        this.errorCode = errorCode;
    }

    public String getCode() {
        return errorCode.getCode();
    }


}
