package serp.project.school_bus_service.service.domain;

import serp.project.school_bus_service.dto.response.RoutePathCoordinateResponse;
import serp.project.school_bus_service.dto.response.RoutePathResponse;

import java.util.List;

/**
 * Low-level routing engine abstraction.
 * Implementations call an external provider (OSRM, OpenRouteService, …)
 * or apply a geometric estimation (StraightLineFallback).
 * Throws {@link RuntimeException} when the engine cannot produce a result.
 */
public interface IRoutingEngineService {

    RoutePathResponse requestRoute(Long routeId, List<RoutePathCoordinateResponse> waypoints, Long tenantId);
}
