package serp.project.school_bus_service.application.dto.request;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.time.LocalTime;

@Getter
@Setter
@NoArgsConstructor
public class PickupPointUpsertRequest extends BaseCommandRequest {

    @NotNull
    private Long schoolId;

    @NotBlank
    private String name;

    @NotBlank
    private String address;

    private Double latitude;

    private Double longitude;

    private LocalTime pickupWindowStart;

    private LocalTime pickupWindowEnd;
}
