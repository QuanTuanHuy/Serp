package serp.project.school_bus_service.dto.response;

import lombok.Getter;
import lombok.Setter;

import java.time.LocalDateTime;

@Getter
@Setter
public class TripStopLogResponse extends BaseResponse {
    private Long tripId;
    private Long routeStopId;
    private String stopName;
    private Integer stopOrder;
    private String status;
    private LocalDateTime actualArrivalTime;
    private LocalDateTime actualDepartureTime;
    private Integer delayMinutes;
    private Integer actualBoardedCount;
    private Integer actualDroppedCount;
    private String note;
}

