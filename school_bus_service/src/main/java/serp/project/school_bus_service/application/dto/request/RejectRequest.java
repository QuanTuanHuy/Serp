package serp.project.school_bus_service.application.dto.request;

import jakarta.validation.constraints.NotBlank;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Getter
@Setter
@NoArgsConstructor
public class RejectRequest extends BaseCommandRequest {

    @NotBlank
    private String reason;
}
