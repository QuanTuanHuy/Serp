package serp.project.school_bus_service.dto.response;

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
    private String routeDirection;
    private String startLocationType;
    private Long startLocationId;
    private String startLocationName;
    private String startLocationAddress;
    private Double startLocationLatitude;
    private Double startLocationLongitude;
    private String endLocationType;
    private Long endLocationId;
    private String endLocationName;
    private String endLocationAddress;
    private Double endLocationLatitude;
    private Double endLocationLongitude;
    private String routeCode;
    private String routeName;
    private LocalDate serviceDate;
    private Long schoolScheduleId;
    private String schoolScheduleName;
    private String status;
    private Double plannedDistanceKm;
    private Integer plannedDurationMin;
    private Integer plannedStudentCount;
    private Integer assignedBusCapacity;
    private Long busId;
    private String busPlateNumber;
    private String busName;
    private Integer busCapacity;
    private String busStatus;
    private Integer stopsCount;
    private Long driverId;
    private String driverName;
    private Long attendantId;
    private String attendantName;
    private String startDepotName;
    private String planningNotes;
    private String geometryPath;
    private LocalDateTime startedAt;
    private LocalDateTime completedAt;
    private Integer requiredCapacity;
    private Long planningSessionId;
    private String planningMethod;
}
