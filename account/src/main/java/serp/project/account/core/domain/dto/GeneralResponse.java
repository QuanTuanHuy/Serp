package serp.project.account.core.domain.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@NoArgsConstructor
@AllArgsConstructor
@Data
@Builder
public class GeneralResponse<T> {
    private String status;
    private Integer code;
    private String message;
    private T data;

    public boolean isSuccess() {
        return code != null && code >= 200 && code < 300;
    }
}
