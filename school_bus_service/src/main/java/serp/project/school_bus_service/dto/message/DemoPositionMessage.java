package serp.project.school_bus_service.dto.message;

import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.time.LocalDateTime;

@Getter
@Setter
@NoArgsConstructor
public class DemoPositionMessage {

    private Long demoSessionId;
    private Long tripId;
    private String tripCode;
    private Long routeId;
    private String routeCode;
    private String status;
    private Double progressPercent;
    private Double currentLatitude;
    private Double currentLongitude;
    private Integer currentStopOrder;
    private LocalDateTime lastTickAt;
    private String lastEventType;
    private String eventType;
    private LocalDateTime timestamp;
}
