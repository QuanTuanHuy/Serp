package serp.project.school_bus_service.application.dto.response;

import lombok.Getter;
import lombok.Setter;

import java.util.List;

@Getter
@Setter
public class RouteAttendanceManifestResponse {

    private RoutePlanResponse route;
    private RouteAssignmentResponse assignment;
    private List<RouteAttendanceManifestItemResponse> students;
}
