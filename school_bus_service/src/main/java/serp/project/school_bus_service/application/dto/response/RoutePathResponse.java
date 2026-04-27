package serp.project.school_bus_service.application.dto.response;

import lombok.Getter;
import lombok.Setter;

import java.util.List;

@Getter
@Setter
public class RoutePathResponse {

    private Long routeId;
    private String provider;
    private Boolean estimated;
    private Double distanceKm;
    private Integer durationMin;
    private String warning;
    private List<RoutePathCoordinateResponse> coordinates;
}

