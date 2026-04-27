/*
Author: Nguyen The Anh
Description: Part of Serp Project
*/

package serp.project.first_mile.dto.response;

import serp.project.first_mile.enums.PickupShift;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;

public record PickupAssignmentResponse(
        Long postOfficeId,
        String postOfficeCode,
        String postOfficeName,
        PickupShift shift,
        LocalDate tripDate,
        Integer totalRequestedOrders,
        Integer assignedOrders,
        Integer unassignedOrders,
        Integer createdTrips,
        List<AssignedTripResponse> trips,
        List<UnassignedAssignmentOrderResponse> unassignedOrderDetails
) {

    public record AssignedTripResponse(
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
            List<AssignedStopResponse> stops
    ) {
    }

    public record AssignedStopResponse(
            Integer sequence,
            Long orderId,
            String orderCode,
            String customerOrderCode,
            LocalDateTime plannedArrivalTime,
            LocalDateTime plannedStartServiceTime,
            LocalDateTime plannedDepartureTime,
            Double distanceFromPreviousKm,
            Long travelMinutes,
            Long latenessMinutes
    ) {
    }

    public record UnassignedAssignmentOrderResponse(
            Long orderId,
            String orderCode,
            String customerOrderCode,
            String reason
    ) {
    }
}
