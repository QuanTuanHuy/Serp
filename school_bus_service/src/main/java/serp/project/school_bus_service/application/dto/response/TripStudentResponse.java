package serp.project.school_bus_service.application.dto.response;

import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class TripStudentResponse extends BaseResponse {
    private Long tripId;
    private Long studentId;
    private String studentName;
    private Long pickupStopId;
    private Long dropoffStopId;
    private Long subscriptionId;
    private String status;
    private String note;
}

