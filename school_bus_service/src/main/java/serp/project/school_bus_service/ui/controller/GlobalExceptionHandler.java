package serp.project.school_bus_service.ui.controller;

import lombok.extern.slf4j.Slf4j;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ControllerAdvice;
import org.springframework.web.bind.annotation.ExceptionHandler;
import serp.project.school_bus_service.application.dto.response.GeneralResponse;
import serp.project.school_bus_service.kernel.shared.exception.AppErrorCode;
import serp.project.school_bus_service.kernel.shared.exception.AppException;

@ControllerAdvice
@Slf4j
public class GlobalExceptionHandler {

    @ExceptionHandler(AppException.class)
    public ResponseEntity<?> handleAppException(AppException e) {
        AppErrorCode errorCode = e.getErrorCode();
        return ResponseEntity.status(errorCode.getStatus())
                .body(GeneralResponse.error(errorCode.getStatus(), "FAILED", errorCode.getMessage()));
    }

    @ExceptionHandler(DataIntegrityViolationException.class)
    public ResponseEntity<?> handleDataIntegrityViolationException(DataIntegrityViolationException e) {
        log.error("Data integrity violation", e);
        AppErrorCode errorCode = AppErrorCode.DATA_INTEGRITY_VIOLATION;
        return ResponseEntity.status(errorCode.getStatus())
                .body(GeneralResponse.error(errorCode.getStatus(), "FAILED", errorCode.getMessage()));
    }

    @ExceptionHandler(MethodArgumentNotValidException.class)
    public ResponseEntity<?> handleMethodArgumentNotValidException(MethodArgumentNotValidException e) {
        AppErrorCode errorCode = AppErrorCode.REQUEST_VALIDATION_FAILED;
        return ResponseEntity.status(errorCode.getStatus())
                .body(GeneralResponse.error(errorCode.getStatus(), "FAILED", errorCode.getMessage()));
    }

    @ExceptionHandler(Exception.class)
    public ResponseEntity<?> handleException(Exception e) {
        log.error("Unexpected exception", e);
        AppErrorCode errorCode = AppErrorCode.UNEXPECTED_EXCEPTION;
        return ResponseEntity.status(errorCode.getStatus())
                .body(GeneralResponse.error(errorCode.getStatus(), "FAILED", errorCode.getMessage()));
    }
}
