package serp.project.school_bus_service.dto.request;

import jakarta.validation.constraints.NotNull;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class TripAttendanceActionRequest extends BaseCommandRequest {

    @NotNull(message = "studentId is required")
    private Long studentId;

    @NotNull(message = "routeStopId is required")
    private Long routeStopId;

    private String notes;
}

