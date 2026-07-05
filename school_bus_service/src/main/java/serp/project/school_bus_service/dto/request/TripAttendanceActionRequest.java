package serp.project.school_bus_service.dto.request;

import jakarta.validation.constraints.NotNull;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class TripAttendanceActionRequest extends BaseCommandRequest {

    @NotNull(message = "Vui lòng chọn học sinh")
    private Long studentId;

    @NotNull(message = "Vui lòng chọn điểm dừng")
    private Long routeStopId;

    private String notes;
}

