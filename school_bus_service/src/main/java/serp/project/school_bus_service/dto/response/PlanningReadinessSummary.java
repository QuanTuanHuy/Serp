package serp.project.school_bus_service.dto.response;

import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class PlanningReadinessSummary {
    private int totalSubscriptions;
    private int eligibleStudents;
    private int blockedStudents;
    private int warningStudents;

    private int pointCount;
    private int pickupPointCount;
    private int dropoffPointCount;

    private int missingCoordinateCount;
    private int missingWindowCount;
    private int pausedCount;
    private int inactiveCount;
    private int outOfEffectiveRangeCount;
    private int dayMismatchCount;
}
