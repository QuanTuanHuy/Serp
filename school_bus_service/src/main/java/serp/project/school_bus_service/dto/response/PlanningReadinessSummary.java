package serp.project.school_bus_service.dto.response;

import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class PlanningReadinessSummary {
    private int totalSubscriptions;
    private int eligibleStudents;
    private int pointCount;
    private int pickupPointCount;
    private int dropoffPointCount;
}
