/**
 * Author: QuanTuanHuy
 * Description: Part of Serp Project
 */

package serp.project.mailservice.core.exception;

import lombok.Getter;
import lombok.Setter;
import serp.project.mailservice.core.domain.constant.Constants;
import serp.project.mailservice.core.domain.constant.ErrorMessages;

@Getter
@Setter
public class AppException extends RuntimeException {
    private Integer code;

    public AppException(String message) {
        super(message);
        this.code = getCodeBaseOnMessage(message);
    }

    public AppException(String message, Integer code) {
        super(message);
        this.code = code;
    }

    private Integer getCodeBaseOnMessage(String message) {
        return switch (message) {
            case ErrorMessages.UNAUTHORIZED -> Constants.HttpStatusCode.UNAUTHORIZED;
            case ErrorMessages.FORBIDDEN -> Constants.HttpStatusCode.FORBIDDEN;
            case ErrorMessages.NOT_FOUND -> Constants.HttpStatusCode.NOT_FOUND;
            case ErrorMessages.INTERNAL_SERVER_ERROR -> Constants.HttpStatusCode.INTERNAL_SERVER_ERROR;
            default -> Constants.HttpStatusCode.BAD_REQUEST;
        };
    }
}