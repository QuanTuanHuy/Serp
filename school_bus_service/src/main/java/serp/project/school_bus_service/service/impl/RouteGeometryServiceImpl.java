package serp.project.school_bus_service.service.impl;

import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestClient;
import org.springframework.web.client.RestClientException;
import serp.project.school_bus_service.entity.RoutePlanEntity;
import serp.project.school_bus_service.entity.RouteStopEntity;
import serp.project.school_bus_service.enums.RouteGeometrySource;
import serp.project.school_bus_service.repository.RouteStopRepository;
import serp.project.school_bus_service.service.IRouteGeometryService;
import serp.project.school_bus_service.service.IRouteStopService;
import org.springframework.context.annotation.Lazy;
import tools.jackson.databind.JsonNode;

import java.util.List;
import java.util.stream.Collectors;

/**
 * Minimal OSRM-based geometry service.
 * Calls OSRM /route/v1/driving to get real polyline + distance + duration.
 * No matrix, no trace, no issue, no objective score, no timeline.
 */
@Service
@Slf4j
public class RouteGeometryServiceImpl implements IRouteGeometryService {

    private final RestClient.Builder restClientBuilder;
    private final RouteStopRepository routeStopRepository;
    private final IRouteStopService routeStopService;

    @Value("${map.routing.osrm.base-url:https://router.project-osrm.org}")
    private String osrmBaseUrl;

    @Value("${map.routing.osrm.profile:driving}")
    private String osrmProfile;

    public RouteGeometryServiceImpl(RestClient.Builder restClientBuilder,
                                     @Lazy IRouteStopService routeStopService,
                                     RouteStopRepository routeStopRepository) {
        this.restClientBuilder = restClientBuilder;
        this.routeStopRepository = routeStopRepository;
        this.routeStopService = routeStopService;
    }

    @Override
    public void recalculateGeometry(RoutePlanEntity route, Long tenantId) {
        List<RouteStopEntity> stops = routeStopService.findByRoute(route.getId(), tenantId);

        // Filter stops with valid coordinates
        List<RouteStopEntity> validStops = stops.stream()
                .filter(s -> s.getLatitude() != null && s.getLongitude() != null)
                .toList();

        if (validStops.size() < 2) {
            log.warn("Skipping geometry calculation for route {} because only {}/{} stops have coordinates",
                    route.getId(), validStops.size(), stops.size());
            if (route.getGeometryPath() == null) {
                route.setGeometrySource(RouteGeometrySource.UNKNOWN);
            }
            return;
        }

        try {
            OsrmRouteResult result = callOsrmRoute(validStops);
            route.setGeometryPath(result.geometryJson);
            route.setGeometrySource(RouteGeometrySource.OSRM);
            route.setPlannedDistanceKm(result.distanceKm);
            route.setPlannedDurationMin(result.durationMin);

            // Update per-stop distance_from_previous_km from OSRM legs
            if (result.legDistancesKm != null && result.legDistancesKm.size() == validStops.size() - 1) {
                validStops.get(0).setDistanceFromPreviousKm(null);
                for (int i = 1; i < validStops.size(); i++) {
                    validStops.get(i).setDistanceFromPreviousKm(result.legDistancesKm.get(i - 1));
                }
                routeStopRepository.saveAll(validStops);
            }
        } catch (Exception e) {
            log.warn("OSRM route calculation failed for route {} ({}). Geometry not updated. Error: {}",
                    route.getId(), route.getRouteName(), e.getMessage());
            // Do NOT fallback to straight-line. Leave geometry as-is (or null).
            // Route creation/publish is not blocked.
        }
    }

    private OsrmRouteResult callOsrmRoute(List<RouteStopEntity> stops) {
        // Build coordinates string: lng,lat;lng,lat;...
        String coordinates = stops.stream()
                .map(s -> s.getLongitude() + "," + s.getLatitude())
                .collect(Collectors.joining(";"));

        String url = osrmBaseUrl + "/route/v1/" + osrmProfile + "/" + coordinates
                + "?overview=full&geometries=geojson&steps=false";

        RestClient client = restClientBuilder.build();
        JsonNode response = client.get()
                .uri(url)
                .retrieve()
                .body(JsonNode.class);

        if (response == null || !"Ok".equals(response.path("code").asText())) {
            String code = response != null ? response.path("code").asText() : "null";
            throw new RestClientException("OSRM returned non-OK response: " + code);
        }

        JsonNode routeNode = response.path("routes").get(0);
        double distanceMeters = routeNode.path("distance").asDouble();
        double durationSeconds = routeNode.path("duration").asDouble();

        // Geometry as GeoJSON coordinate array [[lng,lat],...]
        JsonNode geometryCoords = routeNode.path("geometry").path("coordinates");
        String geometryJson = geometryCoords.toString();

        // Leg distances
        JsonNode legsNode = routeNode.path("legs");
        List<Double> legDistancesKm = new java.util.ArrayList<>();
        if (legsNode.isArray()) {
            for (JsonNode leg : legsNode) {
                legDistancesKm.add(Math.round(leg.path("distance").asDouble() / 10.0) / 100.0);
            }
        }

        OsrmRouteResult result = new OsrmRouteResult();
        result.geometryJson = geometryJson;
        result.distanceKm = Math.round(distanceMeters / 10.0) / 100.0; // meters → km, 2 decimals
        result.durationMin = (int) Math.ceil(durationSeconds / 60.0);
        result.legDistancesKm = legDistancesKm;
        return result;
    }

    private static class OsrmRouteResult {
        String geometryJson;
        double distanceKm;
        int durationMin;
        List<Double> legDistancesKm;
    }
}
