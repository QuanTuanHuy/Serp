package serp.project.school_bus_service.dto.response;

import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class PlanningReadinessIssueResponse {
    private String severity; // BLOCKING / WARNING / INFO
    private String code;
    private String label;
    private Long subscriptionId;
    private Long studentId;
    private Long pointId;
}
