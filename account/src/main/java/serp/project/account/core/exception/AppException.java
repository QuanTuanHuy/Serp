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
        this.code = Constants.HttpStatusCode.BAD_REQUEST;
    }

    public AppException(String message, Integer code) {
        super(message);
        this.code = code;
    }
}