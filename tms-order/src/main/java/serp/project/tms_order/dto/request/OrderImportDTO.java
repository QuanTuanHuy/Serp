/*
Author: Nguyen The Anh
Description: Part of Serp Project
*/

package serp.project.tms_order.dto.request;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import serp.project.tms_order.enums.DeliveryRequestTime;
import serp.project.tms_order.enums.FeePayer;
import serp.project.tms_order.enums.OrderType;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class OrderImportDTO {
	@JsonProperty("customer_order_code")
	private String customerOrderCode;

	@JsonProperty("sender_name")
	private String senderName;

	@JsonProperty("sender_phone")
	private String senderPhone;

	@JsonProperty("sender_province_code")
	private String senderProvinceCode;

	@JsonProperty("sender_ward_code")
	private String senderWardCode;

	@JsonProperty("sender_address_detail")
	private String senderAddressDetail;

	@JsonProperty("receiver_name")
	private String receiverName;

	@JsonProperty("receiver_phone")
	private String receiverPhone;

	@JsonProperty("receiver_province_code")
	private String receiverProvinceCode;

	@JsonProperty("receiver_ward_code")
	private String receiverWardCode;

	@JsonProperty("receiver_address_detail")
	private String receiverAddressDetail;

	@JsonProperty("order_type")
	private OrderType orderType;

	@JsonProperty("note")
	private String note;

	@JsonProperty("pickup_date")
	private LocalDate pickupDate;

	@JsonProperty("pickup_request_time")
	private DeliveryRequestTime pickupRequestTime;

	@JsonProperty("pickup_time_start")
	private LocalDateTime pickupTimeStart;

	@JsonProperty("pickup_time_end")
	private LocalDateTime pickupTimeEnd;

	@JsonProperty("delivery_request_time")
	private DeliveryRequestTime deliveryRequestTime;

	@JsonProperty("is_cod")
	private Boolean isCod;

	@JsonProperty("dimension_length_cm")
	private Double dimensionLengthCm;

	@JsonProperty("dimension_width_cm")
	private Double dimensionWidthCm;

	@JsonProperty("dimension_height_cm")
	private Double dimensionHeightCm;

	@JsonProperty("total_volume_m3")
	private Double totalVolumeM3;

	@JsonProperty("fee_payer")
	private FeePayer feePayer;

	@JsonProperty("source_rows")
	@Builder.Default
	private List<Integer> sourceRows = new ArrayList<>();

	@JsonProperty("products")
	@Builder.Default
	private List<ProductImportItemDTO> products = new ArrayList<>();

	@Data
	@NoArgsConstructor
	@AllArgsConstructor
	@Builder
	public static class ProductImportItemDTO {
		@JsonProperty("name")
		private String name;

		@JsonProperty("value")
		private Long value;

		@JsonProperty("quantity")
		private Integer quantity;

		@JsonProperty("weight_gram")
		private Double weightGram;

		@JsonProperty("product_type_id")
		private Long productTypeId;

		@JsonProperty("product_type_code")
		private String productTypeCode;

		@JsonProperty("product_type_name")
		private String productTypeName;
	}
}

