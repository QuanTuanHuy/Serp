/*
Author: Nguyen The Anh
Description: Part of Serp Project
*/

package serp.project.second_mile.dto.response;

import com.fasterxml.jackson.annotation.JsonProperty;

public record BaggingKpiResponse(
        @JsonProperty("origin_hub_id") Long originHubId,
        @JsonProperty("sealed_bag_count") long sealedBagCount,
        @JsonProperty("avg_fill_rate_weight") double avgFillRateWeight,
        @JsonProperty("avg_fill_rate_volume") double avgFillRateVolume,
        @JsonProperty("avg_orders_per_bag") double avgOrdersPerBag
) {
}
