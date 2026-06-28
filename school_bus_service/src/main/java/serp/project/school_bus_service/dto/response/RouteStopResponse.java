package serp.project.school_bus_service.dto.response;

import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class RouteStopResponse extends BaseResponse {

    private Long routeId;

    /** Location type: PICKUP_POINT | SCHOOL | DEPOT */
    private String locationType;
    /** Stop purpose: START_TERMINAL | PICKUP | DROPOFF | END_TERMINAL */
    private String stopPurpose;
    /** Derived display name from pickupPoint / school / depot. */
    private String displayName;
    private Long locationId;
    private String locationName;
    private String locationAddress;
    private Double latitude;
    private Double longitude;

    // Pickup-point specific fields (null for terminal stops)
    private Long pickupPointId;
    private String pickupPointName;
    private String pickupPointAddress;
    private Double pickupPointLatitude;
    private Double pickupPointLongitude;

    // School terminal fields
    private Long schoolId;
    private String schoolName;

    // Depot terminal fields
    private Long depotId;
    private String depotName;

    private Integer stopOrder;
    private Integer estimatedStudentCount;
    private Double distanceFromPreviousKm;
    private Integer estimatedTravelTimeFromPrevious;
}
