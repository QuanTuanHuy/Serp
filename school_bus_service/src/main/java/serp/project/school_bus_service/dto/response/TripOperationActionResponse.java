package serp.project.school_bus_service.dto.response;

import lombok.Getter;
import lombok.Setter;

import java.time.LocalDateTime;

@Getter
@Setter
public class TripOperationActionResponse {
    private Long tripId;
    private String tripStatus;
    private Long routeStopId;
    private String stopStatus;
    private LocalDateTime actualArrivalTime;
    private LocalDateTime actualDepartureTime;
    private LocalDateTime updatedAt;
    private String message;
}
