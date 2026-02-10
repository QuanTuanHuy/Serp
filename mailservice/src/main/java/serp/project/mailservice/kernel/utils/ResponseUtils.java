/**
 * Author: QuanTuanHuy
 * Description: Part of Serp Project
 */

package serp.project.mailservice.kernel.utils;

import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Component;

import serp.project.mailservice.core.domain.dto.response.GeneralResponse;

@Component
public class ResponseUtils {

    public <T> GeneralResponse<T> success(T data) {
        return GeneralResponse.<T>builder()
                .status("success")
                .code(HttpStatus.OK.value())
                .message("Success")
                .data(data)
                .build();
    }

    public GeneralResponse<?> error(int code, String message) {
        return GeneralResponse.builder()
                .status("error")
                .code(code)
                .message(message)
                .data(null)
                .build();
    }

    public GeneralResponse<?> badRequest(String message) {
        return error(HttpStatus.BAD_REQUEST.value(), message);
    }

    public GeneralResponse<?> unauthorized(String message) {
        return error(HttpStatus.UNAUTHORIZED.value(), message);
    }

    public GeneralResponse<?> forbidden(String message) {
        return error(HttpStatus.FORBIDDEN.value(), message);
    }

    public GeneralResponse<?> notFound(String message) {
        return error(HttpStatus.NOT_FOUND.value(), message);
    }

    public GeneralResponse<?> internalServerError(String message) {
        return error(HttpStatus.INTERNAL_SERVER_ERROR.value(), message);
    }

    public GeneralResponse<?> status(String message) {
        return GeneralResponse.builder()
                .status("success")
                .code(HttpStatus.OK.value())
                .message(message)
                .data(null)
                .build();
    }
}
