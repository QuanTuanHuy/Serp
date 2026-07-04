package serp.project.school_bus_service.dto.request;

import jakarta.validation.constraints.NotBlank;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Getter
@Setter
@NoArgsConstructor
public class PickupPointUpsertRequest extends BaseCommandRequest {

    @NotBlank
    private String name;

    @NotBlank
    private String address;

    private Double latitude;
    private Double longitude;

    private String code;
    private String usageType;
    private String pickupInstruction;
}
