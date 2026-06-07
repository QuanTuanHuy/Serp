/*
Author: SERP Project
Description: Part of Serp Project
*/

package serp.project.first_mile.dto.response;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import serp.project.first_mile.enums.DeliveryOrderStatus;

import java.time.LocalDateTime;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class DeliveryManifestOrderResponse {
    private Long id;
    private Long orderId;
    private String orderCode;
    private Integer sequence;
    private Integer deliveryAttemptCount;
    private DeliveryOrderStatus status;
    private String receiverName;
    private String receiverPhone;
    private String receiverAddressDetail;
    private String receiverWardCode;
    private String receiverProvinceCode;
    private Double receiverLat;
    private Double receiverLng;
    private Long codAmount;
    private Long codCollected;
    private Long shippingFee;
    private Long shippingFeeCollected;
    private String feePayer;
    private String proofPhotoUrl;
    private String failureReason;
    private LocalDateTime deliveredAt;
    private String note;
}
