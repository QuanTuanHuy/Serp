package serp.project.school_bus_service.dto.request;

import jakarta.validation.constraints.NotNull;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Getter
@Setter
@NoArgsConstructor
public class AttendanceActionRequest extends BaseCommandRequest {

    @NotNull
    private Long routeId;

    @NotNull
    private Long studentId;

    private String notes;
}
