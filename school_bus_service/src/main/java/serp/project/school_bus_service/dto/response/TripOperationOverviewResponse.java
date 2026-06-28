package serp.project.school_bus_service.dto.response;

import lombok.Getter;
import lombok.Setter;

import java.util.List;

@Getter
@Setter
public class TripOperationOverviewResponse {
    private Long tripId;
    private String tripCode;
    private Long routeId;
    private String routeCode;
    private String routeName;
    private String routeDirection;
    private String tripStatus;
    private String serviceDate;
    private String busPlateNumber;
    private String driverName;
    private String attendantName;
    private String routeGeometry;
    private Double distanceKm;
    private Integer durationMin;
    private String cancellationReason;
    private TripAttendanceSummaryResponse summary;
    private List<TripAttendanceManifestResponse.TripAttendanceStopItem> stops;
}
