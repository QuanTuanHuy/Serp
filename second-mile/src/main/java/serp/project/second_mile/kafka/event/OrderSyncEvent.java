/*
Author: Nguyen The Anh
Description: Part of Serp Project
*/

package serp.project.second_mile.kafka.event;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import serp.project.second_mile.domain.Dimension;
import serp.project.second_mile.enums.OrderProductCategory;
import serp.project.second_mile.enums.OrderStatus;
import serp.project.second_mile.enums.OrderSyncEventSource;
import serp.project.second_mile.enums.OrderType;

import java.time.LocalDateTime;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class OrderSyncEvent {
    @JsonProperty("order_code")
    private String orderCode;

    @JsonProperty("customer_order_code")
    private String customerOrderCode;

    @JsonProperty("origin_post_office_code")
    private String originPostOfficeCode;

    @JsonProperty("destination_post_office_code")
    private String destinationPostOfficeCode;

    @JsonProperty("status")
    private OrderStatus status;

    @JsonProperty("total_weight")
    private Double totalWeight;

    @JsonProperty("dimensions")
    private Dimension dimensions;

    @JsonProperty("total_volume")
    private Double totalVolume;

    @JsonProperty("order_product_category")
    private OrderProductCategory orderProductCategory;

    @JsonProperty("order_type")
    private OrderType orderType;

    @JsonProperty("note")
    private String note;

    @JsonProperty("created_by")
    private String createdBy;

    @JsonProperty("updated_by")
    private String updatedBy;

    @JsonProperty("created_at")
    private LocalDateTime createdAt;

    @JsonProperty("updated_at")
    private LocalDateTime updatedAt;

    @JsonProperty("tenant_id")
    private Long tenantId;

    @JsonProperty("event_source")
    private OrderSyncEventSource eventSource;
}
