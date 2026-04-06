package serp.project.school_bus_service.application.dto.request;

import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Getter
@Setter
@NoArgsConstructor
public class BusUpsertRequest extends BaseCommandRequest {

    @NotBlank
    private String plateNumber;

    private String busType;

    @NotNull
    @Min(1)
    private Integer capacity;

    @NotBlank
    private String status;
}
