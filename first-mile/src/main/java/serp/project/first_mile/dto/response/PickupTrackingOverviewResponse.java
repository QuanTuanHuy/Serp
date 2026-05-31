/*
Author: GitHub Copilot
Description: Part of Serp Project
*/

package serp.project.first_mile.dto.response;

import serp.project.first_mile.enums.OrderStatus;
import serp.project.first_mile.enums.PickupShift;
import serp.project.first_mile.enums.TripStatus;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;

public record PickupTrackingOverviewResponse(
        LocalDate tripDate,
        String actorScope,
        Long selectedPostOfficeId,
        Long selectedCourierStaffId,
        Integer totalTrips,
        Integer totalOrders,
        Integer checkedInOrders,
        Integer pendingCheckinOrders,
        Integer pickingUpOrders,
        Integer pickedUpOrders,
        Integer pickupFailedOrders,
        List<PickupTrackingTripResponse> trips,
        List<PickupTrackingOrderResponse> orders
) {

    public record PickupTrackingTripResponse(
            Long tripId,
            String tripCode,
            TripStatus tripStatus,
            PickupShift shift,
            Long postOfficeId,
            String postOfficeCode,
            String postOfficeName,
            Long courierStaffId,
            String courierCode,
            String courierName,
            LocalDateTime plannedStartTime,
            LocalDateTime plannedEndTime,
            Integer totalOrders,
            Integer checkedInOrders,
            Integer pendingCheckinOrders,
            Integer returnableToPostOfficeOrders
    ) {
    }

    public record PickupTrackingOrderResponse(
            Long tripOrderId,
            Long tripId,
            String tripCode,
            TripStatus tripStatus,
            Integer sequenceNo,
            Long orderId,
            String orderCode,
            String customerOrderCode,
            OrderStatus orderStatus,
            String senderName,
            String senderPhone,
            String senderAddressDetail,
            Double senderLatitude,
            Double senderLongitude,
            LocalDateTime pickupTimeStart,
            LocalDateTime pickupTimeEnd,
            LocalDateTime plannedArrivalTime,
            LocalDateTime plannedStartServiceTime,
            LocalDateTime plannedDepartureTime,
            Long courierStaffId,
            String courierCode,
            String courierName,
            Long postOfficeId,
            String postOfficeCode,
            String postOfficeName,
            Boolean checkedIn,
            Long checkinId,
            LocalDateTime checkinTime,
            Double checkinLatitude,
            Double checkinLongitude,
            String checkinPhotoUrl,
            Double checkinDistanceM,
            Double allowedRadiusM
    ) {
    }
}
