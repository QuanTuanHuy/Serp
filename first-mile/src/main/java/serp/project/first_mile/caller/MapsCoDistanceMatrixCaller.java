/*
Author: Nguyen The Anh
Description: Part of Serp Project
*/

package serp.project.first_mile.caller;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.stereotype.Component;
import org.springframework.web.client.RestClient;
import serp.project.first_mile.caller.dto.DistanceMatrixElement;
import serp.project.first_mile.caller.dto.DistanceMatrixResult;
import serp.project.first_mile.caller.dto.GeoPoint;
import serp.project.first_mile.enums.RoutingVehicle;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

@Component
@Slf4j
public class MapsCoDistanceMatrixCaller implements DistanceMatrixCaller {

    private static final int MAX_LOG_BODY_LENGTH = 500;
    private static final String CLIENT_USER_AGENT = "first-mile-distance-matrix-client/1.0";

    private final RestClient restClient;
    private final ObjectMapper objectMapper;

    @Value("${distance-matrix.api-key:${GOONG_API_KEY:${GEOCODE_API_KEY:${GEOCODE-API-KEY:}}}}")
    private String distanceMatrixApiKey;

    public MapsCoDistanceMatrixCaller(
            RestClient.Builder restClientBuilder,
            ObjectMapper objectMapper,
            @Value("${distance-matrix.base-url:https://rsapi.goong.io/DistanceMatrix}") String distanceMatrixBaseUrl
    ) {
        this.restClient = restClientBuilder.baseUrl(distanceMatrixBaseUrl).build();
        this.objectMapper = objectMapper;
    }

    @Override
    public DistanceMatrixResult calculateDistanceMatrix(
            List<GeoPoint> origins,
            List<GeoPoint> destinations,
            RoutingVehicle vehicle
    ) {
        List<GeoPoint> safeOrigins = sanitizeGeoPoints(origins);
        List<GeoPoint> safeDestinations = sanitizeGeoPoints(destinations);
        if (safeOrigins.isEmpty() || safeDestinations.isEmpty()) {
            return new DistanceMatrixResult(List.of());
        }

        String apiKey = Optional.ofNullable(distanceMatrixApiKey).orElse("").trim();
        if (apiKey.isBlank()) {
            log.warn("Distance Matrix API key is empty. Fallback calculation will be used.");
            return new DistanceMatrixResult(List.of());
        }

        RoutingVehicle safeVehicle = vehicle == null ? RoutingVehicle.CAR : vehicle;

        try {
            String responseBody = restClient.get()
                    .uri(uriBuilder -> uriBuilder
                            .queryParam("origins", joinPoints(safeOrigins))
                            .queryParam("destinations", joinPoints(safeDestinations))
                            .queryParam("vehicle", safeVehicle.apiValue())
                            .queryParam("api_key", apiKey)
                            .build())
                    .header(HttpHeaders.USER_AGENT, CLIENT_USER_AGENT)
                    .accept(MediaType.APPLICATION_JSON)
                    .retrieve()
                    .body(String.class);

            if (responseBody == null || responseBody.isBlank()) {
                return new DistanceMatrixResult(List.of());
            }

            log.debug("Distance Matrix response length={} preview={}",
                    responseBody.length(),
                    abbreviate(responseBody));

            GoongDistanceMatrixResponse response = objectMapper.readValue(responseBody, GoongDistanceMatrixResponse.class);
            if (response == null || response.rows() == null) {
                return new DistanceMatrixResult(List.of());
            }

            List<List<DistanceMatrixElement>> rows = new ArrayList<>();
            for (GoongDistanceMatrixRow row : response.rows()) {
                if (row == null || row.elements() == null) {
                    rows.add(Collections.emptyList());
                    continue;
                }

                List<DistanceMatrixElement> elements = row.elements().stream()
                        .map(this::toMatrixElement)
                        .toList();
                rows.add(elements);
            }

            return new DistanceMatrixResult(rows);
        } catch (Exception ex) {
            log.warn("Distance Matrix call failed: {}", ex.getMessage());
            return new DistanceMatrixResult(List.of());
        }
    }

    private List<GeoPoint> sanitizeGeoPoints(List<GeoPoint> points) {
        if (points == null || points.isEmpty()) {
            return List.of();
        }

        return points.stream()
                .filter(point -> point != null && point.latitude() != null && point.longitude() != null)
                .toList();
    }

    private String joinPoints(List<GeoPoint> points) {
        return points.stream()
                .map(point -> point.latitude() + "," + point.longitude())
                .collect(Collectors.joining("|"));
    }

    private DistanceMatrixElement toMatrixElement(GoongDistanceMatrixElement element) {
        if (element == null) {
            return new DistanceMatrixElement(null, null, null);
        }

        Long durationSeconds = Optional.ofNullable(element.duration())
                .map(GoongMatrixValue::value)
                .orElse(null);

        Long distanceMeters = Optional.ofNullable(element.distance())
                .map(GoongMatrixValue::value)
                .orElse(null);

        return new DistanceMatrixElement(element.status(), durationSeconds, distanceMeters);
    }

    private String abbreviate(String value) {
        if (value == null) {
            return "null";
        }

        return value.length() <= MAX_LOG_BODY_LENGTH
                ? value
                : value.substring(0, MAX_LOG_BODY_LENGTH) + "...";
    }

    @JsonIgnoreProperties(ignoreUnknown = true)
    private record GoongDistanceMatrixResponse(List<GoongDistanceMatrixRow> rows) {
    }

    @JsonIgnoreProperties(ignoreUnknown = true)
    private record GoongDistanceMatrixRow(List<GoongDistanceMatrixElement> elements) {
    }

    @JsonIgnoreProperties(ignoreUnknown = true)
    private record GoongDistanceMatrixElement(
            String status,
            GoongMatrixValue duration,
            GoongMatrixValue distance
    ) {
    }

    @JsonIgnoreProperties(ignoreUnknown = true)
    private record GoongMatrixValue(Long value) {
    }
}
