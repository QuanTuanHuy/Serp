/*
Author: Nguyen The Anh
Description: Part of Serp Project
*/

package serp.project.second_mile.dto.response;

import com.fasterxml.jackson.annotation.JsonProperty;

public record BagCapacitySettingsResponse(
        Long id,
        @JsonProperty("max_weight") Double maxWeight,
        @JsonProperty("max_volume") Double maxVolume,
        @JsonProperty("max_orders") Integer maxOrders
) {
}
