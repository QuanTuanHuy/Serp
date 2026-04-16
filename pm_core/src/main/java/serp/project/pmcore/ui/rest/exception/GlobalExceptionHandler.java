/**
 * Author: QuanTuanHuy
 * Description: Part of Serp Project
 */

package serp.project.pmcore.ui.rest.exception;

import jakarta.validation.ConstraintViolationException;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;

import serp.project.pmcore.domain.shared.exception.*;
import serp.project.pmcore.ui.rest.shared.response.ResponseUtils;

import java.util.stream.Collectors;

@RestControllerAdvice
@RequiredArgsConstructor
@Slf4j
public class GlobalExceptionHandler {
    private final ResponseUtils responseUtils;

    @ExceptionHandler(ResourceNotFoundException.class)
    public ResponseEntity<?> handle(ResourceNotFoundException ex) {
        log.warn("Not found: code={}, message={}", ex.getErrorCode(), ex.getMessage());
        return ResponseEntity.status(HttpStatus.NOT_FOUND)
                .body(responseUtils.error(404, ex.getMessage()));
    }

    @ExceptionHandler(AccessDeniedException.class)
    public ResponseEntity<?> handle(AccessDeniedException ex) {
        log.warn("Access denied: code={}, message={}", ex.getErrorCode(), ex.getMessage());
        return ResponseEntity.status(HttpStatus.FORBIDDEN)
                .body(responseUtils.error(403, ex.getMessage()));
    }

    @ExceptionHandler(ConcurrencyException.class)
    public ResponseEntity<?> handle(ConcurrencyException ex) {
        log.warn("Concurrency conflict: code={}, message={}", ex.getErrorCode(), ex.getMessage());
        return ResponseEntity.status(HttpStatus.CONFLICT)
                .body(responseUtils.error(409, ex.getMessage()));
    }

    @ExceptionHandler(DomainValidationException.class)
    public ResponseEntity<?> handle(DomainValidationException ex) {
        log.warn("Domain validation failed: code={}, violations={}",
                ex.getErrorCode(), ex.getViolations());

        Object body = ex.getViolations().isEmpty()
                ? responseUtils.error(422, ex.getMessage())
                : responseUtils.error(422, ex.getMessage(), ex.getViolations());

        return ResponseEntity.status(HttpStatus.UNPROCESSABLE_ENTITY).body(body);
    }


    @ExceptionHandler(BusinessRuleViolationException.class)
    public ResponseEntity<?> handle(BusinessRuleViolationException ex) {
        HttpStatus status = resolveBusinessRuleStatus(ex.getErrorCode());
        log.warn("Business rule violation: code={}, status={}, message={}",
                ex.getErrorCode(), status, ex.getMessage());
        return ResponseEntity.status(status)
                .body(responseUtils.error(status.value(), ex.getMessage()));
    }

    @ExceptionHandler(AppException.class)
    public ResponseEntity<?> handleAppException(AppException ex) {
        log.warn("Application error: Code={}, Message={}", ex.getCode(), ex.getMessage());
        
        var response = responseUtils.error(ex.getCode(), ex.getMessage());
        return ResponseEntity.status(ex.getCode()).body(response);
    }

    @ExceptionHandler(IllegalStateException.class)
    public ResponseEntity<?> handleIllegalStateException(IllegalStateException ex) {
        log.warn("Illegal state error: {}", ex.getMessage());
        
        var response = responseUtils.badRequest(ex.getMessage());
        return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(response);
    }

    @ExceptionHandler(IllegalArgumentException.class)
    public ResponseEntity<?> handleIllegalArgumentException(IllegalArgumentException ex) {
        log.warn("Illegal argument error: {}", ex.getMessage());
        
        var response = responseUtils.badRequest(ex.getMessage());
        return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(response);
    }

    @ExceptionHandler(MethodArgumentNotValidException.class)
    public ResponseEntity<?> handleValidationErrors(MethodArgumentNotValidException ex) {
        String errorMessage = ex.getBindingResult()
                .getFieldErrors()
                .stream()
                .map(error -> error.getField() + ": " + error.getDefaultMessage())
                .collect(Collectors.joining(", "));

        log.warn("Validation error: {}", errorMessage);

        var response = responseUtils.badRequest(errorMessage);
        return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(response);
    }

    @ExceptionHandler(ConstraintViolationException.class)
    public ResponseEntity<?> handleConstraintViolation(ConstraintViolationException ex) {
        String errorMessage = ex.getConstraintViolations()
                .stream()
                .map(violation -> violation.getPropertyPath() + ": " + violation.getMessage())
                .collect(Collectors.joining(", "));

        log.warn("Constraint violation: {}", errorMessage);

        var response = responseUtils.badRequest(errorMessage);
        return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(response);
    }

    @ExceptionHandler(Exception.class)
    public ResponseEntity<?> handleGenericException(Exception ex) {
        log.error("Unexpected error: ", ex);

        var response = responseUtils.internalServerError(ex.getMessage());
        return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(response);
    }

    private HttpStatus resolveBusinessRuleStatus(DomainErrorCode code) {
        return switch (code) {
            case PROJECT_KEY_ALREADY_EXISTS,
                 CATEGORY_NAME_ALREADY_EXISTS,
                 CATEGORY_IN_USE,
                 COMPONENT_NAME_ALREADY_EXISTS,
                 VERSION_NAME_ALREADY_EXISTS,
                 ROLE_NAME_ALREADY_EXISTS,
                 ROLE_IN_USE_BY_PERMISSION,
                 ISSUE_TYPE_KEY_ALREADY_EXISTS,
                 PRIORITY_NAME_ALREADY_EXISTS,
                 PRIORITY_IN_USE,
                 STATUS_KEY_ALREADY_EXISTS,
                 DUPLICATE_ISSUE_LINK,
                 DUPLICATE_PERMISSION_ENTRY,
                 DUPLICATE_SECURITY_MEMBER,
                 ROLE_ACTOR_ALREADY_ASSIGNED,
                 SPRINT_ALREADY_ACTIVE -> HttpStatus.CONFLICT;

            case USER_NOT_FOUND,
                 SERVICE_ACCOUNT_NOT_FOUND -> HttpStatus.NOT_FOUND;

            case PROJECT_PERMISSION_DENIED,
                 WORKLOG_NOT_OWNER,
                 WORK_ITEM_SECURITY_ACCESS_DENIED,
                 TRANSITION_CONDITION_FAILED -> HttpStatus.FORBIDDEN;

            case PROJECT_ARCHIVED,
                 ROLE_IS_SYSTEM -> HttpStatus.CONFLICT;

            case SCHEME_INCOMPATIBLE,
                  WORKFLOW_VALIDATION_FAILED,
                  TRANSITION_VALIDATION_FAILED,
                  FIELD_NOT_WRITABLE_ON_UPDATE,
                   FIELD_CANNOT_BE_REQUIRED_AND_HIDDEN,
                  SUMMARY_FIELD_CANNOT_BE_HIDDEN -> HttpStatus.UNPROCESSABLE_ENTITY;

            default -> HttpStatus.BAD_REQUEST;
        };
    }

}
