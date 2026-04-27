package serp.project.first_mile.service.dto;

import serp.project.first_mile.enums.DeliveryRequestTime;
import serp.project.first_mile.enums.FeePayer;
import serp.project.first_mile.enums.OrderProductCategory;
import serp.project.first_mile.enums.OrderPickupMethod;
import serp.project.first_mile.enums.OrderType;

import java.time.LocalDateTime;
import java.util.List;

public record ManualOrderPayload(
        String customerOrderCode,
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
        Boolean isCod,
        Double dimensionLengthCm,
        Double dimensionWidthCm,
        Double dimensionHeightCm,
        Double totalVolumeM3,
        String note,
        List<ManualOrderProductPayload> products
) {
}
