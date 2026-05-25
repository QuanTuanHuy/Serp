package serp.project.school_bus_service.service;

import serp.project.school_bus_service.entity.RoutePlanEntity;
import serp.project.school_bus_service.entity.RouteStopEntity;

import java.util.List;

/**
 * @deprecated Replaced by {@link serp.project.school_bus_service.service.IRoutePlanningSessionService}
 * and session-based greedy generation. This interface has no active callers.
 */
@Deprecated
public interface IRoutePlanningService {

    List<RouteStopEntity> generateGreedyStops(RoutePlanEntity route, Long tenantId);
}
