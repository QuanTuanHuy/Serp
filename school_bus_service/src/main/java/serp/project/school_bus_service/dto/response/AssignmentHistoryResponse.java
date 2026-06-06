package serp.project.school_bus_service.dto.response;

import lombok.Getter;
import lombok.Setter;

import java.time.LocalDateTime;

@Getter
@Setter
public class AssignmentHistoryResponse extends BaseResponse {

    private Long routeId;

    private Long oldBusId;
    private Long newBusId;

    private Long oldDriverId;
    private Long newDriverId;

    private Long oldAttendantId;
    private Long newAttendantId;

    private Long changedBy;
    private LocalDateTime changedAt;
    private String reason;
}
