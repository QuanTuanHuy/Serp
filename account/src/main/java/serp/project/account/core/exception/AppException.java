package serp.project.account.core.exception;

import lombok.Getter;
import lombok.Setter;
import serp.project.account.core.domain.enums.ResponseEnum;

@Getter
@Setter
public class AppException extends RuntimeException {
    private ResponseEnum code;

    public AppException(String message) {
        super(message);
        this.code = ResponseEnum.msg400;
    }

    public AppException(String message, ResponseEnum code) {
        super(message);
        this.code = code;
    }
}