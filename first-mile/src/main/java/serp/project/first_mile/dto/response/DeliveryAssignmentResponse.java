/*
Author: Nguyen The Anh
Description: Part of Serp Project
*/

package serp.project.first_mile.dto.response;

import serp.project.first_mile.enums.PickupShift;

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
            LocalDateTime scanOutTime
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
