package serp.project.first_mile.service.dto;

import java.util.List;

public record SolutionEvaluation(
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
