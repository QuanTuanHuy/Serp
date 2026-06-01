/*
Author: QuanTuanHuy
Description: Part of Serp Project
*/

package serp.project.first_mile.dto.response;

import com.fasterxml.jackson.annotation.JsonProperty;
import serp.project.first_mile.enums.OrderStatus;

import java.time.LocalDateTime;

public record PostOfficeHandoverManifestOrderResponse(
        Long id,
        @JsonProperty("order_id") Long orderId,
        @JsonProperty("order_code") String orderCode,
        @JsonProperty("customer_order_code") String customerOrderCode,
        OrderStatus status,
        @JsonProperty("scan_out_time") LocalDateTime scanOutTime,
        @JsonProperty("scan_in_time") LocalDateTime scanInTime
) {
}
