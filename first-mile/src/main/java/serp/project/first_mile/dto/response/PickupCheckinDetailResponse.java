/*
Author: Nguyen The Anh
Description: Part of Serp Project
*/

package serp.project.first_mile.dto.response;

import serp.project.first_mile.enums.OrderStatus;
import serp.project.first_mile.enums.TripStatus;

import java.time.LocalDateTime;

public record PickupCheckinDetailResponse(
        Long checkinId,
        Long orderId,
        String orderCode,
        String customerOrderCode,
        OrderStatus orderStatus,
        Long tripId,
        String tripCode,
        TripStatus tripStatus,
        Long courierStaffId,
        String courierCode,
        String courierName,
        Long postOfficeId,
        String postOfficeCode,
        String postOfficeName,
        String senderName,
        String senderPhone,
        String senderAddressDetail,
        Double pickupLatitude,
        Double pickupLongitude,
        LocalDateTime checkinTime,
        Double checkinLatitude,
        Double checkinLongitude,
        String photoUrl,
        Double distanceMeters,
        Double allowedRadiusMeters
) {
}
