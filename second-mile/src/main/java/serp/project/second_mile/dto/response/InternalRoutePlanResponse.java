/*
Author: Nguyen The Anh
Description: Part of Serp Project
*/

package serp.project.second_mile.dto.response;

import com.fasterxml.jackson.annotation.JsonProperty;
import serp.project.second_mile.enums.RouteDestinationType;
import serp.project.second_mile.enums.RouteEndpointType;

import java.util.List;

public record InternalRoutePlanResponse(
        @JsonProperty("origin_post_office_code") String originPostOfficeCode,
        @JsonProperty("destination_post_office_code") String destinationPostOfficeCode,
        @JsonProperty("route_found") boolean routeFound,
        @JsonProperty("total_estimated_distance_km") Double totalEstimatedDistanceKm,
        @JsonProperty("total_estimated_duration_minutes") Integer totalEstimatedDurationMinutes,
        List<Leg> legs
) {
    public record Leg(
            Integer sequence,
            @JsonProperty("route_id") Long routeId,
            @JsonProperty("route_code") String routeCode,
            @JsonProperty("route_name") String routeName,
            @JsonProperty("origin_type") RouteEndpointType originType,
            @JsonProperty("origin_hub_id") Long originHubId,
            @JsonProperty("origin_post_office_code") String originPostOfficeCode,
            @JsonProperty("destination_type") RouteDestinationType destinationType,
            @JsonProperty("destination_hub_id") Long destinationHubId,
            @JsonProperty("destination_post_office_code") String destinationPostOfficeCode,
            @JsonProperty("vehicle_id") Long vehicleId,
            @JsonProperty("estimated_distance_km") Double estimatedDistanceKm,
            @JsonProperty("estimated_duration_minutes") Integer estimatedDurationMinutes
    ) {
    }
}
