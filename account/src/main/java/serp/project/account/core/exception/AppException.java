/**
 * Author: QuanTuanHuy
 * Description: Part of Serp Project
 */

package serp.project.account.core.exception;

import lombok.Getter;
import lombok.Setter;
import serp.project.account.core.domain.constant.Constants;

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
            case Constants.ErrorMessage.BAD_REQUEST ->  Constants.HttpStatusCode.BAD_REQUEST;
            case Constants.ErrorMessage.UNAUTHORIZED -> Constants.HttpStatusCode.UNAUTHORIZED;
            case Constants.ErrorMessage.FORBIDDEN -> Constants.HttpStatusCode.FORBIDDEN;
            case Constants.ErrorMessage.NOT_FOUND -> Constants.HttpStatusCode.NOT_FOUND;
            default -> Constants.HttpStatusCode.INTERNAL_SERVER_ERROR;
        };
    }
}