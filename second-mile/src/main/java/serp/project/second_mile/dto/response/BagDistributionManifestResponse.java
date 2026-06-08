/*
Author: Nguyen The Anh
Description: Part of Serp Project
*/

package serp.project.second_mile.dto.response;

import com.fasterxml.jackson.annotation.JsonProperty;
import serp.project.second_mile.enums.BagDestinationType;
import serp.project.second_mile.enums.BagDistributionManifestStatus;

import java.time.LocalDateTime;
import java.util.List;

public record BagDistributionManifestResponse(
        Long id,
        @JsonProperty("manifest_code") String manifestCode,
        @JsonProperty("origin_hub_id") Long originHubId,
        @JsonProperty("origin_hub_code") String originHubCode,
        @JsonProperty("destination_type") BagDestinationType destinationType,
        @JsonProperty("destination_hub_id") Long destinationHubId,
        @JsonProperty("destination_hub_code") String destinationHubCode,
        @JsonProperty("destination_post_office_code") String destinationPostOfficeCode,
        @JsonProperty("route_id") Long routeId,
        @JsonProperty("route_code") String routeCode,
        @JsonProperty("vehicle_id") Long vehicleId,
        @JsonProperty("vehicle_license_plate") String vehicleLicensePlate,
        @JsonProperty("assigned_driver_id") Long assignedDriverId,
        @JsonProperty("planned_departure_at") LocalDateTime plannedDepartureAt,
        @JsonProperty("planned_arrival_at") LocalDateTime plannedArrivalAt,
        @JsonProperty("actual_departure_at") LocalDateTime actualDepartureAt,
        @JsonProperty("actual_arrival_at") LocalDateTime actualArrivalAt,
        @JsonProperty("driver_start_latitude") Double driverStartLatitude,
        @JsonProperty("driver_start_longitude") Double driverStartLongitude,
        @JsonProperty("driver_start_distance_m") Double driverStartDistanceM,
        @JsonProperty("driver_start_photo_url") String driverStartPhotoUrl,
        @JsonProperty("driver_end_latitude") Double driverEndLatitude,
        @JsonProperty("driver_end_longitude") Double driverEndLongitude,
        @JsonProperty("driver_end_distance_m") Double driverEndDistanceM,
        @JsonProperty("driver_end_photo_url") String driverEndPhotoUrl,
        BagDistributionManifestStatus status,
        String note,
        @JsonProperty("bags") List<BagDistributionManifestBagResponse> bags,
        @JsonProperty("created_at") LocalDateTime createdAt,
        @JsonProperty("updated_at") LocalDateTime updatedAt
) {
}
