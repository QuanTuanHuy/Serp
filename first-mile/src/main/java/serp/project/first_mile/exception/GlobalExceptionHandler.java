package serp.project.first_mile.exception;

import jakarta.validation.ConstraintViolation;
import jakarta.servlet.http.HttpServletRequest;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import serp.project.first_mile.dto.ApiResponse;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.MissingServletRequestParameterException;
import org.springframework.web.bind.annotation.ControllerAdvice;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.method.annotation.MethodArgumentTypeMismatchException;
import org.springframework.web.multipart.support.MissingServletRequestPartException;

import java.time.OffsetDateTime;
import java.util.Map;
import java.util.Objects;

@ControllerAdvice
@Slf4j
@RequiredArgsConstructor
public class GlobalExceptionHandler {

    private static final String MIN_ATTRIBUTE = "min";
    private final MessageService messageService;

    @ExceptionHandler(value = RuntimeException.class)
    ResponseEntity<ApiResponse<Void>> handlingRuntimeException(RuntimeException exception, HttpServletRequest request) {
        log.error("Exception: ", exception);
        ErrorCode errorCode = ErrorCode.UNCATEGORIZED_EXCEPTION;
        ApiResponse<Void> apiResponse = buildErrorResponse(
                errorCode,
                messageService.getMessage(errorCode.getMessageKey()),
                getExceptionDetail(exception),
                request
        );
        return ResponseEntity.status(errorCode.getStatusCode()).body(apiResponse);
    }

    @ExceptionHandler(value = AppException.class)
    ResponseEntity<ApiResponse<Void>> handlingAppException(AppException exception, HttpServletRequest request) {
        ErrorCode errorCode = exception.getErrorCode();
        ApiResponse<Void> apiResponse = buildErrorResponse(
                errorCode,
                messageService.getMessage(errorCode.getMessageKey()),
                                resolveAppExceptionDetail(exception),
                request
        );
        return ResponseEntity.status(errorCode.getStatusCode()).body(apiResponse);
    }

    @ExceptionHandler(value = AccessDeniedException.class)
    ResponseEntity<ApiResponse<Void>> handlingAccessDeniedException(
            AccessDeniedException exception,
            HttpServletRequest request
    ) {
        ErrorCode errorCode = ErrorCode.UNAUTHORIZED;

        return ResponseEntity.status(errorCode.getStatusCode())
            .body(buildErrorResponse(
                    errorCode,
                    messageService.getMessage(errorCode.getMessageKey()),
                    getExceptionDetail(exception),
                    request
            ));
    }

    @ExceptionHandler(value = MethodArgumentNotValidException.class)
    ResponseEntity<ApiResponse<Void>> handlingValidation(
            MethodArgumentNotValidException exception,
            HttpServletRequest request
    ) {
        String enumKey = exception.getFieldError().getDefaultMessage();

        ErrorCode errorCode = ErrorCode.INVALID_KEY;
        Map<String, Object> attributes = null;
        try {
            errorCode = ErrorCode.valueOf(enumKey);

            var constraintViolation =
                    exception.getBindingResult().getAllErrors().getFirst().unwrap(ConstraintViolation.class);

            attributes = constraintViolation.getConstraintDescriptor().getAttributes();

            log.info(attributes.toString());

        } catch (IllegalArgumentException e) {

        }

        // Get localized message with parameters
        String localizedMessage;
        if (Objects.nonNull(attributes)) {
            String minValue = String.valueOf(attributes.get(MIN_ATTRIBUTE));
            localizedMessage = messageService.getMessage(errorCode.getMessageKey(), new Object[]{minValue});
        } else {
            localizedMessage = messageService.getMessage(errorCode.getMessageKey());
        }

        String detail = exception.getFieldError() == null
                ? getExceptionDetail(exception)
                : String.format(
                        "field='%s', rejectedValue='%s', reason='%s'",
                        exception.getFieldError().getField(),
                        exception.getFieldError().getRejectedValue(),
                        exception.getFieldError().getDefaultMessage()
                );

        ApiResponse<Void> apiResponse = buildErrorResponse(errorCode, localizedMessage, detail, request);

        return ResponseEntity.badRequest().body(apiResponse);
    }

    @ExceptionHandler(value = MissingServletRequestParameterException.class)
    ResponseEntity<ApiResponse<Void>> handlingMissingServletRequestParameterException(
            MissingServletRequestParameterException exception,
            HttpServletRequest request
    ) {
        ErrorCode errorCode = ErrorCode.INVALID_REQUEST;
        String detail = String.format("Missing required request parameter '%s'.", exception.getParameterName());

        ApiResponse<Void> apiResponse = buildErrorResponse(
                errorCode,
                messageService.getMessage(errorCode.getMessageKey()),
                detail,
                request
        );

        return ResponseEntity.badRequest().body(apiResponse);
    }

    @ExceptionHandler(value = MissingServletRequestPartException.class)
    ResponseEntity<ApiResponse<Void>> handlingMissingServletRequestPartException(
            MissingServletRequestPartException exception,
            HttpServletRequest request
    ) {
        ErrorCode errorCode = ErrorCode.INVALID_REQUEST;
        String detail = String.format("Missing required request part '%s'.", exception.getRequestPartName());

        ApiResponse<Void> apiResponse = buildErrorResponse(
                errorCode,
                messageService.getMessage(errorCode.getMessageKey()),
                detail,
                request
        );

        return ResponseEntity.badRequest().body(apiResponse);
    }

    @ExceptionHandler(value = MethodArgumentTypeMismatchException.class)
    ResponseEntity<ApiResponse<Void>> handlingMethodArgumentTypeMismatchException(
            MethodArgumentTypeMismatchException exception,
            HttpServletRequest request
    ) {
        ErrorCode errorCode = ErrorCode.INVALID_REQUEST;
        String requiredType = exception.getRequiredType() == null
                ? "unknown"
                : exception.getRequiredType().getSimpleName();
        String detail = String.format(
                "Request parameter '%s' has invalid value '%s'; expected type '%s'.",
                exception.getName(),
                exception.getValue(),
                requiredType
        );

        ApiResponse<Void> apiResponse = buildErrorResponse(
                errorCode,
                messageService.getMessage(errorCode.getMessageKey()),
                detail,
                request
        );

        return ResponseEntity.badRequest().body(apiResponse);
    }

    private ApiResponse<Void> buildErrorResponse(
            ErrorCode errorCode,
            String message,
            String detail,
            HttpServletRequest request
    ) {
        return ApiResponse.<Void>builder()
                .code(errorCode.getCode())
                .message(message)
                .detail(detail)
                .path(request.getRequestURI())
                .timestamp(OffsetDateTime.now().toString())
                .build();
    }

    private String getExceptionDetail(Throwable exception) {
        Throwable root = exception;
        while (root.getCause() != null) {
            root = root.getCause();
        }

        String message = root.getMessage();
        return message == null || message.isBlank()
                ? root.getClass().getSimpleName()
                : String.format("%s: %s", root.getClass().getSimpleName(), message);
    }

        private String resolveAppExceptionDetail(AppException exception) {
                if (exception.getDetail() != null && !exception.getDetail().isBlank()) {
                        return exception.getDetail();
                }

                return getExceptionDetail(exception);
        }
}
