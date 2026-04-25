/*
Author: Nguyen The Anh
Description: Part of Serp Project
*/

package serp.project.first_mile.dto.request;

import com.fasterxml.jackson.annotation.JsonProperty;
import jakarta.validation.Valid;
import jakarta.validation.constraints.DecimalMax;
import jakarta.validation.constraints.DecimalMin;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import serp.project.first_mile.enums.DeliveryRequestTime;
import serp.project.first_mile.enums.FeePayer;
import serp.project.first_mile.enums.OrderProductCategory;
import serp.project.first_mile.enums.OrderPickupMethod;
import serp.project.first_mile.enums.OrderType;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class CreateOrderRequest {

    @JsonProperty("customer_order_code")
    @NotBlank
    private String customerOrderCode;

    @JsonProperty("sender_name")
    @NotBlank
    private String senderName;

    @JsonProperty("sender_phone")
    @NotBlank
    private String senderPhone;

    @JsonProperty("sender_province_code")
    @NotBlank
    private String senderProvinceCode;

    @JsonProperty("sender_ward_code")
    @NotBlank
    private String senderWardCode;

    @JsonProperty("sender_address_detail")
    @NotBlank
    private String senderAddressDetail;

    @JsonProperty("sender_latitude")
    @NotNull
    @DecimalMin(value = "-90.0")
    @DecimalMax(value = "90.0")
    private Double senderLatitude;

    @JsonProperty("sender_longitude")
    @NotNull
    @DecimalMin(value = "-180.0")
    @DecimalMax(value = "180.0")
    private Double senderLongitude;

    @JsonProperty("receiver_name")
    @NotBlank
    private String receiverName;

    @JsonProperty("receiver_phone")
    @NotBlank
    private String receiverPhone;

    @JsonProperty("receiver_province_code")
    @NotBlank
    private String receiverProvinceCode;

    @JsonProperty("receiver_ward_code")
    @NotBlank
    private String receiverWardCode;

    @JsonProperty("receiver_address_detail")
    @NotBlank
    private String receiverAddressDetail;

    @JsonProperty("receiver_latitude")
    @NotNull
    @DecimalMin(value = "-90.0")
    @DecimalMax(value = "90.0")
    private Double receiverLatitude;

    @JsonProperty("receiver_longitude")
    @NotNull
    @DecimalMin(value = "-180.0")
    @DecimalMax(value = "180.0")
    private Double receiverLongitude;

    @JsonProperty("pickup_time_start")
    private LocalDateTime pickupTimeStart;

    @JsonProperty("pickup_time_end")
    private LocalDateTime pickupTimeEnd;

    @JsonProperty("delivery_request_time")
    @NotNull
    private DeliveryRequestTime deliveryRequestTime;

    @JsonProperty("pickup_method")
    private OrderPickupMethod pickupMethod;

    @JsonProperty("order_product_category")
    private OrderProductCategory orderProductCategory;

    @JsonProperty("order_type")
    @NotNull
    private OrderType orderType;

    @JsonProperty("fee_payer")
    @NotNull
    private FeePayer feePayer;

    @JsonProperty("is_cod")
    private Boolean isCod;

    @JsonProperty("dimension_length_cm")
    @DecimalMin(value = "0.0", inclusive = false)
    private Double dimensionLengthCm;

    @JsonProperty("dimension_width_cm")
    @DecimalMin(value = "0.0", inclusive = false)
    private Double dimensionWidthCm;

    @JsonProperty("dimension_height_cm")
    @DecimalMin(value = "0.0", inclusive = false)
    private Double dimensionHeightCm;

    @JsonProperty("total_volume_m3")
    @DecimalMin(value = "0.0", inclusive = false)
    private Double totalVolumeM3;

    @JsonProperty("note")
    private String note;

    @JsonProperty("products")
    @Valid
    private List<ProductItem> products = new ArrayList<>();

    @Data
    @NoArgsConstructor
    @AllArgsConstructor
    public static class ProductItem {

        @JsonProperty("name")
        @NotBlank
        private String name;

        @JsonProperty("value")
        @NotNull
        @Min(0)
        private Long value;

        @JsonProperty("quantity")
        @NotNull
        @Min(1)
        private Integer quantity;

        @JsonProperty("weight_gram")
        @NotNull
        @DecimalMin(value = "0.0", inclusive = false)
        private Double weightGram;

        @JsonProperty("product_type_id")
        @NotNull
        private Long productTypeId;
    }
}
