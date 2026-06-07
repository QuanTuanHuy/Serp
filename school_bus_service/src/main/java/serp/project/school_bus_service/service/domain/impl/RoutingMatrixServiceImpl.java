package serp.project.school_bus_service.service.domain.impl;

import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpHeaders;
import org.springframework.stereotype.Service;
import org.springframework.util.StringUtils;
import org.springframework.web.client.RestClient;
import serp.project.school_bus_service.dto.request.RoutingPointRequest;
import serp.project.school_bus_service.dto.response.RoutingMatrixCell;
import serp.project.school_bus_service.dto.response.RoutingMatrixResponse;
import serp.project.school_bus_service.dto.response.RoutingRuntimeConfig;
import serp.project.school_bus_service.service.domain.IRoutingConfigResolver;
import serp.project.school_bus_service.service.domain.IRoutingMatrixService;
import tools.jackson.databind.JsonNode;

import java.util.ArrayList;
import java.util.List;
import java.util.Locale;

@Service
@Slf4j
public class RoutingMatrixServiceImpl implements IRoutingMatrixService {

    private final IRoutingConfigResolver configResolver;
    private final RestClient.Builder restClientBuilder;

    @Value("${map.routing.osrm.base-url:https://router.project-osrm.org}")
    private String osrmBaseUrl;

    @Value("${map.routing.osrm.profile:driving}")
    private String osrmProfile;

    @Value("${map.routing.user-agent:SERP-SchoolBus/0.1 (local-dev)}")
    private String routingUserAgent;

    public RoutingMatrixServiceImpl(IRoutingConfigResolver configResolver, RestClient.Builder restClientBuilder) {
        this.configResolver = configResolver;
        this.restClientBuilder = restClientBuilder;
    }

    /**
     * Builds a distance-duration matrix for all routing points in the current planning context.
     * The matrix is the foundation for manual route validation, greedy route generation,
     * and objective scoring in later phases.
     */
    @Override
    public RoutingMatrixResponse buildMatrix(Long tenantId, List<RoutingPointRequest> points) {
        // Load routing parameters globally
        RoutingRuntimeConfig config = configResolver.resolve();
        List<RoutingMatrixCell> cells = new ArrayList<>();

        if (points == null || points.isEmpty()) {
            return new RoutingMatrixResponse(cells);
        }

        if (config.isOsrmEnabled()) {
            try {
                cells = requestOsrmMatrix(points, config);
                return new RoutingMatrixResponse(cells);
            } catch (Exception e) {
                log.warn("OSRM Table API failed, falling back to straight-line matrix estimation. Error: {}", e.getMessage());
            }
        }

        // Fallback straight line
        cells = buildFallbackMatrix(points, config);
        return new RoutingMatrixResponse(cells);
    }

