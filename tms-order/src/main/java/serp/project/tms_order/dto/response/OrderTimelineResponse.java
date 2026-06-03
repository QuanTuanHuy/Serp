/*
Author: Nguyen The Anh
Description: Part of Serp Project
*/

package serp.project.tms_order.dto.response;

import serp.project.tms_order.enums.OrderStatus;

import java.time.LocalDateTime;

public record OrderTimelineResponse(
        Long id,
        Long orderId,
        String orderCode,
        String customerOrderCode,
        OrderStatus orderStatus,
        String description,
        LocalDateTime eventTime,
        String recordedBy,
        Long tripId,
        String tripCode,
        Long postOfficeId,
        String postOfficeCode,
        String postOfficeName,
        Long courierStaffId,
        String courierCode,
        String courierName,
        Long vehicleId,
        String vehicleLicensePlate,
        Double latitude,
        Double longitude,
        String locationLabel
) {
}
