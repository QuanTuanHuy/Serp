package serp.project.school_bus_service.dto.response;

import lombok.Getter;
import lombok.Setter;

import java.util.List;

@Getter
@Setter
public class RoutePathResponse {

    private Long routeId;
    private String geometrySource;
    private Double distanceKm;
    private Integer durationMin;
    private List<Coordinate> coordinates;

    @Getter
    @Setter
    public static class Coordinate {
        private Double latitude;
        private Double longitude;

        public Coordinate(Double latitude, Double longitude) {
            this.latitude = latitude;
            this.longitude = longitude;
        }
    }
}
