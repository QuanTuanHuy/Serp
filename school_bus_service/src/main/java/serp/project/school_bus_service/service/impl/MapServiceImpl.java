package serp.project.school_bus_service.service.impl;

import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpHeaders;
import org.springframework.stereotype.Service;
import org.springframework.util.StringUtils;
import org.springframework.web.client.RestClient;
import org.springframework.web.client.RestClientException;
import serp.project.school_bus_service.dto.response.MapLocationResponse;
import serp.project.school_bus_service.service.IMapService;
import serp.project.school_bus_service.shared.exception.AppErrorCode;
import serp.project.school_bus_service.shared.exception.AppException;
import serp.project.school_bus_service.shared.i18n.MessageCommon;
import tools.jackson.databind.JsonNode;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

@Service
@Slf4j
public class MapServiceImpl implements IMapService {

    private final RestClient.Builder restClientBuilder;
    private final MessageCommon messageCommon;


    public MapServiceImpl(
    RestClient.Builder restClientBuilder,
    MessageCommon messageCommon) {
        this.restClientBuilder = restClientBuilder;
        this.messageCommon = messageCommon;
    }


    @Value("${map.nominatim.base-url:https://nominatim.openstreetmap.org}")
    private String nominatimBaseUrl;

    @Value("${map.nominatim.user-agent:SERP-SchoolBus/0.1 (local-dev)}")
    private String nominatimUserAgent;

    @Override
    public List<MapLocationResponse> geocode(String query) {
        if (!StringUtils.hasText(query)) {
            throw new AppException(AppErrorCode.Map.QUERY_REQUIRED, messageCommon.getMessage(AppErrorCode.Map.QUERY_REQUIRED));
        }

        try {
            JsonNode response = buildClient()
                    .get()
                    .uri(uriBuilder -> uriBuilder
                            .path("/search")
                            .queryParam("format", "jsonv2")
                            .queryParam("addressdetails", 1)
                            .queryParam("limit", 5)
                            .queryParam("q", query)
                            .build())
                    .retrieve()
                    .body(JsonNode.class);

            List<MapLocationResponse> results = new ArrayList<>();
            if (response != null && response.isArray()) {
                response.forEach(node -> results.add(toLocationResponse(node)));
            }
            return results;
        } catch (RestClientException | IllegalArgumentException e) {
            log.warn("Nominatim geocode lookup failed for query={}", query, e);
            return List.of();
        }
    }

    @Override
    public MapLocationResponse reverseGeocode(Double latitude, Double longitude) {
        if (latitude == null || longitude == null) {
            throw new AppException(AppErrorCode.Map.COORDINATES_REQUIRED, messageCommon.getMessage(AppErrorCode.Map.COORDINATES_REQUIRED));
        }

        try {
            JsonNode response = buildClient()
                    .get()
                    .uri(uriBuilder -> uriBuilder
                            .path("/reverse")
                            .queryParam("format", "jsonv2")
                            .queryParam("addressdetails", 1)
                            .queryParam("lat", latitude)
                            .queryParam("lon", longitude)
                            .build())
                    .retrieve()
                    .body(JsonNode.class);

            if (response == null || response.isNull()) {
                throw new AppException(AppErrorCode.Map.ADDRESS_NOT_FOUND, messageCommon.getMessage(AppErrorCode.Map.ADDRESS_NOT_FOUND));
            }

            return toLocationResponse(response);
        } catch (RestClientException | IllegalArgumentException e) {
            log.warn("Nominatim reverse geocode lookup failed for lat={}, lng={}", latitude, longitude, e);
            return fallbackLocation(latitude, longitude);
        }
    }

    private RestClient buildClient() {
        return restClientBuilder
                .baseUrl(nominatimBaseUrl)
                .defaultHeader(HttpHeaders.USER_AGENT, nominatimUserAgent)
                .defaultHeader(HttpHeaders.ACCEPT, "application/json")
                .build();
    }

    private MapLocationResponse toLocationResponse(JsonNode node) {
        MapLocationResponse response = new MapLocationResponse();
        response.setDisplayName(node.path("display_name").asText(null));
        response.setLatitude(parseDouble(node.path("lat").asText(null)));
        response.setLongitude(parseDouble(node.path("lon").asText(null)));
        response.setAddressParts(toAddressParts(node.path("address")));
        return response;
    }

    private Map<String, String> toAddressParts(JsonNode node) {
        Map<String, String> parts = new LinkedHashMap<>();
        if (node == null || !node.isObject()) {
            return parts;
        }

        Iterator<Map.Entry<String, JsonNode>> fields = node.properties().iterator();
        while (fields.hasNext()) {
            Map.Entry<String, JsonNode> field = fields.next();
            parts.put(field.getKey(), field.getValue().asText());
        }
        return parts;
    }

    private Double parseDouble(String value) {
        if (!StringUtils.hasText(value)) {
            return null;
        }
        try {
            return Double.parseDouble(value);
        } catch (NumberFormatException e) {
            return null;
        }
    }

    private MapLocationResponse fallbackLocation(Double latitude, Double longitude) {
        MapLocationResponse response = new MapLocationResponse();
        response.setDisplayName(String.format("Selected location %.6f, %.6f", latitude, longitude));
        response.setLatitude(latitude);
        response.setLongitude(longitude);
        response.setAddressParts(Map.of());
        return response;
    }
}
