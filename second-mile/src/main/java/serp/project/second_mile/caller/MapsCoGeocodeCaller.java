/*
Author: Nguyen The Anh
Description: Part of Serp Project
*/

package serp.project.second_mile.caller;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatusCode;
import org.springframework.http.MediaType;
import org.springframework.stereotype.Component;
import org.springframework.web.client.RestClient;
import serp.project.second_mile.caller.dto.GeoPoint;
import serp.project.second_mile.exception.AppException;
import serp.project.second_mile.exception.ErrorCode;

import java.text.Normalizer;
import java.util.Arrays;
import java.util.List;
import java.util.Objects;
import java.util.Optional;

@Component
@Slf4j
public class MapsCoGeocodeCaller implements GeocodeCaller {
    private static final int MAX_LOG_BODY_LENGTH = 500;
    private static final String CLIENT_USER_AGENT = "second-mile-geocode-client/1.0";

    private final RestClient restClient;
    private final RestClient fallbackRestClient;
    private final ObjectMapper objectMapper;
    private final Object fallbackRateLimitMonitor = new Object();
    private long nextFallbackRequestAtMillis;

    @Value("${geocode.api-key:${GOONG_API_KEY:${GEOCODE_API_KEY:${GEOCODE-API-KEY:}}}}")
    private String geocodeApiKey;

    @Value("${geocode.fallback.api-key:${GEOCODE_FALLBACK_API_KEY:${MAPS_CO_API_KEY:}}}")
    private String fallbackGeocodeApiKey;

    @Value("${geocode.fallback.min-interval-ms:1000}")
    private long fallbackMinIntervalMs;

    public MapsCoGeocodeCaller(
            RestClient.Builder restClientBuilder,
            ObjectMapper objectMapper,
            @Value("${geocode.base-url:https://rsapi.goong.io/geocode}") String geocodeBaseUrl,
            @Value("${geocode.fallback.base-url:https://geocode.maps.co/search}") String fallbackGeocodeBaseUrl) {

        this.restClient = restClientBuilder.baseUrl(geocodeBaseUrl).build();
        this.fallbackRestClient = restClientBuilder.baseUrl(fallbackGeocodeBaseUrl).build();
        this.objectMapper = objectMapper;
    }

    @Override
    public Optional<GeoPoint> searchFirst(String query) {
        String normalizedQuery = normalizeQuery(query);
        String sanitizedApiKey = (geocodeApiKey == null) ? "" : geocodeApiKey.trim();

        if (sanitizedApiKey.isBlank()) {
            log.warn("Goong geocode API key is empty. Falling back to maps.co for query: {}",
                    escapeForLog(normalizedQuery));
            return searchFirstWithFallback(normalizedQuery, "missing Goong API key");
        }

        try {
            return searchFirstWithGoong(normalizedQuery, sanitizedApiKey);
        } catch (Exception e) {
            log.warn("Goong geocode failed for query [{}]: {}. Falling back to maps.co.",
                    escapeForLog(normalizedQuery), e.getMessage());
            return searchFirstWithFallback(normalizedQuery, e.getMessage());
        }
    }

    private Optional<GeoPoint> searchFirstWithGoong(String normalizedQuery, String sanitizedApiKey) throws Exception {
        log.debug("Calling Goong geocode provider for query: {}", escapeForLog(normalizedQuery));

        String responseBody = restClient.get()
                .uri(uriBuilder -> uriBuilder
                        .queryParam("address", normalizedQuery)
                        .queryParam("api_key", sanitizedApiKey)
                        .build())
                .header(HttpHeaders.USER_AGENT, CLIENT_USER_AGENT)
                .accept(MediaType.APPLICATION_JSON)
                .retrieve()
                .onStatus(HttpStatusCode::isError, (request, response) -> {
                    throw new IllegalStateException("Goong geocode returned status " + response.getStatusCode());
                })
                .body(String.class);

        if (responseBody == null || responseBody.isBlank()) {
            throw new IllegalStateException("Goong geocode returned empty response");
        }

        log.info("Goong geocode response received. Length: {}, Preview: {}",
                responseBody.length(), abbreviate(responseBody));

        GoongGeocodeResponse goongResponse = objectMapper.readValue(responseBody, GoongGeocodeResponse.class);
        if (goongResponse == null) {
            return Optional.empty();
        }

        if (goongResponse.status() != null && !"OK".equalsIgnoreCase(goongResponse.status())) {
            throw new IllegalStateException("Goong geocode returned status " + goongResponse.status());
        }

        if (goongResponse.results() == null || goongResponse.results().isEmpty()) {
            log.debug("No geocode results returned for query: {}", normalizedQuery);
            return Optional.empty();
        }

        return goongResponse.results().stream()
                .map(item -> {
                    GoongLocation location = Optional.ofNullable(item.geometry())
                            .map(GoongGeometry::location)
                            .orElse(null);

                    return (location != null && location.lat() != null && location.lng() != null)
                            ? new GeoPoint(location.lat(), location.lng())
                            : null;
                })
                .filter(Objects::nonNull)
                .findFirst()
                .or(() -> {
                    log.debug("No valid coordinates found in {} items for query: {}",
                            goongResponse.results().size(), normalizedQuery);
                    return Optional.empty();
                });
    }

