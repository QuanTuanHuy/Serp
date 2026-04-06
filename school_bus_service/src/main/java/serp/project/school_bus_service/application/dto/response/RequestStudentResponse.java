package serp.project.school_bus_service.application.dto.response;

import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class RequestStudentResponse extends BaseResponse {

    private Long requestId;
    private Long studentId;
    private String studentName;
    private Long pickupPointId;
    private String pickupPointName;
}
