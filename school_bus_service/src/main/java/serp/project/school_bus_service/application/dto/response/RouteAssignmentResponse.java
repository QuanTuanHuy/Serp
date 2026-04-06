package serp.project.school_bus_service.application.dto.response;

import lombok.Getter;
import lombok.Setter;

import java.time.LocalDateTime;

@Getter
@Setter
public class RouteAssignmentResponse extends BaseResponse {

    private Long routeId;
    private Long busId;
    private String busPlateNumber;
    private Long driverId;
    private String driverName;
    private Long attendantId;
    private String attendantName;
    private LocalDateTime assignedAt;
}
