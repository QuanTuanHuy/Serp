package serp.project.first_mile.service.dto;

import java.time.LocalDateTime;
import java.util.List;

public record RouteEvaluation(
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
    public static RouteEvaluation infeasible() {
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
