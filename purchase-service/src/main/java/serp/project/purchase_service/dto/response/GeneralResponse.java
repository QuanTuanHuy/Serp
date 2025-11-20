package serp.project.purchase_service.dto.response;

import com.fasterxml.jackson.annotation.JsonInclude;
import lombok.Getter;
import org.springframework.http.HttpStatus;

@Getter
public class GeneralResponse<T> {

    private final HttpStatus httpStatus;
    private final String code;
    private final String message;

    @JsonInclude(JsonInclude.Include.NON_NULL)
    private T data;

    public GeneralResponse(HttpStatus httpStatus, String code, String message, T data) {
        this.httpStatus = httpStatus;
        this.code = code;
        this.message = message;
        this.data = data;
    }

    public GeneralResponse(HttpStatus httpStatus, String code, String message) {
        this.httpStatus = httpStatus;
        this.code = code;
        this.message = message;
    }

    public static <T> GeneralResponse<T> success(String message) {
        return new GeneralResponse<>(HttpStatus.OK, "SUCCESS", message);
    }

    public static <T> GeneralResponse<T> success(String message, T data) {
        return new GeneralResponse<>(HttpStatus.OK, "SUCCESS", message, data);
    }

    public static <T> GeneralResponse<T> error(HttpStatus httpStatus, String code, String message) {
        return new GeneralResponse<>(httpStatus, code, message);
    }

}
