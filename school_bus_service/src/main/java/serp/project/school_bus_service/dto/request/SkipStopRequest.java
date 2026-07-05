package serp.project.school_bus_service.dto.request;

import jakarta.validation.constraints.NotBlank;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class SkipStopRequest extends BaseCommandRequest {

    @NotBlank(message = "Vui lòng nhập lý do")
    private String reason;
}
