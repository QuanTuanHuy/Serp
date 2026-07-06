package serp.project.school_bus_service.dto.request;

import jakarta.validation.constraints.NotNull;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class SchoolPickupPointUpsertRequest {

    @NotNull(message = "Vui lòng chọn điểm đón/trả.")
    private Long pickupPointId;

    private Boolean isDefault;
    private Boolean isActive;
}
