package serp.project.school_bus_service.dto.response;

import lombok.Getter;
import lombok.Setter;
import java.time.DayOfWeek;
import java.time.LocalDate;
import java.time.LocalTime;
import java.util.List;

/** Response for the demand-preview endpoint. */
@Getter
@Setter
public class PlanningPreviewResponse {

    private Long schoolId;
    private String schoolCode;
    private String schoolName;
    private String schoolAddress;

    private List<DayOfWeek> activeDays;

    private LocalDate serviceDate;
    private DayOfWeek serviceDayOfWeek;
    private String direction;
    private String planningMethod;

    private PlanningReadinessSummary summary;

    private List<PlanningDemandResponse> eligibleDemands;
    private List<PlanningPointResponse> points;

    // --- Legacy / Backward compatibility fields ---
    private String routeDirection;
    private Integer totalEligibleStudents;
    private Integer totalEligiblePickupPoints;
    private List<EligibleStudentResponse> eligibleStudents;
    private List<EligiblePickupPointResponse> eligiblePickupPoints;

    @Getter
    @Setter
    public static class EligiblePickupPointResponse {
        private Long pickupPointId;
        private String pickupPointName;
        private Double latitude;
        private Double longitude;
        private Integer studentCount;
        private Boolean hasWindow;
    }
}