    private List<RoutingMatrixCell> requestOsrmMatrix(List<RoutingPointRequest> points, RoutingRuntimeConfig config) {
        List<RoutingMatrixCell> cells = new ArrayList<>();
        String coordinatePath = points.stream()
                .map(p -> String.format(Locale.US, "%.6f,%.6f", p.getLongitude(), p.getLatitude()))
                .reduce((l, r) -> l + ";" + r)
                .orElse("");

        String requestUri = String.format(
                "%s/table/v1/%s/%s?annotations=duration,distance",
                trimSlash(osrmBaseUrl), osrmProfile, coordinatePath);

        JsonNode body = restClientBuilder
                .defaultHeader(HttpHeaders.USER_AGENT, routingUserAgent)
                .defaultHeader(HttpHeaders.ACCEPT, "application/json")
                .build()
                .get()
                .uri(requestUri)
                .retrieve()
                .body(JsonNode.class);

        if (body == null || !"Ok".equalsIgnoreCase(body.path("code").asText())) {
            throw new IllegalStateException("OSRM Table returned non-OK response");
        }

        JsonNode durations = body.path("durations");
        JsonNode distances = body.path("distances");

        int n = points.size();
        for (int i = 0; i < n; i++) {
            RoutingPointRequest fromPoint = points.get(i);
            for (int j = 0; j < n; j++) {
                RoutingPointRequest toPoint = points.get(j);

                double distanceKm;
                double durationSec;

                // Check distance
                if (distances.isArray() && distances.size() > i && distances.get(i).isArray() && distances.get(i).size() > j) {
                    double distanceMeters = distances.get(i).get(j).asDouble(-1.0);
                    if (distanceMeters >= 0) {
                        distanceKm = round(distanceMeters / 1000.0);
                    } else {
                        distanceKm = estimateFallbackDistance(fromPoint, toPoint, config);
                    }
                } else {
                    distanceKm = estimateFallbackDistance(fromPoint, toPoint, config);
                }

                // Check duration
                if (durations.isArray() && durations.size() > i && durations.get(i).isArray() && durations.get(i).size() > j) {
                    double durationSecondsVal = durations.get(i).get(j).asDouble(-1.0);
                    if (durationSecondsVal >= 0) {
                        durationSec = durationSecondsVal;
                    } else {
                        durationSec = round((distanceKm / config.getAverageSpeedKmph()) * 3600.0);
                    }
                } else {
                    durationSec = round((distanceKm / config.getAverageSpeedKmph()) * 3600.0);
                }

                cells.add(RoutingMatrixCell.builder()
                        .fromKey(fromPoint.getPointKey())
                        .toKey(toPoint.getPointKey())
                        .distanceKm(distanceKm)
                        .durationSeconds((int) Math.round(durationSec))
                        .source("OSRM")
                        .build());
            }
        }

        return cells;
    }

    private List<RoutingMatrixCell> buildFallbackMatrix(List<RoutingPointRequest> points, RoutingRuntimeConfig config) {
        List<RoutingMatrixCell> cells = new ArrayList<>();
        int n = points.size();

        for (int i = 0; i < n; i++) {
            RoutingPointRequest fromPoint = points.get(i);
            for (int j = 0; j < n; j++) {
                RoutingPointRequest toPoint = points.get(j);

                double distanceKm = estimateFallbackDistance(fromPoint, toPoint, config);
                double durationSec = round((distanceKm / config.getAverageSpeedKmph()) * 3600.0);

                cells.add(RoutingMatrixCell.builder()
                        .fromKey(fromPoint.getPointKey())
                        .toKey(toPoint.getPointKey())
                        .distanceKm(distanceKm)
                        .durationSeconds((int) Math.round(durationSec))
                        .source("STRAIGHT_LINE_FALLBACK")
                        .build());
            }
        }
        return cells;
    }

    private double estimateFallbackDistance(RoutingPointRequest fromPoint, RoutingPointRequest toPoint, RoutingRuntimeConfig config) {
        if (fromPoint.getLatitude() == null || fromPoint.getLongitude() == null ||
            toPoint.getLatitude() == null || toPoint.getLongitude() == null) {
            return 0.0;
        }
        double haversine = calculateHaversineDistance(
                fromPoint.getLatitude(), fromPoint.getLongitude(),
                toPoint.getLatitude(), toPoint.getLongitude()
        );
        return round(haversine * config.getRoadFactor());
    }

    private double calculateHaversineDistance(double lat1, double lon1, double lat2, double lon2) {
        double dLat = Math.toRadians(lat2 - lat1);
        double dLon = Math.toRadians(lon2 - lon1);
        double a = Math.sin(dLat / 2) * Math.sin(dLat / 2)
                + Math.cos(Math.toRadians(lat1)) * Math.cos(Math.toRadians(lat2))
                * Math.sin(dLon / 2) * Math.sin(dLon / 2);
        double c = 2 * Math.atan2(Math.sqrt(a), Math.sqrt(1 - a));
        return 6371.0 * c;
    }

    private String trimSlash(String value) {
        if (!StringUtils.hasText(value)) return "";
        return value.endsWith("/") ? value.substring(0, value.length() - 1) : value;
    }

    private double round(double value) {
        return Math.round(value * 100.0) / 100.0;
    }
}
