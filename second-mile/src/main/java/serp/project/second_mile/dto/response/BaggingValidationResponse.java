/*
Author: Nguyen The Anh
Description: Part of Serp Project
*/

package serp.project.second_mile.dto.response;

import com.fasterxml.jackson.annotation.JsonProperty;

import java.util.List;

public record BaggingValidationResponse(
        @JsonProperty("bag_id") Long bagId,
        @JsonProperty("accepted_count") int acceptedCount,
        @JsonProperty("rejected_count") int rejectedCount,
        @JsonProperty("items") List<BaggingValidationItemResponse> items
) {
}
