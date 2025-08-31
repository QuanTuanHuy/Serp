package serp.project.account.kernel.utils;

import lombok.Data;
import lombok.NoArgsConstructor;
import org.springframework.stereotype.Component;
import serp.project.account.core.domain.constant.Constants;
import serp.project.account.core.domain.dto.GeneralResponse;
import serp.project.account.core.domain.enums.ResponseEnum;

@Component
@Data
@NoArgsConstructor
public class GenericResponse<T> {
    private T message;
    private ResponseEnum responseMessage;

    public GenericResponse(T message, ResponseEnum responseMessage) {
        this.message = message;
        this.responseMessage = responseMessage;
    }

    public GeneralResponse<?> matchingResponseMessage(GenericResponse<?> validation) {
        switch (validation.getResponseMessage()) {
            case msg200 -> {
                return GeneralResponse.builder()
                        .status(Constants.HttpStatus.SUCCESS)
                        .code(200)
                        .message(Constants.ErrorMessage.OK)
                        .data(validation.getMessage())
                        .build();
            }
            case msg400 -> {
                return GeneralResponse.builder()
                        .status(Constants.HttpStatus.ERROR)
                        .code(400)
                        .message(validation.getMessage().toString())
                        .data(null)
                        .build();
            }
            case msg401 -> {
                return GeneralResponse.builder()
                        .status(Constants.HttpStatus.ERROR)
                        .code(401)
                        .message(validation.getMessage().toString())
                        .data(null)
                        .build();
            }
            case msg403 -> {
                return GeneralResponse.builder()
                        .status(Constants.HttpStatus.ERROR)
                        .code(403)
                        .message(validation.getMessage().toString())
                        .data(null)
                        .build();
            }
            case msg404 -> {
                return GeneralResponse.builder()
                        .status(Constants.HttpStatus.ERROR)
                        .code(404)
                        .message(validation.getMessage().toString())
                        .data(null)
                        .build();
            }
            case msg500 -> {
                return GeneralResponse.builder()
                        .status(Constants.HttpStatus.ERROR)
                        .code(500)
                        .message(validation.getMessage().toString())
                        .data(null)
                        .build();
            }
            case status -> {
                return GeneralResponse.builder()
                        .status(Constants.HttpStatus.SUCCESS)
                        .code(200)
                        .message(validation.getMessage().toString())
                        .data(null)
                        .build();
            }
            default -> {
                return GeneralResponse.builder()
                        .status(Constants.HttpStatus.ERROR)
                        .code(500)
                        .message(validation.getMessage().toString())
                        .data(null)
                        .build();
            }
        }
    }
}
