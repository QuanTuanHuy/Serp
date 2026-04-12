package serp.project.school_bus_service.application.dto.response;

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
    private Integer stopOrder;
    private Integer estimatedStudentCount;
    private LocalTime plannedArrivalTime;
    private LocalTime plannedDepartureTime;
}
