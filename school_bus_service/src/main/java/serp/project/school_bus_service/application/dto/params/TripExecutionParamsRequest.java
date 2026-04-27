package serp.project.school_bus_service.application.dto.params;

import lombok.Getter;
import lombok.Setter;
import serp.project.school_bus_service.application.dto.request.BaseParamsRequest;

import java.time.LocalDate;

@Getter
@Setter
public class TripExecutionParamsRequest extends BaseParamsRequest {
    private Long routeId;
    private Long schoolId;
    private String status;
    private String direction;
    private LocalDate serviceDate;
}

