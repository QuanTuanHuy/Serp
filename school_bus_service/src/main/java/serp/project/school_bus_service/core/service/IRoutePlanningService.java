package serp.project.school_bus_service.core.service;

import serp.project.school_bus_service.infrastructure.store.model.RoutePlanEntity;
import serp.project.school_bus_service.infrastructure.store.model.RouteStopEntity;

import java.util.List;

public interface IRoutePlanningService {

    List<RouteStopEntity> generateGreedyStops(RoutePlanEntity route, Long tenantId);
}
