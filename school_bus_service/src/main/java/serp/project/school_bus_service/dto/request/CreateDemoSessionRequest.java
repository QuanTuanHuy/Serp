package serp.project.school_bus_service.dto.request;

import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class CreateDemoSessionRequest extends BaseCommandRequest {

    @Min(10)
    @Max(3600)
    private Integer durationSeconds;

    private Boolean autoAdvanceStops;

    private Boolean autoAttendance;
}
