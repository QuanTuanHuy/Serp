package serp.project.school_bus_service.dto.response;

import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class RouteCalculationTraceResponse extends BaseResponse {
    private Long routePlanId;
    private Long planningSessionId;
    private String calculationType;
    private String calculationStatus;
    private String inputJson;
    private String matrixJson;
    private String timelineJson;
    private String issuesJson;
    private String configSnapshotJson;
    private String sourceSummary;
}
