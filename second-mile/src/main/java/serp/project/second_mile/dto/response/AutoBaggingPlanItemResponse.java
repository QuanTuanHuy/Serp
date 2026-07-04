/*
Author: Nguyen The Anh
Description: Part of Serp Project
*/

package serp.project.second_mile.dto.response;

import com.fasterxml.jackson.annotation.JsonProperty;

import java.util.List;

public record AutoBaggingPlanItemResponse(
        @JsonProperty("bag_code") String bagCode,
        @JsonProperty("order_codes") List<String> orderCodes,
        @JsonProperty("total_weight") Double totalWeight,
        @JsonProperty("total_volume") Double totalVolume
) {
}