    private Optional<GeoPoint> searchFirstWithFallback(String normalizedQuery, String failureReason) {
        String sanitizedFallbackApiKey = (fallbackGeocodeApiKey == null) ? "" : fallbackGeocodeApiKey.trim();
        if (sanitizedFallbackApiKey.isBlank()) {
            log.error("Fallback geocode API key is empty. Configure GEOCODE_FALLBACK_API_KEY or MAPS_CO_API_KEY.");
            throw new AppException(ErrorCode.UNCATEGORIZED_EXCEPTION);
        }

        try {
            waitForFallbackRateLimit();
            log.debug("Calling maps.co geocode fallback for query: {}", escapeForLog(normalizedQuery));

            String responseBody = fallbackRestClient.get()
                    .uri(uriBuilder -> uriBuilder
                            .queryParam("q", normalizedQuery)
                            .queryParam("api_key", sanitizedFallbackApiKey)
                            .build())
                    .header(HttpHeaders.USER_AGENT, CLIENT_USER_AGENT)
                    .accept(MediaType.APPLICATION_JSON)
                    .retrieve()
                    .onStatus(HttpStatusCode::isError, (request, response) -> {
                        throw new IllegalStateException("maps.co geocode returned status " + response.getStatusCode());
                    })
                    .body(String.class);

            if (responseBody == null || responseBody.isBlank()) {
                log.warn("maps.co geocode fallback returned empty response for query: {}", normalizedQuery);
                return Optional.empty();
            }

            log.info("maps.co geocode fallback response received. Length: {}, Preview: {}",
                    responseBody.length(), abbreviate(responseBody));

            MapsCoGeocodeResult[] mapsCoResults = objectMapper.readValue(responseBody, MapsCoGeocodeResult[].class);
            if (mapsCoResults == null || mapsCoResults.length == 0) {
                log.debug("No maps.co fallback results returned for query: {}", normalizedQuery);
                return Optional.empty();
            }

            return Arrays.stream(mapsCoResults)
                    .map(this::toGeoPoint)
                    .filter(Objects::nonNull)
                    .findFirst()
                    .or(() -> {
                        log.debug("No valid coordinates found in {} maps.co items for query: {}",
                                mapsCoResults.length, normalizedQuery);
                        return Optional.empty();
                    });
        } catch (Exception e) {
            if (e instanceof InterruptedException) {
                Thread.currentThread().interrupt();
            }
            log.error("maps.co geocode fallback failed for query [{}] after Goong failure [{}]: {}",
                    escapeForLog(normalizedQuery), failureReason, e.getMessage());
            throw new AppException(ErrorCode.UNCATEGORIZED_EXCEPTION);
        }
    }

    private GeoPoint toGeoPoint(MapsCoGeocodeResult result) {
        if (result == null || result.lat() == null || result.lon() == null) {
            return null;
        }
        try {
            return new GeoPoint(Double.parseDouble(result.lat()), Double.parseDouble(result.lon()));
        } catch (NumberFormatException e) {
            return null;
        }
    }

    private void waitForFallbackRateLimit() throws InterruptedException {
        long minIntervalMs = Math.max(1000L, fallbackMinIntervalMs);
        synchronized (fallbackRateLimitMonitor) {
            long now = System.currentTimeMillis();
            long waitMs = nextFallbackRequestAtMillis - now;
            if (waitMs > 0) {
                Thread.sleep(waitMs);
                now = System.currentTimeMillis();
            }
            nextFallbackRequestAtMillis = now + minIntervalMs;
        }
    }

    private String abbreviate(String value) {
        if (value == null) return "null";
        return value.length() <= MAX_LOG_BODY_LENGTH
                ? value
                : value.substring(0, MAX_LOG_BODY_LENGTH) + "...";
    }

    private String normalizeQuery(String query) {
        if (query == null) return "";
        String normalized = Normalizer.normalize(query, Normalizer.Form.NFC);
        return normalized
                .replace('\u00A0', ' ')
                .replaceAll("[\\u200B\\u200C\\u200D\\uFEFF]", "")
                .replaceAll("[\\p{Cntrl}&&[^\\r\\n\\t]]", "")
                .replaceAll("\\s+", " ")
                .trim();
    }

    private String escapeForLog(String value) {
        if (value == null) return "null";
        StringBuilder escaped = new StringBuilder();
        for (char current : value.toCharArray()) {
            if (Character.isISOControl(current) || Character.getType(current) == Character.FORMAT) {
                escaped.append(String.format("\\u%04X", (int) current));
            } else {
                escaped.append(current);
            }
        }
        return escaped.toString();
    }

    @JsonIgnoreProperties(ignoreUnknown = true)
    private record GoongGeocodeResponse(String status, List<GoongGeocodeResult> results) {
    }

    @JsonIgnoreProperties(ignoreUnknown = true)
    private record GoongGeocodeResult(GoongGeometry geometry) {
    }

    @JsonIgnoreProperties(ignoreUnknown = true)
    private record GoongGeometry(GoongLocation location) {
    }

    @JsonIgnoreProperties(ignoreUnknown = true)
    private record GoongLocation(Double lat, Double lng) {
    }

    @JsonIgnoreProperties(ignoreUnknown = true)
    private record MapsCoGeocodeResult(String lat, String lon) {
    }
}
