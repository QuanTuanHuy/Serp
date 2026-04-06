package serp.project.school_bus_service.application.dto.response;

import lombok.Getter;
import lombok.Setter;

import java.util.List;

@Getter
@Setter
public class RouteDetailResponse {

    private RoutePlanResponse route;
    private List<RouteStopResponse> stops;
    private RouteAssignmentResponse assignment;
}
