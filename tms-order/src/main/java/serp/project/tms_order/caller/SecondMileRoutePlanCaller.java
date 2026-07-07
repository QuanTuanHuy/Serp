/*
Author: Nguyen The Anh
Description: Part of Serp Project
*/

package serp.project.tms_order.caller;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.stereotype.Component;
import org.springframework.web.client.RestClient;
import org.springframework.web.client.RestClientException;
import org.springframework.web.client.RestClientResponseException;
import serp.project.tms_order.domain.PlannedOrderRoute;
import serp.project.tms_order.exception.AppException;
import serp.project.tms_order.exception.ErrorCode;
import serp.project.tms_order.kernel.utils.AuthUtils;

import java.util.List;

@Component
@Slf4j
public class SecondMileRoutePlanCaller {
    private final RestClient restClient;
    private final AuthUtils authUtils;
    private final String internalApiKey;

    @Value("${second-mile.service.route-plan-path:/api/v1/internal/route-plans/orders}")
    private String routePlanPath;

    public SecondMileRoutePlanCaller(
            RestClient.Builder restClientBuilder,
            AuthUtils authUtils,
            @Value("${second-mile.service.base-url:http://localhost:8102}") String secondMileBaseUrl,
            @Value("${internal-api.api-key:}") String internalApiKey
    ) {
        this.authUtils = authUtils;
        this.internalApiKey = normalizeText(internalApiKey);
        this.restClient = restClientBuilder.baseUrl(secondMileBaseUrl).build();
    }

    public PlannedOrderRoute planOrderRoute(String originPostOfficeCode, String destinationPostOfficeCode) {
        try {
            RoutePlanResponse response = restClient.post()
                    .uri(routePlanPath)
                    .contentType(MediaType.APPLICATION_JSON)
                    .headers(this::applyAuthHeaders)
                    .body(new RoutePlanRequest(originPostOfficeCode, destinationPostOfficeCode))
                    .retrieve()
                    .body(RoutePlanResponse.class);
            if (response == null || !response.routeFound) {
                throw new AppException(
                        ErrorCode.INVALID_REQUEST,
                        "No active route plan found from origin post office to destination post office."
                );
            }
            return toPlannedOrderRoute(response);
        } catch (RestClientException exception) {
            log.error("Failed to call second-mile route planner: {}", exception.getMessage(), exception);
            throw toSecondMileException(exception);
        }
    }

    private PlannedOrderRoute toPlannedOrderRoute(RoutePlanResponse response) {
        List<PlannedOrderRoute.Leg> legs = response.legs == null
                ? List.of()
                : response.legs.stream()
                .map(item -> PlannedOrderRoute.Leg.builder()
                        .sequence(item.sequence)
                        .routeId(item.routeId)
                        .routeCode(item.routeCode)
                        .routeName(item.routeName)
                        .originType(item.originType)
                        .originHubId(item.originHubId)
                        .originPostOfficeCode(item.originPostOfficeCode)
                        .destinationType(item.destinationType)
                        .destinationHubId(item.destinationHubId)
                        .destinationPostOfficeCode(item.destinationPostOfficeCode)
                        .vehicleId(item.vehicleId)
                        .estimatedDistanceKm(item.estimatedDistanceKm)
                        .estimatedDurationMinutes(item.estimatedDurationMinutes)
                        .build())
                .toList();
        return PlannedOrderRoute.builder()
                .originPostOfficeCode(response.originPostOfficeCode)
                .destinationPostOfficeCode(response.destinationPostOfficeCode)
                .totalEstimatedDistanceKm(response.totalEstimatedDistanceKm)
                .totalEstimatedDurationMinutes(response.totalEstimatedDurationMinutes)
                .legs(legs)
                .build();
    }

    private void applyAuthHeaders(HttpHeaders headers) {
        Long tenantId = authUtils.getCurrentTenantId().orElse(null);
        if (internalApiKey == null || tenantId == null) {
            throw new AppException(
                    ErrorCode.UNAUTHORIZED,
                    "Missing internal API key or tenant id for second-mile route planner call."
            );
        }
        headers.set("X-Internal-Api-Key", internalApiKey);
        headers.set("X-Tenant-Id", tenantId.toString());
        headers.set("X-Internal-Service", "tms-order");
    }

    private AppException toSecondMileException(RestClientException exception) {
        if (exception instanceof RestClientResponseException responseException) {
            int statusCode = responseException.getStatusCode().value();
            if (statusCode == 401 || statusCode == 403) {
                return new AppException(ErrorCode.UNAUTHORIZED, "Second-mile service rejected route planning access.");
            }
            if (statusCode == 400 || statusCode == 409) {
                return new AppException(
                        ErrorCode.INVALID_REQUEST,
                        "Second-mile service cannot determine an active route plan for this order."
                );
            }
            return new AppException(
                    ErrorCode.UNCATEGORIZED_EXCEPTION,
                    "Second-mile service returned HTTP " + statusCode + "."
            );
        }
        return new AppException(
                ErrorCode.UNCATEGORIZED_EXCEPTION,
                "Cannot connect to second-mile service. Ensure second-mile is running and SECOND_MILE_SERVICE_BASE_URL is correct."
        );
    }

    private String normalizeText(String value) {
        if (value == null) {
            return null;
        }
        String trimmed = value.trim();
        return trimmed.isEmpty() ? null : trimmed;
    }

    private record RoutePlanRequest(
            @JsonProperty("origin_post_office_code") String originPostOfficeCode,
            @JsonProperty("destination_post_office_code") String destinationPostOfficeCode
    ) {
    }

    @Data
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    private static class RoutePlanResponse {
        @JsonProperty("origin_post_office_code")
        private String originPostOfficeCode;
        @JsonProperty("destination_post_office_code")
        private String destinationPostOfficeCode;
        @JsonProperty("route_found")
        private boolean routeFound;
        @JsonProperty("total_estimated_distance_km")
        private Double totalEstimatedDistanceKm;
        @JsonProperty("total_estimated_duration_minutes")
        private Integer totalEstimatedDurationMinutes;
        private List<RoutePlanLegResponse> legs;
    }

    @Data
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    private static class RoutePlanLegResponse {
        private Integer sequence;
        @JsonProperty("route_id")
        private Long routeId;
        @JsonProperty("route_code")
        private String routeCode;
        @JsonProperty("route_name")
        private String routeName;
        @JsonProperty("origin_type")
        private String originType;
        @JsonProperty("origin_hub_id")
        private Long originHubId;
        @JsonProperty("origin_post_office_code")
        private String originPostOfficeCode;
        @JsonProperty("destination_type")
        private String destinationType;
        @JsonProperty("destination_hub_id")
        private Long destinationHubId;
        @JsonProperty("destination_post_office_code")
        private String destinationPostOfficeCode;
        @JsonProperty("vehicle_id")
        private Long vehicleId;
        @JsonProperty("estimated_distance_km")
        private Double estimatedDistanceKm;
        @JsonProperty("estimated_duration_minutes")
        private Integer estimatedDurationMinutes;
    }
}
