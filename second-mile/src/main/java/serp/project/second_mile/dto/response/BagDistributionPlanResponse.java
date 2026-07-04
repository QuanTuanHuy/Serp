/*
Author: Nguyen The Anh
Description: Part of Serp Project
*/

package serp.project.second_mile.dto.response;

import com.fasterxml.jackson.annotation.JsonProperty;

import java.util.List;

public record BagDistributionPlanResponse(
        boolean executed,
        @JsonProperty("manifest_count") int manifestCount,
        List<BagDistributionPlanItemResponse> items
) {
}
