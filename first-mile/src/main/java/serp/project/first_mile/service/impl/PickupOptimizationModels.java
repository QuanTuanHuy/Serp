/*
Author: QuanTuanHuy
Description: Part of Serp Project
*/

package serp.project.first_mile.service.impl;

import serp.project.first_mile.enums.RoutingVehicle;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

record AlgorithmConfig(
        LocalDateTime planningStartTime,
        LocalDateTime planningEndTime,
        LocalDate planningDate,
        int orderLimit,
        double averageSpeedKmph,
        int serviceMinutesPerStop,
        int maxIterations,
        long maxRuntimeMillis,
        double destroyRate,
        double initialTemperature,
        double coolingRate,
        boolean allowLateness,
        boolean enforcePlanningEnd,
        boolean enforceCapacity,
        double distanceWeight,
        double latenessWeight,
        double unassignedPenalty,
        double usedRoutePenalty,
        RoutingVehicle routingVehicle,
        int distanceMatrixBatchSize,
        int distanceMatrixMaxNodes,
        TravelMetricProvider travelMetricProvider
) {
    AlgorithmConfig withTravelMetricProvider(TravelMetricProvider value) {
        return new AlgorithmConfig(
                planningStartTime,
                planningEndTime,
                planningDate,
                orderLimit,
                averageSpeedKmph,
                serviceMinutesPerStop,
                maxIterations,
                maxRuntimeMillis,
                destroyRate,
                initialTemperature,
                coolingRate,
                allowLateness,
                enforcePlanningEnd,
                enforceCapacity,
                distanceWeight,
                latenessWeight,
                unassignedPenalty,
                usedRoutePenalty,
                routingVehicle,
                distanceMatrixBatchSize,
                distanceMatrixMaxNodes,
                value
        );
    }
}

record CourierResource(
        Long staffId,
        String code,
        String fullName,
        Integer maxStops
) {
}

record PickupOrderNode(
        Long orderId,
        String orderCode,
        String customerOrderCode,
        String senderName,
        String senderPhone,
        Double latitude,
        Double longitude,
        double weight,
        double volume,
        LocalDateTime pickupTimeStart,
        LocalDateTime pickupTimeEnd
) {
}

record PreparedOrderData(
        List<PickupOrderNode> assignableOrders,
        List<UnassignedOrderState> initialUnassignedOrders
) {
}

record NodePoint(Long orderId, Double latitude, Double longitude) {
}

record TravelMetricProvider(
        Map<Long, Integer> orderNodeIndexByOrderId,
        List<NodePoint> nodes,
        double[][] distanceKm,
        long[][] travelMinutes
) {
    int nodeCount() {
        return nodes == null ? 0 : nodes.size();
    }
}

record LegMetric(double distanceKm, long travelMinutes) {
}

record RouteState(
        Long courierStaffId,
        String courierCode,
        String courierName,
        Integer maxStops,
        Long vehicleId,
        String vehicleLicensePlate,
        double maxWeight,
        double maxVolume,
        double depotLatitude,
        double depotLongitude,
        List<PickupOrderNode> stops
) {
    RouteState copy() {
        return new RouteState(
                courierStaffId,
                courierCode,
                courierName,
                maxStops,
                vehicleId,
                vehicleLicensePlate,
                maxWeight,
                maxVolume,
                depotLatitude,
                depotLongitude,
                new ArrayList<>(stops)
        );
    }
}

final class UnassignedOrderState {
    private final PickupOrderNode order;
    private final boolean reinsertable;
    private String reason;

    UnassignedOrderState(PickupOrderNode order, String reason, boolean reinsertable) {
        this.order = order;
        this.reason = reason;
        this.reinsertable = reinsertable;
    }

    PickupOrderNode order() {
        return order;
    }

    boolean reinsertable() {
        return reinsertable;
    }

    String reason() {
        return reason;
    }

    void setReason(String reason) {
        this.reason = reason;
    }

    UnassignedOrderState copy() {
        return new UnassignedOrderState(order, reason, reinsertable);
    }
}

record SolutionState(
        List<RouteState> routes,
        List<UnassignedOrderState> unassignedOrders
) {
}

record StopEvaluationData(
        int sequence,
        PickupOrderNode order,
        double distanceFromPreviousKm,
        long travelMinutes,
        LocalDateTime arrivalTime,
        LocalDateTime startServiceTime,
        LocalDateTime departureTime,
        long latenessMinutes
) {
}

record RouteEvaluation(
        boolean feasible,
        double totalDistanceKm,
        long totalTravelMinutes,
        long totalServiceMinutes,
        long totalLatenessMinutes,
        double totalWeight,
        double totalVolume,
        LocalDateTime routeStartTime,
        LocalDateTime routeEndTime,
        List<StopEvaluationData> stopDetails
) {
    static RouteEvaluation infeasible() {
        return new RouteEvaluation(
                false,
                0.0,
                0,
                0,
                0,
                0.0,
                0.0,
                null,
                null,
                List.of()
        );
    }
}

record SolutionEvaluation(
        double objectiveScore,
        double totalDistanceKm,
        long totalTravelMinutes,
        long totalServiceMinutes,
        long totalLatenessMinutes,
        int assignedOrders,
        int unassignedOrders,
        int usedRoutes,
        List<RouteEvaluation> routeEvaluations
) {
}

record RouteOrderRef(int routeIndex, int stopIndex) {
}

record RouteOrderContribution(int routeIndex, int stopIndex, double contribution) {
}

record InsertionCandidate(int routeIndex, int insertPosition, double deltaCost) {
}

record InsertionDecision(PickupOrderNode order, InsertionCandidate candidate) {
}

record RegretDecision(PickupOrderNode order, InsertionCandidate candidate, double regretValue) {
}
