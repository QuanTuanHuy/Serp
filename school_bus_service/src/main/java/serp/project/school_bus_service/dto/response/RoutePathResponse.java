package serp.project.school_bus_service.dto.response;

import lombok.Getter;
import lombok.Setter;

import java.util.List;

@Getter
@Setter
public class RoutePathResponse {

    private Long routeId;
    /** Routing provider: OSRM | STRAIGHT_LINE_FALLBACK | NONE */
    private String provider;
    /** True when geometry is not a real road network result. */
    private Boolean estimated;
    /** True when the primary routing engine (OSRM) was unavailable and straight-line fallback was used. */
    private Boolean fallbackUsed;
    /** Source of geometry data: ROAD_NETWORK | STRAIGHT_LINE_ESTIMATE | NONE */
    private String geometrySource;
    private Double distanceKm;
    private Integer durationMin;
    private String warning;
    private List<RoutePathCoordinateResponse> coordinates;
    /** Per-leg distances/durations; index k is the segment from waypoint[k] to waypoint[k+1]. */
    private List<RoutePathLegInfo> legs;
}

