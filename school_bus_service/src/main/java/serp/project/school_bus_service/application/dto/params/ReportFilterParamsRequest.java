package serp.project.school_bus_service.application.dto.params;

import lombok.Getter;
import lombok.Setter;
import serp.project.school_bus_service.application.dto.request.BaseParamsRequest;

import java.time.LocalDate;

@Getter
@Setter
public class ReportFilterParamsRequest extends BaseParamsRequest {
    private LocalDate dateFrom;
    private LocalDate dateTo;
    private Long schoolId;
    private Long routeId;
    private Long tripId;
    private String direction;
    private String tripStatus;
}

