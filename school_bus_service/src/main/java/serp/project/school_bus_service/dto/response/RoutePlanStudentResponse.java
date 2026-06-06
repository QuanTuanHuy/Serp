package serp.project.school_bus_service.dto.response;

import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class RoutePlanStudentResponse {

    private Long id;
    private Long routeId;
    private Long routeStopId;
    private Long studentId;
    private String studentName;
    private Long subscriptionId;
    private String serviceAction;
    /** @deprecated use stopDisplayName — kept for backward compatibility */
    private String stopName;
    private String stopDisplayName;
    private String stopLocationType;
    private String stopPurpose;
    private String plannedTime;
}
