package serp.project.school_bus_service.service.domain;

import serp.project.school_bus_service.entity.RoutePlanEntity;
import serp.project.school_bus_service.entity.RouteStopEntity;

import java.util.List;

public interface ITimelineCalculatorService {
    void calculateTimeline(RoutePlanEntity route, List<RouteStopEntity> stops);
}
