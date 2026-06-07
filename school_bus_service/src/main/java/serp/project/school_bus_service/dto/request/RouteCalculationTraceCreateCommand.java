package serp.project.school_bus_service.dto.request;

import lombok.Getter;
import lombok.Setter;
import serp.project.school_bus_service.enums.RouteCalculationStatus;
import serp.project.school_bus_service.enums.RouteCalculationType;

@Getter
@Setter
public class RouteCalculationTraceCreateCommand extends BaseCommandRequest {
    private Long routePlanId;
    private Long planningSessionId;
    private Long tenantId;
    private RouteCalculationType calculationType;
    private RouteCalculationStatus calculationStatus;
    private String inputJson;
    private String matrixJson;
    private String timelineJson;
    private String issuesJson;
    private String configSnapshotJson;
    private String sourceSummary;
}
