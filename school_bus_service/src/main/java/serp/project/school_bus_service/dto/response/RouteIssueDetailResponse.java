package serp.project.school_bus_service.dto.response;

import lombok.Getter;
import lombok.Setter;
import lombok.NoArgsConstructor;
import lombok.AllArgsConstructor;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class RouteIssueDetailResponse {
    private String issueType;
    private String severity;
    private String message;

    private Long routeStopId;
    private String stopName;

    private Long studentId;
    private String studentName;

    private String suggestedFix;

    public static String getSuggestedFix(String issueType) {
        if (issueType == null) return "";
        return switch (issueType.toUpperCase()) {
            case "TIME_WINDOW_LATE" -> "Adjust the pickup/drop-off window, reorder stops, choose another depot, or split the route.";
            case "MISSING_TIME_WINDOW" -> "Configure the pickup/drop-off time window for this school schedule and direction.";
            case "MISSING_COORDINATES", "MISSING_COORDINATE" -> "Update coordinates for this stop before route calculation.";
            case "STOP_DIRECTION_NOT_COMPATIBLE" -> "Check whether this route direction should use pickup or drop-off points.";
            case "ROUTE_CAPACITY_EXCEEDED", "CAPACITY_OVERFLOW" -> "Assign a bus with larger capacity or split students into multiple routes.";
            case "BUS_NOT_ASSIGNED_CAPACITY_UNKNOWN" -> "Assign a bus to verify capacity constraints.";
            default -> "";
        };
    }
}
