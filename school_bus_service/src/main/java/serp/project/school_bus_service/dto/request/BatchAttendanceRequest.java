package serp.project.school_bus_service.dto.request;

import jakarta.validation.constraints.NotEmpty;
import jakarta.validation.constraints.NotNull;
import lombok.Getter;
import lombok.Setter;

import java.util.List;

@Getter
@Setter
public class BatchAttendanceRequest extends BaseCommandRequest {

    @NotNull(message = "action is required")
    private String action;

    @NotEmpty(message = "studentIds is required")
    private List<Long> studentIds;

    private String note;
}
