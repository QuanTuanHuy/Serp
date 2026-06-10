package serp.project.school_bus_service.dto.response;

import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class RoutePlanStudentResponse {

    private Long id;
    private Long routeId;
    private Long studentId;
    private String studentName;
    private Long subscriptionId;
    private Long pickupStopId;
    private String pickupStopName;
    private Long dropoffStopId;
    private String dropoffStopName;
}
