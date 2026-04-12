package serp.project.school_bus_service.application.dto.response;

import lombok.Getter;
import lombok.Setter;

import java.time.LocalDate;
import java.time.LocalDateTime;

@Getter
@Setter
public class RoutePlanResponse extends BaseResponse {

    private Long schoolId;
    private String schoolName;
    private Double schoolLatitude;
    private Double schoolLongitude;
    private String routeCode;
    private String routeName;
    private LocalDate serviceDate;
    private String shiftType;
    private String status;
    private Double plannedDistanceKm;
    private Integer plannedDurationMin;
    private String planningNotes;
    private LocalDateTime startedAt;
    private LocalDateTime completedAt;
}
