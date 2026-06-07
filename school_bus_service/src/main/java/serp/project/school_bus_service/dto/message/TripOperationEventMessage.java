package serp.project.school_bus_service.dto.message;

import lombok.Data;
import java.time.LocalDateTime;

@Data
public class TripOperationEventMessage {
    private Long tripId;
    private Long stopId;
    private Long studentId;
    private String action;
    private Long tenantId;
    private String eventType; // "TRIP_UPDATE", "ATTENDANCE_UPDATE"
    private Double progressPercent;
    private LocalDateTime timestamp;
}
