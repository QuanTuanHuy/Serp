/**
 * Author: QuanTuanHuy
 * Description: Part of Serp Project
 */

package serp.project.mailservice.kernel.utils;

import org.springframework.stereotype.Component;

import serp.project.mailservice.core.domain.constant.Constants;
import serp.project.mailservice.core.domain.constant.ErrorMessages;
import serp.project.mailservice.core.domain.dto.response.GeneralResponse;

@Component
public class ResponseUtils {

    public <T> GeneralResponse<T> success(T data) {
        return GeneralResponse.<T>builder()
                .status(Constants.HttpStatus.SUCCESS)
                .code(Constants.HttpStatusCode.SUCCESS)
                .message(ErrorMessages.SUCCESS)
                .data(data)
                .build();
    }

    public GeneralResponse<?> error(int code, String message) {
        return GeneralResponse.builder()
                .status(Constants.HttpStatus.ERROR)
                .code(code)
                .message(message)
                .data(null)
                .build();
    }

    public GeneralResponse<?> badRequest(String message) {
        return error(Constants.HttpStatusCode.BAD_REQUEST, message);
    }

    public GeneralResponse<?> unauthorized(String message) {
        return error(Constants.HttpStatusCode.UNAUTHORIZED, message);
    }

    public GeneralResponse<?> forbidden(String message) {
        return error(Constants.HttpStatusCode.FORBIDDEN, message);
    }

    public GeneralResponse<?> notFound(String message) {
        return error(Constants.HttpStatusCode.NOT_FOUND, message);
    }

    public GeneralResponse<?> internalServerError(String message) {
        return error(Constants.HttpStatusCode.INTERNAL_SERVER_ERROR, message);
    }

    public GeneralResponse<?> status(String message) {
        return GeneralResponse.builder()
                .status(Constants.HttpStatus.SUCCESS)
                .code(Constants.HttpStatusCode.SUCCESS)
                .message(message)
                .data(null)
                .build();
    }
}