package serp.project.school_bus_service.dto.response;

import lombok.Getter;
import lombok.Setter;

/** A planning issue attached to a session, route, or student. */
@Getter
@Setter
public class PlanningIssueResponse {

    private Long id;
    private Long planningSessionId;
    private Long routeId;
    private Long routeStopId;
    private Long studentId;
    private String studentName;
    private Long subscriptionId;

    /** e.g. MISSING_COORDINATE, CAPACITY_EXCEEDED, DEFAULT_CAPACITY_USED */
    private String issueType;

    /** INFO | WARNING | BLOCKING */
    private String severity;

    private String message;
    private Boolean isResolved;
}
