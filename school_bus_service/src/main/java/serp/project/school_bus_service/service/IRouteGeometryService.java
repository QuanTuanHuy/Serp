package serp.project.school_bus_service.service;

import serp.project.school_bus_service.entity.RoutePlanEntity;

/**
 * Minimal route geometry service.
 * Calls OSRM to get real road-network polyline and distance.
 * Does NOT compute timeline, matrix, trace, issues, or objective score.
 */
public interface IRouteGeometryService {

    /**
     * Recalculate the route geometry using OSRM.
     * Updates: route.geometryPath, route.plannedDistanceKm, route.plannedDurationMin.
     * If OSRM fails: logs warning, leaves geometry unchanged, does not block route operations.
     */
    void recalculateGeometry(RoutePlanEntity route, Long tenantId);
}
