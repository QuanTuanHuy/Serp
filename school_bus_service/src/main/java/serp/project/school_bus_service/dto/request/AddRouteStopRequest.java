package serp.project.school_bus_service.dto.request;

import jakarta.validation.constraints.NotNull;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class AddRouteStopRequest {

    @NotNull(message = "pickupPointId is required")
    private Long pickupPointId;

    private String stopType;

    private Integer estimatedStudentCount;
}
