/*
Author: Nguyen The Anh
Description: Part of Serp Project
*/

package serp.project.first_mile.dto.response;

import serp.project.first_mile.enums.PickupShift;
import serp.project.first_mile.enums.DeliveryOrderStatus;
import serp.project.first_mile.enums.OrderStatus;
import serp.project.first_mile.enums.PaymentStatus;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;

public record DeliveryAssignmentResponse(
        Long postOfficeId,
        String postOfficeCode,
        String postOfficeName,
        PickupShift shift,
        LocalDate tripDate,
        Integer totalRequestedOrders,
        Integer assignedOrders,
        Integer unassignedOrders,
        Integer createdTrips,
        List<DeliveryTripResponse> trips,
        List<UnassignedDeliveryOrderResponse> unassignedOrderDetails
) {

    public record DeliveryTripResponse(
            Long tripId,
            String tripCode,
            Long courierStaffId,
            String courierCode,
            String courierName,
            Long vehicleId,
            String vehicleLicensePlate,
            Integer totalStops,
            Double totalDistanceKm,
            Long totalTravelMinutes,
            Long totalServiceMinutes,
            Long totalLatenessMinutes,
            LocalDateTime plannedStartTime,
            LocalDateTime plannedEndTime,
            List<DeliveryStopResponse> stops
    ) {
    }

    public record DeliveryStopResponse(
            Integer sequence,
            Long orderId,
            String orderCode,
            String customerOrderCode,
            String receiverName,
            String receiverPhone,
            Double latitude,
            Double longitude,
            LocalDateTime plannedArrivalTime,
            LocalDateTime plannedStartServiceTime,
            LocalDateTime plannedDepartureTime,
            Double distanceFromPreviousKm,
            Long travelMinutes,
            Long latenessMinutes,
            LocalDateTime scanOutTime,
            OrderStatus orderStatus,
            DeliveryOrderStatus deliveryStatus,
            String receiverAddressDetail,
            String receiverWardCode,
            String receiverProvinceCode,
            Long codAmount,
            Long totalShippingFee,
            String feePayer,
            PaymentStatus deliveryPaymentStatus,
            Long deliveryPaymentAmount,
            String deliveryPaymentAppTransId,
            LocalDateTime deliveryPaymentConfirmedAt,
            Integer deliveryAttemptCount,
            Long codCollected,
            Long shippingFeeCollected,
            String failureReason,
            String note,
            LocalDateTime deliveredAt,
            LocalDateTime returnedAt,
            Long checkinId,
            LocalDateTime deliveryCheckinTime,
            Double deliveryCheckinLat,
            Double deliveryCheckinLng,
            Double deliveryCheckinDistanceM,
            String proofPhotoUrl
    ) {
    }

    public record UnassignedDeliveryOrderResponse(
            Long orderId,
            String orderCode,
            String customerOrderCode,
            String reason
    ) {
    }
}
