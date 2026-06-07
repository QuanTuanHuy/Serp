package serp.project.school_bus_service.dto.response;

import lombok.Getter;
import lombok.Setter;

import java.util.List;

/** Route quality summary computed after generation or recalculation. */
@Getter
@Setter
public class RouteQualityResponse {

    private Long routeId;
    private String routeCode;
    private String routeName;
    private String status;

    private Integer studentCount;
    private Integer stopCount;
    private Double totalDistanceKm;
    private Integer totalDurationMin;
    private Integer requiredCapacity;
    private Double capacityUtilizationPercent;

    /** 0–100 overall quality score */
    private Double qualityScore;

    private Integer blockingIssueCount;
    private Integer warningIssueCount;
    private Integer infoIssueCount;

    /** Whether arrivalDeadline / departureTime is respected */
    private String arrivalDeadlineStatus;
    private String departureTimeStatus;

    private List<PlanningIssueResponse> issues;
}
