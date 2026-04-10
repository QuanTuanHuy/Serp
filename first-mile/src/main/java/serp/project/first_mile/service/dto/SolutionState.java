package serp.project.first_mile.service.dto;

import java.util.List;

public record SolutionState(
        List<RouteState> routes,
        List<UnassignedOrderState> unassignedOrders
) {
}
