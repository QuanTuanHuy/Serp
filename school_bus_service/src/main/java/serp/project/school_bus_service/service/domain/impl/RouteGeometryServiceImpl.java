package serp.project.school_bus_service.service.domain.impl;

import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.stereotype.Service;
import org.springframework.util.StringUtils;
import serp.project.school_bus_service.dto.response.RoutePathCoordinateResponse;
import serp.project.school_bus_service.dto.response.RoutePathLegInfo;
import serp.project.school_bus_service.dto.response.RoutePathResponse;
import serp.project.school_bus_service.entity.RoutePlanEntity;
import serp.project.school_bus_service.entity.RouteStopEntity;
import serp.project.school_bus_service.service.domain.IRouteGeometryService;
import serp.project.school_bus_service.service.domain.IRoutingEngineService;
import tools.jackson.databind.ObjectMapper;

import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;

/**
 * Orchestrates geometry computation for {@link RoutePlanEntity}.
 *
 * <p>Strategy:
 * <ol>
 *   <li>Collect ordered waypoints from the route's start/end locations and active stops.</li>
 *   <li>Call the primary (OSRM) engine.</li>
 *   <li>On any failure, fall back to the straight-line engine.</li>
 *   <li>Mutate the route entity in-place; <b>caller must persist</b>.</li>
 * </ol>
 */
@Service
@Slf4j
public class RouteGeometryServiceImpl implements IRouteGeometryService {

    private final IRoutingEngineService primaryEngine;
    private final IRoutingEngineService fallbackEngine;
    private final ObjectMapper objectMapper;

    public RouteGeometryServiceImpl(
            @Qualifier("osrmRoutingEngine") IRoutingEngineService primaryEngine,
            @Qualifier("straightLineRoutingEngine") IRoutingEngineService fallbackEngine,
            ObjectMapper objectMapper) {
        this.primaryEngine = primaryEngine;
        this.fallbackEngine = fallbackEngine;
        this.objectMapper = objectMapper;
    }

    @Override
    public RoutePathResponse computeAndUpdate(RoutePlanEntity route, List<RouteStopEntity> stops) {
        boolean hasStart = startLatitude(route) != null && startLongitude(route) != null;
        List<RoutePathCoordinateResponse> waypoints = collectWaypoints(route, stops);

        if (waypoints.size() < 2) {
            RoutePathResponse noGeom = noGeometryResponse(route.getId(), waypoints);
            route.setGeometryPath(serialize(noGeom));
            clearStopDistances(stops);
            return noGeom;
        }

        RoutePathResponse result;
        try {
            result = primaryEngine.requestRoute(route.getId(), waypoints);
        } catch (Exception ex) {
            log.warn("Primary routing engine failed for route {}, using straight-line fallback: {}",
                    route.getId(), ex.getMessage());
            result = fallbackEngine.requestRoute(route.getId(), waypoints);
        }

        route.setGeometryPath(serialize(result));
        if (result.getDistanceKm() != null) {
            route.setPlannedDistanceKm(result.getDistanceKm());
        }
        if (result.getDurationMin() != null) {
            route.setPlannedDurationMin(result.getDurationMin());
        }
        applyLegDistancesToStops(stops, result, hasStart);
        return result;
    }

    @Override
    public String serialize(RoutePathResponse response) {
        try {
            return objectMapper.writeValueAsString(response);
        } catch (Exception ex) {
            log.warn("Unable to serialize RoutePathResponse", ex);
            return null;
        }
    }

    @Override
    public RoutePathResponse deserialize(String rawJson) {
        if (!StringUtils.hasText(rawJson)) {
            return null;
        }
        try {
            return objectMapper.readValue(rawJson, RoutePathResponse.class);
        } catch (Exception ex) {
            log.warn("Unable to deserialize RoutePathResponse from geometry_path", ex);
            return null;
        }
    }

    // ── Private helpers ───────────────────────────────────────────────────────

