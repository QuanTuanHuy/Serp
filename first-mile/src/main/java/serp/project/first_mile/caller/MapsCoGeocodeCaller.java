/*
 * Author: Nguyen The Anh
 * Description: Optimized Geocode Caller using RestClient - Part of Serp Project
 */

package serp.project.first_mile.caller;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatusCode;
import org.springframework.http.MediaType;
import org.springframework.stereotype.Component;
import org.springframework.web.client.RestClient;
import serp.project.first_mile.exception.AppException;
import serp.project.first_mile.exception.ErrorCode;

import java.text.Normalizer;
import java.util.Arrays;
import java.util.Objects;
import java.util.Optional;

@Component
@Slf4j
public class MapsCoGeocodeCaller implements GeocodeCaller {

    private static final int MAX_LOG_BODY_LENGTH = 500;
    private static final String CLIENT_USER_AGENT = "first-mile-geocode-client/1.0";

    private final RestClient restClient;
    private final ObjectMapper objectMapper;

    @Value("${geocode.api-key:${GEOCODE_API_KEY:${GEOCODE-API-KEY:}}}")
    private String geocodeApiKey;

    /**
     * Khởi tạo RestClient với Base URL cố định để tối ưu hóa việc gọi URI sau này.
     */
    public MapsCoGeocodeCaller(
            RestClient.Builder restClientBuilder,
            ObjectMapper objectMapper,
            @Value("${geocode.base-url:https://geocode.maps.co/search}") String geocodeBaseUrl) {

        this.restClient = restClientBuilder.baseUrl(geocodeBaseUrl).build();
        this.objectMapper = objectMapper;
    }

    @Override
    public Optional<GeoPoint> searchFirst(String query) {
        String normalizedQuery = normalizeQuery(query);
        String sanitizedApiKey = (geocodeApiKey == null) ? "" : geocodeApiKey.trim();

        if (sanitizedApiKey.isBlank()) {
            log.error("Geocode API key is empty. Please check your configuration (GEOCODE_API_KEY).");
            throw new AppException(ErrorCode.INVALID_REQUEST);
        }

        log.debug("Calling geocode provider for query: {}", escapeForLog(normalizedQuery));

        try {
            // Sử dụng uriBuilder để thêm query parameters một cách an toàn
            String responseBody = restClient.get()
                    .uri(uriBuilder -> uriBuilder
                            .queryParam("q", normalizedQuery)
                            .queryParam("api_key", sanitizedApiKey)
                            .build())
                    .header(HttpHeaders.USER_AGENT, CLIENT_USER_AGENT)
                    .accept(MediaType.APPLICATION_JSON)
                    .retrieve()
                    .onStatus(HttpStatusCode::isError, (request, response) -> {
                        log.error("External Geocode API error: status={}", response.getStatusCode());
                    })
                    .body(String.class);

            if (responseBody == null || responseBody.isBlank()) {
                log.warn("Geocode provider returned empty response for query: {}", query);
                return Optional.empty();
            }

            log.info("Geocode response received. Length: {}, Preview: {}",
                    responseBody.length(), abbreviate(responseBody));

            // Parse trực tiếp sang Array Record
            GeocodeSearchItem[] items = objectMapper.readValue(responseBody, GeocodeSearchItem[].class);

            return Arrays.stream(items)
                    .map(item -> {
                        Double lat = parseDouble(item.lat());
                        Double lon = parseDouble(item.lon());
                        return (lat != null && lon != null) ? new GeoPoint(lat, lon) : null;
                    })
                    .filter(Objects::nonNull)
                    .findFirst()
                    .or(() -> {
                        log.debug("No valid coordinates found in {} items for query: {}", items.length, query);
                        return Optional.empty();
                    });

        } catch (Exception e) {
            log.error("Geocode call failed for query [{}]: {}", query, e.getMessage());
            // Tránh leak thông tin exception chi tiết ra ngoài nếu không cần thiết
            throw new AppException(ErrorCode.UNCATEGORIZED_EXCEPTION);
        }
    }

    private Double parseDouble(String value) {
        try {
            return (value == null) ? null : Double.parseDouble(value);
        } catch (NumberFormatException e) {
            return null;
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

    /**
     * Record đại diện cho item trong mảng JSON trả về từ Maps.co
     */
    @JsonIgnoreProperties(ignoreUnknown = true)
    private record GeocodeSearchItem(String lat, String lon) {
    }
}