package serp.project.school_bus_service.dto.response;

import lombok.Getter;
import lombok.Setter;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;

@Getter
@Setter
public class TripExecutionResponse extends BaseResponse {
    private String tripCode;
    private Long routeId;
    private String routeCode;
    private String routeName;
    private LocalDate serviceDate;
    private String routeDirection;
    private String status;
    private LocalDateTime plannedStartAt;
    private LocalDateTime plannedEndAt;
    private LocalDateTime startedAt;
    private LocalDateTime completedAt;
    private String completionNote;
    private LocalDateTime cancelledAt;
    private Long cancelledBy;
    private String cancellationReason;

    private Long busId;
    private String busPlateNumber;
    private Long driverId;
    private String driverName;
    private Long attendantId;
    private String attendantName;
    private List<TripStopLogResponse> stops;
    private List<TripStudentResponse> students;
    private String startLocationType;
    private String startLocationName;
    private String endLocationType;
    private String endLocationName;
}

