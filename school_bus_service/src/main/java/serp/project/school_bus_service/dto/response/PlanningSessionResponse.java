package serp.project.school_bus_service.dto.response;

import lombok.Getter;
import lombok.Setter;

import java.time.LocalDate;
import java.time.LocalDateTime;

@Getter
@Setter
public class PlanningSessionResponse {

    private Long id;
    private Long schoolId;
    private String schoolName;

    private LocalDate serviceDate;
    private String routeDirection;
    private String planningMethod;
    private String status;

    private Integer totalEligibleStudents;
    private Integer totalPlannedStudents;
    private Integer totalUnassignedStudents;
    private Integer totalRoutes;
    private Integer totalStops;
    private Double totalDistanceKm;
    private Integer totalDurationMin;

    private LocalDateTime generatedAt;
    private LocalDateTime publishedAt;
    private String planningNotes;

    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;
}
