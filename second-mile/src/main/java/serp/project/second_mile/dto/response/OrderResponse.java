/*
Author: Nguyen The Anh
Description: Part of Serp Project
*/

package serp.project.second_mile.dto.response;

import com.fasterxml.jackson.annotation.JsonProperty;
import serp.project.second_mile.enums.OrderProductCategory;
import serp.project.second_mile.enums.OrderStatus;
import serp.project.second_mile.enums.OrderType;

import java.time.LocalDateTime;

public record OrderResponse(
        Long id,
        @JsonProperty("order_code") String orderCode,
        @JsonProperty("customer_order_code") String customerOrderCode,
        @JsonProperty("origin_post_office_code") String originPostOfficeCode,
        @JsonProperty("destination_post_office_code") String destinationPostOfficeCode,
        OrderStatus status,
        @JsonProperty("total_weight") Double totalWeight,
        @JsonProperty("total_volume") Double totalVolume,
        @JsonProperty("order_product_category") OrderProductCategory orderProductCategory,
        @JsonProperty("order_type") OrderType orderType,
        String note,
        @JsonProperty("assigned_bag_id") Long assignedBagId,
        @JsonProperty("assigned_bag_code") String assignedBagCode,
        @JsonProperty("created_at") LocalDateTime createdAt,
        @JsonProperty("updated_at") LocalDateTime updatedAt,
        @JsonProperty("tenant_id") Long tenantId
) {
}
