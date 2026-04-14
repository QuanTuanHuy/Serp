package serp.project.school_bus_service.application.dto.request;

import jakarta.validation.constraints.NotBlank;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Getter
@Setter
@NoArgsConstructor
public class DepotUpsertRequest extends BaseCommandRequest {

    @NotBlank
    private String name;

    private String address;

    private Double latitude;

    private Double longitude;

    private String contactPhone;

    private String description;
}
