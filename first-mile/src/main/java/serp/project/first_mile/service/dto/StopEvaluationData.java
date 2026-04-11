package serp.project.first_mile.service.dto;

import java.time.LocalDateTime;

public record StopEvaluationData(
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
