/*
Author: Nguyen The Anh
Description: Part of Serp Project
*/

package serp.project.first_mile.dto.response;

import serp.project.first_mile.enums.OrderStatus;
import serp.project.first_mile.enums.TripStatus;

import java.time.LocalDateTime;

public record DeliveryScanOutResponse(
        Long tripId,
        String tripCode,
        TripStatus tripStatus,
        Long tripOrderId,
        Long orderId,
        String orderCode,
        String customerOrderCode,
        OrderStatus orderStatus,
        Integer sequence,
        Long courierStaffId,
        String courierCode,
        String courierName,
        Long postOfficeId,
        String postOfficeCode,
        String postOfficeName,
        Long vehicleId,
        String vehicleLicensePlate,
        LocalDateTime scanOutTime
) {
}
