package serp.project.school_bus_service.service.domain;

import serp.project.school_bus_service.dto.response.RoutePathResponse;
import serp.project.school_bus_service.entity.RoutePlanEntity;
import serp.project.school_bus_service.entity.RouteStopEntity;

import java.util.List;

/**
 * Orchestrates route geometry computation.
 *
 * <p>Builds waypoints from the route start/end locations and ordered stops,
 * calls {@link IRoutingEngineService} (falling back to the straight-line engine
 * when the primary provider is unavailable), and writes the result back into the
 * route entity in-place.
 *
 * <p>The caller is responsible for persisting the mutated route entity after
 * {@link #computeAndUpdate} returns.
 */
public interface IRouteGeometryService {

    /**
     * Compute geometry and update the route entity's {@code geometryPath},
     * {@code plannedDistanceKm}, and {@code plannedDurationMin} fields.
     * <b>Does not persist</b> — caller must save the entity.
     */
    RoutePathResponse computeAndUpdate(RoutePlanEntity route, List<RouteStopEntity> stops);

    /** Serialise a {@link RoutePathResponse} to the JSON string stored in {@code geometry_path}. */
    String serialize(RoutePathResponse response);

    /** Deserialise the JSON string stored in {@code geometry_path}. Returns {@code null} on blank / invalid input. */
    RoutePathResponse deserialize(String rawJson);
}
