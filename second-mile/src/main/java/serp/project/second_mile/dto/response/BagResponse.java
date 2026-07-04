/*
Author: Nguyen The Anh
Description: Part of Serp Project
*/

package serp.project.second_mile.dto.response;

import com.fasterxml.jackson.annotation.JsonProperty;
import serp.project.second_mile.enums.BagDestinationType;
import serp.project.second_mile.enums.BagStatus;

import java.time.LocalDateTime;
import java.util.List;

public record BagResponse(
        Long id,
        @JsonProperty("bag_code") String bagCode,
        @JsonProperty("origin_hub_id") Long originHubId,
        @JsonProperty("destination_type") BagDestinationType destinationType,
        @JsonProperty("destination_hub_id") Long destinationHubId,
        @JsonProperty("destination_post_office_code") String destinationPostOfficeCode,
        @JsonProperty("vehicle_id") Long vehicleId,
        @JsonProperty("route_id") Long routeId,
        @JsonProperty("max_weight") Double maxWeight,
        @JsonProperty("max_volume") Double maxVolume,
        @JsonProperty("max_orders") Integer maxOrders,
        @JsonProperty("current_weight") Double currentWeight,
        @JsonProperty("current_volume") Double currentVolume,
        @JsonProperty("current_orders") Integer currentOrders,
        BagStatus status,
        @JsonProperty("sealed_at") LocalDateTime sealedAt,
        String note,
        @JsonProperty("orders") List<BagOrderResponse> orders,
        @JsonProperty("created_at") LocalDateTime createdAt,
        @JsonProperty("updated_at") LocalDateTime updatedAt,
        @JsonProperty("created_by") String createdBy,
        @JsonProperty("updated_by") String updatedBy,
        @JsonProperty("tenant_id") Long tenantId
) {
}
