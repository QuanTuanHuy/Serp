package serp.project.school_bus_service.application.dto.response;

import lombok.Getter;
import lombok.Setter;

import java.time.LocalDateTime;

@Getter
@Setter
public class RouteAttendanceManifestItemResponse {

    private Long studentId;
    private String studentName;
    private Long pickupPointId;
    private String pickupPointName;
    private String latestAttendanceType;
    private String latestAttendanceStatus;
    private LocalDateTime latestRecordedAt;
}
