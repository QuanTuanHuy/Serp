package serp.project.school_bus_service.dto.response;

import lombok.Getter;
import lombok.Setter;

/** Distance and duration for one leg (segment between two consecutive waypoints). */
@Getter
@Setter
public class RoutePathLegInfo {
    private Double distanceKm;
    private Integer durationMin;
}
