package serp.project.school_bus_service.dto.params;

import lombok.Getter;
import lombok.Setter;
import serp.project.school_bus_service.dto.request.BaseParamsRequest;

@Getter
@Setter
public class SchoolScheduleParamsRequest extends BaseParamsRequest {
    private Long schoolId;
    private String shiftType;
    private String educationLevel;
}
