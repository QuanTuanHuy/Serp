package serp.project.school_bus_service.dto.params;

import lombok.Getter;
import lombok.Setter;
import serp.project.school_bus_service.dto.request.BaseParamsRequest;

@Getter
@Setter
public class StudentParamsRequest extends BaseParamsRequest {
    private Long schoolId;
    private Long parentProfileId;
}
