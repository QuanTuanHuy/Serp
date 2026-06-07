package serp.project.school_bus_service.dto.response;

import lombok.Getter;
import lombok.Setter;

import java.util.List;

@Getter
@Setter
public class RouteManualValidationResponse {
    private Long routePlanId;
    private String routeCode;
    private String routeName;
    private boolean isValid;
    private int issueCount;
    private int blockingIssueCount;
    private int warningIssueCount;
    private List<PlanningIssueResponse> issues;
    private List<StopValidationResponse> stops;

    @Getter
    @Setter
    public static class StopValidationResponse {
        private Long stopId;
        private String displayName;
        private Integer stopOrder;
        private String locationType;
        private String plannedArrivalTime;
        private String plannedDepartureTime;
        private boolean isTerminal;
        private int issueCount;
        private boolean hasBlockingIssue;
        private List<PlanningIssueResponse> issues;
    }
}