    /**
     * Assigns distanceFromPreviousKm and estimatedTravelTimeFromPrevious to each stop from legs.
     *
     * <p>Leg index mapping:
     * <pre>
     * waypoints = [start?, stop[0], stop[1], ..., stop[N-1], end?]
     * legs[k] = waypoints[k] → waypoints[k+1]
     * stop[i] is at waypoints[hasStart ? i+1 : i]
     * incoming leg for stop[i] = legs[hasStart ? i : i-1]
     * </pre>
     */
    private void applyLegDistancesToStops(List<RouteStopEntity> stops,
                                          RoutePathResponse result, boolean hasStart) {
        List<RoutePathLegInfo> legs = result.getLegs();
        if (legs == null || legs.isEmpty()) {
            clearStopDistances(stops);
            return;
        }
        List<RouteStopEntity> ordered = stops.stream()
                .sorted(Comparator.comparingInt(RouteStopEntity::getStopOrder))
                .toList();
        for (int i = 0; i < ordered.size(); i++) {
            int legIndex = hasStart ? i : i - 1;
            RouteStopEntity stop = ordered.get(i);
            if (legIndex >= 0 && legIndex < legs.size()) {
                RoutePathLegInfo leg = legs.get(legIndex);
                stop.setDistanceFromPreviousKm(leg.getDistanceKm());
                stop.setEstimatedTravelTimeFromPrevious(leg.getDurationMin());
            } else {
                stop.setDistanceFromPreviousKm(null);
                stop.setEstimatedTravelTimeFromPrevious(null);
            }
        }
    }

    private void clearStopDistances(List<RouteStopEntity> stops) {
        if (stops == null) return;
        stops.forEach(s -> {
            s.setDistanceFromPreviousKm(null);
            s.setEstimatedTravelTimeFromPrevious(null);
        });
    }

    private List<RoutePathCoordinateResponse> collectWaypoints(RoutePlanEntity route, List<RouteStopEntity> stops) {
        List<RoutePathCoordinateResponse> waypoints = new ArrayList<>();
        appendCoordinate(waypoints, startLatitude(route), startLongitude(route));
        stops.stream()
                .sorted(Comparator.comparingInt(RouteStopEntity::getStopOrder))
                .forEach(stop -> appendCoordinate(waypoints,
                        stop.getPickupPoint().getLatitude(),
                        stop.getPickupPoint().getLongitude()));
        appendCoordinate(waypoints, endLatitude(route), endLongitude(route));
        return waypoints;
    }

    private void appendCoordinate(List<RoutePathCoordinateResponse> target, Double lat, Double lon) {
        if (lat != null && lon != null) {
            target.add(new RoutePathCoordinateResponse(lat, lon));
        }
    }

    private Double startLatitude(RoutePlanEntity route) {
        if (route.getStartSchool() != null) return route.getStartSchool().getLatitude();
        return route.getStartDepot() == null ? null : route.getStartDepot().getLatitude();
    }

    private Double startLongitude(RoutePlanEntity route) {
        if (route.getStartSchool() != null) return route.getStartSchool().getLongitude();
        return route.getStartDepot() == null ? null : route.getStartDepot().getLongitude();
    }

    private Double endLatitude(RoutePlanEntity route) {
        if (route.getEndSchool() != null) return route.getEndSchool().getLatitude();
        return route.getEndDepot() == null ? null : route.getEndDepot().getLatitude();
    }

    private Double endLongitude(RoutePlanEntity route) {
        if (route.getEndSchool() != null) return route.getEndSchool().getLongitude();
        return route.getEndDepot() == null ? null : route.getEndDepot().getLongitude();
    }

    private RoutePathResponse noGeometryResponse(Long routeId, List<RoutePathCoordinateResponse> waypoints) {
        RoutePathResponse r = new RoutePathResponse();
        r.setRouteId(routeId);
        r.setProvider("NONE");
        r.setEstimated(Boolean.TRUE);
        r.setFallbackUsed(Boolean.FALSE);
        r.setGeometrySource("NONE");
        r.setCoordinates(waypoints == null ? List.of() : waypoints);
        r.setWarning("Insufficient coordinates — add latitude/longitude to all route locations.");
        return r;
    }
}
