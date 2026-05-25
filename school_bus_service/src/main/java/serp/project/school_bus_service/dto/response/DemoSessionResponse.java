package serp.project.school_bus_service.dto.response;

import lombok.Getter;
import lombok.Setter;

import java.time.LocalDateTime;
import java.util.List;

@Getter
@Setter
public class DemoSessionResponse extends BaseResponse {
    private String demoCode;
    private Long tripId;
    private String tripCode;
    private String status;
    private Integer speedMultiplier;
    private Integer currentStopOrder;
    private Double currentLatitude;
    private Double currentLongitude;
    private Double progressPercent;
    private LocalDateTime startedAt;
    private LocalDateTime pausedAt;
    private LocalDateTime completedAt;
    private List<DemoEventLogResponse> events;
}

