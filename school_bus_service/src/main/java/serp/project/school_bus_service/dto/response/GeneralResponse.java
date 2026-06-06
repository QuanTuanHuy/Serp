package serp.project.school_bus_service.dto.response;

import com.fasterxml.jackson.annotation.JsonInclude;
import lombok.Getter;
import org.springframework.http.HttpStatus;

@Getter
public class GeneralResponse<T> {

    private final int code;
    private final String status;
    private final String message;

    @JsonInclude(JsonInclude.Include.NON_NULL)
    private final T data;

    public GeneralResponse(HttpStatus httpStatus, String status, String message, T data) {
        this.code = httpStatus.value();
        this.status = status;
        this.message = message;
        this.data = data;
    }

    public GeneralResponse(HttpStatus httpStatus, String status, String message) {
        this(httpStatus, status, message, null);
    }

    public static <T> GeneralResponse<T> success(String message) {
        return new GeneralResponse<>(HttpStatus.OK, "SUCCESS", message);
    }

    public static <T> GeneralResponse<T> success(String message, T data) {
        return new GeneralResponse<>(HttpStatus.OK, "SUCCESS", message, data);
    }

    public static <T> GeneralResponse<T> success(HttpStatus httpStatus, String message, T data) {
        return new GeneralResponse<>(httpStatus, "SUCCESS", message, data);
    }

    public static <T> GeneralResponse<T> error(HttpStatus httpStatus, String status, String message) {
        return new GeneralResponse<>(httpStatus, status, message);
    }
}
