package serp.project.school_bus_service.service;

import serp.project.school_bus_service.dto.response.RoutePlanResponse;

public interface IRouteLifecycleService {

    RoutePlanResponse startRoute(Long routeId, Long tenantId, Long actorId);

    RoutePlanResponse completeRoute(Long routeId, Long tenantId, Long actorId);
}
