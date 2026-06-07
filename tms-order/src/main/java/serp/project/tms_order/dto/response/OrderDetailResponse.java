/*
Author: Nguyen The Anh
Description: Part of Serp Project
*/

package serp.project.tms_order.dto.response;

import serp.project.tms_order.enums.DeliveryRequestTime;
import serp.project.tms_order.enums.FeePayer;
import serp.project.tms_order.enums.OrderPickupMethod;
import serp.project.tms_order.enums.OrderProductCategory;
import serp.project.tms_order.enums.OrderStatus;
import serp.project.tms_order.enums.OrderType;
import serp.project.tms_order.enums.PaymentStatus;

import java.time.LocalDateTime;
import java.util.List;

public record OrderDetailResponse(
        Long id,
        String orderCode,
        String customerOrderCode,
        OrderStatus status,
        Boolean isConfirm,
        String senderName,
        String senderPhone,
        String senderProvinceCode,
        String senderWardCode,
        String senderAddressDetail,
        Double senderLatitude,
        Double senderLongitude,
        String receiverName,
        String receiverPhone,
        String receiverProvinceCode,
        String receiverWardCode,
        String receiverAddressDetail,
        Double receiverLatitude,
        Double receiverLongitude,
        LocalDateTime pickupTimeStart,
        LocalDateTime pickupTimeEnd,
        DeliveryRequestTime deliveryRequestTime,
        OrderPickupMethod pickupMethod,
        OrderProductCategory orderProductCategory,
        OrderType orderType,
        FeePayer feePayer,
        PaymentStatus paymentStatus,
        Long codAmount,
        Double totalWeight,
        Double totalValue,
        Double totalVolume,
        Double dimensionLengthCm,
        Double dimensionWidthCm,
        Double dimensionHeightCm,
        Long baseShippingFee,
        Long codFee,
        Long extraFee,
        Long totalShippingFee,
        String originPostOfficeCode,
        String destinationPostOfficeCode,
        String note,
        List<ProductItem> products,
        LocalDateTime createdAt,
        LocalDateTime updatedAt,
        String createdBy,
        String updatedBy,
        Long tenantId
) {
    public record ProductItem(
            Long id,
            String name,
            Long value,
            Integer quantity,
            Double weight,
            Long productTypeId,
            String productTypeCode,
            String productTypeName
    ) {
    }
}
