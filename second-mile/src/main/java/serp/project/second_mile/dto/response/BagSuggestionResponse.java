/*
Author: Nguyen The Anh
Description: Part of Serp Project
*/

package serp.project.second_mile.dto.response;

import com.fasterxml.jackson.annotation.JsonProperty;

public record BagSuggestionResponse(
        @JsonProperty("bag_id") Long bagId,
        @JsonProperty("bag_code") String bagCode,
        @JsonProperty("remaining_weight") Double remainingWeight,
        @JsonProperty("remaining_volume") Double remainingVolume,
        @JsonProperty("remaining_orders") Integer remainingOrders
) {
}
