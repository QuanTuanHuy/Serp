package serp.project.school_bus_service.dto.request;

import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotNull;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class JumpToProgressRequest {

    @NotNull
    @Min(0)
    @Max(100)
    private Double progressPercent;
}
