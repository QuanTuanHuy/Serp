/*
Author: Nguyen The Anh
Description: Part of Serp Project
*/

package serp.project.second_mile.dto.response;

import com.fasterxml.jackson.annotation.JsonProperty;

public record BaggingValidationItemResponse(
        @JsonProperty("order_code") String orderCode,
        boolean accepted,
        String reason
) {
}
