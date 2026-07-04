package serp.project.school_bus_service.dto.request;

import jakarta.validation.constraints.NotNull;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class MoveStudentRequest {

    @NotNull(message = "studentId is required")
    private Long studentId;

    @NotNull(message = "subscriptionId is required")
    private Long subscriptionId;

    @NotNull(message = "targetRouteId is required")
    private Long targetRouteId;
}
