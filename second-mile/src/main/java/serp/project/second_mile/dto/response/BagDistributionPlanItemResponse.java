/*
Author: Nguyen The Anh
Description: Part of Serp Project
*/

package serp.project.second_mile.dto.response;

import com.fasterxml.jackson.annotation.JsonProperty;
import serp.project.second_mile.enums.BagDestinationType;

import java.time.LocalDateTime;
import java.util.List;

public record BagDistributionPlanItemResponse(
        @JsonProperty("origin_hub_id") Long originHubId,
        @JsonProperty("destination_type") BagDestinationType destinationType,
        @JsonProperty("destination_hub_id") Long destinationHubId,
        @JsonProperty("destination_post_office_code") String destinationPostOfficeCode,
        @JsonProperty("route_id") Long routeId,
        @JsonProperty("route_code") String routeCode,
        @JsonProperty("vehicle_id") Long vehicleId,
        @JsonProperty("vehicle_license_plate") String vehicleLicensePlate,
        @JsonProperty("assigned_driver_id") Long assignedDriverId,
        @JsonProperty("planned_departure_at") LocalDateTime plannedDepartureAt,
        @JsonProperty("planned_arrival_at") LocalDateTime plannedArrivalAt,
        @JsonProperty("bag_ids") List<Long> bagIds,
        @JsonProperty("bag_codes") List<String> bagCodes,
        @JsonProperty("total_weight") Double totalWeight,
        @JsonProperty("total_volume") Double totalVolume,
        @JsonProperty("total_orders") Integer totalOrders,
        Double score,
        List<String> hints,
        @JsonProperty("created_manifest_id") Long createdManifestId,
        @JsonProperty("created_manifest_code") String createdManifestCode
) {
}
