/*
Author: Nguyen The Anh
Description: Part of Serp Project
*/

package serp.project.second_mile.dto.response;

import com.fasterxml.jackson.annotation.JsonProperty;

import java.time.LocalDateTime;

public record HandoverManifestOrderResponse(
        Long id,
        @JsonProperty("order_id") Long orderId,
        @JsonProperty("order_code") String orderCode,
        @JsonProperty("scan_out_time") LocalDateTime scanOutTime,
        @JsonProperty("scan_in_time") LocalDateTime scanInTime
) {
}
