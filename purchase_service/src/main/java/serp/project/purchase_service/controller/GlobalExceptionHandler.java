package serp.project.purchase_service.controller;

import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.ControllerAdvice;
import org.springframework.web.bind.annotation.ExceptionHandler;
import serp.project.purchase_service.dto.response.GeneralResponse;
import serp.project.purchase_service.exception.AppErrorCode;
import serp.project.purchase_service.exception.AppException;

@ControllerAdvice
@Slf4j
public class GlobalExceptionHandler {

    @ExceptionHandler(value = AppException.class)
    public ResponseEntity<GeneralResponse<?>> handleAppException(AppException e) {
        AppErrorCode errorCode = e.getErrorCode();
        log.error("Error code: {}, message: {}", errorCode.getCode(), errorCode.getMessage());

        return ResponseEntity
                .status(errorCode.getStatus())
                .body(GeneralResponse.error(
                        errorCode.getStatus(),
                        "FAILED",
                        errorCode.getMessage()
                ));
    }

    @ExceptionHandler(value = Exception.class)
    public ResponseEntity<GeneralResponse<?>> handleRuntimeException(Exception e) {
        log.error("Unexpected error: {}", e.getMessage(), e);
        AppErrorCode errorCode = AppErrorCode.UNEXPECTED_EXCEPTION;

        return ResponseEntity
                .status(errorCode.getStatus())
                .body(GeneralResponse.error(
                        errorCode.getStatus(),
                        "FAILED",
                        errorCode.getMessage()
                ));
    }

}
