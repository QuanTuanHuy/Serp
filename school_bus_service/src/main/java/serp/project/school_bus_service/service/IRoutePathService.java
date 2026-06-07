package serp.project.school_bus_service.service;

import serp.project.school_bus_service.dto.response.RoutePathCoordinateResponse;
import serp.project.school_bus_service.dto.response.RoutePathResponse;

import java.util.List;

public interface IRoutePathService {

    RoutePathResponse computePath(Long routeId, List<RoutePathCoordinateResponse> waypoints);

    String serialize(RoutePathResponse response);

    RoutePathResponse deserialize(String rawJson);
}

