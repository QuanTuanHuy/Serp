package serp.project.school_bus_service.dto.response;

import lombok.Getter;
import lombok.Setter;

import java.util.List;

/** Response for the demand-preview endpoint. */
@Getter
@Setter
public class PlanningPreviewResponse {

    private Long schoolId;
    private String schoolName;
    private Long schoolScheduleId;
    private String schoolScheduleName;
    private String serviceDate;
    private String routeDirection;

    private Integer totalEligibleStudents;
    private Integer totalEligiblePickupPoints;

    private List<EligibleStudentResponse> eligibleStudents;
    private List<EligiblePickupPointResponse> eligiblePickupPoints;
    private List<PlanningIssueResponse> issues;

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
