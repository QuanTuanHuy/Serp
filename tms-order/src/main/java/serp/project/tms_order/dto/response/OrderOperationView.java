/*
Author: Nguyen The Anh
Description: Part of Serp Project
*/

package serp.project.tms_order.dto.response;

import serp.project.tms_order.enums.OrderPickupMethod;
import serp.project.tms_order.enums.OrderStatus;

import java.time.LocalDateTime;

public record OrderOperationView(
        Long id,
        String orderCode,
        String customerOrderCode,
        OrderStatus status,
        Boolean isConfirm,
        String originPostOfficeCode,
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
        OrderPickupMethod pickupMethod,
        String createdBy,
        LocalDateTime createdAt,
        Long tenantId
) {
}
