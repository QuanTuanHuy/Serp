/*
Author: Nguyen The Anh
Description: Part of Serp Project
*/

package serp.project.tms_order.dto.request;

import com.fasterxml.jackson.annotation.JsonAlias;
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
import serp.project.tms_order.enums.DeliveryRequestTime;
import serp.project.tms_order.enums.FeePayer;
import serp.project.tms_order.enums.OrderPickupMethod;
import serp.project.tms_order.enums.OrderProductCategory;
import serp.project.tms_order.enums.OrderType;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class CreateOrderRequest {

    @JsonProperty("customer_order_code")
    @JsonAlias("customerOrderCode")
    @NotBlank
    private String customerOrderCode;

    @JsonProperty("sender_name")
    @JsonAlias("senderName")
    @NotBlank
    private String senderName;

    @JsonProperty("sender_phone")
    @JsonAlias("senderPhone")
    @NotBlank
    private String senderPhone;

    @JsonProperty("sender_province_code")
    @JsonAlias("senderProvinceCode")
    @NotBlank
    private String senderProvinceCode;

    @JsonProperty("sender_ward_code")
    @JsonAlias("senderWardCode")
    @NotBlank
    private String senderWardCode;

    @JsonProperty("sender_address_detail")
    @JsonAlias("senderAddressDetail")
    @NotBlank
    private String senderAddressDetail;

    @JsonProperty("sender_latitude")
    @JsonAlias("senderLatitude")
    @NotNull
    @DecimalMin(value = "-90.0")
    @DecimalMax(value = "90.0")
    private Double senderLatitude;

    @JsonProperty("sender_longitude")
    @JsonAlias("senderLongitude")
    @NotNull
    @DecimalMin(value = "-180.0")
    @DecimalMax(value = "180.0")
    private Double senderLongitude;

    @JsonProperty("receiver_name")
    @JsonAlias("receiverName")
    @NotBlank
    private String receiverName;

    @JsonProperty("receiver_phone")
    @JsonAlias("receiverPhone")
    @NotBlank
    private String receiverPhone;

    @JsonProperty("receiver_province_code")
    @JsonAlias("receiverProvinceCode")
    @NotBlank
    private String receiverProvinceCode;

    @JsonProperty("receiver_ward_code")
    @JsonAlias("receiverWardCode")
    @NotBlank
    private String receiverWardCode;

    @JsonProperty("receiver_address_detail")
    @JsonAlias("receiverAddressDetail")
    @NotBlank
    private String receiverAddressDetail;

    @JsonProperty("receiver_latitude")
    @JsonAlias("receiverLatitude")
    @NotNull
    @DecimalMin(value = "-90.0")
    @DecimalMax(value = "90.0")
    private Double receiverLatitude;

    @JsonProperty("receiver_longitude")
    @JsonAlias("receiverLongitude")
    @NotNull
    @DecimalMin(value = "-180.0")
    @DecimalMax(value = "180.0")
    private Double receiverLongitude;

    @JsonProperty("pickup_time_start")
    @JsonAlias("pickupTimeStart")
    private LocalDateTime pickupTimeStart;

    @JsonProperty("pickup_time_end")
    @JsonAlias("pickupTimeEnd")
    private LocalDateTime pickupTimeEnd;

    @JsonProperty("delivery_request_time")
    @JsonAlias("deliveryRequestTime")
    @NotNull
    private DeliveryRequestTime deliveryRequestTime;

    @JsonProperty("pickup_method")
    @JsonAlias("pickupMethod")
    private OrderPickupMethod pickupMethod;

    @JsonProperty("order_type")
    @JsonAlias("orderType")
    @NotNull
    private OrderType orderType;

    @JsonProperty("order_product_category")
    @JsonAlias("orderProductCategory")
    private OrderProductCategory orderProductCategory;

    @JsonProperty("fee_payer")
    @JsonAlias("feePayer")
    @NotNull
    private FeePayer feePayer;

    @JsonProperty("is_cod")
    @JsonAlias("isCod")
    private Boolean isCod;

    @JsonProperty("dimension_length_cm")
    @JsonAlias("dimensionLengthCm")
    @DecimalMin(value = "0.0", inclusive = false)
    private Double dimensionLengthCm;

    @JsonProperty("dimension_width_cm")
    @JsonAlias("dimensionWidthCm")
    @DecimalMin(value = "0.0", inclusive = false)
    private Double dimensionWidthCm;

    @JsonProperty("dimension_height_cm")
    @JsonAlias("dimensionHeightCm")
    @DecimalMin(value = "0.0", inclusive = false)
    private Double dimensionHeightCm;

    @JsonProperty("total_volume_m3")
    @JsonAlias("totalVolumeM3")
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
        @JsonAlias("weightGram")
        @NotNull
        @DecimalMin(value = "0.0", inclusive = false)
        private Double weightGram;

        @JsonProperty("product_type_id")
        @JsonAlias("productTypeId")
        @NotNull
        private Long productTypeId;
    }
}
