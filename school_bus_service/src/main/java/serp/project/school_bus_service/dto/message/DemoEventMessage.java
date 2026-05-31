package serp.project.school_bus_service.dto.message;

import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.time.LocalDateTime;

@Getter
@Setter
@NoArgsConstructor
public class DemoEventMessage {

    private Long demoSessionId;
    private Long tripId;
    private String eventType;
    private LocalDateTime eventTime;
    private String payloadJson;
    private Double progressPercent;
    private Double currentLatitude;
    private Double currentLongitude;
    private Integer currentStopOrder;
}
