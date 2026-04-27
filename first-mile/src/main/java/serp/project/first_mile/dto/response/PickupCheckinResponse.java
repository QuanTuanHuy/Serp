/*
Author: Nguyen The Anh
Description: Part of Serp Project
*/

package serp.project.first_mile.dto.response;

import serp.project.first_mile.enums.OrderStatus;

import java.time.LocalDateTime;

public record PickupCheckinResponse(
        Long checkinId,
        Long orderId,
        String orderCode,
        OrderStatus orderStatus,
        Long tripId,
        String tripCode,
        Long courierStaffId,
        LocalDateTime checkinTime,
        String photoUrl,
        Double checkinLatitude,
        Double checkinLongitude,
        Double pickupLatitude,
        Double pickupLongitude,
        Double distanceMeters,
        Double allowedRadiusMeters
) {
}