package serp.project.school_bus_service.dto.response;

import lombok.Getter;
import lombok.Setter;

import java.util.List;

@Getter
@Setter
public class RouteMapResponse {

    private RoutePlanResponse route;
    private List<RouteStopResponse> stops;
    private RouteAssignmentResponse assignment;
    private RoutePathResponse path;
}
