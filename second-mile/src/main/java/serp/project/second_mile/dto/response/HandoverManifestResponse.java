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
        @JsonProperty("assigned_driver_id") Long assignedDriverId,
        @JsonProperty("route_id") Long routeId,
        @JsonProperty("route_code") String routeCode,
        @JsonProperty("planned_departure_at") LocalDateTime plannedDepartureAt,
        @JsonProperty("planned_arrival_at") LocalDateTime plannedArrivalAt,
        @JsonProperty("origin_post_office_latitude") Double originPostOfficeLatitude,
        @JsonProperty("origin_post_office_longitude") Double originPostOfficeLongitude,
        @JsonProperty("target_hub_latitude") Double targetHubLatitude,
        @JsonProperty("target_hub_longitude") Double targetHubLongitude,
        @JsonProperty("driver_start_checkin_at") LocalDateTime driverStartCheckinAt,
        @JsonProperty("driver_start_latitude") Double driverStartLatitude,
        @JsonProperty("driver_start_longitude") Double driverStartLongitude,
        @JsonProperty("driver_start_distance_m") Double driverStartDistanceM,
        @JsonProperty("driver_start_photo_url") String driverStartPhotoUrl,
        @JsonProperty("driver_end_checkin_at") LocalDateTime driverEndCheckinAt,
        @JsonProperty("driver_end_latitude") Double driverEndLatitude,
        @JsonProperty("driver_end_longitude") Double driverEndLongitude,
        @JsonProperty("driver_end_distance_m") Double driverEndDistanceM,
        @JsonProperty("driver_end_photo_url") String driverEndPhotoUrl,
        HandoverManifestStatus status,
        @JsonProperty("orders") List<HandoverManifestOrderResponse> orders,
        @JsonProperty("created_at") LocalDateTime createdAt,
        @JsonProperty("updated_at") LocalDateTime updatedAt
) {
}
