/*
Author: Nguyen The Anh
Description: Part of Serp Project
*/

package serp.project.second_mile.dto.response;

import com.fasterxml.jackson.annotation.JsonProperty;
import serp.project.second_mile.enums.HandoverManifestStatus;

import java.time.LocalDateTime;
import java.util.List;

public record HandoverManifestResponse(
        Long id,
        @JsonProperty("manifest_code") String manifestCode,
        @JsonProperty("origin_post_office_code") String originPostOfficeCode,
        @JsonProperty("target_hub_id") Long targetHubId,
        @JsonProperty("vehicle_id") Long vehicleId,
        @JsonProperty("vehicle_license_plate") String vehicleLicensePlate,
        @JsonProperty("route_id") Long routeId,
        @JsonProperty("route_code") String routeCode,
        HandoverManifestStatus status,
        @JsonProperty("orders") List<HandoverManifestOrderResponse> orders,
        @JsonProperty("created_at") LocalDateTime createdAt,
        @JsonProperty("updated_at") LocalDateTime updatedAt
) {
}
