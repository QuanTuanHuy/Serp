/*
Author: Nguyen The Anh
Description: Part of Serp Project
*/

package serp.project.first_mile.service.dto;

import serp.project.first_mile.enums.RoutingVehicle;

import java.time.LocalDate;
import java.time.LocalDateTime;

public record AlgorithmConfig(
        LocalDateTime planningStartTime,
        LocalDateTime planningEndTime,
        LocalDate planningDate,
        int orderLimit,
        double averageSpeedKmph,
        int serviceMinutesPerStop,
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
        serp.project.first_mile.service.dto.TravelMetricProvider travelMetricProvider
) {
    public AlgorithmConfig withTravelMetricProvider(serp.project.first_mile.service.dto.TravelMetricProvider value) {
        return new AlgorithmConfig(
                planningStartTime,
                planningEndTime,
                planningDate,
                orderLimit,
                averageSpeedKmph,
                serviceMinutesPerStop,
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
