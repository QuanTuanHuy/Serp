/*
Author: Nguyen The Anh
Description: Part of Serp Project
*/

package serp.project.first_mile.dto.response;

import java.time.LocalDateTime;
import java.util.List;

public record PickupOptimizationResponse(
        Long postOfficeId,
        String postOfficeCode,
        String postOfficeName,
        LocalDateTime planningStartTime,
        LocalDateTime planningEndTime,
        Integer totalOrders,
        Integer assignedOrders,
        Integer unassignedOrders,
        Double totalDistanceKm,
        Long totalTravelMinutes,
        Long totalServiceMinutes,
        Long totalLatenessMinutes,
        Double objectiveScore,
        Integer totalRoutes,
        Integer usedRoutes,
        List<PickupRoutePlanResponse> routes,
        List<UnassignedPickupOrderResponse> unassignedOrderDetails
) {
    public record PickupRoutePlanResponse(
            Long courierStaffId,
            String courierCode,
            String courierName,
            Long vehicleId,
            String vehicleLicensePlate,
            Double vehicleMaxWeight,
            Double vehicleMaxVolume,
            Integer totalStops,
            Double totalWeight,
            Double totalVolume,
            Double totalDistanceKm,
            Long totalTravelMinutes,
            Long totalServiceMinutes,
            Long totalLatenessMinutes,
            LocalDateTime startTime,
            LocalDateTime endTime,
            List<PickupStopResponse> stops
    ) {
    }

    public record PickupStopResponse(
            Integer sequence,
            Long orderId,
            String orderCode,
            String customerOrderCode,
            String senderName,
            String senderPhone,
            Double latitude,
            Double longitude,
            LocalDateTime pickupTimeStart,
            LocalDateTime pickupTimeEnd,
            LocalDateTime arrivalTime,
            LocalDateTime startServiceTime,
            LocalDateTime departureTime,
            Double distanceFromPreviousKm,
            Long travelMinutes,
            Long latenessMinutes
    ) {
    }

    public record UnassignedPickupOrderResponse(
            Long orderId,
            String orderCode,
            String customerOrderCode,
            String reason
    ) {
    }
}
