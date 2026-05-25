/*
Author: Nguyen The Anh
Description: Part of Serp Project
*/

package serp.project.second_mile.dto.response;

import com.fasterxml.jackson.annotation.JsonProperty;

public record BagOrderResponse(
        Long id,
        @JsonProperty("order_id") Long orderId,
        @JsonProperty("order_code") String orderCode
) {
}
