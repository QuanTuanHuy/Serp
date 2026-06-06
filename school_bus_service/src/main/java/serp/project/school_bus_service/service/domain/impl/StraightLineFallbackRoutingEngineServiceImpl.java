package serp.project.school_bus_service.service.domain.impl;

import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.stereotype.Service;
import serp.project.school_bus_service.dto.response.RoutePathCoordinateResponse;
import serp.project.school_bus_service.dto.response.RoutePathLegInfo;
import serp.project.school_bus_service.dto.response.RoutePathResponse;
import serp.project.school_bus_service.dto.response.RoutingRuntimeConfig;
import serp.project.school_bus_service.service.domain.IRoutingConfigResolver;
import serp.project.school_bus_service.service.domain.IRoutingEngineService;

import java.util.ArrayList;
import java.util.List;

/**
 * Fallback routing engine that estimates the route using straight-line (Haversine) geometry.
 * Used automatically by {@link RouteGeometryServiceImpl} when OSRM is unavailable.
 * Provider tag: {@code STRAIGHT_LINE_FALLBACK}.
 */
@Service
@Qualifier("straightLineRoutingEngine")
public class StraightLineFallbackRoutingEngineServiceImpl implements IRoutingEngineService {

    private final IRoutingConfigResolver routingConfigResolver;

    public StraightLineFallbackRoutingEngineServiceImpl(IRoutingConfigResolver routingConfigResolver) {
        this.routingConfigResolver = routingConfigResolver;
    }

    /**
     * Calculates the estimated route using pairwise Haversine straight-line distance,
     * adjusted by the road multiplier factor and average vehicle speed config.
     */
    @Override
    public RoutePathResponse requestRoute(Long routeId, List<RoutePathCoordinateResponse> waypoints, Long tenantId) {
        // Load routing parameters globally
        RoutingRuntimeConfig config = routingConfigResolver.resolve();
        double averageSpeedKmph = config.getAverageSpeedKmph();
        double roadFactor = config.getRoadFactor();

        double totalDistanceKm = 0D;
        List<RoutePathLegInfo> legs = new ArrayList<>();

        for (int i = 1; i < waypoints.size(); i++) {
            RoutePathCoordinateResponse from = waypoints.get(i - 1);
            RoutePathCoordinateResponse to = waypoints.get(i);
            double haversineDist = haversineKm(from.getLatitude(), from.getLongitude(),
                    to.getLatitude(), to.getLongitude());
            double legDist = haversineDist * roadFactor;
            totalDistanceKm += legDist;
            RoutePathLegInfo leg = new RoutePathLegInfo();
            leg.setDistanceKm(round(legDist));
            leg.setDurationMin((int) Math.round((legDist / averageSpeedKmph) * 60D));
            legs.add(leg);
        }

        int durationMin = (int) Math.round((totalDistanceKm / averageSpeedKmph) * 60D);

        // Use the waypoints themselves as coordinates (straight-line path)
        List<RoutePathCoordinateResponse> coordinates = new ArrayList<>(waypoints);

        RoutePathResponse response = new RoutePathResponse();
        response.setRouteId(routeId);
        response.setProvider("STRAIGHT_LINE_FALLBACK");
        response.setEstimated(Boolean.TRUE);
        response.setFallbackUsed(Boolean.TRUE);
        response.setGeometrySource("STRAIGHT_LINE_ESTIMATE");
        response.setDistanceKm(round(totalDistanceKm));
        response.setDurationMin(durationMin);
        response.setCoordinates(coordinates);
        response.setLegs(legs);
        response.setWarning("Road routing unavailable. Showing straight-line estimate — actual road distance may differ.");
        return response;
    }

    // ── Private helpers ───────────────────────────────────────────────────────

    /** Haversine great-circle distance between two points in kilometres. */
    private double haversineKm(double lat1, double lon1, double lat2, double lon2) {
        double earthRadiusKm = 6371.0;
        double dLat = Math.toRadians(lat2 - lat1);
        double dLon = Math.toRadians(lon2 - lon1);
        double a = Math.sin(dLat / 2) * Math.sin(dLat / 2)
                + Math.cos(Math.toRadians(lat1)) * Math.cos(Math.toRadians(lat2))
                * Math.sin(dLon / 2) * Math.sin(dLon / 2);
        double c = 2 * Math.atan2(Math.sqrt(a), Math.sqrt(1 - a));
        return earthRadiusKm * c;
    }

    private double round(double value) {
        return Math.round(value * 100D) / 100D;
    }
}
