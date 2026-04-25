package serp.project.first_mile.service.dto;

import java.util.List;
import java.util.Map;

public record ExistingRouteData(
        Map<Long, List<serp.project.first_mile.service.dto.PickupOrderNode>> routeNodesByTripId,
        List<serp.project.first_mile.service.dto.UnassignedOrderState> invalidOrderStates
) {
}
