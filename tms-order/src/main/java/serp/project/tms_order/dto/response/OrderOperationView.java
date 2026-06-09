/*
Author: Nguyen The Anh
Description: Part of Serp Project
*/

package serp.project.tms_order.dto.response;

import serp.project.tms_order.domain.Dimension;
import serp.project.tms_order.enums.OrderPickupMethod;
import serp.project.tms_order.enums.OrderStatus;
import serp.project.tms_order.enums.OrderType;

import java.time.LocalDateTime;

public record OrderOperationView(
        Long id,
        String orderCode,
        String customerOrderCode,
        OrderStatus status,
        Boolean isConfirm,
        String originPostOfficeCode,
        String destinationPostOfficeCode,
        String senderName,
        String senderPhone,
        String senderProvinceCode,
        String senderWardCode,
        String senderAddressDetail,
        Double senderLatitude,
        Double senderLongitude,
        LocalDateTime pickupTimeStart,
        LocalDateTime pickupTimeEnd,
        Double totalWeight,
        Double totalVolume,
        Dimension dimensions,
        OrderType orderType,
        String note,
        OrderPickupMethod pickupMethod,
        String createdBy,
        LocalDateTime createdAt,
        Long tenantId,
        // Receiver info
        String receiverName,
        String receiverPhone,
        String receiverWardCode,
        String receiverProvinceCode,
        String receiverAddressDetail,
        Double receiverLatitude,
        Double receiverLongitude,
        // COD & fee
        Long codAmount,
        Long totalShippingFee,
        String feePayer,
        String paymentStatus
) {
}
