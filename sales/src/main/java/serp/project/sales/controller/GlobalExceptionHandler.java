package serp.project.sales.controller;

import lombok.extern.slf4j.Slf4j;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ControllerAdvice;
import org.springframework.web.bind.annotation.ExceptionHandler;
import serp.project.sales.dto.response.GeneralResponse;
import serp.project.sales.exception.AppErrorCode;
import serp.project.sales.exception.AppException;

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
        log.error("Unexpected error: {}", e.getMessage());
        AppErrorCode errorCode = AppErrorCode.UNEXPECTED_EXCEPTION;

        return ResponseEntity
                .status(errorCode.getStatus())
                .body(GeneralResponse.error(
                        errorCode.getStatus(),
                        "FAILED",
                        errorCode.getMessage()
                ));
    }

    @ExceptionHandler(value = DataIntegrityViolationException.class)
    public ResponseEntity<GeneralResponse<?>> handleDataIntegrityViolationException(DataIntegrityViolationException e) {
        log.error("Data integrity violation: {}", e.getMessage());
        AppErrorCode errorCode = AppErrorCode.DATA_INTEGRITY_VIOLATION;

        return ResponseEntity
                .status(errorCode.getStatus())
                .body(GeneralResponse.error(
                        errorCode.getStatus(),
                        "FAILED",
                        "Data integrity violation occurred"
                ));
    }

    @ExceptionHandler(value = MethodArgumentNotValidException.class)
    public ResponseEntity<GeneralResponse<?>> handleMethodArgumentNotValidException(MethodArgumentNotValidException e) {
        log.error("Validation error: {}", e.getMessage());
        AppErrorCode errorCode = AppErrorCode.REQUEST_VALIDATION_FAILED;

        return ResponseEntity
                .status(errorCode.getStatus())
                .body(GeneralResponse.error(
                        errorCode.getStatus(),
                        "FAILED",
                        "Validation error occurred"
                ));
    }

}
