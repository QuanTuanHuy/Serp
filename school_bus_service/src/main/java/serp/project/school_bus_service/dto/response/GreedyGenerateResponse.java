package serp.project.school_bus_service.dto.response;

import lombok.Getter;
import lombok.Setter;

import java.util.List;

/** Summary returned after greedy generation for a session. */
@Getter
@Setter
public class GreedyGenerateResponse {

    private PlanningSessionResponse session;
    private List<RouteQualityResponse> routes;
    private Integer totalUnassignedStudents;
    private List<EligibleStudentResponse> unassignedStudents;
    private List<PlanningIssueResponse> sessionIssues;
}
