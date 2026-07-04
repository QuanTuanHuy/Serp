package serp.project.school_bus_service.service.algorithm.model;

import lombok.Data;
import serp.project.school_bus_service.entity.RoutePlanEntity;
import serp.project.school_bus_service.entity.RoutePlanningSessionEntity;
import serp.project.school_bus_service.entity.RouteStopEntity;
import serp.project.school_bus_service.enums.RouteDirection;

import java.util.List;

@Data
public class RouteContext {

    private RoutePlanningSessionEntity session;
    private RoutePlanEntity route;
    private RouteDirection direction;
    private RouteStopEntity startTerminal;
    private RouteStopEntity endTerminal;
    private List<RouteStopEntity> serviceStops;
}
