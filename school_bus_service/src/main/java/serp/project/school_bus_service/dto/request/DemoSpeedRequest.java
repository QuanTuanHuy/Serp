package serp.project.school_bus_service.dto.request;

import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotNull;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class DemoSpeedRequest extends BaseCommandRequest {

    @NotNull(message = "speedMultiplier is required")
    @Min(1)
    @Max(10)
    private Integer speedMultiplier;
}

