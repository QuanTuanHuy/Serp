package serp.project.school_bus_service.controller;

import lombok.extern.slf4j.Slf4j;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ControllerAdvice;
import org.springframework.web.bind.annotation.ExceptionHandler;
import serp.project.school_bus_service.dto.response.GeneralResponse;
import serp.project.school_bus_service.shared.exception.AppErrorCode;
import serp.project.school_bus_service.shared.exception.AppException;

@ControllerAdvice
@Slf4j
public class GlobalExceptionHandler {

    @ExceptionHandler(AppException.class)
    public ResponseEntity<GeneralResponse<Void>> handleAppException(AppException e) {
        AppErrorCode.ErrorInfo errorCode = e.getErrorCode();
        return ResponseEntity.status(errorCode.status())
                .body(GeneralResponse.error(errorCode.status(), "FAILED", e.getMessage()));
    }

    @ExceptionHandler(DataIntegrityViolationException.class)
    public ResponseEntity<GeneralResponse<Void>> handleDataIntegrityViolationException(DataIntegrityViolationException e) {
        log.error("Data integrity violation", e);
        AppErrorCode.ErrorInfo errorCode = AppErrorCode.DATA_INTEGRITY_VIOLATION;
        return ResponseEntity.status(errorCode.status())
                .body(GeneralResponse.error(errorCode.status(), "FAILED", errorCode.defaultMessage()));
    }

    @ExceptionHandler(MethodArgumentNotValidException.class)
    public ResponseEntity<GeneralResponse<Void>> handleMethodArgumentNotValidException(MethodArgumentNotValidException e) {
        AppErrorCode.ErrorInfo errorCode = AppErrorCode.REQUEST_VALIDATION_FAILED;
        return ResponseEntity.status(errorCode.status())
                .body(GeneralResponse.error(errorCode.status(), "FAILED", errorCode.defaultMessage()));
    }

    @ExceptionHandler(Exception.class)
    public ResponseEntity<GeneralResponse<Void>> handleException(Exception e) {
        log.error("Unexpected exception", e);
        AppErrorCode.ErrorInfo errorCode = AppErrorCode.UNEXPECTED_EXCEPTION;
        return ResponseEntity.status(errorCode.status())
                .body(GeneralResponse.error(errorCode.status(), "FAILED", errorCode.defaultMessage()));
    }
}
