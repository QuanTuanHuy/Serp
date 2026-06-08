/*
Author: Nguyen The Anh
Description: Part of Serp Project
*/

package serp.project.second_mile.dto.response;

import com.fasterxml.jackson.annotation.JsonProperty;
import serp.project.second_mile.enums.BagDestinationType;

import java.time.LocalDateTime;

public record BagDistributionManifestBagResponse(
        Long id,
        @JsonProperty("bag_id") Long bagId,
        @JsonProperty("bag_code") String bagCode,
        @JsonProperty("origin_hub_id") Long originHubId,
        @JsonProperty("destination_type") BagDestinationType destinationType,
        @JsonProperty("destination_hub_id") Long destinationHubId,
        @JsonProperty("destination_post_office_code") String destinationPostOfficeCode,
        @JsonProperty("total_weight_snapshot") Double totalWeightSnapshot,
        @JsonProperty("total_volume_snapshot") Double totalVolumeSnapshot,
        @JsonProperty("total_orders_snapshot") Integer totalOrdersSnapshot,
        @JsonProperty("scan_out_time") LocalDateTime scanOutTime,
        @JsonProperty("scan_in_time") LocalDateTime scanInTime
) {
}
