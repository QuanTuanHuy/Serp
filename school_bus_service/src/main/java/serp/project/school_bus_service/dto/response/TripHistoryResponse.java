package serp.project.school_bus_service.dto.response;

import lombok.Getter;
import lombok.Setter;

import java.time.LocalDate;
import java.time.LocalDateTime;

@Getter
@Setter
public class TripHistoryResponse extends BaseResponse {

    private Long routeId;
    private String routeCode;
    private LocalDate serviceDate;
    private String status;
    private LocalDateTime startedAt;
    private LocalDateTime completedAt;
    private Long busId;
    private String busPlateNumber;
    private Long driverId;
    private String driverName;
    private Long attendantId;
    private String attendantName;
}
