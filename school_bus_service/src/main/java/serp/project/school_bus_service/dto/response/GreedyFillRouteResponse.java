package serp.project.school_bus_service.dto.response;

import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class GreedyFillRouteResponse {

    private Long routeId;
    private Long sessionId;
    private Integer addedStudents;
    private Integer addedStops;
    private Integer totalAssignedStudents;
    private Integer remainingCapacity;
    private Double plannedDistanceKm;
    private Integer plannedDurationMin;
    private Integer unassignedCandidates;
    private Integer skippedAssignedElsewhere;
    private Integer skippedMissingCoordinates;
    private Integer skippedInvalidPoint;
    private String message;
}
