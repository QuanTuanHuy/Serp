/*
Author: Nguyen The Anh
Description: Part of Serp Project
*/

package serp.project.second_mile.dto.response;

import com.fasterxml.jackson.annotation.JsonProperty;

import java.util.List;

public record AutoBaggingPlanResponse(
        boolean executed,
        @JsonProperty("bag_count") int bagCount,
        @JsonProperty("items") List<AutoBaggingPlanItemResponse> items
) {
}
