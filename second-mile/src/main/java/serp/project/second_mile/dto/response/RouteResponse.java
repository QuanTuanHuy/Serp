/*
Author: Nguyen The Anh
Description: Part of Serp Project
*/

package serp.project.second_mile.dto.response;

import com.fasterxml.jackson.annotation.JsonProperty;
import serp.project.second_mile.enums.RouteDestinationType;
import serp.project.second_mile.enums.RouteStatus;

import java.time.LocalDateTime;
import java.time.LocalTime;

public record RouteResponse(
        Long id,
        @JsonProperty("route_code") String routeCode,
        @JsonProperty("route_name") String routeName,
        @JsonProperty("origin_hub_id") Long originHubId,
        @JsonProperty("destination_type") RouteDestinationType destinationType,
        @JsonProperty("destination_hub_id") Long destinationHubId,
        @JsonProperty("destination_post_office_code") String destinationPostOfficeCode,
        @JsonProperty("vehicle_id") Long vehicleId,
        @JsonProperty("estimated_distance_km") Double estimatedDistanceKm,
        @JsonProperty("estimated_duration_minutes") Integer estimatedDurationMinutes,
        @JsonProperty("fixed_departure_time") LocalTime fixedDepartureTime,
        RouteStatus status,
        String note,
        @JsonProperty("created_at") LocalDateTime createdAt,
        @JsonProperty("updated_at") LocalDateTime updatedAt,
        @JsonProperty("created_by") String createdBy,
        @JsonProperty("updated_by") String updatedBy,
        @JsonProperty("tenant_id") Long tenantId
) {
}
