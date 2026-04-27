package serp.project.school_bus_service.core.service.impl;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpHeaders;
import org.springframework.stereotype.Service;
import org.springframework.util.StringUtils;
import org.springframework.web.client.RestClient;
import serp.project.school_bus_service.application.dto.response.RoutePathCoordinateResponse;
import serp.project.school_bus_service.application.dto.response.RoutePathResponse;
import serp.project.school_bus_service.core.service.IRoutePathService;
import tools.jackson.databind.JsonNode;
import tools.jackson.databind.ObjectMapper;

import java.util.ArrayList;
import java.util.List;
import java.util.Locale;

@Service
@RequiredArgsConstructor
@Slf4j
public class RoutePathServiceImpl implements IRoutePathService {

    private final RestClient.Builder restClientBuilder;
    private final ObjectMapper objectMapper;

    @Value("${map.routing.osrm.base-url:https://router.project-osrm.org}")
    private String osrmBaseUrl;

    @Value("${map.routing.osrm.profile:driving}")
    private String osrmProfile;

    @Value("${map.routing.user-agent:SERP-SchoolBus/0.1 (local-dev)}")
    private String routingUserAgent;

    @Override
    public RoutePathResponse computePath(Long routeId, List<RoutePathCoordinateResponse> waypoints) {
        String coordinatePath = waypoints.stream()
                .map(point -> String.format(Locale.US, "%.6f,%.6f", point.getLongitude(), point.getLatitude()))
                .reduce((left, right) -> left + ";" + right)
                .orElse("");

        String requestUri = String.format(
                "%s/route/v1/%s/%s?overview=full&geometries=geojson&steps=false",
                trimSlash(osrmBaseUrl),
                osrmProfile,
                coordinatePath);

        JsonNode body = buildClient()
                .get()
                .uri(requestUri)
                .retrieve()
                .body(JsonNode.class);

        if (body == null || !"Ok".equalsIgnoreCase(body.path("code").asText())) {
            throw new IllegalStateException("Routing provider returned non-OK response");
        }

        JsonNode routeNode = body.path("routes").isArray() && body.path("routes").size() > 0
                ? body.path("routes").get(0)
                : null;
        if (routeNode == null || routeNode.isMissingNode()) {
            throw new IllegalStateException("Routing provider returned empty route");
        }

        List<RoutePathCoordinateResponse> coordinates = parseCoordinates(routeNode.path("geometry").path("coordinates"));
        if (coordinates.size() < 2) {
            throw new IllegalStateException("Routing provider returned invalid geometry");
        }

        RoutePathResponse response = new RoutePathResponse();
        response.setRouteId(routeId);
        response.setProvider("OSRM");
        response.setEstimated(Boolean.FALSE);
        response.setDistanceKm(round(routeNode.path("distance").asDouble(0D) / 1000D));
        response.setDurationMin((int) Math.round(routeNode.path("duration").asDouble(0D) / 60D));
        response.setCoordinates(coordinates);
        return response;
    }

    @Override
    public String serialize(RoutePathResponse response) {
        try {
            return objectMapper.writeValueAsString(response);
        } catch (Exception exception) {
            log.warn("Unable to serialize route path", exception);
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
        } catch (Exception exception) {
            log.warn("Unable to deserialize route path from geometry_path", exception);
            return null;
        }
    }

    private RestClient buildClient() {
        return restClientBuilder
                .defaultHeader(HttpHeaders.USER_AGENT, routingUserAgent)
                .defaultHeader(HttpHeaders.ACCEPT, "application/json")
                .build();
    }

    private List<RoutePathCoordinateResponse> parseCoordinates(JsonNode coordinatesNode) {
        List<RoutePathCoordinateResponse> coordinates = new ArrayList<>();
        if (coordinatesNode == null || !coordinatesNode.isArray()) {
            return coordinates;
        }

        coordinatesNode.forEach(pointNode -> {
            if (pointNode != null && pointNode.isArray() && pointNode.size() >= 2) {
                coordinates.add(new RoutePathCoordinateResponse(
                        pointNode.get(1).asDouble(),
                        pointNode.get(0).asDouble()));
            }
        });

        return coordinates;
    }

    private String trimSlash(String value) {
        if (!StringUtils.hasText(value)) {
            return "";
        }
        return value.endsWith("/") ? value.substring(0, value.length() - 1) : value;
    }

    private double round(double value) {
        return Math.round(value * 100D) / 100D;
    }
}
