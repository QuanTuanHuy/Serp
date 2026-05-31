package serp.project.school_bus_service.service.domain.impl;

import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpHeaders;
import org.springframework.stereotype.Service;
import org.springframework.util.StringUtils;
import org.springframework.web.client.RestClient;
import serp.project.school_bus_service.dto.response.RoutePathCoordinateResponse;
import serp.project.school_bus_service.dto.response.RoutePathLegInfo;
import serp.project.school_bus_service.dto.response.RoutePathResponse;
import serp.project.school_bus_service.service.domain.IRoutingEngineService;
import tools.jackson.databind.JsonNode;

import java.util.ArrayList;
import java.util.List;
import java.util.Locale;

/**
 * Routing engine implementation backed by OSRM.
 * Calls the OSRM HTTP API and parses the GeoJSON response.
 * Throws {@link IllegalStateException} when OSRM is unreachable or returns a non-OK code.
 */
@Service
@Qualifier("osrmRoutingEngine")
@Slf4j
public class OsrmRoutingEngineServiceImpl implements IRoutingEngineService {

    private final RestClient.Builder restClientBuilder;

    @Value("${map.routing.osrm.base-url:https://router.project-osrm.org}")
    private String osrmBaseUrl;

    @Value("${map.routing.osrm.profile:driving}")
    private String osrmProfile;

    @Value("${map.routing.user-agent:SERP-SchoolBus/0.1 (local-dev)}")
    private String routingUserAgent;

    public OsrmRoutingEngineServiceImpl(RestClient.Builder restClientBuilder) {
        this.restClientBuilder = restClientBuilder;
    }

    @Override
    public RoutePathResponse requestRoute(Long routeId, List<RoutePathCoordinateResponse> waypoints) {
        String coordinatePath = waypoints.stream()
                .map(p -> String.format(Locale.US, "%.6f,%.6f", p.getLongitude(), p.getLatitude()))
                .reduce((l, r) -> l + ";" + r)
                .orElse("");

        String requestUri = String.format(
                "%s/route/v1/%s/%s?overview=full&geometries=geojson&steps=false",
                trimSlash(osrmBaseUrl), osrmProfile, coordinatePath);

        JsonNode body = buildClient()
                .get()
                .uri(requestUri)
                .retrieve()
                .body(JsonNode.class);

        if (body == null || !"Ok".equalsIgnoreCase(body.path("code").asText())) {
            throw new IllegalStateException("OSRM returned non-OK response for route " + routeId);
        }

        JsonNode routeNode = body.path("routes").isArray() && body.path("routes").size() > 0
                ? body.path("routes").get(0) : null;
        if (routeNode == null || routeNode.isMissingNode()) {
            throw new IllegalStateException("OSRM returned empty route list for route " + routeId);
        }

        List<RoutePathCoordinateResponse> coordinates =
                parseCoordinates(routeNode.path("geometry").path("coordinates"));
        if (coordinates.size() < 2) {
            throw new IllegalStateException("OSRM returned invalid geometry for route " + routeId);
        }

        List<RoutePathLegInfo> legs = parseLegs(routeNode.path("legs"));

        RoutePathResponse response = new RoutePathResponse();
        response.setRouteId(routeId);
        response.setProvider("OSRM");
        response.setEstimated(Boolean.FALSE);
        response.setFallbackUsed(Boolean.FALSE);
        response.setGeometrySource("ROAD_NETWORK");
        response.setDistanceKm(round(routeNode.path("distance").asDouble(0D) / 1000D));
        response.setDurationMin((int) Math.round(routeNode.path("duration").asDouble(0D) / 60D));
        response.setCoordinates(coordinates);
        response.setLegs(legs);
        return response;
    }

    // ── Private helpers ───────────────────────────────────────────────────────

    private RestClient buildClient() {
        return restClientBuilder
                .defaultHeader(HttpHeaders.USER_AGENT, routingUserAgent)
                .defaultHeader(HttpHeaders.ACCEPT, "application/json")
                .build();
    }

    private List<RoutePathCoordinateResponse> parseCoordinates(JsonNode coordinatesNode) {
        List<RoutePathCoordinateResponse> result = new ArrayList<>();
        if (coordinatesNode == null || !coordinatesNode.isArray()) {
            return result;
        }
        coordinatesNode.forEach(point -> {
            if (point != null && point.isArray() && point.size() >= 2) {
                result.add(new RoutePathCoordinateResponse(
                        point.get(1).asDouble(), // lat
                        point.get(0).asDouble()  // lng
                ));
            }
        });
        return result;
    }

    private String trimSlash(String value) {
        if (!StringUtils.hasText(value)) return "";
        return value.endsWith("/") ? value.substring(0, value.length() - 1) : value;
    }

    private List<RoutePathLegInfo> parseLegs(JsonNode legsNode) {
        List<RoutePathLegInfo> legs = new ArrayList<>();
        if (legsNode == null || !legsNode.isArray()) return legs;
        legsNode.forEach(legNode -> {
            RoutePathLegInfo leg = new RoutePathLegInfo();
            leg.setDistanceKm(round(legNode.path("distance").asDouble(0D) / 1000D));
            leg.setDurationMin((int) Math.round(legNode.path("duration").asDouble(0D) / 60D));
            legs.add(leg);
        });
        return legs;
    }

    private double round(double value) {
        return Math.round(value * 100D) / 100D;
    }
}
