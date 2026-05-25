package serp.project.school_bus_service.dto.response;

import lombok.Getter;
import lombok.Setter;

import java.time.LocalTime;

@Getter
@Setter
public class RouteStopResponse extends BaseResponse {

    private Long routeId;
    private Long pickupPointId;
    private String pickupPointName;
    private String pickupPointAddress;
    private Double pickupPointLatitude;
    private Double pickupPointLongitude;
    private String stopType;
    private Integer stopOrder;
    private Integer estimatedStudentCount;
    private LocalTime plannedArrivalTime;
    private LocalTime plannedDepartureTime;
    private Double distanceFromPreviousKm;
    private Integer estimatedTravelTimeFromPrevious;
}
